package com.todolist.controller;

import com.todolist.dto.ContaRequest;
import com.todolist.dto.ContaResponse;
import com.todolist.service.ContaFinanceiraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financas/contas")
@RequiredArgsConstructor
@Tag(name = "Finanças - Contas", description = "Gerenciamento de contas e carteiras financeiras")
public class ContaFinanceiraController {

    private final ContaFinanceiraService contaService;

    @GetMapping
    @Operation(summary = "Listar contas do usuário")
    public ResponseEntity<List<ContaResponse>> listar() {
        return ResponseEntity.ok(contaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova conta financeira")
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contaService.criar(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar conta financeira")
    public ResponseEntity<ContaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody ContaRequest request) {
        return ResponseEntity.ok(contaService.atualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar conta financeira")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        contaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}