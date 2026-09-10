package com.todolist.service;

import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.Task;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.entity.Usuario;
import com.todolist.repository.TaskRepository;
import com.todolist.dto.TaskFiltro;
import com.todolist.repository.EtiquetaRepository;
import com.todolist.repository.ProjetoRepository;
import com.todolist.repository.UsuarioRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    /** Dono usado em todas as chamadas: o serviço deixou de operar sem um. */
    private static final Long USUARIO_ID = 7L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private EtiquetaRepository etiquetaRepository;

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
                .usuario(Usuario.builder().id(USUARIO_ID).build())
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
            when(taskRepository.saveAndFlush(any(Task.class))).thenReturn(task);

            TaskResponse response = taskService.criar(USUARIO_ID, request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitulo()).isEqualTo("Estudar Java");
            assertThat(response.getDescricao()).isEqualTo("Estudar Spring Boot e JPA");
            assertThat(response.getConcluida()).isFalse();

            verify(taskRepository).saveAndFlush(taskCaptor.capture());
            Task saved = taskCaptor.getValue();
            assertThat(saved.getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve criar tarefa com concluida=true quando fornecida")
        void deveCriarTarefaConcluida() {
            request.setConcluida(true);
            when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> {
                Task t = invocation.getArgument(0);
                t.setId(2L);
                t.setDataCriacao(LocalDateTime.now());
                t.setDataAtualizacao(LocalDateTime.now());
                return t;
            });

            TaskResponse response = taskService.criar(USUARIO_ID, request);

            assertThat(response.getConcluida()).isTrue();
        }

        @Test
        @DisplayName("Deve criar tarefa com concluida=false quando não fornecida")
        void deveCriarTarefaComConcluidaPadrao() {
            request.setConcluida(null);
            when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> {
                Task t = invocation.getArgument(0);
                t.setId(3L);
                t.setDataCriacao(LocalDateTime.now());
                t.setDataAtualizacao(LocalDateTime.now());
                return t;
            });

            TaskResponse response = taskService.criar(USUARIO_ID, request);

            assertThat(response.getConcluida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Listagem de tarefas")
    class Listar {

        private static final TaskFiltro SEM_FILTRO =
                new TaskFiltro(null, null, null, null, null, null, null);
        private static final Pageable PRIMEIRA_PAGINA = PageRequest.of(0, 50);

        @Test
        @DisplayName("Deve devolver uma página com as tarefas")
        void deveListarTodas() {
            when(taskRepository.findAll(any(Specification.class), eq(PRIMEIRA_PAGINA)))
                    .thenReturn(new PageImpl<>(List.of(task), PRIMEIRA_PAGINA, 1));

            Page<TaskResponse> pagina = taskService.listar(USUARIO_ID, SEM_FILTRO, PRIMEIRA_PAGINA);

            assertThat(pagina.getTotalElements()).isEqualTo(1);
            assertThat(pagina.getContent().get(0).getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve devolver página vazia quando não houver tarefas")
        void deveRetornarListaVazia() {
            when(taskRepository.findAll(any(Specification.class), eq(PRIMEIRA_PAGINA)))
                    .thenReturn(new PageImpl<>(List.of(), PRIMEIRA_PAGINA, 0));

            Page<TaskResponse> pagina = taskService.listar(USUARIO_ID, SEM_FILTRO, PRIMEIRA_PAGINA);

            assertThat(pagina).isEmpty();
            assertThat(pagina.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("Busca por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve buscar tarefa por ID com sucesso")
        void deveBuscarPorId() {
            when(taskRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.buscarPorId(USUARIO_ID, 1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não existir")
        void deveLancarExcecaoQuandoNaoExistir() {
            when(taskRepository.findByIdAndUsuarioId(99L, USUARIO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.buscarPorId(USUARIO_ID, 99L))
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
            when(taskRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(task));
            when(taskRepository.saveAndFlush(any(Task.class))).thenReturn(task);

            TaskRequest updateRequest = TaskRequest.builder()
                    .titulo("Estudar Spring Boot")
                    .descricao("Aprofundar em JPA e Flyway")
                    .concluida(true)
                    .build();

            TaskResponse response = taskService.atualizar(USUARIO_ID, 1L, updateRequest);

            assertThat(response.getTitulo()).isEqualTo("Estudar Spring Boot");
            assertThat(response.getDescricao()).isEqualTo("Aprofundar em JPA e Flyway");
            assertThat(response.getConcluida()).isTrue();
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar tarefa inexistente")
        void deveLancarExcecaoAoAtualizarInexistente() {
            when(taskRepository.findByIdAndUsuarioId(99L, USUARIO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.atualizar(USUARIO_ID, 99L, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Deve manter concluida atual quando não fornecida na atualização")
        void deveManterConcluidaQuandoNaoFornecida() {
            task.setConcluida(true);
            request.setConcluida(null);

            when(taskRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(task));
            when(taskRepository.saveAndFlush(any(Task.class))).thenReturn(task);

            TaskResponse response = taskService.atualizar(USUARIO_ID, 1L, request);

            assertThat(response.getConcluida()).isTrue();
        }
    }

    @Nested
    @DisplayName("Exclusão de tarefa")
    class Deletar {

        @Test
        @DisplayName("Deve deletar tarefa com sucesso")
        void deveDeletarTarefa() {
            when(taskRepository.findByIdAndUsuarioId(1L, USUARIO_ID)).thenReturn(Optional.of(task));

            taskService.deletar(USUARIO_ID, 1L);

            verify(taskRepository).delete(task);
        }

        @Test
        @DisplayName("Deve lançar exceção ao deletar tarefa inexistente")
        void deveLancarExcecaoAoDeletarInexistente() {
            when(taskRepository.findByIdAndUsuarioId(99L, USUARIO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.deletar(USUARIO_ID, 99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }
}
