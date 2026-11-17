package br.com.fiap.geladeira.dto;

import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;

@Relation(itemRelation = "ocupacao", collectionRelation = "ocupacoes")
public record OcupacaoResponse(
        Long geladeiraId,
        String nome,
        String bairro,
        BigDecimal capacidadeKg,
        BigDecimal ocupadoKg,
        BigDecimal livreKg,
        BigDecimal ocupacaoPct
) {
}
