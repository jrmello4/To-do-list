package com.todolist.entity;

/**
 * Persistida com @Enumerated(EnumType.STRING) — nunca ORDINAL, que grava a
 * posição na declaração e corromperia os dados em silêncio se alguém
 * inserisse um valor no meio da lista.
 */
public enum Prioridade {
    BAIXA,
    MEDIA,
    ALTA,
    URGENTE
}
