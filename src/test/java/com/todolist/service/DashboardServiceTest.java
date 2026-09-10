package com.todolist.service;

import com.todolist.dto.ContaResponse;
import com.todolist.dto.DashboardResumoResponse;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.*;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskService taskService;

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private TransacaoService transacaoService;

    @Mock
    private ContaFinanceiraService contaService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private DashboardService dashboardService;

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
    @DisplayName("Deve calcular resumo do dashboard agregando tarefas e finanças")
    void deveObterResumoConsolidadoCorretamente() {
        LocalDate hoje = LocalDate.now();

        // Tarefas
        Task tHoje = Task.builder().id(1L).titulo("Tarefa Hoje").dataVencimento(hoje).concluida(false).prioridade(Prioridade.ALTA).build();
        Task tAtrasada = Task.builder().id(2L).titulo("Tarefa Atrasada").dataVencimento(hoje.minusDays(2)).concluida(false).prioridade(Prioridade.MEDIA).build();
        Task tConcluida = Task.builder().id(3L).titulo("Concluída").concluida(true).build();

        when(taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(1L))
                .thenReturn(List.of(tHoje, tAtrasada, tConcluida));

        when(taskService.toResponse(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            return TaskResponse.builder().id(t.getId()).titulo(t.getTitulo()).build();
        });

        // Contas
        ContaResponse c1 = ContaResponse.builder().id(1L).nome("Nubank").saldoAtual(BigDecimal.valueOf(1500)).build();
        when(contaService.listarTodas()).thenReturn(List.of(c1));

        // Finanças
        when(transacaoRepository.sumValorByUsuarioIdAndTipoAndDataVencimentoBetween(eq(1L), eq(TipoTransacao.RECEITA), any(), any()))
                .thenReturn(BigDecimal.valueOf(5000));
        when(transacaoRepository.sumValorByUsuarioIdAndTipoAndDataVencimentoBetween(eq(1L), eq(TipoTransacao.DESPESA), any(), any()))
                .thenReturn(BigDecimal.valueOf(2000));
        when(transacaoRepository.sumValorByUsuarioIdAndTipoAndStatusAndDataVencimentoBetween(eq(1L), eq(TipoTransacao.RECEITA), eq(StatusTransacao.PENDENTE), any(), any()))
                .thenReturn(BigDecimal.valueOf(1000));
        when(transacaoRepository.sumValorByUsuarioIdAndTipoAndStatusAndDataVencimentoBetween(eq(1L), eq(TipoTransacao.DESPESA), eq(StatusTransacao.PENDENTE), any(), any()))
                .thenReturn(BigDecimal.valueOf(500));

        DashboardResumoResponse resumo = dashboardService.obterResumo();

        assertThat(resumo.getTarefasHoje()).isEqualTo(2); // hoje + atrasada
        assertThat(resumo.getTarefasAtrasadas()).isEqualTo(1);
        assertThat(resumo.getTarefasConcluidas()).isEqualTo(1);
        assertThat(resumo.getSaldoTotalContas()).isEqualByComparingTo("1500");
        assertThat(resumo.getTotalReceitasMes()).isEqualByComparingTo("5000");
        assertThat(resumo.getTotalDespesasMes()).isEqualByComparingTo("2000");
        // Saldo previsto: 1500 + 1000 (rec pend) - 500 (desp pend) = 2000
        assertThat(resumo.getSaldoPrevistoMes()).isEqualByComparingTo("2000");
    }
}