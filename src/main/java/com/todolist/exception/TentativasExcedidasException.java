package com.todolist.exception;

import java.time.Duration;

/** Tentativas de senha demais vindas da mesma origem. */
public class TentativasExcedidasException extends RuntimeException {

    private final transient Duration espera;

    public TentativasExcedidasException(Duration espera) {
        super("Tentativas demais. Aguarde " + espera.toMinutes() + " minutos e tente de novo.");
        this.espera = espera;
    }

    public Duration getEspera() {
        return espera;
    }
}
