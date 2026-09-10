package com.todolist.controller;

import com.todolist.dto.CategoriaTransacaoRequest;
import com.todolist.dto.CategoriaTransacaoResponse;
import com.todolist.entity.TipoTransacao;
import com.todolist.service.CategoriaTransacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financas/categorias")
@RequiredArgsConstructor
@Tag(name = "Finanças - Categorias", description = "Gerenciamento de categorias de receitas e despesas")
public class CategoriaTransacaoController {

    private final CategoriaTransacaoService categoriaService;

    @GetMapping
    @Operation(summary = "Listar categorias financeiras")
    public ResponseEntity<List<CategoriaTransacaoResponse>> listar(@RequestParam(required = false) TipoTransacao tipo) {
        if (tipo != null) {
            return ResponseEntity.ok(categoriaService.listarPorTipo(tipo));
        }
        return ResponseEntity.ok(categoriaService.listarTodas());
    }

    @PostMapping
    @Operation(summary = "Criar nova categoria financeira")
    public ResponseEntity<CategoriaTransacaoResponse> criar(@Valid @RequestBody CategoriaTransacaoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria financeira")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}