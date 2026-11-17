package br.com.fiap.geladeira.exception;

/** 404: o registro pedido nao existe. */
public class RecursoNaoEncontradoException extends RuntimeException {
    public RecursoNaoEncontradoException(String recurso, Long id) {
        super(recurso + " com id " + id + " nao encontrado(a)");
    }
}
