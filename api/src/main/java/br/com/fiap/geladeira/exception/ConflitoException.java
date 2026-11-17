package br.com.fiap.geladeira.exception;

/** 409: conflito com o estado atual (e-mail ja usado, registro com dependencias...). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
