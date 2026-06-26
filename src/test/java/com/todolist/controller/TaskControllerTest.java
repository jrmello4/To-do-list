package com.todolist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    private TaskResponse taskResponse;
    private TaskRequest taskRequest;

    @BeforeEach
    void setUp() {
        taskResponse = TaskResponse.builder()
                .id(1L)
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot")
                .concluida(false)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        taskRequest = TaskRequest.builder()
                .titulo("Estudar Java")
                .descricao("Estudar Spring Boot")
                .concluida(false)
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
                .andExpect(jsonPath("$.concluida").value(false));
    }

    @Test
    @DisplayName("POST /api/tarefas - Deve retornar 400 quando título estiver vazio")
    void deveRetornar400QuandoTituloVazio() throws Exception {
        taskRequest.setTitulo("");

        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
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
    @DisplayName("GET /api/tarefas/1 - Deve buscar tarefa por ID")
    void deveBuscarTarefaPorId() throws Exception {
        when(taskService.buscarPorId(1L)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/tarefas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Estudar Java"));
    }

    @Test
    @DisplayName("GET /api/tarefas/99 - Deve retornar 404 quando tarefa não existir")
    void deveRetornar404QuandoNaoExistir() throws Exception {
        when(taskService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Tarefa", 99L));

        mockMvc.perform(get("/api/tarefas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value(org.hamcrest.Matchers.containsString("99")));
    }

    @Test
    @DisplayName("PUT /api/tarefas/1 - Deve atualizar tarefa com sucesso")
    void deveAtualizarTarefa() throws Exception {
        TaskResponse updated = TaskResponse.builder()
                .id(1L)
                .titulo("Estudar Spring Boot")
                .descricao("Aprofundar")
                .concluida(true)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        when(taskService.atualizar(eq(1L), any(TaskRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tarefas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estudar Spring Boot"))
                .andExpect(jsonPath("$.concluida").value(true));
    }

    @Test
    @DisplayName("DELETE /api/tarefas/1 - Deve deletar tarefa com sucesso")
    void deveDeletarTarefa() throws Exception {
        doNothing().when(taskService).deletar(1L);

        mockMvc.perform(delete("/api/tarefas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tarefas/99 - Deve retornar 404 ao deletar inexistente")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        doThrow(new ResourceNotFoundException("Tarefa", 99L))
                .when(taskService).deletar(99L);

        mockMvc.perform(delete("/api/tarefas/99"))
                .andExpect(status().isNotFound());
    }
}
