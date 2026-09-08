package com.todolist.exception;

public class NomeDeHabitoEmUsoException extends RuntimeException {

    public NomeDeHabitoEmUsoException(String nome) {
        super("Já existe um hábito chamado \"" + nome + "\" nesta conta");
    }
}
