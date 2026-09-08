package com.todolist.controller;

import com.todolist.dto.TagRequest;
import com.todolist.dto.TagResponse;
import com.todolist.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "Tags", description = "Endpoints para gerenciamento de etiquetas livres personalizadas")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "Listar todas as tags do usuário autenticado")
    public ResponseEntity<List<TagResponse>> listar() {
        return ResponseEntity.ok(tagService.listarPorUsuario());
    }

    @PostMapping
    @Operation(summary = "Criar uma nova tag personalizada com cor")
    public ResponseEntity<TagResponse> criar(@Valid @RequestBody TagRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tagService.criar(request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir uma tag do usuário")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        tagService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
