package com.todolist.controller;

import com.todolist.dto.*;
import com.todolist.entity.Categoria;
import com.todolist.entity.Prioridade;
import com.todolist.entity.StatusTarefa;
import com.todolist.service.PdfReportService;
import com.todolist.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tarefas")
@RequiredArgsConstructor
@Tag(name = "Tarefas", description = "Endpoints para gerenciamento avançado de tarefas, kanban e subtarefas")
public class TaskController {

    private final TaskService taskService;
    private final PdfReportService pdfReportService;

    @PostMapping
    @Operation(summary = "Criar uma nova tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos")
    })
    public ResponseEntity<TaskResponse> criar(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar tarefas com filtros opcionais")
    @ApiResponse(responseCode = "200", description = "Lista de tarefas retornada com sucesso")
    public ResponseEntity<List<TaskResponse>> listar(
            @RequestParam(required = false) Boolean concluida,
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(required = false) Prioridade prioridade,
            @RequestParam(required = false) Categoria categoria,
            @RequestParam(required = false) String busca,
            @RequestParam(required = false) Long tagId) {
        if (concluida == null && status == null && prioridade == null && categoria == null && (busca == null || busca.isBlank()) && tagId == null) {
            return ResponseEntity.ok(taskService.listarTodas());
        }
        return ResponseEntity.ok(taskService.listarComFiltros(concluida, status, prioridade, categoria, busca, tagId));
    }

    @GetMapping("/resumo")
    @Operation(summary = "Obter resumo estatístico das tarefas")
    @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso")
    public ResponseEntity<TaskSummaryResponse> obterResumo() {
        return ResponseEntity.ok(taskService.obterResumo());
    }

    @GetMapping("/export/csv")
    @Operation(summary = "Exportar tarefas ativas em formato CSV")
    public ResponseEntity<byte[]> exportarCsv() {
        String csvData = taskService.exportarCsv();
        byte[] bytes = ("\uFEFF" + csvData).getBytes(StandardCharsets.UTF_8); // BOM para Excel

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDispositionFormData("attachment", "tarefas.csv");

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    @GetMapping("/export/pdf")
    @Operation(summary = "Exportar relatório executivo profissional de tarefas e métricas em PDF")
    public ResponseEntity<byte[]> exportarPdf() {
        byte[] pdfBytes = pdfReportService.gerarRelatorioTarefasPdf();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "relatorio-executivo-tarefas.pdf");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/notificacoes")
    @Operation(summary = "Obter alertas e notificações em tempo real de tarefas atrasadas ou urgentes")
    public ResponseEntity<List<NotificationResponse>> obterNotificacoes() {
        return ResponseEntity.ok(taskService.obterNotificacoes());
    }

    @GetMapping("/lixeira")
    @Operation(summary = "Listar tarefas na lixeira")
    public ResponseEntity<List<TaskResponse>> listarLixeira() {
        return ResponseEntity.ok(taskService.listarLixeira());
    }

    @PatchMapping("/{id}/restaurar")
    @Operation(summary = "Restaurar tarefa da lixeira")
    public ResponseEntity<TaskResponse> restaurar(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.restaurarDaLixeira(id));
    }

    @DeleteMapping("/{id}/definitivo")
    @Operation(summary = "Excluir permanentemente uma tarefa")
    public ResponseEntity<Void> excluirDefinitivamente(@PathVariable Long id) {
        taskService.excluirDefinitivamente(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/lixeira/esvaziar")
    @Operation(summary = "Esvaziar todas as tarefas da lixeira")
    public ResponseEntity<Void> esvaziarLixeira() {
        taskService.esvaziarLixeira();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TaskResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma tarefa existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos fornecidos"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TaskResponse> atualizar(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.atualizar(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Alternar status de conclusão da tarefa")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alternado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<TaskResponse> alternarStatus(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.alternarStatus(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Definir status de conclusão da tarefa")
    public ResponseEntity<TaskResponse> atualizarStatus(
            @PathVariable Long id,
            @RequestParam boolean concluida) {
        return ResponseEntity.ok(taskService.atualizarStatus(id, concluida));
    }

    @PatchMapping("/{id}/status-kanban")
    @Operation(summary = "Definir status kanban da tarefa (A_FAZER, EM_ANDAMENTO, CONCLUIDA)")
    public ResponseEntity<TaskResponse> atualizarStatusKanban(
            @PathVariable Long id,
            @RequestParam StatusTarefa status) {
        return ResponseEntity.ok(taskService.atualizarStatusKanban(id, status));
    }

    // --- Subtarefas ---
    @PostMapping("/{id}/subtarefas")
    @Operation(summary = "Adicionar uma subtarefa")
    public ResponseEntity<TaskResponse> adicionarSubtarefa(
            @PathVariable Long id,
            @Valid @RequestBody SubtaskRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.adicionarSubtarefa(id, request));
    }

    @PatchMapping("/{taskId}/subtarefas/{subtaskId}/toggle")
    @Operation(summary = "Alternar conclusão de uma subtarefa")
    public ResponseEntity<TaskResponse> alternarSubtarefa(
            @PathVariable Long taskId,
            @PathVariable Long subtaskId) {
        return ResponseEntity.ok(taskService.alternarSubtarefa(taskId, subtaskId));
    }

    @DeleteMapping("/{taskId}/subtarefas/{subtaskId}")
    @Operation(summary = "Remover uma subtarefa")
    public ResponseEntity<TaskResponse> deletarSubtarefa(
            @PathVariable Long taskId,
            @PathVariable Long subtaskId) {
        return ResponseEntity.ok(taskService.deletarSubtarefa(taskId, subtaskId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Mover tarefa para a lixeira")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarefa enviada para a lixeira"),
            @ApiResponse(responseCode = "404", description = "Tarefa não encontrada")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        taskService.moverParaLixeira(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/pomodoro/increment")
    @Operation(summary = "Incrementar contagem de pomodoros realizados na tarefa")
    public ResponseEntity<TaskResponse> incrementarPomodoro(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.incrementarPomodoro(id));
    }

    @GetMapping("/estatisticas")
    @Operation(summary = "Obter estatísticas e métricas para dashboard de gráficos Chart.js")
    public ResponseEntity<com.todolist.dto.TaskStatsResponse> obterEstatisticas() {
        return ResponseEntity.ok(taskService.obterEstatisticas());
    }
}
