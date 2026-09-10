package com.todolist.service;

import com.todolist.dto.NotaRapidaRequest;
import com.todolist.dto.NotaRapidaResponse;
import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.NotaRapida;
import com.todolist.entity.Role;
import com.todolist.entity.User;
import com.todolist.repository.NotaRapidaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotaRapidaServiceTest {

    @Mock
    private NotaRapidaRepository notaRapidaRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private NotaRapidaService notaRapidaService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .id(1L)
                .nome("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);
    }

    @Test
    @DisplayName("Deve salvar ou atualizar nota rápida")
    void deveSalvarNotaRapida() {
        NotaRapidaRequest request = NotaRapidaRequest.builder()
                .titulo("Ideia de App")
                .conteudo("Criar um aplicativo unificado...")
                .build();

        when(notaRapidaRepository.save(any(NotaRapida.class))).thenAnswer(inv -> {
            NotaRapida n = inv.getArgument(0);
            n.setId(5L);
            n.setDataCriacao(LocalDateTime.now());
            n.setDataAtualizacao(LocalDateTime.now());
            return n;
        });

        NotaRapidaResponse response = notaRapidaService.salvarOuAtualizar(null, request);

        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitulo()).isEqualTo("Ideia de App");
    }

    @Test
    @DisplayName("Deve converter nota rápida em tarefa com sucesso")
    void deveConverterNotaEmTarefa() {
        NotaRapida nota = NotaRapida.builder()
                .id(1L)
                .titulo("Comprar Livro")
                .conteudo("Comprar o livro de Spring Microservices")
                .usuario(usuario)
                .dataCriacao(LocalDateTime.now())
                .dataAtualizacao(LocalDateTime.now())
                .build();

        when(notaRapidaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(nota));
        when(taskService.criar(any(TaskRequest.class))).thenAnswer(inv -> {
            TaskRequest req = inv.getArgument(0);
            return TaskResponse.builder().id(100L).titulo(req.getTitulo()).descricao(req.getDescricao()).build();
        });

        TaskResponse taskResp = notaRapidaService.converterEmTarefa(1L);

        assertThat(taskResp.getId()).isEqualTo(100L);
        assertThat(taskResp.getTitulo()).isEqualTo("Comprar Livro");
        verify(taskService).criar(any(TaskRequest.class));
    }
}