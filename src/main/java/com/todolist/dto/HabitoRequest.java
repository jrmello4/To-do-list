package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HabitoRequest {
    @NotBlank(message = "O nome do hábito é obrigatório")
    @Size(max = 100)
    private String nome;
    @Size(max = 50)
    private String icone;
    @Size(max = 10)
    private String cor;
}