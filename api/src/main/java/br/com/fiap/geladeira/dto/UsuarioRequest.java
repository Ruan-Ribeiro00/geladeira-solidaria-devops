package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.Papel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Criacao de usuarios internos (ADMIN, VOLUNTARIO ou SENSOR) pelo administrador. */
public record UsuarioRequest(
        @NotBlank @Size(max = 100) @Schema(example = "Lucas Voluntario")
        String nome,

        @NotBlank @Email @Size(max = 120) @Schema(example = "lucas@geladeira.test")
        String email,

        @NotBlank
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                 message = "A senha precisa ter de 8 a 64 caracteres, com letras e numeros")
        @Schema(example = "Voluntario@2026")
        String senha,

        @NotNull @Schema(example = "VOLUNTARIO")
        Papel papel
) {
}
