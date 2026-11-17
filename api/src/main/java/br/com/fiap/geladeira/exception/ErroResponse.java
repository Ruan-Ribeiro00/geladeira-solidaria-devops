package br.com.fiap.geladeira.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** Formato unico de erro devolvido por toda a API. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        List<CampoInvalido> campos,
        Object sugestao
) {
    public record CampoInvalido(String campo, String mensagem) {
    }
}
