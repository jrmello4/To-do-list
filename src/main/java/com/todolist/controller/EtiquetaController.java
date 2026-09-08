package com.todolist.controller;

import com.todolist.dto.ErrorResponse;
import com.todolist.dto.EtiquetaRequest;
import com.todolist.dto.EtiquetaResponse;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.EtiquetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/etiquetas")
@RequiredArgsConstructor
@Tag(name = "Etiquetas", description = "Marcações transversais aos projetos")
public class EtiquetaController {

    private static final String ERRO_JSON = "application/json";

    private final EtiquetaService etiquetaService;

    @GetMapping
    @Operation(summary = "Listar etiquetas",
            description = "Em ordem alfabética, com a contagem de tarefas pendentes de cada uma.")
    public ResponseEntity<List<EtiquetaResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(etiquetaService.listar(usuario.getId()));
    }

    @PostMapping
    @Operation(summary = "Criar etiqueta")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Etiqueta criada"),
            @ApiResponse(responseCode = "409", description = "Já existe etiqueta com esse nome",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EtiquetaResponse> criar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody EtiquetaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(etiquetaService.criar(usuario.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar etiqueta")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Etiqueta atualizada"),
            @ApiResponse(responseCode = "404", description = "Etiqueta não encontrada",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EtiquetaResponse> atualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da etiqueta", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody EtiquetaRequest request) {
        return ResponseEntity.ok(etiquetaService.atualizar(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir etiqueta",
            description = "As tarefas não são afetadas: apenas a marcação some delas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Etiqueta excluída"),
            @ApiResponse(responseCode = "404", description = "Etiqueta não encontrada",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da etiqueta", example = "1")
            @PathVariable Long id) {
        etiquetaService.deletar(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
