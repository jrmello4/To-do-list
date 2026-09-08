package com.todolist.db;

import com.todolist.entity.Task;
import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercita a persistência contra o schema que o Flyway criou, e não contra um
 * schema gerado pelo Hibernate a partir das anotações. É a diferença entre
 * testar o que roda em produção e testar uma aproximação dele.
 *
 * Replace.NONE mantém o datasource do perfil de teste; sem isso o Spring
 * substituiria por um banco embutido próprio e o Flyway não entraria na conta.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("TaskRepository sobre o schema do Flyway")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("grava e recupera uma tarefa")
    void gravaERecupera() {
        Task salva = taskRepository.save(Task.builder()
                .titulo("Estudar Flyway")
                .descricao("Entender o histórico de migrations")
                .build());

        entityManager.flush();
        entityManager.clear();

        Optional<Task> encontrada = taskRepository.findById(salva.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.get().getTitulo()).isEqualTo("Estudar Flyway");
        assertThat(encontrada.get().getDescricao()).isEqualTo("Entender o histórico de migrations");
        assertThat(encontrada.get().getConcluida()).isFalse();
    }

    @Test
    @DisplayName("preenche as datas pelos callbacks da entidade")
    void preencheAsDatas() {
        Task salva = taskRepository.save(Task.builder().titulo("Com data").build());
        entityManager.flush();

        assertThat(salva.getDataCriacao()).isNotNull();
        assertThat(salva.getDataAtualizacao()).isNotNull();
    }

    @Test
    @DisplayName("aceita descrição longa, porque a coluna é TEXT e não VARCHAR(255)")
    void aceitaDescricaoLonga() {
        String longa = "d".repeat(1000);

        Task salva = taskRepository.save(Task.builder().titulo("Descrição longa").descricao(longa).build());
        entityManager.flush();
        entityManager.clear();

        assertThat(taskRepository.findById(salva.getId()))
                .get()
                .extracting(Task::getDescricao)
                .isEqualTo(longa);
    }

    @Test
    @DisplayName("remove uma tarefa")
    void remove() {
        Task salva = taskRepository.save(Task.builder().titulo("Para excluir").build());
        entityManager.flush();

        taskRepository.deleteById(salva.getId());
        entityManager.flush();

        assertThat(taskRepository.findById(salva.getId())).isEmpty();
    }
}
