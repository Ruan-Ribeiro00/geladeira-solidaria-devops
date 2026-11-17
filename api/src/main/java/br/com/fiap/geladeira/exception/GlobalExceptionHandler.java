package br.com.fiap.geladeira.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.List;

/**
 * Tratamento centralizado de erros: toda excecao vira um {@link ErroResponse}
 * com o status HTTP certo, em vez de stack trace para o cliente.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 400: Bean Validation no corpo da requisicao (@Valid). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> campos = ex.getBindingResult().getAllErrors().stream()
                .map(e -> new ErroResponse.CampoInvalido(
                        e instanceof FieldError fe ? fe.getField() : e.getObjectName(),
                        e.getDefaultMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Dados invalidos na requisicao", req, campos, null);
    }

    /** 400: validacao em parametros de URL/query (@Validated no controller). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> validacaoParametro(ConstraintViolationException ex, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> campos = ex.getConstraintViolations().stream()
                .map(v -> new ErroResponse.CampoInvalido(nomeParametro(v.getPropertyPath().toString()), v.getMessage()))
                .toList();
        return montar(HttpStatus.BAD_REQUEST, "Parametros invalidos", req, campos, null);
    }

    /** 400: JSON mal formado ou valor de enum que nao existe. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "Corpo da requisicao invalido. Confira o formato do JSON, das datas (aaaa-mm-dd) e dos valores aceitos.",
                req, null, null);
    }

    /** 400: parametro de URL com tipo errado (ex.: /geladeiras/abc). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> tipoErrado(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        return montar(HttpStatus.BAD_REQUEST,
                "Valor invalido para o parametro '" + ex.getName() + "': " + ex.getValue(), req, null, null);
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, ex.getMessage(), req, null, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return montar(HttpStatus.NOT_FOUND, "Rota nao encontrada", req, null, null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodo(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return montar(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), req, null, null);
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> conflito(ConflitoException ex, HttpServletRequest req) {
        return montar(HttpStatus.CONFLICT, ex.getMessage(), req, null, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Violacao de integridade em {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return montar(HttpStatus.CONFLICT,
                "A operacao fere uma restricao do banco (registro duplicado ou em uso)", req, null, null);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> regraNegocio(RegraNegocioException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), req, null, ex.getSugestao());
    }

    @ExceptionHandler({BadCredentialsException.class, DisabledException.class})
    public ResponseEntity<ErroResponse> credenciais(RuntimeException ex, HttpServletRequest req) {
        return montar(HttpStatus.UNAUTHORIZED, "E-mail ou senha invalidos", req, null, null);
    }

    /** 403 vindo do @PreAuthorize (o papel do usuario nao pode fazer isso). */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return montar(HttpStatus.FORBIDDEN, "Seu perfil nao tem permissao para esta operacao", req, null, null);
    }

    /** 500: qualquer erro nao previsto. O detalhe fica no log, nao na resposta. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        log.error("Erro inesperado em {} {}", req.getMethod(), req.getRequestURI(), ex);
        return montar(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente em instantes.", req, null, null);
    }

    private ResponseEntity<ErroResponse> montar(HttpStatus status, String mensagem, HttpServletRequest req,
                                                List<ErroResponse.CampoInvalido> campos, Object sugestao) {
        ErroResponse corpo = new ErroResponse(Instant.now(), status.value(), status.getReasonPhrase(), mensagem,
                req.getRequestURI(), campos, sugestao);
        return ResponseEntity.status(status).body(corpo);
    }

    private static String nomeParametro(String caminho) {
        int ponto = caminho.lastIndexOf('.');
        return ponto >= 0 ? caminho.substring(ponto + 1) : caminho;
    }
}
