package com.todolist.repository;

import com.todolist.exception.OrdenacaoInvalidaException;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

/**
 * Campos pelos quais a listagem de tarefas aceita ordenar.
 *
 * Sem esta lista, ?sort= aceita qualquer caminho que o Spring Data consiga
 * resolver na entidade — inclusive coisas que não são campos da tarefa:
 *
 * - `sort=subtarefas.titulo` navega para uma coleção, o que vira junção e
 *   repete a tarefa uma vez por passo. Com paginação, a página volta com
 *   linhas duplicadas e totalElements passando a contar junção, não tarefa.
 * - `sort=usuario.senhaHash` ordena por uma coluna que nenhuma resposta expõe.
 *   Como a listagem já é filtrada pelo dono, ninguém lê o hash de outra conta
 *   por aqui — mas ordenar por ele não é algo que a API deva aceitar.
 *
 * A lista também é o que a documentação promete e o que a mensagem de erro
 * enumera: os três saem daqui, e não de três lugares que precisam concordar.
 */
public final class OrdenacaoDeTarefas {

    /** Ordem fixa para a mensagem de erro sair sempre igual. */
    public static final List<String> CAMPOS = List.of(
            "id", "ordem", "titulo", "prazo", "prioridade",
            "concluida", "dataCriacao", "dataAtualizacao", "dataConclusao");

    private static final Set<String> PERMITIDOS = Set.copyOf(CAMPOS);

    private OrdenacaoDeTarefas() {
    }

    /** Texto pronto para a documentação e para o detalhe do erro. */
    public static String listados() {
        return String.join(", ", CAMPOS);
    }

    public static void verificar(Sort ordenacao) {
        if (ordenacao == null || ordenacao.isUnsorted()) {
            return;
        }

        for (Sort.Order ordem : ordenacao) {
            if (!PERMITIDOS.contains(ordem.getProperty())) {
                throw new OrdenacaoInvalidaException(ordem.getProperty());
            }
        }
    }
}
