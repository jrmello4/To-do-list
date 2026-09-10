package com.todolist.controller;

import com.todolist.dto.TransacaoRequest;
import com.todolist.dto.TransacaoResponse;
import com.todolist.entity.StatusTransacao;
import com.todolist.entity.TipoTransacao;
import com.todolist.service.TransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/financas/transacoes")
@RequiredArgsConstructor
@Tag(name = "Finanças - Transações", description = "Lançamentos financeiros, receitas, despesas e parcelamentos")
public class TransacaoController {

    private final TransacaoService transacaoService;

    @GetMapping
    @Operation(summary = "Listar transações com filtros por período, status, tipo e conta")
    public ResponseEntity<List<TransacaoResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            @RequestParam(required = false) StatusTransacao status,
            @RequestParam(required = false) TipoTransacao tipo,
            @RequestParam(required = false) Long contaId) {
        return ResponseEntity.ok(transacaoService.listarTodas(inicio, fim, status, tipo, contaId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar transação por ID")
    public ResponseEntity<TransacaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(transacaoService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova transação (suporta parcelamento mensal)")
    public ResponseEntity<TransacaoResponse> criar(@Valid @RequestBody TransacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.criar(request));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status de liquidação da transação (Pendente <-> Pago)")
    public ResponseEntity<TransacaoResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestParam StatusTransacao status) {
        return ResponseEntity.ok(transacaoService.atualizarStatus(id, status));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir transação financeira")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        transacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transferencias")
    @Operation(summary = "Transferir valor entre duas contas (liquidação imediata)")
    public ResponseEntity<java.util.Map<String, Object>> transferir(
            @Valid @RequestBody com.todolist.dto.TransferenciaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoService.transferir(request));
    }

    @GetMapping("/orcamento")
    @Operation(summary = "Orçamento do mês por categoria (limite vs gasto)")
    public ResponseEntity<List<com.todolist.dto.OrcamentoItemResponse>> orcamento(
            @RequestParam(required = false) Integer ano,
            @RequestParam(required = false) Integer mes) {
        return ResponseEntity.ok(transacaoService.listarOrcamento(ano, mes));
    }

    @GetMapping("/projecao")
    @Operation(summary = "Projeção de saldo para os próximos N dias (default 90)")
    public ResponseEntity<com.todolist.dto.ProjecaoResponse> projecao(
            @RequestParam(required = false, defaultValue = "90") Integer dias) {
        return ResponseEntity.ok(transacaoService.projetarSaldo(dias));
    }
}