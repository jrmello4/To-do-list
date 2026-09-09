package com.todolist.dto;

import com.todolist.entity.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "TaskResponse", description = "Representação de uma tarefa retornada pela API")
public class TaskResponse {

    @Schema(description = "Identificador único da tarefa", example = "1")
    private Long id;

    @Schema(description = "Título da tarefa", example = "Estudar Spring Boot")
    private String titulo;

    @Schema(description = "Detalhamento da tarefa", example = "Aprofundar em JPA e Flyway")
    private String descricao;

    @Schema(description = "Indica se a tarefa já foi concluída", example = "false")
    private Boolean concluida;

    @Schema(description = "Projeto da tarefa. Nulo significa caixa de entrada.")
    private ProjetoResumoResponse projeto;

    @Schema(description = "Etiquetas aplicadas à tarefa")
    private List<EtiquetaResumoResponse> etiquetas;

    @Schema(description = "Posição manual na lista da conta", example = "3")
    private Integer ordem;

    @Schema(description = "Passos da tarefa, em ordem")
    private List<SubtarefaResponse> subtarefas;

    /**
     * Contagens vindas prontas do servidor. O cliente poderia somar a lista
     * acima, mas então dois lugares saberiam a mesma regra — e o dia em que a
     * regra mudar, só um deles muda.
     */
    @Schema(description = "Quantos passos a tarefa tem", example = "5")
    private Integer totalDePassos;

    @Schema(description = "Quantos passos já foram cumpridos", example = "2")
    private Integer passosConcluidos;

    @Schema(description = "Data limite. Nula quando a tarefa não tem prazo.", example = "2026-09-30")
    private LocalDate prazo;

    @Schema(description = "Prioridade da tarefa", example = "ALTA")
    private Prioridade prioridade;

    @Schema(description = "Quando a tarefa foi concluída. Nula enquanto estiver pendente.",
            example = "2026-09-08T18:20:00")
    private LocalDateTime dataConclusao;

    @Schema(description = "Data e hora em que a tarefa foi criada", example = "2026-06-26T10:00:00")
    private LocalDateTime dataCriacao;

    @Schema(description = "Data e hora da última alteração", example = "2026-06-26T10:00:00")
    private LocalDateTime dataAtualizacao;
}
