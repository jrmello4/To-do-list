package com.todolist.service;

import com.todolist.dto.AttachmentResponse;
import com.todolist.entity.Attachment;
import com.todolist.entity.Task;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.AttachmentRepository;
import com.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;

    @Value("${app.upload.dir:uploads/}")
    private String uploadDir;

    @Transactional
    public AttachmentResponse salvarAnexo(Long taskId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado não pode estar vazio.");
        }

        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", taskId));

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "arquivo"));
        // Remove caracteres perigosos de path traversal
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Nome de arquivo inválido: " + originalFilename);
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetLocation = uploadPath.resolve(storedFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            contentType = "application/octet-stream";
        }

        Attachment attachment = Attachment.builder()
                .nomeOriginal(originalFilename)
                .nomeArmazenado(storedFilename)
                .tipoConteudo(contentType)
                .tamanho(file.getSize())
                .task(task)
                .build();

        Attachment salvo = attachmentRepository.save(attachment);
        return toResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<AttachmentResponse> listarPorTarefa(Long taskId) {
        User user = authService.obterUsuarioAutenticado();
        taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", taskId));

        return attachmentRepository.findByTaskId(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Attachment obterEntidade(Long taskId, Long anexoId) {
        User user = authService.obterUsuarioAutenticado();
        return attachmentRepository.findByIdAndTaskIdAndTaskUsuarioId(anexoId, taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Anexo", anexoId));
    }

    public Resource carregarArquivo(Attachment attachment) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(attachment.getNomeArmazenado());
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Arquivo físico do anexo", attachment.getId());
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Arquivo do anexo", attachment.getId());
        }
    }

    @Transactional
    public void deletarAnexo(Long taskId, Long anexoId) {
        User user = authService.obterUsuarioAutenticado();
        Attachment attachment = attachmentRepository.findByIdAndTaskIdAndTaskUsuarioId(anexoId, taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Anexo", anexoId));

        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(attachment.getNomeArmazenado());
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // Log e continua
        }

        attachmentRepository.delete(attachment);
    }

    public AttachmentResponse toResponse(Attachment attachment) {
        boolean isImagem = attachment.getTipoConteudo() != null &&
                attachment.getTipoConteudo().toLowerCase().startsWith("image/");

        String urlDownload = "/api/tarefas/" + attachment.getTask().getId() + "/anexos/" + attachment.getId();

        return AttachmentResponse.builder()
                .id(attachment.getId())
                .nomeOriginal(attachment.getNomeOriginal())
                .tipoConteudo(attachment.getTipoConteudo())
                .tamanho(attachment.getTamanho())
                .dataCriacao(attachment.getDataCriacao())
                .urlDownload(urlDownload)
                .isImagem(isImagem)
                .build();
    }
}
