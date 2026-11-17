package br.com.fiap.geladeira.exception;

/** 422: a requisicao e valida, mas fere uma regra de negocio (ex.: geladeira lotada). */
public class RegraNegocioException extends RuntimeException {

    private final transient Object sugestao;

    public RegraNegocioException(String mensagem) {
        this(mensagem, null);
    }

    public RegraNegocioException(String mensagem, Object sugestao) {
        super(mensagem);
        this.sugestao = sugestao;
    }

    public Object getSugestao() {
        return sugestao;
    }
}
