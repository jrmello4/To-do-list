package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaEsporteRequest {

    @NotBlank(message = "O esporte é obrigatório")
    private String esporte;

    @NotBlank(message = "O nome do interesse/time/atleta é obrigatório")
    private String nomeInteresse;

    private String icone;

    private String cor;
}
