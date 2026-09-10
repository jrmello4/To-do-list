package com.todolist.controller;

import com.todolist.dto.MetaAporteRequest;
import com.todolist.dto.MetaRequest;
import com.todolist.dto.MetaResponse;
import com.todolist.service.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
@Tag(name = "Metas & Objetivos", description = "Endpoints para gerenciamento de metas e aportes de médio/longo prazo")
public class MetaController {

    private final MetaService metaService;

    @GetMapping
    @Operation(summary = "Listar todas as metas ativas do usuário")
    public ResponseEntity<List<MetaResponse>> listar() {
        return ResponseEntity.ok(metaService.listarMetas());
    }

    @PostMapping
    @Operation(summary = "Criar uma nova meta")
    public ResponseEntity<MetaResponse> criar(@Valid @RequestBody MetaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criarMeta(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de uma meta")
    public ResponseEntity<MetaResponse> atualizar(@PathVariable Long id, @Valid @RequestBody MetaRequest request) {
        return ResponseEntity.ok(metaService.atualizarMeta(id, request));
    }

    @PostMapping("/{id}/aporte")
    @Operation(summary = "Registrar um aporte ou avanço no progresso da meta")
    public ResponseEntity<MetaResponse> registrarAporte(@PathVariable Long id, @Valid @RequestBody MetaAporteRequest request) {
        return ResponseEntity.ok(metaService.registrarAporte(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma meta logicamente")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        metaService.excluirMeta(id);
        return ResponseEntity.noContent().build();
    }
}
