package com.todolist.controller;

import com.todolist.dto.ErrorResponse;
import com.todolist.dto.ProjetoRequest;
import com.todolist.dto.ProjetoResponse;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.ProjetoService;
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
@RequestMapping("/api/projetos")
@RequiredArgsConstructor
@Tag(name = "Projetos", description = "Agrupamento das tarefas da conta autenticada")
public class ProjetoController {

    private static final String ERRO_JSON = "application/json";

    private final ProjetoService projetoService;

    @GetMapping
    @Operation(summary = "Listar projetos",
            description = "Em ordem alfabética, com a contagem de tarefas pendentes de cada um.")
    public ResponseEntity<List<ProjetoResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(projetoService.listar(usuario.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar projeto por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto encontrado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProjetoResponse> buscarPorId(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador do projeto", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(projetoService.buscarPorId(usuario.getId(), id));
    }

    @PostMapping
    @Operation(summary = "Criar projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Projeto criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe projeto com esse nome",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProjetoResponse> criar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody ProjetoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projetoService.criar(usuario.getId(), request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar projeto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Projeto atualizado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Já existe projeto com esse nome",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<ProjetoResponse> atualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador do projeto", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProjetoRequest request) {
        return ResponseEntity.ok(projetoService.atualizar(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir projeto",
            description = "As tarefas do projeto não são apagadas: voltam para a caixa de entrada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Projeto excluído"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador do projeto", example = "1")
            @PathVariable Long id) {
        projetoService.deletar(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
