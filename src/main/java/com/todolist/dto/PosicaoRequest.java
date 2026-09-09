package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Para onde a tarefa vai, dito por um vizinho e não por um número.
 *
 * "Posição 7" não significaria nada estável aqui: a listagem é paginada e
 * filtrada, então o sétimo da tela raramente é o sétimo da conta, e o sétimo
 * de hoje não é o de amanhã. Um vizinho concreto é sempre o mesmo em qualquer
 * filtro, e é sempre alguém que quem arrastou estava vendo.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "PosicaoRequest", description = "Nova posição da tarefa, relativa a outra")
public class PosicaoRequest {

    @Schema(description = "Colocar imediatamente antes desta tarefa", example = "12")
    private Long antesDe;

    @Schema(description = "Colocar imediatamente depois desta tarefa", example = "12")
    private Long depoisDe;
}
