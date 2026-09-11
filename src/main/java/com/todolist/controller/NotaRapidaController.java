package com.todolist.controller;

import com.todolist.dto.NotaRapidaRequest;
import com.todolist.dto.NotaRapidaResponse;
import com.todolist.dto.TaskResponse;
import com.todolist.service.NotaRapidaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
@RequiredArgsConstructor
@Tag(name = "Notas Rápidas", description = "Bloco de notas instantâneo (scratchpad) e conversão para tarefas")
public class NotaRapidaController {

    private final NotaRapidaService notaService;

    @GetMapping
    @Operation(summary = "Listar todas as notas rápidas do usuário")
    public ResponseEntity<List<NotaRapidaResponse>> listar() {
        return ResponseEntity.ok(notaService.listarTodas());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar nota por ID")
    public ResponseEntity<NotaRapidaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(notaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar nova nota rápida")
    public ResponseEntity<NotaRapidaResponse> criar(@Valid @RequestBody NotaRapidaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notaService.salvarOuAtualizar(null, request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar nota rápida existente (usado para auto-save)")
    public ResponseEntity<NotaRapidaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody NotaRapidaRequest request) {
        return ResponseEntity.ok(notaService.salvarOuAtualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir nota rápida")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        notaService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/converter-em-tarefa")
    @Operation(summary = "Converter nota rápida diretamente em uma tarefa ativa")
    public ResponseEntity<TaskResponse> converterEmTarefa(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notaService.converterEmTarefa(id));
    }
}