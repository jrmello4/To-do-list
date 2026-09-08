package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "HabitoRequest", description = "Dados para criar ou atualizar um hábito")
public class HabitoRequest {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres")
    @Schema(description = "Nome do hábito. Único dentro da conta.", example = "Ler 20 páginas",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String nome;

    @Size(max = 20, message = "A cor deve ter no máximo 20 caracteres")
    @Schema(description = "Identificador de cor da paleta da interface",
            example = "verde", defaultValue = "verde")
    private String cor;

    @Schema(description = "Dias da semana em que o hábito vale, no padrão ISO "
            + "(1 = segunda, 7 = domingo). Omitir vale por todos os dias.",
            example = "[1, 3, 5]")
    private List<@Min(value = 1, message = "Dia da semana deve estar entre 1 e 7")
                 @Max(value = 7, message = "Dia da semana deve estar entre 1 e 7") Integer> diasSemana;

    @Schema(description = "Hábito inativo sai da lista sem apagar o histórico",
            example = "true", defaultValue = "true")
    private Boolean ativo;
}
