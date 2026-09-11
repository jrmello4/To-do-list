package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreferenciaEsporteRequest {

    @NotBlank(message = "O esporte é obrigatório")
    @Size(max = 50, message = "O esporte deve ter no máximo 50 caracteres")
    private String esporte;

    @NotBlank(message = "O nome do interesse/time/atleta é obrigatório")
    @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
    private String nomeInteresse;

    @Size(max = 20, message = "O ícone deve ter no máximo 20 caracteres")
    private String icone;

    @Size(max = 10, message = "A cor deve ter no máximo 10 caracteres")
    private String cor;
}
