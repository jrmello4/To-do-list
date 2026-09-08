package com.todolist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.SubtaskRequest;
import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.dto.TaskSummaryResponse;
import com.todolist.entity.Categoria;
import com.todolist.entity.Prioridade;
import com.todolist.entity.StatusTarefa;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import com.todolist.config.SecurityConfig;
import com.todolist.security.JwtAuthenticationFilter;

@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, JwtAuthenticationFilter.class}
        ),
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                SecurityFilterAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private com.todolist.service.PdfReportService pdfReportService;

    private TaskResponse taskResponse;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        taskResponse = TaskResponse.builder()
                .id(1L)
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot")
                .concluida(false)
                .prioridade(Prioridade.ALTA)
                .status(StatusTarefa.A_FAZER)
                .categoria(Categoria.ESTUDOS)
                .dataVencimento(LocalDate.now().plusDays(2))
                .estaAtrasada(false)
                .deletada(false)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        taskRequest = TaskRequest.builder()
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot")
                .concluida(false)
                .prioridade(Prioridade.ALTA)
                .status(StatusTarefa.A_FAZER)
                .categoria(Categoria.ESTUDOS)
                .dataVencimento(LocalDate.now().plusDays(2))
                .build();
    }

    @Test
    @DisplayName("POST /api/tarefas - Deve criar tarefa com sucesso")
    void deveCriarTarefa() throws Exception {
        when(taskService.criar(any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Estudar Java"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.status").value("A_FAZER"));
    }

    @Test
    @DisplayName("GET /api/tarefas - Deve listar todas as tarefas")
    void deveListarTarefas() throws Exception {
        when(taskService.listarTodas()).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Estudar Java"))
                .andExpect(jsonPath("$[0].concluida").value(false));
    }

    @Test
    @DisplayName("GET /api/tarefas/resumo - Deve retornar resumo de tarefas")
    void deveRetornarResumo() throws Exception {
        TaskSummaryResponse resumo = TaskSummaryResponse.builder()
                .total(10L)
                .concluidas(6L)
                .pendentes(4L)
                .aFazer(3L)
                .emAndamento(1L)
                .atrasadas(2L)
                .naLixeira(1L)
                .build();

        when(taskService.obterResumo()).thenReturn(resumo);

        mockMvc.perform(get("/api/tarefas/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(10))
                .andExpect(jsonPath("$.concluidas").value(6))
                .andExpect(jsonPath("$.atrasadas").value(2));
    }

    @Test
    @DisplayName("GET /api/tarefas/export/csv - Deve retornar arquivo CSV")
    void deveExportarCsv() throws Exception {
        when(taskService.exportarCsv()).thenReturn("ID;Título\n1;Estudar");

        mockMvc.perform(get("/api/tarefas/export/csv"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "form-data; name=\"attachment\"; filename=\"tarefas.csv\""));
    }

    @Test
    @DisplayName("GET /api/tarefas/lixeira - Deve listar tarefas na lixeira")
    void deveListarLixeira() throws Exception {
        taskResponse.setDeletada(true);
        when(taskService.listarLixeira()).thenReturn(List.of(taskResponse));

        mockMvc.perform(get("/api/tarefas/lixeira"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].deletada").value(true));
    }

    @Test
    @DisplayName("PATCH /api/tarefas/1/restaurar - Deve restaurar tarefa da lixeira")
    void deveRestaurarTarefa() throws Exception {
        taskResponse.setDeletada(false);
        when(taskService.restaurarDaLixeira(1L)).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/tarefas/1/restaurar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deletada").value(false));
    }

    @Test
    @DisplayName("PATCH /api/tarefas/1/status-kanban - Deve atualizar status kanban")
    void deveAtualizarStatusKanban() throws Exception {
        taskResponse.setStatus(StatusTarefa.EM_ANDAMENTO);
        when(taskService.atualizarStatusKanban(1L, StatusTarefa.EM_ANDAMENTO)).thenReturn(taskResponse);

        mockMvc.perform(patch("/api/tarefas/1/status-kanban").param("status", "EM_ANDAMENTO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"));
    }

    @Test
    @DisplayName("POST /api/tarefas/1/subtarefas - Deve adicionar subtarefa")
    void deveAdicionarSubtarefa() throws Exception {
        SubtaskRequest subRequest = SubtaskRequest.builder().titulo("Capítulo 1").concluida(false).build();
        when(taskService.adicionarSubtarefa(eq(1L), any(SubtaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tarefas/1/subtarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(subRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/tarefas/1 - Deve mover tarefa para a lixeira")
    void deveMoverParaLixeira() throws Exception {
        doNothing().when(taskService).moverParaLixeira(1L);

        mockMvc.perform(delete("/api/tarefas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tarefas/1/definitivo - Deve excluir definitivamente")
    void deveExcluirDefinitivamente() throws Exception {
        doNothing().when(taskService).excluirDefinitivamente(1L);

        mockMvc.perform(delete("/api/tarefas/1/definitivo"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tarefas/lixeira/esvaziar - Deve esvaziar lixeira")
    void deveEsvaziarLixeira() throws Exception {
        doNothing().when(taskService).esvaziarLixeira();

        mockMvc.perform(delete("/api/tarefas/lixeira/esvaziar"))
                .andExpect(status().isNoContent());
    }
}
