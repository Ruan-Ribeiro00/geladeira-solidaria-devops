package br.com.fiap.geladeira.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Rotina diaria: antes de os voluntarios sairem, reclassifica o risco de tudo que esta nas geladeiras. */
@Component
public class RotinasAgendadas {

    private static final Logger log = LoggerFactory.getLogger(RotinasAgendadas.class);

    private final DoacaoService doacaoService;

    public RotinasAgendadas(DoacaoService doacaoService) {
        this.doacaoService = doacaoService;
    }

    @Scheduled(cron = "0 0 6 * * *", zone = "America/Sao_Paulo")
    public void processarRiscosDiario() {
        var resultado = doacaoService.processarRiscos();
        log.info("Rotina diaria de riscos concluida: {}", resultado);
    }
}
