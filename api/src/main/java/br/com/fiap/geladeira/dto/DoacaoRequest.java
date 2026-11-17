package br.com.fiap.geladeira.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Registro de uma nova doacao numa geladeira")
public record DoacaoRequest(
        @NotNull(message = "Informe a geladeira")
        @Schema(example = "5")
        Long geladeiraId,

        @Schema(description = "Obrigatorio para ADMIN e VOLUNTARIO. Para o papel DOADOR, usa o doador da propria conta.",
                example = "5")
        Long doadorId,

        @NotNull(message = "Informe a categoria")
        @Schema(example = "6")
        Long categoriaId,

        @NotBlank(message = "A descricao e obrigatoria")
        @Size(max = 120, message = "A descricao pode ter no maximo 120 caracteres")
        @Schema(example = "Marmitas de macarrao")
        String descricao,

        @NotNull(message = "A quantidade e obrigatoria")
        @Positive(message = "A quantidade tem que ser maior que zero")
        @DecimalMax(value = "200", message = "Uma doacao pode ter no maximo 200 kg")
        @Schema(example = "6")
        BigDecimal quantidadeKg,

        @NotNull(message = "A validade e obrigatoria")
        @FutureOrPresent(message = "Nao e permitido doar alimento vencido")
        @Schema(example = "2026-11-12")
        LocalDate dtValidade
) {
}
