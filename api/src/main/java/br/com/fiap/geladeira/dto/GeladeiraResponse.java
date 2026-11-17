package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.StatusGeladeira;
import org.springframework.hateoas.server.core.Relation;

import java.math.BigDecimal;
import java.time.LocalDate;

@Relation(itemRelation = "geladeira", collectionRelation = "geladeiras")
public record GeladeiraResponse(
        Long id,
        String nome,
        String endereco,
        String bairro,
        String cidade,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal capacidadeKg,
        BigDecimal ocupadoKg,
        BigDecimal livreKg,
        BigDecimal ocupacaoPct,
        String responsavel,
        StatusGeladeira status,
        LocalDate dtInstalacao
) {
}
