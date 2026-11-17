package br.com.fiap.geladeira.security;

import br.com.fiap.geladeira.exception.ErroResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/** 401 e 403 no mesmo formato JSON de erro do resto da API. */
@Component
public class RespostasSeguranca {

    private final ObjectMapper mapper;

    public RespostasSeguranca(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public AuthenticationEntryPoint naoAutenticado() {
        return (req, resp, ex) -> escrever(req, resp, HttpStatus.UNAUTHORIZED,
                "Faca login e envie o token no header Authorization: Bearer <token>");
    }

    public AccessDeniedHandler acessoNegado() {
        return (req, resp, ex) -> escrever(req, resp, HttpStatus.FORBIDDEN,
                "Seu perfil nao tem permissao para esta operacao");
    }

    private void escrever(HttpServletRequest req, HttpServletResponse resp, HttpStatus status, String mensagem)
            throws IOException {
        resp.setStatus(status.value());
        resp.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resp.setCharacterEncoding("UTF-8");
        mapper.writeValue(resp.getOutputStream(), new ErroResponse(Instant.now(), status.value(),
                status.getReasonPhrase(), mensagem, req.getRequestURI(), null, null));
    }
}
