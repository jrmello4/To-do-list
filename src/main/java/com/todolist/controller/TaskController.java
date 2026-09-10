package com.todolist.controller;

import com.todolist.dto.*;
import com.todolist.entity.Prioridade;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
            summary = "Listar tarefas",
            description = """
                    Devolve uma página das tarefas da conta, com filtros combináveis.

                    A resposta é paginada: o corpo é um objeto com `content`, `totalElements` e
                    `totalPages` — não um array. Use `page`, `size` e `sort` para navegar
                    (por exemplo `sort=prazo,asc`).

                    Só os campos da própria tarefa são ordenáveis, e qualquer outro devolve 400:
                    ordenar por uma coleção viraria junção, e a página voltaria com a mesma
                    tarefa repetida uma vez por passo.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de tarefas"),
            @ApiResponse(responseCode = "400", description = "Filtro ou ordenação inválidos",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Page<TaskResponse>> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario,

            @Parameter(description = "Filtra por situação de conclusão")
            @RequestParam(required = false) Boolean concluida,

            @Parameter(description = "Filtra pelas tarefas de um projeto", example = "1")
            @RequestParam(required = false) Long projeto,

            @Parameter(description = "Filtra pelas tarefas com uma etiqueta", example = "2")
            @RequestParam(required = false) Long etiqueta,

            @Parameter(description = "Apenas a caixa de entrada (tarefas sem projeto)")
            @RequestParam(required = false) Boolean semProjeto,

            @Parameter(description = "Filtra por prioridade", example = "ALTA")
            @RequestParam(required = false) Prioridade prioridade,

            @Parameter(description = "Apenas tarefas com prazo até esta data", example = "2026-09-30")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate prazoAte,

            @Parameter(description = "Busca no título e na descrição", example = "flyway")
            @RequestParam(required = false) String busca,

            @PageableDefault(size = 50, sort = "id") Pageable paginacao) {

        TaskFiltro filtro = new TaskFiltro(
                concluida, projeto, etiqueta, semProjeto, prioridade, prazoAte, busca);
        return ResponseEntity.ok(taskService.listar(usuario.getId(), filtro, paginacao));
    }

    @GetMapping("/resumo")
    @Operation(
            summary = "Resumo das tarefas",
            description = "Contagens calculadas no banco. Existe porque a listagem é paginada: "
                    + "somar a página no cliente daria números errados."
    )
    public ResponseEntity<ResumoResponse> resumo(
            @AuthenticationPrincipal UsuarioAutenticado usuario,

            @Parameter(description = "Data de referência para \"atrasada\" e \"vence hoje\". "
                    + "Envie o hoje de quem usa; o do servidor pode ser outro.",
                    example = "2026-09-08")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hoje) {

        return ResponseEntity.ok(taskService.resumo(usuario.getId(), hoje));
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

    @PatchMapping("/{id}/conclusao")
    @Operation(
            summary = "Concluir ou reabrir uma tarefa",
            description = "Rota própria porque alternar a situação não deveria exigir reenviar a "
                    + "tarefa inteira. Registra e limpa a data de conclusão."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Situação alterada"),
            @ApiResponse(responseCode = "400", description = "Corpo inválido",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> definirConclusao(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ConclusaoRequest request) {
        return ResponseEntity.ok(
                taskService.definirConclusao(usuario.getId(), id, request.getConcluida()));
    }

    @PatchMapping("/{id}/posicao")
    @Operation(
            summary = "Mover a tarefa na lista",
            description = "A posição é dita por um vizinho — antesDe ou depoisDe —, e não por um "
                    + "número. Com a lista paginada e filtrada, um índice da tela não corresponde "
                    + "a lugar nenhum da conta; um vizinho é o mesmo em qualquer filtro."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa movida"),
            @ApiResponse(responseCode = "400", description = "Nenhum vizinho informado, ou a própria tarefa",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa ou vizinho não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> mover(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody PosicaoRequest request) {
        return ResponseEntity.ok(taskService.mover(usuario.getId(), id, request));
    }

    /* --------------------------------------------------------- Subtarefas */

    /*
     * Os três endpoints devolvem a tarefa inteira, e não o passo mexido.
     *
     * O passo sozinho obrigaria o cliente a recalcular "2 de 5" por conta
     * própria, e aí a regra de contagem existiria em dois lugares. Devolvendo
     * a mãe, o cliente recebe o estado já coerente numa viagem só.
     */

    @PostMapping("/{id}/subtarefas")
    @Operation(
            summary = "Adicionar um passo à tarefa",
            description = "O passo entra no fim da lista. Passo é um degrau dentro da tarefa: "
                    + "não tem prazo, prioridade, projeto nem etiqueta próprios."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Passo adicionado"),
            @ApiResponse(responseCode = "400", description = "Título vazio ou limite de passos atingido",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> adicionarSubtarefa(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody SubtarefaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.adicionarSubtarefa(usuario.getId(), id, request));
    }

    @PatchMapping("/{id}/subtarefas/{subtarefaId}")
    @Operation(
            summary = "Alterar um passo",
            description = "Muda o texto, a situação, ou os dois. Campo omitido fica como está."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passo alterado"),
            @ApiResponse(responseCode = "404", description = "Tarefa ou passo não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> atualizarSubtarefa(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Identificador do passo", example = "1")
            @PathVariable Long subtarefaId,
            @Valid @RequestBody SubtarefaRequest request) {
        return ResponseEntity.ok(
                taskService.atualizarSubtarefa(usuario.getId(), id, subtarefaId, request));
    }

    @DeleteMapping("/{id}/subtarefas/{subtarefaId}")
    @Operation(summary = "Remover um passo", description = "Apaga o passo da tarefa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Passo removido"),
            @ApiResponse(responseCode = "404", description = "Tarefa ou passo não encontrado",
                    content = @Content(mediaType = ERRO_JSON,
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TaskResponse> removerSubtarefa(
            @AuthenticationPrincipal UsuarioAutenticado usuario,
            @Parameter(description = "Identificador da tarefa", example = "1")
            @PathVariable Long id,
            @Parameter(description = "Identificador do passo", example = "1")
            @PathVariable Long subtarefaId) {
        return ResponseEntity.ok(
                taskService.removerSubtarefa(usuario.getId(), id, subtarefaId));
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
