package com.todolist.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResumoResponse {

    private long tarefasHoje;
    private long tarefasAtrasadas;
    private long tarefasPendentes;
    private long tarefasConcluidas;
    private List<TaskResponse> tarefasHojeLista;
    private List<TaskResponse> tarefasUrgentesLista;

    private BigDecimal saldoTotalContas;
    private BigDecimal totalReceitasMes;
    private BigDecimal totalDespesasMes;
    private BigDecimal saldoPrevistoMes;
    private long contasPagarHoje;
    private long contasPagarAtrasadas;
    private List<TransacaoResponse> contasProximasVencimento;
    private List<ContaResponse> contas;
}