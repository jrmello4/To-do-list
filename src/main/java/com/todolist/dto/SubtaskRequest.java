package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubtaskRequest {

    @NotBlank(message = "O título da subtarefa é obrigatório")
    @Size(max = 200, message = "O título da subtarefa deve ter no máximo 200 caracteres")
    private String titulo;

    private Boolean concluida;
}
