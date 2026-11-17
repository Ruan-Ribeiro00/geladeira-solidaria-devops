package br.com.fiap.geladeira.service;

import br.com.fiap.geladeira.config.RegrasProperties;
import br.com.fiap.geladeira.dto.*;
import br.com.fiap.geladeira.exception.ConflitoException;
import br.com.fiap.geladeira.exception.RecursoNaoEncontradoException;
import br.com.fiap.geladeira.model.Geladeira;
import br.com.fiap.geladeira.model.LeituraSensor;
import br.com.fiap.geladeira.model.StatusDoacao;
import br.com.fiap.geladeira.model.StatusGeladeira;
import br.com.fiap.geladeira.repository.DoacaoRepository;
import br.com.fiap.geladeira.repository.GeladeiraRepository;
import br.com.fiap.geladeira.repository.LeituraSensorRepository;
import br.com.fiap.geladeira.repository.OcupacaoGeladeira;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GeladeiraService {

    private final GeladeiraRepository repository;
    private final DoacaoRepository doacaoRepository;
    private final LeituraSensorRepository leituraRepository;
    private final RegrasProperties regras;

    public GeladeiraService(GeladeiraRepository repository, DoacaoRepository doacaoRepository,
                            LeituraSensorRepository leituraRepository, RegrasProperties regras) {
        this.repository = repository;
        this.doacaoRepository = doacaoRepository;
        this.leituraRepository = leituraRepository;
        this.regras = regras;
    }

    // ------------------------------------------------------------------ consultas

    /** Lista paginada. Usa um query method diferente conforme os filtros que vierem. */
    @Transactional(readOnly = true)
    public Page<GeladeiraResponse> listar(String bairro, StatusGeladeira status, Pageable pageable) {
        boolean temBairro = bairro != null && !bairro.isBlank();
        Page<Geladeira> pagina;
        if (temBairro && status != null) {
            pagina = repository.findByBairroContainingIgnoreCaseAndStatus(bairro, status, pageable);
        } else if (temBairro) {
            pagina = repository.findByBairroContainingIgnoreCase(bairro, pageable);
        } else if (status != null) {
            pagina = repository.findByStatus(status, pageable);
        } else {
            pagina = repository.findAll(pageable);
        }
        return pagina.map(this::toResponse);
    }

    /**
     * Detalhe da geladeira. Fica em cache porque e a tela mais acessada do app
     * (o mapa abre o detalhe a cada toque). O cache e limpo sempre que a geladeira
     * ou as doacoes dela mudam.
     */
    @Cacheable(value = "geladeiras", key = "#id")
    @Transactional(readOnly = true)
    public GeladeiraResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    /** RN1: situacao da geladeira, avaliada em ordem de prioridade. */
    @Transactional(readOnly = true)
    public SituacaoGeladeiraResponse situacao(Long id) {
        Geladeira g = buscarEntidade(id);
        BigDecimal ocupacaoPct = Calculos.percentual(ocupadoKg(id), g.getCapacidadeKg());
        Optional<LeituraSensor> ultima = leituraRepository.findFirstByGeladeiraIdOrderByDtLeituraDesc(id);
        Double media = leituraRepository.temperaturaMedia(id, LocalDateTime.now().minusHours(24));
        BigDecimal mediaC = media == null ? null : BigDecimal.valueOf(media).setScale(1, RoundingMode.HALF_UP);

        SituacaoGeladeira situacao;
        String recomendacao;
        LocalDateTime limiteSinal = LocalDateTime.now().minusMinutes(regras.sensorSemSinalMinutos());

        if (!g.isAtiva()) {
            situacao = SituacaoGeladeira.FORA_DE_OPERACAO;
            recomendacao = "Geladeira " + g.getStatus() + ". Nao recebe doacoes.";
        } else if (ultima.isEmpty() || ultima.get().getDtLeitura().isBefore(limiteSinal)) {
            situacao = SituacaoGeladeira.SENSOR_SEM_SINAL;
            recomendacao = "Sensor sem leitura recente. Enviar voluntario para conferir a geladeira.";
        } else if (mediaC != null && mediaC.compareTo(regras.temperaturaMaximaC()) > 0) {
            // temperatura vem antes de lotacao: e risco a saude, lotacao e logistica
            situacao = SituacaoGeladeira.TEMPERATURA_FORA_DO_LIMITE;
            recomendacao = "Temperatura media de " + mediaC + " C. Verificar refrigeracao e alimentos refrigerados.";
        } else if (ocupacaoPct.compareTo(BigDecimal.valueOf(regras.ocupacaoAltaPct())) >= 0) {
            situacao = SituacaoGeladeira.LOTADA;
            recomendacao = "Redirecionar novas doacoes para geladeiras proximas.";
        } else if (ocupacaoPct.compareTo(BigDecimal.valueOf(regras.ocupacaoBaixaPct())) <= 0) {
            situacao = SituacaoGeladeira.ESTOQUE_BAIXO;
            recomendacao = "Mobilizar doadores da regiao.";
        } else {
            situacao = SituacaoGeladeira.NORMAL;
            recomendacao = "Operando normalmente.";
        }

        return new SituacaoGeladeiraResponse(g.getId(), g.getNome(), situacao, recomendacao, mediaC,
                ultima.map(LeituraSensor::getDtLeitura).orElse(null),
                ultima.map(LeituraSensor::isPortaAberta).orElse(null),
                ocupacaoPct);
    }

    @Transactional(readOnly = true)
    public List<OcupacaoResponse> rankingOcupacao() {
        return repository.rankingOcupacao(StatusGeladeira.ATIVA, StatusDoacao.DISPONIVEL).stream()
                .map(GeladeiraService::toOcupacao)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OcupacaoResponse> comEspacoLivre(BigDecimal kg) {
        return repository.comEspacoLivre(kg, StatusGeladeira.ATIVA, StatusDoacao.DISPONIVEL).stream()
                .map(GeladeiraService::toOcupacao)
                .toList();
    }

    // ------------------------------------------------------------------ escrita

    @Transactional
    public GeladeiraResponse criar(GeladeiraRequest req) {
        Geladeira g = new Geladeira();
        aplicar(g, req);
        if (g.getStatus() == null) {
            g.setStatus(StatusGeladeira.ATIVA);
        }
        if (g.getDtInstalacao() == null) {
            g.setDtInstalacao(LocalDate.now());
        }
        return toResponse(repository.save(g));
    }

    @CacheEvict(value = "geladeiras", key = "#id")
    @Transactional
    public GeladeiraResponse atualizar(Long id, GeladeiraRequest req) {
        Geladeira g = buscarEntidade(id);
        aplicar(g, req);
        BigDecimal ocupado = ocupadoKg(id);
        if (g.getCapacidadeKg().compareTo(ocupado) < 0) {
            throw new ConflitoException("A nova capacidade (" + g.getCapacidadeKg()
                    + " kg) e menor que o que ja esta guardado (" + ocupado + " kg)");
        }
        return toResponse(g);
    }

    /** So apaga geladeira sem historico. Com historico, o certo e mudar o status para INATIVA. */
    @CacheEvict(value = "geladeiras", key = "#id")
    @Transactional
    public void excluir(Long id) {
        Geladeira g = buscarEntidade(id);
        if (doacaoRepository.existsByGeladeiraId(id) || !g.getLeituras().isEmpty()) {
            throw new ConflitoException("A geladeira tem doacoes ou leituras registradas. "
                    + "Para tira-la da rede, atualize o status para INATIVA.");
        }
        repository.delete(g);
    }

    /** Chamado pelos outros services quando as doacoes de uma geladeira mudam. */
    @CacheEvict(value = "geladeiras", key = "#id")
    public void limparCache(Long id) {
        // so a anotacao ja faz o trabalho
    }

    @CacheEvict(value = "geladeiras", allEntries = true)
    public void limparCacheTodas() {
        // so a anotacao ja faz o trabalho
    }

    // ------------------------------------------------------------------ apoio

    Geladeira buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Geladeira", id));
    }

    BigDecimal ocupadoKg(Long id) {
        return repository.somarOcupacaoKg(id, StatusDoacao.DISPONIVEL);
    }

    private void aplicar(Geladeira g, GeladeiraRequest req) {
        g.setNome(req.nome().trim());
        g.setEndereco(req.endereco().trim());
        g.setBairro(req.bairro().trim());
        g.setCidade(req.cidade().trim());
        g.setLatitude(req.latitude());
        g.setLongitude(req.longitude());
        g.setCapacidadeKg(req.capacidadeKg());
        g.setResponsavel(req.responsavel().trim());
        if (req.status() != null) {
            g.setStatus(req.status());
        }
        if (req.dtInstalacao() != null) {
            g.setDtInstalacao(req.dtInstalacao());
        }
    }

    private GeladeiraResponse toResponse(Geladeira g) {
        BigDecimal ocupado = ocupadoKg(g.getId());
        return new GeladeiraResponse(
                g.getId(), g.getNome(), g.getEndereco(), g.getBairro(), g.getCidade(),
                g.getLatitude(), g.getLongitude(), g.getCapacidadeKg(),
                ocupado,
                Calculos.livre(g.getCapacidadeKg(), ocupado),
                Calculos.percentual(ocupado, g.getCapacidadeKg()),
                g.getResponsavel(), g.getStatus(), g.getDtInstalacao());
    }

    static OcupacaoResponse toOcupacao(OcupacaoGeladeira o) {
        return new OcupacaoResponse(o.getId(), o.getNome(), o.getBairro(), o.getCapacidadeKg(), o.getOcupadoKg(),
                Calculos.livre(o.getCapacidadeKg(), o.getOcupadoKg()),
                Calculos.percentual(o.getOcupadoKg(), o.getCapacidadeKg()));
    }
}
