package com.todolist.controller;

import com.todolist.dto.ErrorResponse;
import com.todolist.dto.HabitoRequest;
import com.todolist.dto.HabitoResponse;
import com.todolist.security.UsuarioAutenticado;
import com.todolist.service.HabitoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/habitos")
@RequiredArgsConstructor
@Tag(name = "Hábitos", description = "Rotina recorrente, com sequências calculadas a partir "
        + "dos registros")
public class HabitoController {

    private static final String ERRO_JSON = "application/json";
    private static final String DESCRICAO_HOJE =
            "Data de referência. Envie o hoje de quem usa; o do servidor pode ser outro.";

    private final HabitoService habitoService;

    @GetMapping
    @Operation(
            summary = "Listar hábitos",
            description = """
                    Cada hábito vem com a sequência atual, a maior dos últimos 365 dias e a
                    grade dos últimos 14 dias.

                    Tarefa e hábito são modelos diferentes de propósito: tarefa termina, hábito
                    se repete. A pergunta que interessa aqui não é "está concluída?", e sim
                    "em quantos dos últimos dias eu fiz?".
                    """
    )
    public ResponseEntity<List<HabitoResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = DESCRICAO_HOJE, example = "2026-09-08")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje) {
        return ResponseEntity.ok(habitoService.listar(usuario.getId(), hoje));
    }

    @PostMapping
    @Operation(summary = "Criar hábito")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hábito criado"),
            @ApiResponse(responseCode = "409", description = "Já existe hábito com esse nome",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<HabitoResponse> criar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Valid @RequestBody HabitoRequest request,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(habitoService.criar(usuario.getId(), request, hoje));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar hábito")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hábito atualizado"),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<HabitoResponse> atualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador do hábito", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody HabitoRequest request,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje) {
        return ResponseEntity.ok(habitoService.atualizar(usuario.getId(), id, request, hoje));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir hábito", description = "Apaga também o histórico de registros.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hábito excluído"),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id) {
        habitoService.deletar(usuario.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/registros/{data}")
    @Operation(
            summary = "Marcar um dia como cumprido",
            description = "Idempotente: repetir a chamada para o mesmo dia não duplica nada."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dia marcado"),
            @ApiResponse(responseCode = "400", description = "Data no futuro",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Hábito não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<HabitoResponse> marcar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @Parameter(description = "Dia a marcar", example = "2026-09-08")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje) {
        return ResponseEntity.ok(habitoService.marcar(usuario.getId(), id, data, hoje));
    }

    @DeleteMapping("/{id}/registros/{data}")
    @Operation(summary = "Desmarcar um dia",
            description = "Também idempotente: desmarcar o que já não estava marcado não é erro.")
    public ResponseEntity<HabitoResponse> desmarcar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @PathVariable Long id,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje) {
        return ResponseEntity.ok(habitoService.desmarcar(usuario.getId(), id, data, hoje));
    }
}
