package br.com.fiap.geladeira.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CategoriaRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 60)
        @Schema(example = "Congelados")
        String nome,

        @NotNull(message = "Informe se a categoria precisa de refrigeracao")
        @Schema(example = "true")
        Boolean refrigerado,

        @DecimalMin(value = "-30.0", message = "Temperatura maxima invalida")
        @DecimalMax(value = "25.0", message = "Temperatura maxima invalida")
        @Schema(description = "Obrigatoria para categorias refrigeradas", example = "-12.0")
        BigDecimal tempMaxC
) {
    /** Regra de validacao que depende de dois campos: refrigerado exige temperatura maxima. */
    @AssertTrue(message = "Categorias refrigeradas precisam de temperatura maxima (tempMaxC)")
    @Schema(hidden = true)
    public boolean isTemperaturaCoerente() {
        return !Boolean.TRUE.equals(refrigerado) || tempMaxC != null;
    }
}
