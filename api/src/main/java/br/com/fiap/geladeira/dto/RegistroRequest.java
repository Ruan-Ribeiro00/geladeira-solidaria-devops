package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.TipoDoador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Cadastro publico de um novo doador (cria o doador e a conta de acesso com papel DOADOR). */
public record RegistroRequest(
        @NotBlank(message = "O nome e obrigatorio") @Size(max = 100)
        @Schema(example = "Pedro Henrique Alves")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio") @Email(message = "E-mail invalido") @Size(max = 120)
        @Schema(example = "pedro.alves@example.com")
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$",
                 message = "A senha precisa ter de 8 a 64 caracteres, com letras e numeros")
        @Schema(example = "Senha@2026")
        String senha,

        @NotNull(message = "O tipo e obrigatorio (PF ou PJ)")
        @Schema(example = "PF")
        TipoDoador tipo,

        @Pattern(regexp = "^\\(\\d{2}\\) \\d{4,5}-\\d{4}$", message = "Telefone no formato (11) 99999-9999")
        @Schema(example = "(11) 97777-6666")
        String telefone
) {
}
