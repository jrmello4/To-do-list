package com.todolist.service;

import com.todolist.dto.ContaResponse;
import com.todolist.dto.DashboardResumoResponse;
import com.todolist.dto.TaskResponse;
import com.todolist.dto.TransacaoResponse;
import com.todolist.entity.*;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final TransacaoRepository transacaoRepository;
    private final TransacaoService transacaoService;
    private final ContaFinanceiraService contaService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public DashboardResumoResponse obterResumo() {
        User user = authService.obterUsuarioAutenticado();
        LocalDate hoje = LocalDate.now();

        // 1. Métricas e Listas de Tarefas
        List<Task> todasTarefas = taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(user.getId());

        List<TaskResponse> tarefasHojeLista = new ArrayList<>();
        List<TaskResponse> tarefasUrgentesLista = new ArrayList<>();
        long tarefasPendentes = 0;
        long tarefasConcluidas = 0;
        long tarefasAtrasadas = 0;

        for (Task t : todasTarefas) {
            boolean concluida = Boolean.TRUE.equals(t.getConcluida());
            if (concluida) {
                tarefasConcluidas++;
                continue;
            }
            tarefasPendentes++;

            boolean ehUrgente = t.getPrioridade() == Prioridade.ALTA || t.getPrioridade() == Prioridade.URGENTE;
            boolean venceHoje = t.getDataVencimento() != null && t.getDataVencimento().isEqual(hoje);
            boolean atrasada = t.getDataVencimento() != null && t.getDataVencimento().isBefore(hoje);

            if (ehUrgente || venceHoje || atrasada) {
                TaskResponse resp = taskService.toResponse(t);
                if (venceHoje || atrasada) {
                    tarefasHojeLista.add(resp);
                }
                if (atrasada) {
                    tarefasAtrasadas++;
                }
                if (ehUrgente) {
                    tarefasUrgentesLista.add(resp);
                }
            }
        }

        // 2. Métricas Financeiras
        List<ContaResponse> contas = contaService.listarTodas();
        BigDecimal saldoTotalContas = contas.stream()
                .map(ContaResponse::getSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDate inicioMes = hoje.withDayOfMonth(1);
        LocalDate fimMes = hoje.withDayOfMonth(hoje.lengthOfMonth());

        BigDecimal totalReceitasMes = transacaoRepository.sumValorByUsuarioIdAndTipoAndDataVencimentoBetween(
                user.getId(), TipoTransacao.RECEITA, inicioMes, fimMes);
        BigDecimal totalDespesasMes = transacaoRepository.sumValorByUsuarioIdAndTipoAndDataVencimentoBetween(
                user.getId(), TipoTransacao.DESPESA, inicioMes, fimMes);

        BigDecimal receitasPendentes = transacaoRepository.sumValorByUsuarioIdAndTipoAndStatusAndDataVencimentoBetween(
                user.getId(), TipoTransacao.RECEITA, StatusTransacao.PENDENTE, inicioMes, fimMes);
        BigDecimal despesasPendentes = transacaoRepository.sumValorByUsuarioIdAndTipoAndStatusAndDataVencimentoBetween(
                user.getId(), TipoTransacao.DESPESA, StatusTransacao.PENDENTE, inicioMes, fimMes);

        BigDecimal saldoPrevistoMes = saldoTotalContas.add(receitasPendentes).subtract(despesasPendentes);

        // Contas a pagar/receber nos próximos 7 dias
        List<Transacao> proximas = transacaoRepository.findByUsuarioIdAndStatusAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                user.getId(), StatusTransacao.PENDENTE, hoje, hoje.plusDays(7));
        List<TransacaoResponse> proximasResp = proximas.stream()
                .map(transacaoService::paraResponse)
                .collect(Collectors.toList());

        // Contas atrasadas
        List<Transacao> atrasadas = transacaoRepository.findByUsuarioIdAndDataVencimentoBeforeAndStatusOrderByDataVencimentoAsc(
                user.getId(), hoje, StatusTransacao.PENDENTE);
        List<TransacaoResponse> atrasadasResp = atrasadas.stream()
                .map(transacaoService::paraResponse)
                .collect(Collectors.toList());

        long contasPagarHoje = proximas.stream()
                .filter(t -> t.getTipo() == TipoTransacao.DESPESA && t.getDataVencimento().isEqual(hoje))
                .count();

        long contasPagarAtrasadas = atrasadas.stream()
                .filter(t -> t.getTipo() == TipoTransacao.DESPESA)
                .count();

        // Une na lista de atenção as atrasadas + as próximas 7 dias
        List<TransacaoResponse> contasAtencao = new ArrayList<>(atrasadasResp);
        contasAtencao.addAll(proximasResp);

        return DashboardResumoResponse.builder()
                .tarefasHoje(tarefasHojeLista.size())
                .tarefasAtrasadas(tarefasAtrasadas)
                .tarefasPendentes(tarefasPendentes)
                .tarefasConcluidas(tarefasConcluidas)
                .tarefasHojeLista(tarefasHojeLista)
                .tarefasUrgentesLista(tarefasUrgentesLista)
                .saldoTotalContas(saldoTotalContas)
                .totalReceitasMes(totalReceitasMes)
                .totalDespesasMes(totalDespesasMes)
                .saldoPrevistoMes(saldoPrevistoMes)
                .contasPagarHoje(contasPagarHoje)
                .contasPagarAtrasadas(contasPagarAtrasadas)
                .contasProximasVencimento(contasAtencao)
                .contas(contas)
                .build();
    }
}