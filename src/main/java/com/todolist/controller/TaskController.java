package com.todolist.controller;

import com.todolist.dto.ErrorResponse;
import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.TaskService;
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
@RequestMapping("/api/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Criação, consulta, atualização e exclusão das tarefas da conta autenticada")
public class TaskController {

    private static final String ERRO_JSON = "application/json";

    private final TaskService taskService;

    @PostMapping
    @Operation(
            summary = "Criar uma nova tarefa",
            description = "Cadastra uma tarefa. O título é obrigatório; quando `concluida` não é "
                    + "informado, a tarefa é criada como pendente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> criar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.criar(usuario.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as tarefas",
            description = "Retorna todas as tarefas cadastradas, concluídas e pendentes. "
                    + "A lista vem vazia quando não há nenhuma tarefa."
    )
    @ApiResponse(responseCode = "200", description = "Lista de tarefas retornada com sucesso")
    public ResponseEntity<List<TaskResponse>> listarTodas(
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(taskService.listarTodas(usuario.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID", description = "Retorna uma única tarefa pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> buscarPorId(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(taskService.buscarPorId(usuario.getId(), id));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar uma tarefa existente",
            description = "Substitui título e descrição da tarefa. O campo `concluida` só é alterado "
                    + "quando enviado no corpo da requisição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> atualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.atualizar(usuario.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar uma tarefa", description = "Remove definitivamente a tarefa informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id) {
        taskService.deletar(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
