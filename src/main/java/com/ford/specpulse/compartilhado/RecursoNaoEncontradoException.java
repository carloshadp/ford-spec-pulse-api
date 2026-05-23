package com.ford.specpulse.compartilhado;


public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }

    public static RecursoNaoEncontradoException porId(String recurso, Object id) {
        return new RecursoNaoEncontradoException(recurso + " com id " + id + " nao encontrado.");
    }
}
