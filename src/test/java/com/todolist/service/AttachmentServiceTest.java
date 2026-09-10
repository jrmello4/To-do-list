package com.todolist.service;

import com.todolist.dto.AttachmentResponse;
import com.todolist.entity.Attachment;
import com.todolist.entity.Role;
import com.todolist.entity.Task;
import com.todolist.entity.User;
import com.todolist.repository.AttachmentRepository;
import com.todolist.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AttachmentService attachmentService;

    @TempDir
    Path tempDir;

    private User user;
    private Task task;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attachmentService, "uploadDir", tempDir.toString());

        user = User.builder()
                .id(1L)
                .nome("Dev")
                .email("dev@teste.com")
                .role(Role.ROLE_USER)
                .build();

        task = Task.builder()
                .id(10L)
                .titulo("Tarefa Teste")
                .usuario(user)
                .deletada(false)
                .build();

        lenient().when(authService.obterUsuarioAutenticado()).thenReturn(user);
    }

    @Test
    @DisplayName("Deve salvar anexo com sucesso")
    void deveSalvarAnexoComSucesso() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "relatorio.pdf", "application/pdf", "conteudo teste".getBytes()
        );

        when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(10L, 1L)).thenReturn(Optional.of(task));
        when(attachmentRepository.save(any(Attachment.class))).thenAnswer(i -> {
            Attachment a = i.getArgument(0);
            a.setId(100L);
            a.setDataCriacao(LocalDateTime.now());
            return a;
        });

        AttachmentResponse response = attachmentService.salvarAnexo(10L, file);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getNomeOriginal()).isEqualTo("relatorio.pdf");
        assertThat(response.getTipoConteudo()).isEqualTo("application/pdf");
        assertThat(response.getIsImagem()).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar arquivo vazio")
    void deveRejeitarArquivoVazio() {
        MockMultipartFile file = new MockMultipartFile("arquivo", "vazio.txt", "text/plain", new byte[0]);

        assertThatThrownBy(() -> attachmentService.salvarAnexo(10L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    @DisplayName("Deve rejeitar arquivo com path traversal")
    void deveRejeitarPathTraversal() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "../../../etc/passwd", "text/plain", "dados".getBytes()
        );

        when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(10L, 1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> attachmentService.salvarAnexo(10L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome de arquivo inválido");
    }

    @Test
    @DisplayName("Deve rejeitar extensão de arquivo executável perigoso")
    void deveRejeitarExtensaoExecutavel() {
        MockMultipartFile file = new MockMultipartFile(
                "arquivo", "virus.exe", "application/octet-stream", "malware".getBytes()
        );

        when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(10L, 1L)).thenReturn(Optional.of(task));

        assertThatThrownBy(() -> attachmentService.salvarAnexo(10L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Extensão de arquivo não permitida");
    }

    @Test
    @DisplayName("Deve excluir anexo logicamente e fisicamente")
    void deveExcluirAnexoComLimpezaFisica() throws IOException {
        Path dummyFile = tempDir.resolve("teste-storage.txt");
        Files.writeString(dummyFile, "conteudo");

        Attachment attachment = Attachment.builder()
                .id(50L)
                .nomeOriginal("teste.txt")
                .nomeArmazenado(dummyFile.getFileName().toString())
                .tipoConteudo("text/plain")
                .task(task)
                .build();

        when(attachmentRepository.findByIdAndTaskIdAndTaskUsuarioId(50L, 10L, 1L)).thenReturn(Optional.of(attachment));

        attachmentService.deletarAnexo(10L, 50L);

        verify(attachmentRepository).delete(attachment);
        assertThat(Files.exists(dummyFile)).isFalse();
    }

    @Test
    @DisplayName("Deve excluir arquivos físicos em lote")
    void deveExcluirArquivosFisicosEmLote() throws IOException {
        Path dummy1 = tempDir.resolve("f1.txt");
        Path dummy2 = tempDir.resolve("f2.txt");
        Files.writeString(dummy1, "1");
        Files.writeString(dummy2, "2");

        Attachment a1 = Attachment.builder().nomeArmazenado(dummy1.getFileName().toString()).build();
        Attachment a2 = Attachment.builder().nomeArmazenado(dummy2.getFileName().toString()).build();

        attachmentService.deletarArquivosFisicos(List.of(a1, a2));

        assertThat(Files.exists(dummy1)).isFalse();
        assertThat(Files.exists(dummy2)).isFalse();
    }
}
