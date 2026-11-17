package br.com.fiap.geladeira.dto;

import br.com.fiap.geladeira.model.TipoDoador;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record DoadorRequest(
        @NotBlank(message = "O nome e obrigatorio")
        @Size(max = 100)
        @Schema(example = "Quitanda Dona Rosa")
        String nome,

        @NotNull(message = "O tipo e obrigatorio (PF ou PJ)")
        @Schema(example = "PJ")
        TipoDoador tipo,

        @Pattern(regexp = "^\\(\\d{2}\\) \\d{4,5}-\\d{4}$", message = "Telefone no formato (11) 99999-9999")
        @Schema(example = "(11) 98888-7777")
        String telefone,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 120)
        @Schema(example = "contato@quitandarosa.example.com")
        String email
) {
}
