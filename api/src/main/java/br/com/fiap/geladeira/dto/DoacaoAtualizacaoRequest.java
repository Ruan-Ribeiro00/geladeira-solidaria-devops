package br.com.fiap.geladeira.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Correcao de uma doacao ja registrada (a geladeira e o doador nao mudam). */
public record DoacaoAtualizacaoRequest(
        @NotNull(message = "Informe a categoria")
        Long categoriaId,

        @NotBlank(message = "A descricao e obrigatoria")
        @Size(max = 120)
        String descricao,

        @NotNull(message = "A quantidade e obrigatoria")
        @Positive(message = "A quantidade tem que ser maior que zero")
        @DecimalMax(value = "200", message = "Uma doacao pode ter no maximo 200 kg")
        BigDecimal quantidadeKg,

        @NotNull(message = "A validade e obrigatoria")
        LocalDate dtValidade
) {
}
