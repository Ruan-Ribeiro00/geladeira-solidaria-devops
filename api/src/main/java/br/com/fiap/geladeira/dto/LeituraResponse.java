package br.com.fiap.geladeira.dto;

import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Relation(itemRelation = "leitura", collectionRelation = "leituras")
public record LeituraResponse(
        Long id,
        Long geladeiraId,
        LocalDateTime dtLeitura,
        BigDecimal temperaturaC,
        BigDecimal umidadePct,
        boolean portaAberta,
        boolean foraDoLimite,
        Boolean alertaGerado
) {
}
