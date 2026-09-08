package com.todolist.dto;

import com.todolist.entity.Prioridade;

import java.time.LocalDate;

/**
 * Critérios opcionais da listagem. Todos podem ser nulos; o dono da tarefa
 * não entra aqui de propósito — ele nunca é opcional e é aplicado à parte.
 */
public record TaskFiltro(
        Boolean concluida,
        Long projetoId,
        Long etiquetaId,
        Boolean semProjeto,
        Prioridade prioridade,
        LocalDate prazoAte,
        String busca) {
}
