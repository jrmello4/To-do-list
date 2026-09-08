package com.todolist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagRequest {

    @NotBlank(message = "O nome da tag é obrigatório")
    @Size(max = 50, message = "O nome da tag deve ter no máximo 50 caracteres")
    private String nome;

    @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "A cor deve estar no formato hexadecimal (ex: #6366f1)")
    @Builder.Default
    private String cor = "#6366f1";
}
