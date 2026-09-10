package com.todolist.controller;

import com.todolist.dto.HabitoRequest;
import com.todolist.dto.HabitoResponse;
import com.todolist.service.HabitoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/habitos")
@RequiredArgsConstructor
@Tag(name = "Hábitos", description = "Gerenciamento de rotinas diárias e cálculo de streaks")
public class HabitoController {

    private final HabitoService habitoService;

    @GetMapping
    @Operation(summary = "Listar hábitos do usuário com status de hoje e contagem de streaks")
    public ResponseEntity<List<HabitoResponse>> listar() {
        return ResponseEntity.ok(habitoService.listarTodos());
    }

    @PostMapping
    @Operation(summary = "Criar novo hábito")
    public ResponseEntity<HabitoResponse> criar(@Valid @RequestBody HabitoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(habitoService.criar(request));
    }

    @PostMapping("/{id}/toggle-hoje")
    @Operation(summary = "Alternar conclusão do hábito para o dia de hoje")
    public ResponseEntity<HabitoResponse> toggleHoje(@PathVariable Long id) {
        return ResponseEntity.ok(habitoService.toggleHoje(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar hábito")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        habitoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}