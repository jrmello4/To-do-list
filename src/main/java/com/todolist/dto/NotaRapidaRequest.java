package com.todolist.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaRapidaRequest {
    @Size(max = 150, message = "O título deve ter no máximo 150 caracteres")
    private String titulo;
    private String conteudo;
    @Size(max = 10, message = "A cor deve ter no máximo 10 caracteres")
    private String cor;
    private Boolean fixada;
}
