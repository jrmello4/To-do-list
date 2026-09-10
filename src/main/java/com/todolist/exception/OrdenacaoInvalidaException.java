package com.todolist.exception;

/**
 * Pedido de ordenação por um campo que a listagem não aceita.
 *
 * Própria, e não IllegalArgumentException, porque o tratamento devolve a lista
 * de campos válidos junto — informação que só existe neste caso.
 */
public class OrdenacaoInvalidaException extends RuntimeException {

    private final transient String campo;

    public OrdenacaoInvalidaException(String campo) {
        super("Não é possível ordenar por \"" + campo + "\"");
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
