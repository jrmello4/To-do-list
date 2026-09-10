package com.todolist.service;

import com.todolist.dto.EventoCalendarioRequest;
import com.todolist.dto.ItemCalendarioResponse;
import com.todolist.entity.*;
import com.todolist.repository.EventoCalendarioRepository;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalendarioServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private EventoCalendarioRepository eventoRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CalendarioService calendarioService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .id(1L)
                .nome("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("Deve consolidar tarefas, transacoes e eventos no mes")
    void deveConsolidarItensNoMes() {
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);

        LocalDate data = LocalDate.of(2026, 9, 15);

        Task tarefa = Task.builder()
                .id(10L)
                .titulo("Entregar Relatorio")
                .dataVencimento(data)
                .concluida(false)
                .prioridade(Prioridade.ALTA)
                .categoria(Categoria.TRABALHO)
                .build();

        Transacao transacao = Transacao.builder()
                .id(20L)
                .descricao("Conta de Energia")
                .valor(new BigDecimal("180.50"))
                .tipo(TipoTransacao.DESPESA)
                .status(StatusTransacao.PENDENTE)
                .dataVencimento(data)
                .build();

        EventoCalendario evento = EventoCalendario.builder()
                .id(30L)
                .titulo("Consulta Médica")
                .dataEvento(data)
                .horaInicio(LocalTime.of(14, 30))
                .cor("#8b5cf6")
                .categoria("Saúde")
                .build();

        when(taskRepository.findByUsuarioIdAndDeletadaFalseAndDataVencimentoBetween(eq(1L), any(), any()))
                .thenReturn(List.of(tarefa));
        when(transacaoRepository.findByUsuarioIdAndDataVencimentoBetweenOrderByDataVencimentoAsc(eq(1L), any(), any()))
                .thenReturn(List.of(transacao));
        when(eventoRepository.findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(eq(usuario), any(), any()))
                .thenReturn(List.of(evento));

        List<ItemCalendarioResponse> itens = calendarioService.obterItensDoMes(2026, 9);

        assertThat(itens).hasSize(3);
        assertThat(itens).extracting("tipo")
                .containsExactlyInAnyOrder("TAREFA", "DESPESA", "EVENTO");
    }

    @Test
    @DisplayName("Deve criar evento no calendario com sucesso")
    void deveCriarEventoComSucesso() {
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);

        EventoCalendarioRequest request = EventoCalendarioRequest.builder()
                .titulo("Almoço com Equipe")
                .dataEvento(LocalDate.of(2026, 9, 20))
                .horaInicio(LocalTime.of(12, 0))
                .categoria("Trabalho")
                .cor("#6366f1")
                .build();

        when(eventoRepository.save(any(EventoCalendario.class))).thenAnswer(inv -> {
            EventoCalendario e = inv.getArgument(0);
            e.setId(50L);
            return e;
        });

        ItemCalendarioResponse response = calendarioService.criarEvento(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("EVENTO-50");
        assertThat(response.getTitulo()).isEqualTo("Almoço com Equipe");
        assertThat(response.getTipo()).isEqualTo("EVENTO");
    }

    @Test
    @DisplayName("Deve excluir evento logicamente")
    void deveExcluirEventoLogicamente() {
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);

        EventoCalendario evento = EventoCalendario.builder()
                .id(50L)
                .titulo("Evento")
                .ativo(true)
                .usuario(usuario)
                .build();

        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));

        calendarioService.excluirEvento(50L);

        assertThat(evento.getAtivo()).isFalse();
        verify(eventoRepository).save(evento);
    }
}
