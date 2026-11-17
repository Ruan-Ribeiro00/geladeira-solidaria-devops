package br.com.fiap.geladeira.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Informe o e-mail") @Email(message = "E-mail invalido")
        @Schema(example = "admin@geladeira.test")
        String email,

        @NotBlank(message = "Informe a senha")
        @Schema(example = "Admin@123")
        String senha
) {
}
