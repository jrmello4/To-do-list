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

    private static final java.util.Set<String> EXTENSOES_BLOQUEADAS = java.util.Set.of(
            ".exe", ".bat", ".cmd", ".sh", ".com", ".msi", ".jar", ".vbs", ".ps1", ".scr", ".pif"
    );

    private static final long TAMANHO_MAXIMO = 15L * 1024 * 1024;

    @Transactional
    public AttachmentResponse salvarAnexo(Long taskId, MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo enviado não pode estar vazio.");
        }
        if (file.getSize() > TAMANHO_MAXIMO) {
            throw new IllegalArgumentException("O arquivo excede o tamanho máximo de 15MB.");
        }

        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", taskId));

        String originalFilename = sanitizarNomeArquivo(
                StringUtils.cleanPath(Objects.requireNonNullElse(file.getOriginalFilename(), "arquivo")));

        String lowerName = originalFilename.toLowerCase();
        for (String ext : EXTENSOES_BLOQUEADAS) {
            if (lowerName.endsWith(ext)) {
                throw new IllegalArgumentException("Extensão de arquivo não permitida por motivos de segurança: " + ext);
            }
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String storedFilename = UUID.randomUUID() + "_" + originalFilename;
        Path targetLocation = uploadPath.resolve(storedFilename).normalize();
        // Garante que o destino permaneça dentro do diretório de upload (anti path traversal).
        if (!targetLocation.startsWith(uploadPath)) {
            throw new IllegalArgumentException("Caminho de arquivo inválido.");
        }

        String contentType = resolverTipoConteudo(file, originalFilename);

        // Persiste o registro antes do arquivo; em caso de falha, o arquivo parcial é removido.
        Attachment attachment = Attachment.builder()
                .nomeOriginal(originalFilename)
                .nomeArmazenado(storedFilename)
                .tipoConteudo(contentType)
                .tamanho(file.getSize())
                .task(task)
                .build();

        Attachment salvo = attachmentRepository.save(attachment);

        try (java.io.InputStream in = file.getInputStream()) {
            Files.copy(in, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            Files.deleteIfExists(targetLocation);
            throw ex;
        }

        return toResponse(salvo);
    }

    private String sanitizarNomeArquivo(String nome) {
        if (nome == null || nome.isBlank()) {
            return "arquivo";
        }
        // Mantém apenas o último segmento de caminho (remove diretórios e "..").
        String apenasNome = Paths.get(nome).getFileName().toString();
        apenasNome = apenasNome.replaceAll("[\\r\\n\\t\\u0000]", "_").trim();
        if (apenasNome.isBlank() || apenasNome.equals(".") || apenasNome.equals("..")) {
            return "arquivo";
        }
        if (apenasNome.length() > 180) {
            String ext = "";
            int dot = apenasNome.lastIndexOf('.');
            if (dot > 0) {
                ext = apenasNome.substring(dot);
            }
            apenasNome = apenasNome.substring(0, 180 - ext.length()) + ext;
        }
        return apenasNome;
    }

    private String resolverTipoConteudo(MultipartFile file, String originalFilename) {
        byte[] header = lerCabecalho(file);
        String detectado = detectarTipoPorMagicBytes(header);
        String informado = file.getContentType();
        if (detectado != null) {
            return detectado;
        }
        if (informado != null && informado.toLowerCase().startsWith("image/")) {
            // Conteúdo não corresponde a uma imagem válida — não permite renderização inline.
            return "application/octet-stream";
        }
        if (informado != null && !informado.isBlank()) {
            return informado;
        }
        return "application/octet-stream";
    }

    private byte[] lerCabecalho(MultipartFile file) {
        try (java.io.InputStream in = file.getInputStream()) {
            byte[] header = new byte[12];
            int lidos = in.read(header);
            if (lidos <= 0) {
                return new byte[0];
            }
            return java.util.Arrays.copyOf(header, lidos);
        } catch (IOException ex) {
            return new byte[0];
        }
    }

    private String detectarTipoPorMagicBytes(byte[] h) {
        if (h == null || h.length < 4) return null;
        if ((h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "image/png";
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "image/jpeg";
        if (h[0] == 'G' && h[1] == 'I' && h[2] == 'F' && h[3] == '8') return "image/gif";
        if (h.length >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return "image/webp";
        if (h[0] == '%' && h[1] == 'P' && h[2] == 'D' && h[3] == 'F') return "application/pdf";
        return null;
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
            Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = base.resolve(attachment.getNomeArmazenado()).normalize();
            if (!filePath.startsWith(base)) {
                throw new ResourceNotFoundException("Arquivo físico do anexo", attachment.getId());
            }
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

        deletarArquivoFisico(attachment);
        attachmentRepository.delete(attachment);
    }

    public void deletarArquivoFisico(Attachment attachment) {
        if (attachment == null || attachment.getNomeArmazenado() == null) return;
        Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path filePath = base.resolve(attachment.getNomeArmazenado()).normalize();
        if (!filePath.startsWith(base)) {
            return;
        }
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            org.slf4j.LoggerFactory.getLogger(AttachmentService.class)
                    .warn("Falha ao remover arquivo físico do anexo {}: {}", attachment.getId(), ex.getMessage());
        }
    }

    public void deletarArquivosFisicos(java.util.Collection<Attachment> attachments) {
        if (attachments == null) return;
        for (Attachment a : attachments) {
            deletarArquivoFisico(a);
        }
    }

    public AttachmentResponse toResponse(Attachment attachment) {
        boolean isImagem = attachment.getTipoConteudo() != null &&
                (attachment.getTipoConteudo().equalsIgnoreCase("image/png") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/jpeg") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/jpg") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/gif") ||
                 attachment.getTipoConteudo().equalsIgnoreCase("image/webp"));

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
