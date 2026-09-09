package com.todolist.lembretes;

import com.todolist.dto.TaskResponse;

import java.util.List;

/**
 * O que um lembrete diz. Separado de como ele é entregue, para a regra de
 * quando enviar poder ser testada sem depender de servidor de e-mail.
 */
public record Lembrete(
        String destinatario,
        String nome,
        List<TaskResponse> atrasadas,
        List<TaskResponse> vencemHoje) {

    public int total() {
        return atrasadas.size() + vencemHoje.size();
    }

    public String assunto() {
        if (!atrasadas.isEmpty()) {
            return atrasadas.size() == 1
                    ? "1 tarefa passou do prazo"
                    : atrasadas.size() + " tarefas passaram do prazo";
        }
        return vencemHoje.size() == 1 ? "1 tarefa vence hoje" : vencemHoje.size() + " tarefas vencem hoje";
    }

    public String corpo() {
        StringBuilder texto = new StringBuilder("Olá, ").append(nome).append(".\n\n");

        if (!atrasadas.isEmpty()) {
            texto.append("Passaram do prazo:\n");
            atrasadas.forEach(tarefa -> texto.append("  · ").append(tarefa.getTitulo())
                    .append(" (").append(tarefa.getPrazo()).append(")\n"));
            texto.append('\n');
        }

        if (!vencemHoje.isEmpty()) {
            texto.append("Vencem hoje:\n");
            vencemHoje.forEach(tarefa -> texto.append("  · ").append(tarefa.getTitulo()).append('\n'));
            texto.append('\n');
        }

        return texto.append("— To-do List\n").toString();
    }
}
