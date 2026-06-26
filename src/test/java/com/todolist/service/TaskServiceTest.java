package com.todolist.service;

import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.Task;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    private Task task;
    private TaskRequest request;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot e JPA")
                .concluida(false)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        request = TaskRequest.builder()
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot e JPA")
                .concluida(false)
                .build();
    }

    @Nested
    @DisplayName("Criação de tarefa")
    class Criar {

        @Test
        @DisplayName("Deve criar tarefa com sucesso")
        void deveCriarTarefaComSucesso() {
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            TaskResponse response = taskService.criar(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitulo()).isEqualTo("Estudar Java");
            assertThat(response.getDescricao()).isEqualTo("Estudar Spring Boot e JPA");
            assertThat(response.getConcluida()).isFalse();

            verify(taskRepository).save(taskCaptor.capture());
            Task saved = taskCaptor.getValue();
            assertThat(saved.getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve criar tarefa com concluida=true quando fornecida")
        void deveCriarTarefaConcluida() {
            request.setConcluida(true);
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task t = invocation.getArgument(0);
                t.setId(2L);
                t.setDataCriacao(LocalDateTime.now());
                t.setDataAtualizacao(LocalDateTime.now());
                return t;
            });

            TaskResponse response = taskService.criar(request);

            assertThat(response.getConcluida()).isTrue();
        }

        @Test
        @DisplayName("Deve criar tarefa com concluida=false quando não fornecida")
        void deveCriarTarefaComConcluidaPadrao() {
            request.setConcluida(null);
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
                Task t = invocation.getArgument(0);
                t.setId(3L);
                t.setDataCriacao(LocalDateTime.now());
                t.setDataAtualizacao(LocalDateTime.now());
                return t;
            });

            TaskResponse response = taskService.criar(request);

            assertThat(response.getConcluida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Listagem de tarefas")
    class Listar {

        @Test
        @DisplayName("Deve listar todas as tarefas")
        void deveListarTodas() {
            when(taskRepository.findAll()).thenReturn(List.of(task));

            List<TaskResponse> responses = taskService.listarTodas();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não houver tarefas")
        void deveRetornarListaVazia() {
            when(taskRepository.findAll()).thenReturn(List.of());

            List<TaskResponse> responses = taskService.listarTodas();

            assertThat(responses).isEmpty();
        }
    }

    @Nested
    @DisplayName("Busca por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve buscar tarefa por ID com sucesso")
        void deveBuscarPorId() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.buscarPorId(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não existir")
        void deveLancarExcecaoQuandoNaoExistir() {
            when(taskRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Tarefa")
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("Atualização de tarefa")
    class Atualizar {

        @Test
        @DisplayName("Deve atualizar tarefa com sucesso")
        void deveAtualizarTarefa() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            TaskRequest updateRequest = TaskRequest.builder()
                    .titulo("Estudar Spring Boot")
                    .descricao("Aprofundar em JPA e Flyway")
                    .concluida(true)
                    .build();

            TaskResponse response = taskService.atualizar(1L, updateRequest);

            assertThat(response.getTitulo()).isEqualTo("Estudar Spring Boot");
            assertThat(response.getDescricao()).isEqualTo("Aprofundar em JPA e Flyway");
            assertThat(response.getConcluida()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar tarefa inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            when(taskRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.atualizar(99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Deve manter concluida atual quando não fornecida na atualização")
        void deveManterConcluidaQuandoNaoFornecida() {
            task.setConcluida(true);
            request.setConcluida(null);

            when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            TaskResponse response = taskService.atualizar(1L, request);

            assertThat(response.getConcluida()).isTrue();
        }
    }

    @Nested
    @DisplayName("Exclusão de tarefa")
    class Deletar {

        @Test
        @DisplayName("Deve deletar tarefa com sucesso")
        void deveDeletarTarefa() {
            when(taskRepository.existsById(1L)).thenReturn(true);

            taskService.deletar(1L);

            verify(taskRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar tarefa inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            when(taskRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> taskService.deletar(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
