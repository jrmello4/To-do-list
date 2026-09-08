package com.todolist.service;

import com.todolist.dto.AttachmentResponse;
import com.todolist.dto.NotificationResponse;
import com.todolist.dto.TagRequest;
import com.todolist.dto.TagResponse;
import com.todolist.entity.*;
import com.todolist.repository.AttachmentRepository;
import com.todolist.repository.TagRepository;
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
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewFeaturesServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TagService tagService;

    @InjectMocks
    private AttachmentService attachmentService;

    @InjectMocks
    private PdfReportService pdfReportService;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    @TempDir
    Path tempUploadDir;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .nome("Dev User")
                .email("dev@teste.com")
                .role(Role.ROLE_USER)
                .build();

        task = Task.builder()
                .id(10L)
                .titulo("Tarefa Teste")
                .concluida(false)
                .prioridade(Prioridade.URGENTE)
                .status(StatusTarefa.A_FAZER)
                .categoria(Categoria.TRABALHO)
                .dataVencimento(LocalDate.now().minusDays(1)) // Atrasada
                .usuario(user)
                .build();

        lenient().when(authService.obterUsuarioAutenticado()).thenReturn(user);
    }

    @Test
    @DisplayName("TagService: Deve criar tag com sucesso")
    void deveCriarTagComSucesso() {
        TagRequest req = TagRequest.builder().nome("frontend").cor("#3b82f6").build();
        Tag savedTag = Tag.builder().id(1L).nome("frontend").cor("#3b82f6").usuario(user).build();

        when(tagRepository.existsByUsuarioIdAndNomeIgnoreCase(eq(1L), eq("frontend"))).thenReturn(false);
        when(tagRepository.save(any(Tag.class))).thenReturn(savedTag);

        TagResponse resp = tagService.criar(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getNome()).isEqualTo("frontend");
        assertThat(resp.getCor()).isEqualTo("#3b82f6");
    }

    @Test
    @DisplayName("TagService: Deve rejeitar criação de tag duplicada")
    void deveRejeitarTagDuplicada() {
        TagRequest req = TagRequest.builder().nome("frontend").cor("#3b82f6").build();
        when(tagRepository.existsByUsuarioIdAndNomeIgnoreCase(eq(1L), eq("frontend"))).thenReturn(true);

        assertThatThrownBy(() -> tagService.criar(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Já existe uma tag com este nome");
    }

    @Test
    @DisplayName("AttachmentService: Deve fazer upload de imagem e identificar tipo")
    void deveSalvarAnexoImagemComSucesso() throws IOException {
        ReflectionTestUtils.setField(attachmentService, "uploadDir", tempUploadDir.toString());

        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "screenshot.png",
                "image/png",
                "dummy image content".getBytes()
        );

        when(taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(10L, 1L)).thenReturn(Optional.of(task));

        Attachment savedAttachment = Attachment.builder()
                .id(100L)
                .nomeOriginal("screenshot.png")
                .nomeArmazenado("uuid_screenshot.png")
                .tipoConteudo("image/png")
                .tamanho(file.getSize())
                .task(task)
                .build();

        when(attachmentRepository.save(any(Attachment.class))).thenReturn(savedAttachment);

        AttachmentResponse resp = attachmentService.salvarAnexo(10L, file);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isEqualTo(100L);
        assertThat(resp.getNomeOriginal()).isEqualTo("screenshot.png");
        assertThat(resp.getIsImagem()).isTrue();
        assertThat(resp.getUrlDownload()).contains("/api/tarefas/10/anexos/100");
    }

    @Test
    @DisplayName("AttachmentService: Deve rejeitar arquivo vazio")
    void deveRejeitarArquivoVazio() {
        MockMultipartFile emptyFile = new MockMultipartFile("arquivo", "", "text/plain", new byte[0]);

        assertThatThrownBy(() -> attachmentService.salvarAnexo(10L, emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode estar vazio");
    }

    @Test
    @DisplayName("PdfReportService: Deve gerar bytes válidos de PDF")
    void deveGerarRelatorioPdfComSucesso() {
        when(taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(1L)).thenReturn(List.of(task));

        byte[] pdfBytes = pdfReportService.gerarRelatorioTarefasPdf();

        assertThat(pdfBytes).isNotNull();
        assertThat(pdfBytes.length).isGreaterThan(100);
        // Assinatura mágica de arquivo PDF: "%PDF"
        String header = new String(pdfBytes, 0, 4);
        assertThat(header).isEqualTo("%PDF");
    }

    @Test
    @DisplayName("TaskService: Deve gerar notificações de tarefas atrasadas e urgentes")
    void deveGerarNotificacoes() {
        when(taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(1L)).thenReturn(List.of(task));

        List<NotificationResponse> notificacoes = taskService.obterNotificacoes();

        assertThat(notificacoes).isNotEmpty();
        NotificationResponse n = notificacoes.get(0);
        assertThat(n.getTaskId()).isEqualTo(10L);
        assertThat(n.getTipo()).isEqualTo("ATRASADA");
        assertThat(n.getTitulo()).contains("Tarefa Atrasada");
    }
}
