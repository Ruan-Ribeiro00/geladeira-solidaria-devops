package br.com.fiap.geladeira.service;

import br.com.fiap.geladeira.config.RegrasProperties;
import br.com.fiap.geladeira.dto.LeituraRequest;
import br.com.fiap.geladeira.dto.LeituraResponse;
import br.com.fiap.geladeira.model.*;
import br.com.fiap.geladeira.repository.LeituraSensorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Recebe as leituras do sensor IoT e abre alerta quando a temperatura passa do limite. */
@Service
public class LeituraService {

    private final LeituraSensorRepository repository;
    private final GeladeiraService geladeiraService;
    private final AlertaService alertaService;
    private final RegrasProperties regras;

    public LeituraService(LeituraSensorRepository repository, GeladeiraService geladeiraService,
                          AlertaService alertaService, RegrasProperties regras) {
        this.repository = repository;
        this.geladeiraService = geladeiraService;
        this.alertaService = alertaService;
        this.regras = regras;
    }

    @Transactional
    public LeituraResponse registrar(LeituraRequest req) {
        Geladeira g = geladeiraService.buscarEntidade(req.geladeiraId());

        LeituraSensor l = new LeituraSensor();
        l.setGeladeira(g);
        l.setDtLeitura(req.dtLeitura() != null ? req.dtLeitura() : LocalDateTime.now());
        l.setTemperaturaC(req.temperaturaC());
        l.setUmidadePct(req.umidadePct());
        l.setPortaAberta(req.portaAberta());
        repository.save(l);

        boolean foraDoLimite = foraDoLimite(l.getTemperaturaC());
        boolean alertaGerado = false;
        if (foraDoLimite && g.isAtiva()) {
            alertaGerado = alertaService.abrirParaGeladeira(g, TipoAlerta.TEMPERATURA, NivelAlerta.CRITICO,
                    "Temperatura em " + l.getTemperaturaC() + " C, acima do limite seguro de "
                            + regras.temperaturaMaximaC() + " C.");
        }
        return toResponse(l, alertaGerado);
    }

    @Transactional(readOnly = true)
    public Page<LeituraResponse> listarPorGeladeira(Long idGeladeira, Pageable pageable) {
        geladeiraService.buscarEntidade(idGeladeira);
        return repository.findByGeladeiraId(idGeladeira, pageable).map(l -> toResponse(l, null));
    }

    private boolean foraDoLimite(BigDecimal temperatura) {
        return temperatura.compareTo(regras.temperaturaMaximaC()) > 0;
    }

    private LeituraResponse toResponse(LeituraSensor l, Boolean alertaGerado) {
        return new LeituraResponse(l.getId(), l.getGeladeira().getId(), l.getDtLeitura(), l.getTemperaturaC(),
                l.getUmidadePct(), l.isPortaAberta(), foraDoLimite(l.getTemperaturaC()), alertaGerado);
    }
}
