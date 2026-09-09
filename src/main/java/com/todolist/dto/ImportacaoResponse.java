package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "ImportacaoResponse", description = "O que a importação criou e o que ignorou")
public class ImportacaoResponse {

    @Schema(description = "Projetos criados", example = "2")
    private int projetos;

    @Schema(description = "Etiquetas criadas", example = "3")
    private int etiquetas;

    @Schema(description = "Tarefas criadas", example = "48")
    private int tarefas;

    @Schema(description = "Hábitos criados", example = "1")
    private int habitos;

    @Schema(description = "Dias de hábito registrados", example = "120")
    private int registrosDeHabito;

    @Schema(description = "O que já existia e foi reaproveitado em vez de duplicado")
    private List<String> reaproveitados;
}
