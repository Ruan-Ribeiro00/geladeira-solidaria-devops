package br.com.fiap.geladeira.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/** Limites das regras de negocio, configuraveis no application.yml (app.regras.*). */
@ConfigurationProperties(prefix = "app.regras")
public record RegrasProperties(
        BigDecimal temperaturaMaximaC,
        int ocupacaoAltaPct,
        int ocupacaoBaixaPct,
        int sensorSemSinalMinutos
) {
}
