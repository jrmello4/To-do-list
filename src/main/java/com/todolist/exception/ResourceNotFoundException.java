package com.todolist.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String recurso;
    private final Long id;

    public ResourceNotFoundException(String recurso, Long id) {
        super(String.format("%s com id %d não encontrado(a)", recurso, id));
        this.recurso = recurso;
        this.id = id;
    }
}
