package br.com.fiap.geladeira.service;

import br.com.fiap.geladeira.config.RegrasProperties;
import br.com.fiap.geladeira.dto.*;
import br.com.fiap.geladeira.exception.RecursoNaoEncontradoException;
import br.com.fiap.geladeira.exception.RegraNegocioException;
import br.com.fiap.geladeira.model.*;
import br.com.fiap.geladeira.repository.*;
import br.com.fiap.geladeira.security.UsuarioAutenticado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class DoacaoService {

    private static final Logger log = LoggerFactory.getLogger(DoacaoService.class);

    private final DoacaoRepository repository;
    private final GeladeiraRepository geladeiraRepository;
    private final LeituraSensorRepository leituraRepository;
    private final AlertaRepository alertaRepository;
    private final GeladeiraService geladeiraService;
    private final DoadorService doadorService;
    private final CategoriaService categoriaService;
    private final AlertaService alertaService;
    private final RegrasProperties regras;

    public DoacaoService(DoacaoRepository repository, GeladeiraRepository geladeiraRepository,
                         LeituraSensorRepository leituraRepository, AlertaRepository alertaRepository,
                         GeladeiraService geladeiraService, DoadorService doadorService,
                         CategoriaService categoriaService, AlertaService alertaService, RegrasProperties regras) {
        this.repository = repository;
        this.geladeiraRepository = geladeiraRepository;
        this.leituraRepository = leituraRepository;
        this.alertaRepository = alertaRepository;
        this.geladeiraService = geladeiraService;
        this.doadorService = doadorService;
        this.categoriaService = categoriaService;
        this.alertaService = alertaService;
        this.regras = regras;
    }

    // ================================================================== consultas

    /** Filtro dinamico com Specification. Um DOADOR so enxerga as proprias doacoes. */
    @Transactional(readOnly = true)
    public Page<DoacaoResponse> listar(DoacaoFiltro filtro, Pageable pageable, UsuarioAutenticado usuario) {
        DoacaoFiltro f = filtro;
        if (usuario != null && usuario.is(Papel.DOADOR)) {
            f = new DoacaoFiltro(filtro.geladeiraId(), usuario.doadorId(), filtro.categoriaId(), filtro.status(),
                    filtro.nivelRisco(), filtro.bairro(), filtro.descricao(), filtro.validadeDe(),
                    filtro.validadeAte(), filtro.refrigerado());
        }
        return repository.findAll(DoacaoSpecs.comFiltro(f), pageable).map(DoacaoService::toResponse);
    }

    @Transactional(readOnly = true)
    public DoacaoResponse buscar(Long id, UsuarioAutenticado usuario) {
        Doacao d = buscarEntidade(id);
        if (usuario.is(Papel.DOADOR) && !d.getDoador().getId().equals(usuario.doadorId())) {
            throw new AccessDeniedException("Doacao de outro doador");
        }
        return toResponse(d);
    }

    /** RN3: lista de distribuicao prioritaria (o que vence nos proximos N dias). */
    @Transactional(readOnly = true)
    public List<DoacaoResponse> listaPrioritaria(int dias) {
        LocalDate hoje = LocalDate.now();
        return repository.listaDistribuicaoPrioritaria(hoje, hoje.plusDays(dias), StatusDoacao.DISPONIVEL,
                        StatusGeladeira.ATIVA).stream()
                .map(DoacaoService::toResponse)
                .toList();
    }

    // ================================================================== RN4: registro

    /**
     * Registra uma doacao aplicando a RN4:
     * geladeira precisa existir e estar ATIVA; alimento nao pode chegar vencido;
     * alimento refrigerado nao entra em geladeira acima da temperatura da categoria;
     * a doacao tem que caber (se nao couber, sugere a geladeira ativa com mais espaco);
     * passou de 90% de ocupacao, abre alerta de capacidade.
     */
    @Transactional
    public DoacaoResponse registrar(DoacaoRequest req, UsuarioAutenticado usuario) {
        Doador doador = resolverDoador(req.doadorId(), usuario);
        Geladeira geladeira = geladeiraService.buscarEntidade(req.geladeiraId());
        Categoria categoria = categoriaService.buscarEntidade(req.categoriaId());

        if (!geladeira.isAtiva()) {
            throw new RegraNegocioException("A geladeira '" + geladeira.getNome() + "' esta " + geladeira.getStatus()
                    + " e nao recebe doacoes", sugestaoDeGeladeira(req.quantidadeKg(), geladeira.getId()));
        }
        if (req.dtValidade().isBefore(LocalDate.now())) {
            throw new RegraNegocioException("Nao e permitido doar alimento vencido");
        }
        BigDecimal temperaturaAtual = ultimaTemperatura(geladeira.getId());
        if (categoria.isRefrigerado() && temperaturaAtual != null && categoria.getTempMaxC() != null
                && temperaturaAtual.compareTo(categoria.getTempMaxC()) > 0) {
            throw new RegraNegocioException("A geladeira esta a " + temperaturaAtual + " C, acima do limite de "
                    + categoria.getTempMaxC() + " C para " + categoria.getNome() + ". Leve o alimento para outra geladeira.",
                    sugestaoDeGeladeira(req.quantidadeKg(), geladeira.getId()));
        }

        BigDecimal ocupado = geladeiraService.ocupadoKg(geladeira.getId());
        BigDecimal livre = Calculos.livre(geladeira.getCapacidadeKg(), ocupado);
        if (req.quantidadeKg().compareTo(livre) > 0) {
            throw new RegraNegocioException("A doacao de " + req.quantidadeKg() + " kg nao cabe na geladeira '"
                    + geladeira.getNome() + "' (" + livre + " kg livres)",
                    sugestaoDeGeladeira(req.quantidadeKg(), geladeira.getId()));
        }

        Doacao d = new Doacao();
        d.setGeladeira(geladeira);
        d.setDoador(doador);
        d.setCategoria(categoria);
        d.setDescricao(req.descricao().trim());
        d.setQuantidadeKg(req.quantidadeKg());
        d.setDtDoacao(LocalDateTime.now());
        d.setDtValidade(req.dtValidade());
        d.setStatus(StatusDoacao.DISPONIVEL);
        d.setNivelRisco(calcularRisco(d, temperaturaAtual));
        repository.save(d);

        String aviso = verificarCapacidade(geladeira, ocupado.add(req.quantidadeKg()));
        geladeiraService.limparCache(geladeira.getId());
        log.info("Doacao {} registrada: {} kg em {}", d.getId(), d.getQuantidadeKg(), geladeira.getNome());
        return toResponse(d).comAviso(aviso);
    }

    @Transactional
    public DoacaoResponse atualizar(Long id, DoacaoAtualizacaoRequest req) {
        Doacao d = buscarEntidade(id);
        exigirDisponivel(d);
        Categoria categoria = categoriaService.buscarEntidade(req.categoriaId());

        Geladeira g = d.getGeladeira();
        BigDecimal ocupadoSemEsta = geladeiraService.ocupadoKg(g.getId()).subtract(d.getQuantidadeKg());
        BigDecimal livre = Calculos.livre(g.getCapacidadeKg(), ocupadoSemEsta);
        if (req.quantidadeKg().compareTo(livre) > 0) {
            throw new RegraNegocioException("A nova quantidade nao cabe na geladeira (" + livre + " kg livres)");
        }

        d.setCategoria(categoria);
        d.setDescricao(req.descricao().trim());
        d.setQuantidadeKg(req.quantidadeKg());
        d.setDtValidade(req.dtValidade());
        d.setNivelRisco(calcularRisco(d, ultimaTemperatura(g.getId())));
        geladeiraService.limparCache(g.getId());
        return toResponse(d);
    }

    /** Alimento retirado por uma familia: sai do estoque da geladeira. */
    @Transactional
    public DoacaoResponse registrarRetirada(Long id) {
        Doacao d = buscarEntidade(id);
        exigirDisponivel(d);
        d.setStatus(StatusDoacao.RETIRADA);
        geladeiraService.limparCache(d.getGeladeira().getId());
        return toResponse(d);
    }

    /** Alimento descartado pelo voluntario (estragado, embalagem violada...). */
    @Transactional
    public DoacaoResponse registrarDescarte(Long id) {
        Doacao d = buscarEntidade(id);
        exigirDisponivel(d);
        d.setStatus(StatusDoacao.DESCARTADA);
        geladeiraService.limparCache(d.getGeladeira().getId());
        return toResponse(d);
    }

    @Transactional
    public void excluir(Long id) {
        Doacao d = buscarEntidade(id);
        alertaRepository.desvincularDoacao(id);
        repository.delete(d);
        geladeiraService.limparCache(d.getGeladeira().getId());
    }

    // ================================================================== RN5: riscos

    /**
     * RN5: classifica o risco de todas as doacoes disponiveis.
     * Validade passou -> VENCIDO (vira DESCARTADA). Refrigerado acima da temperatura
     * maxima da categoria -> CRITICO. Vence hoje ou amanha -> CRITICO.
     * Vence em 2 ou 3 dias -> ATENCAO. Senao, OK.
     * Roda sob demanda (endpoint) e todo dia as 6h (agendado).
     */
    @Transactional
    public ProcessamentoRiscoResponse processarRiscos() {
        List<Doacao> doacoes = repository.findByStatus(StatusDoacao.DISPONIVEL);
        Map<Long, Optional<BigDecimal>> temperaturas = new HashMap<>();
        int ok = 0, atencao = 0, critico = 0, vencidas = 0, alertas = 0;
        BigDecimal kgDescartados = BigDecimal.ZERO;

        for (Doacao d : doacoes) {
            BigDecimal temp = temperaturas
                    .computeIfAbsent(d.getGeladeira().getId(), id -> Optional.ofNullable(ultimaTemperatura(id)))
                    .orElse(null);
            NivelRisco risco = calcularRisco(d, temp);
            d.setNivelRisco(risco);

            switch (risco) {
                case VENCIDO -> {
                    vencidas++;
                    kgDescartados = kgDescartados.add(d.getQuantidadeKg());
                    d.setStatus(StatusDoacao.DESCARTADA);
                    if (alertaService.abrirParaDoacao(d, TipoAlerta.VALIDADE, NivelAlerta.ALTO,
                            d.getDescricao() + " (" + d.getQuantidadeKg() + " kg) venceu e foi retirado(a) da lista. "
                                    + "Descartar na geladeira " + d.getGeladeira().getNome() + ".")) {
                        alertas++;
                    }
                }
                case CRITICO -> {
                    critico++;
                    boolean porTemperatura = acimaDaTemperatura(d, temp);
                    String msg = porTemperatura
                            ? d.getDescricao() + " exposto(a) a " + temp + " C (maximo " + d.getCategoria().getTempMaxC()
                              + " C). Avaliar descarte."
                            : d.getDescricao() + " vence " + (diasParaVencer(d) == 0 ? "hoje" : "amanha")
                              + ". Distribuir com urgencia.";
                    TipoAlerta tipo = porTemperatura ? TipoAlerta.TEMPERATURA : TipoAlerta.VALIDADE;
                    NivelAlerta nivel = porTemperatura ? NivelAlerta.CRITICO : NivelAlerta.ALTO;
                    if (alertaService.abrirParaDoacao(d, tipo, nivel, msg)) {
                        alertas++;
                    }
                }
                case ATENCAO -> atencao++;
                case OK -> ok++;
            }
        }
        geladeiraService.limparCacheTodas();
        log.info("Processamento de riscos: {} doacoes, {} vencidas, {} alertas", doacoes.size(), vencidas, alertas);
        return new ProcessamentoRiscoResponse(doacoes.size(), ok, atencao, critico, vencidas, kgDescartados, alertas);
    }

    // ================================================================== regras auxiliares

    NivelRisco calcularRisco(Doacao d, BigDecimal temperaturaGeladeira) {
        long dias = diasParaVencer(d);
        if (dias < 0) {
            return NivelRisco.VENCIDO;
        }
        if (acimaDaTemperatura(d, temperaturaGeladeira) || dias <= 1) {
            return NivelRisco.CRITICO;
        }
        if (dias <= 3) {
            return NivelRisco.ATENCAO;
        }
        return NivelRisco.OK;
    }

    private static boolean acimaDaTemperatura(Doacao d, BigDecimal temp) {
        Categoria c = d.getCategoria();
        return c.isRefrigerado() && c.getTempMaxC() != null && temp != null && temp.compareTo(c.getTempMaxC()) > 0;
    }

    private static long diasParaVencer(Doacao d) {
        return ChronoUnit.DAYS.between(LocalDate.now(), d.getDtValidade());
    }

    private BigDecimal ultimaTemperatura(Long idGeladeira) {
        return leituraRepository.findFirstByGeladeiraIdOrderByDtLeituraDesc(idGeladeira)
                .map(LeituraSensor::getTemperaturaC)
                .orElse(null);
    }

    private String verificarCapacidade(Geladeira g, BigDecimal novoOcupado) {
        BigDecimal pct = Calculos.percentual(novoOcupado, g.getCapacidadeKg());
        if (pct.compareTo(BigDecimal.valueOf(regras.ocupacaoAltaPct())) >= 0) {
            alertaService.abrirParaGeladeira(g, TipoAlerta.CAPACIDADE, NivelAlerta.ALTO,
                    "Ocupacao em " + pct + "%. Redirecionar novas doacoes.");
            return "Geladeira com " + pct + "% de ocupacao. Alerta de capacidade aberto para os voluntarios.";
        }
        return null;
    }

    /** Sugestao devolvida no erro 422: a geladeira ativa com mais espaco livre que caiba a doacao. */
    private OcupacaoResponse sugestaoDeGeladeira(BigDecimal kg, Long ignorarId) {
        return geladeiraRepository.comEspacoLivre(kg, StatusGeladeira.ATIVA, StatusDoacao.DISPONIVEL).stream()
                .filter(o -> !o.getId().equals(ignorarId))
                .findFirst()
                .map(GeladeiraService::toOcupacao)
                .orElse(null);
    }

    private Doador resolverDoador(Long doadorId, UsuarioAutenticado usuario) {
        if (usuario.is(Papel.DOADOR)) {
            if (usuario.doadorId() == null) {
                throw new RegraNegocioException("Sua conta de doador nao esta ligada a um cadastro de doador");
            }
            if (doadorId != null && !doadorId.equals(usuario.doadorId())) {
                throw new AccessDeniedException("Doadores so registram doacoes em nome proprio");
            }
            return doadorService.buscarEntidade(usuario.doadorId());
        }
        if (doadorId == null) {
            throw new RegraNegocioException("Informe o doadorId da doacao");
        }
        return doadorService.buscarEntidade(doadorId);
    }

    private static void exigirDisponivel(Doacao d) {
        if (!d.isDisponivel()) {
            throw new RegraNegocioException("A doacao " + d.getId() + " ja esta " + d.getStatus());
        }
    }

    private Doacao buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Doacao", id));
    }

    static DoacaoResponse toResponse(Doacao d) {
        return new DoacaoResponse(
                d.getId(), d.getDescricao(), d.getQuantidadeKg(), d.getDtDoacao(), d.getDtValidade(),
                diasParaVencer(d), d.getStatus(), d.getNivelRisco(),
                d.getGeladeira().getId(), d.getGeladeira().getNome(), d.getGeladeira().getBairro(),
                d.getDoador().getId(), d.getDoador().getNome(),
                d.getCategoria().getId(), d.getCategoria().getNome(),
                null);
    }
}
