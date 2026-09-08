package com.todolist.repository;

import com.todolist.entity.Prioridade;
import com.todolist.entity.Task;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Filtros compostos para a listagem de tarefas.
 *
 * Specification em vez de uma explosão de findByXAndYAndZ: são cinco filtros
 * opcionais e combináveis, o que daria dezenas de métodos derivados.
 *
 * doUsuario não é opcional. Toda consulta parte dela — é o que mantém o
 * isolamento entre contas também nesta rota.
 */
public final class TaskSpecifications {

    private TaskSpecifications() {
    }

    public static Specification<Task> doUsuario(Long usuarioId) {
        return (raiz, consulta, construtor) ->
                construtor.equal(raiz.get("usuario").get("id"), usuarioId);
    }

    public static Specification<Task> concluida(Boolean valor) {
        if (valor == null) {
            return null;
        }
        return (raiz, consulta, construtor) -> construtor.equal(raiz.get("concluida"), valor);
    }

    public static Specification<Task> doProjeto(Long projetoId) {
        if (projetoId == null) {
            return null;
        }
        return (raiz, consulta, construtor) ->
                construtor.equal(raiz.get("projeto").get("id"), projetoId);
    }

    /** Caixa de entrada: tarefas que não pertencem a projeto algum. */
    public static Specification<Task> semProjeto(Boolean valor) {
        if (!Boolean.TRUE.equals(valor)) {
            return null;
        }
        return (raiz, consulta, construtor) -> construtor.isNull(raiz.get("projeto"));
    }

    public static Specification<Task> comPrioridade(Prioridade prioridade) {
        if (prioridade == null) {
            return null;
        }
        return (raiz, consulta, construtor) -> construtor.equal(raiz.get("prioridade"), prioridade);
    }

    /** Vence até a data informada. Tarefa sem prazo fica de fora. */
    public static Specification<Task> prazoAte(LocalDate data) {
        if (data == null) {
            return null;
        }
        return (raiz, consulta, construtor) ->
                construtor.and(
                        construtor.isNotNull(raiz.get("prazo")),
                        construtor.lessThanOrEqualTo(raiz.get("prazo"), data));
    }

    public static Specification<Task> busca(String termo) {
        if (termo == null || termo.isBlank()) {
            return null;
        }

        String padrao = "%" + termo.trim().toLowerCase() + "%";

        return (raiz, consulta, construtor) -> construtor.or(
                construtor.like(construtor.lower(raiz.get("titulo")), padrao),
                construtor.like(construtor.lower(construtor.coalesce(raiz.get("descricao"), "")),
                        padrao));
    }
}
