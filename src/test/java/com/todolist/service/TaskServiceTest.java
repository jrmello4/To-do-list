package com.todolist.service;

import com.todolist.dto.SubtaskRequest;
import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.dto.TaskSummaryResponse;
import com.todolist.entity.*;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.TagRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AuthService authService;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private TaskService taskService;

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    private User user;
    private Task task;
    private TaskRequest request;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .nome("Adenilson")
                .email("dev@teste.com")
                .role(Role.ROLE_USER)
                .build();

        lenient().when(authService.obterUsuarioAutenticado()).thenReturn(user);

        task = Task.builder()
                .id(1L)
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot e JPA")
                .concluida(false)
                .prioridade(Prioridade.ALTA)
                .status(StatusTarefa.A_FAZER)
                .categoria(Categoria.ESTUDOS)
                .dataVencimento(LocalDate.now().plusDays(3))
                .deletada(false)
                .usuario(user)
                .subtarefas(new ArrayList<>())
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        request = TaskRequest.builder()
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot e JPA")
                .concluida(false)
                .prioridade(Prioridade.ALTA)
                .status(StatusTarefa.A_FAZER)
                .categoria(Categoria.ESTUDOS)
                .dataVencimento(LocalDate.now().plusDays(3))
                .build();
    }

    @Nested
    @DisplayName("Criação de tarefa")
    class Criar {

        @Test
        @DisplayName("Deve criar tarefa vinculada ao usuário com sucesso")
        void deveCriarTarefaComSucesso() {
            when(taskRepository.save(any(Task.class))).thenReturn(task);

            TaskResponse response = taskService.criar(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitulo()).isEqualTo("Estudar Java");
            assertThat(response.getPrioridade()).isEqualTo(Prioridade.ALTA);

            verify(taskRepository).save(taskCaptor.capture());
            Task saved = taskCaptor.getValue();
            assertThat(saved.getUsuario()).isEqualTo(user);
        }

        @Test
        @DisplayName("Deve criar tarefa com subtarefas")
        void deveCriarTarefaComSubtarefas() {
            request.setSubtarefas(List.of(
                    SubtaskRequest.builder().titulo("Ler documentação").concluida(false).build()
            ));

            when(taskRepository.save(any(Task.class))).thenAnswer(i -> {
                Task t = i.getArgument(0);
                t.setId(2L);
                return t;
            });

            TaskResponse response = taskService.criar(request);

            assertThat(response).isNotNull();
            verify(taskRepository).save(taskCaptor.capture());
            Task saved = taskCaptor.getValue();
            assertThat(saved.getSubtarefas()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Listagem de tarefas")
    class Listar {

        @Test
        @DisplayName("Deve listar todas as tarefas ativas do usuário")
        void deveListarTodas() {
            when(taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(1L)).thenReturn(List.of(task));

            List<TaskResponse> responses = taskService.listarTodas();

            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve listar com filtros avançados")
        void deveListarComFiltrosAvancados() {
            when(taskRepository.findByUsuarioFiltrosAvancados(1L, false, StatusTarefa.A_FAZER, Prioridade.ALTA, Categoria.ESTUDOS, "Java"))
                    .thenReturn(List.of(task));

            List<TaskResponse> responses = taskService.listarComFiltros(false, StatusTarefa.A_FAZER, Prioridade.ALTA, Categoria.ESTUDOS, "Java");

            assertThat(responses).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Busca por ID")
    class BuscarPorId {

        @Test
        @DisplayName("Deve buscar tarefa por ID do usuário")
        void deveBuscarPorId() {
            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(1L, 1L)).thenReturn(Optional.of(task));

            TaskResponse response = taskService.buscarPorId(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getTitulo()).isEqualTo("Estudar Java");
        }

        @Test
        @DisplayName("Deve lançar exceção quando tarefa não existir ou pertencer a outro")
        void deveLancarExcecaoQuandoNaoExistir() {
            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(99L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> taskService.buscarPorId(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Atualização e Status Kanban")
    class Atualizacoes {

        @Test
        @DisplayName("Deve atualizar status kanban da tarefa do usuário")
        void deveAtualizarStatusKanban() {
            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(1L, 1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            TaskResponse response = taskService.atualizarStatusKanban(1L, StatusTarefa.CONCLUIDA);

            assertThat(response.getStatus()).isEqualTo(StatusTarefa.CONCLUIDA);
            assertThat(response.getConcluida()).isTrue();
        }

        @Test
        @DisplayName("Deve alternar status de conclusão")
        void deveAlternarStatus() {
            task.setConcluida(false);
            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(1L, 1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            TaskResponse response = taskService.alternarStatus(1L);

            assertThat(response.getConcluida()).isTrue();
        }
    }

    @Nested
    @DisplayName("Lixeira e Soft Delete")
    class Lixeira {

        @Test
        @DisplayName("Deve mover tarefa para a lixeira")
        void deveMoverParaLixeira() {
            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(1L, 1L)).thenReturn(Optional.of(task));

            taskService.moverParaLixeira(1L);

            verify(taskRepository).save(taskCaptor.capture());
            Task deleted = taskCaptor.getValue();
            assertThat(deleted.getDeletada()).isTrue();
        }

        @Test
        @DisplayName("Deve restaurar tarefa da lixeira")
        void deveRestaurarDaLixeira() {
            task.setDeletada(true);
            task.setDataDelecao(LocalDateTime.now());

            when(taskRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            TaskResponse response = taskService.restaurarDaLixeira(1L);

            assertThat(response.getDeletada()).isFalse();
        }

        @Test
        @DisplayName("Deve excluir definitivamente")
        void deveExcluirDefinitivamente() {
            when(taskRepository.existsByIdAndUsuarioId(1L, 1L)).thenReturn(true);

            taskService.excluirDefinitivamente(1L);

            verify(taskRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Deve esvaziar lixeira apenas do usuário atual")
        void deveEsvaziarLixeira() {
            taskService.esvaziarLixeira();
            verify(taskRepository).esvaziarLixeiraDoUsuario(1L);
        }
    }

    @Nested
    @DisplayName("Gestão de Subtarefas")
    class Subtarefas {

        @Test
        @DisplayName("Deve adicionar subtarefa")
        void deveAdicionarSubtarefa() {
            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(1L, 1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            SubtaskRequest subRequest = SubtaskRequest.builder().titulo("Configurar Maven").concluida(false).build();
            TaskResponse response = taskService.adicionarSubtarefa(1L, subRequest);

            assertThat(response.getTotalSubtarefas()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Resumo e Exportação")
    class ResumoEExportacao {

        @Test
        @DisplayName("Deve calcular resumo do usuário")
        void deveCalcularResumoCompleto() {
            when(taskRepository.countByUsuarioIdAndDeletadaFalse(1L)).thenReturn(10L);
            when(taskRepository.countByUsuarioIdAndDeletadaFalseAndConcluida(1L, true)).thenReturn(6L);
            when(taskRepository.countByUsuarioIdAndDeletadaFalseAndStatus(1L, StatusTarefa.A_FAZER)).thenReturn(3L);
            when(taskRepository.countByUsuarioIdAndDeletadaFalseAndStatus(1L, StatusTarefa.EM_ANDAMENTO)).thenReturn(1L);
            when(taskRepository.countByUsuarioIdAndDeletadaFalseAndConcluidaFalseAndDataVencimentoBefore(eq(1L), any(LocalDate.class))).thenReturn(2L);
            when(taskRepository.countByUsuarioIdAndDeletadaTrue(1L)).thenReturn(4L);

            TaskSummaryResponse resumo = taskService.obterResumo();

            assertThat(resumo.getTotal()).isEqualTo(10L);
            assertThat(resumo.getConcluidas()).isEqualTo(6L);
            assertThat(resumo.getPendentes()).isEqualTo(4L);
        }

        @Test
        @DisplayName("Deve gerar CSV do usuário")
        void deveExportarCsv() {
            when(taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(1L)).thenReturn(List.of(task));

            String csv = taskService.exportarCsv();

            assertThat(csv).contains("ID;Título;Descrição;Prioridade;Status;Categoria;Vencimento");
            assertThat(csv).contains("Recorrência;Pomodoros;Concluída");
            assertThat(csv).contains("Estudar Java");
        }
    }

    @Nested
    @DisplayName("Pomodoro, Recorrência e Estatísticas")
    class PomodoroERecorrencia {

        @Test
        @DisplayName("Deve incrementar pomodoros realizados da tarefa")
        void deveIncrementarPomodoro() {
            task.setPomodorosRealizados(1);
            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(1L, 1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            TaskResponse response = taskService.incrementarPomodoro(1L);

            assertThat(response.getPomodorosRealizados()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve gerar próxima tarefa automaticamente ao concluir tarefa recorrente")
        void deveGerarProximaRecorrencia() {
            LocalDate baseDate = LocalDate.of(2026, 9, 10);
            task.setRecorrencia(Recorrencia.SEMANAL);
            task.setDataVencimento(baseDate);
            task.setConcluida(false);

            when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(1L, 1L)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(i -> i.getArgument(0));

            taskService.atualizarStatus(1L, true);

            verify(taskRepository, times(2)).save(taskCaptor.capture());
            List<Task> savedTasks = taskCaptor.getAllValues();
            assertThat(savedTasks).hasSize(2);

            Task proxima = savedTasks.get(1);
            assertThat(proxima.getTitulo()).isEqualTo(task.getTitulo());
            assertThat(proxima.getConcluida()).isFalse();
            assertThat(proxima.getStatus()).isEqualTo(StatusTarefa.A_FAZER);
            assertThat(proxima.getDataVencimento()).isEqualTo(baseDate.plusWeeks(1));
            assertThat(proxima.getRecorrencia()).isEqualTo(Recorrencia.SEMANAL);
        }

        @Test
        @DisplayName("Deve calcular estatísticas do dashboard Chart.js")
        void deveCalcularEstatisticas() {
            when(taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(1L)).thenReturn(List.of(task));

            var stats = taskService.obterEstatisticas();

            assertThat(stats).isNotNull();
            assertThat(stats.getPorCategoria()).containsKey("ESTUDOS");
            assertThat(stats.getPorPrioridade()).containsKey("ALTA");
            assertThat(stats.getUltimos7DiasRotulos()).hasSize(7);
            assertThat(stats.getUltimos7DiasCriadas()).hasSize(7);
            assertThat(stats.getUltimos7DiasConcluidas()).hasSize(7);
        }
    }
}
