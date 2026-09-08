package com.todolist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.Usuario;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.UsuarioRepository;
import com.todolist.security.JwtService;
import com.todolist.security.RespostaDeErroDeSeguranca;
import com.todolist.security.SecurityConfig;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
// Sem este import a fatia usaria a segurança padrão do Boot, e não as regras
// deste projeto — os testes de 401 estariam validando outra coisa.
@Import({SecurityConfig.class, RespostaDeErroDeSeguranca.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    // A fatia web carrega a cadeia de segurança real (JwtAuthenticationFilter
    // é um Filter, e @WebMvcTest inclui filtros). Estes mocks satisfazem as
    // dependências do filtro; a autenticação em si vem do post-processor user().
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    private static final Long USUARIO_ID = 7L;

    /** As rotas passaram a exigir autenticação; este é o dono das requisições. */
    private static final UsuarioAutenticado AUTENTICADO = new UsuarioAutenticado(
            Usuario.builder()
                    .id(USUARIO_ID)
                    .nome("Ana Ribeiro")
                    .email("ana@exemplo.com")
                    .senhaHash("$2a$10$hashfalso")
                    .ativo(true)
                    .build());

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
        when(taskService.criar(eq(USUARIO_ID), any(TaskRequest.class))).thenReturn(taskResponse);

        mockMvc.perform(post("/api/tarefas")
                        .with(user(AUTENTICADO))
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
                        .with(user(AUTENTICADO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/tarefas - Deve listar todas as tarefas")
    void deveListarTarefas() throws Exception {
        when(taskService.listar(eq(USUARIO_ID), any(), any()))
                .thenReturn(new PageImpl<>(List.of(taskResponse)));

        mockMvc.perform(get("/api/tarefas")
                        .with(user(AUTENTICADO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].titulo").value("Estudar Java"))
                .andExpect(jsonPath("$.content[0].concluida").value(false))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/tarefas/1 - Deve buscar tarefa por ID")
    void deveBuscarTarefaPorId() throws Exception {
        when(taskService.buscarPorId(USUARIO_ID, 1L)).thenReturn(taskResponse);

        mockMvc.perform(get("/api/tarefas/1")
                        .with(user(AUTENTICADO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Estudar Java"));
    }

    @Test
    @DisplayName("GET /api/tarefas/99 - Deve retornar 404 quando tarefa não existir")
    void deveRetornar404QuandoNaoExistir() throws Exception {
        when(taskService.buscarPorId(USUARIO_ID, 99L))
                .thenThrow(new ResourceNotFoundException("Tarefa", 99L));

        mockMvc.perform(get("/api/tarefas/99")
                        .with(user(AUTENTICADO)))
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

        when(taskService.atualizar(eq(USUARIO_ID), eq(1L), any(TaskRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/tarefas/1")
                        .with(user(AUTENTICADO))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Estudar Spring Boot"))
                .andExpect(jsonPath("$.concluida").value(true));
    }

    @Test
    @DisplayName("DELETE /api/tarefas/1 - Deve deletar tarefa com sucesso")
    void deveDeletarTarefa() throws Exception {
        doNothing().when(taskService).deletar(USUARIO_ID, 1L);

        mockMvc.perform(delete("/api/tarefas/1")
                        .with(user(AUTENTICADO)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/tarefas/99 - Deve retornar 404 ao deletar inexistente")
    void deveRetornar404AoDeletarInexistente() throws Exception {
        doThrow(new ResourceNotFoundException("Tarefa", 99L))
                .when(taskService).deletar(USUARIO_ID, 99L);

        mockMvc.perform(delete("/api/tarefas/99")
                        .with(user(AUTENTICADO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tarefas - Deve retornar 401 sem autenticação")
    void deveRetornar401SemAutenticacao() throws Exception {
        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value(
                        org.hamcrest.Matchers.containsString("Authorization")));

        verifyNoInteractions(taskService);
    }

    @Test
    @DisplayName("POST /api/tarefas - Deve retornar 401 sem autenticação")
    void devePostarSemAutenticacaoERetornar401() throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskRequest)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(taskService);
    }
}
