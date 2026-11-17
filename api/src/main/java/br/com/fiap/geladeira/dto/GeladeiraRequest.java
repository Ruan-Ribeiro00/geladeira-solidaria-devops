package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.StatusGeladeira;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Dados para cadastrar ou atualizar uma geladeira")
public record GeladeiraRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 100, message = "O nome pode ter no maximo 100 caracteres")
        @Schema(example = "Geladeira Solidaria Vila Prudente")
        String nome,

        @NotBlank(message = "O endereco e obrigatorio")
        @Size(max = 150)
        @Schema(example = "Rua do Orfanato, 300")
        String endereco,

        @NotBlank(message = "O bairro e obrigatorio")
        @Size(max = 60)
        @Schema(example = "Vila Prudente")
        String bairro,

        @NotBlank(message = "A cidade e obrigatoria")
        @Size(max = 60)
        @Schema(example = "Sao Paulo")
        String cidade,

        @DecimalMin(value = "-90.0", message = "Latitude invalida")
        @DecimalMax(value = "90.0", message = "Latitude invalida")
        @Schema(example = "-23.5850")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude invalida")
        @DecimalMax(value = "180.0", message = "Longitude invalida")
        @Schema(example = "-46.5820")
        BigDecimal longitude,

        @NotNull(message = "A capacidade e obrigatoria")
        @Positive(message = "A capacidade tem que ser maior que zero")
        @DecimalMax(value = "1000", message = "A capacidade maxima aceita e 1000 kg")
        @Schema(example = "80")
        BigDecimal capacidadeKg,

        @NotBlank(message = "O responsavel e obrigatorio")
        @Size(max = 100)
        @Schema(example = "Associacao de Moradores da Vila Prudente")
        String responsavel,

        @Schema(description = "Se nao vier, a geladeira e criada como ATIVA", example = "ATIVA")
        StatusGeladeira status,

        @PastOrPresent(message = "A data de instalacao nao pode estar no futuro")
        @Schema(description = "Se nao vier, usa a data de hoje", example = "2026-11-01")
        LocalDate dtInstalacao
) {
}
