package com.todolist.exception;

public class NomeDeProjetoEmUsoException extends RuntimeException {

    public NomeDeProjetoEmUsoException(String nome) {
        super("Já existe um projeto chamado \"" + nome + "\" nesta conta");
    }
}
