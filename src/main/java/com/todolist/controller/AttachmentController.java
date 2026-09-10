package com.todolist.controller;

import com.todolist.dto.AttachmentResponse;
import com.todolist.entity.Attachment;
import com.todolist.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/tarefas/{taskId}/anexos")
@RequiredArgsConstructor
@Tag(name = "Anexos", description = "Endpoints para upload, download e gerenciamento de arquivos das tarefas")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar anexo de arquivo ou imagem para uma tarefa")
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable Long taskId,
            @RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        AttachmentResponse response = attachmentService.salvarAnexo(taskId, arquivo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar anexos de uma tarefa")
    public ResponseEntity<List<AttachmentResponse>> listar(@PathVariable Long taskId) {
        return ResponseEntity.ok(attachmentService.listarPorTarefa(taskId));
    }

    @GetMapping("/{anexoId}")
    @Operation(summary = "Baixar ou visualizar anexo de uma tarefa")
    public ResponseEntity<Resource> download(
            @PathVariable Long taskId,
            @PathVariable Long anexoId) {
        Attachment attachment = attachmentService.obterEntidade(taskId, anexoId);
        Resource resource = attachmentService.carregarArquivo(attachment);

        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(attachment.getTipoConteudo());
        } catch (Exception e) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        boolean isSafeRasterImage = attachment.getTipoConteudo() != null &&
                (attachment.getTipoConteudo().equalsIgnoreCase("image/png") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/jpeg") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/jpg") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/gif") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/webp"));

        String dispositionType = isSafeRasterImage ? "inline" : "attachment";

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.builder(dispositionType)
                        .filename(attachment.getNomeOriginal(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(resource);
    }

    @DeleteMapping("/{anexoId}")
    @Operation(summary = "Excluir um anexo de uma tarefa")
    public ResponseEntity<Void> deletar(
            @PathVariable Long taskId,
            @PathVariable Long anexoId) {
        attachmentService.deletarAnexo(taskId, anexoId);
        return ResponseEntity.noContent().build();
    }
}
