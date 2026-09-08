package com.todolist.exception;

public class NomeDeEtiquetaEmUsoException extends RuntimeException {

    public NomeDeEtiquetaEmUsoException(String nome) {
        super("Já existe uma etiqueta chamada \"" + nome + "\" nesta conta");
    }
}
