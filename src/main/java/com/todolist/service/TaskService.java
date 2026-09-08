package com.todolist.service;

import com.todolist.dto.*;
import com.todolist.entity.*;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.TagRepository;
import com.todolist.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final AuthService authService;
    private final TagRepository tagRepository;
    private final AttachmentService attachmentService;

    public Long obterUsuarioIdAtual() {
        return authService.obterUsuarioAutenticado().getId();
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse criar(TaskRequest request) {
        User user = authService.obterUsuarioAutenticado();

        boolean concluida = Boolean.TRUE.equals(request.getConcluida());
        StatusTarefa status = request.getStatus();
        if (status == null) {
            status = concluida ? StatusTarefa.CONCLUIDA : StatusTarefa.A_FAZER;
        }

        Task task = Task.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .concluida(concluida || status == StatusTarefa.CONCLUIDA)
                .prioridade(request.getPrioridade() != null ? request.getPrioridade() : Prioridade.MEDIA)
                .status(status)
                .categoria(request.getCategoria() != null ? request.getCategoria() : Categoria.GERAL)
                .dataVencimento(request.getDataVencimento())
                .recorrencia(request.getRecorrencia() != null ? request.getRecorrencia() : Recorrencia.NENHUMA)
                .pomodorosEstimados(request.getPomodorosEstimados() != null ? request.getPomodorosEstimados() : 1)
                .pomodorosRealizados(0)
                .deletada(false)
                .usuario(user)
                .subtarefas(new ArrayList<>())
                .build();

        if (request.getSubtarefas() != null && !request.getSubtarefas().isEmpty()) {
            for (SubtaskRequest subDto : request.getSubtarefas()) {
                if (subDto.getTitulo() != null && !subDto.getTitulo().isBlank()) {
                    Subtask subtask = Subtask.builder()
                            .titulo(subDto.getTitulo().trim())
                            .concluida(Boolean.TRUE.equals(subDto.getConcluida()))
                            .task(task)
                            .build();
                    task.getSubtarefas().add(subtask);
                }
            }
        }

        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tagsEncontradas = tagRepository.findByIdInAndUsuarioId(request.getTagIds(), user.getId());
            task.setTags(new HashSet<>(tagsEncontradas));
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarTodas() {
        User user = authService.obterUsuarioAutenticado();
        return taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarComFiltros(
            Boolean concluida,
            StatusTarefa status,
            Prioridade prioridade,
            Categoria categoria,
            String termo,
            Long tagId) {
        User user = authService.obterUsuarioAutenticado();
        String termoFormatado = (termo != null && !termo.isBlank()) ? termo.trim() : null;

        List<Task> tasks;
        if (concluida == null && status == null && prioridade == null && categoria == null && termoFormatado == null) {
            tasks = taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(user.getId());
        } else {
            tasks = taskRepository.findByUsuarioFiltrosAvancados(user.getId(), concluida, status, prioridade, categoria, termoFormatado);
        }

        if (tagId != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getTags() != null && t.getTags().stream().anyMatch(tag -> tag.getId().equals(tagId)))
                    .toList();
        }

        return tasks.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarComFiltros(
            Boolean concluida,
            StatusTarefa status,
            Prioridade prioridade,
            Categoria categoria,
            String termo) {
        return listarComFiltros(concluida, status, prioridade, categoria, termo, null);
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarComFiltros(Boolean concluida, String termo) {
        return listarComFiltros(concluida, null, null, null, termo);
    }

    @Transactional(readOnly = true)
    public TaskResponse buscarPorId(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
        return toResponse(task);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse atualizar(Long id, TaskRequest request) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        task.setTitulo(request.getTitulo());
        task.setDescricao(request.getDescricao());

        if (request.getPrioridade() != null) {
            task.setPrioridade(request.getPrioridade());
        }
        if (request.getCategoria() != null) {
            task.setCategoria(request.getCategoria());
        }
        if (request.getDataVencimento() != null) {
            task.setDataVencimento(request.getDataVencimento());
        }
        if (request.getRecorrencia() != null) {
            task.setRecorrencia(request.getRecorrencia());
        }
        if (request.getPomodorosEstimados() != null) {
            task.setPomodorosEstimados(request.getPomodorosEstimados());
        }

        if (request.getStatus() != null) {
            task.sincronizarStatus(request.getStatus());
        } else if (request.getConcluida() != null) {
            task.sincronizarStatusComConcluida(request.getConcluida());
        }

        if (request.getTagIds() != null) {
            List<Tag> tagsEncontradas = tagRepository.findByIdInAndUsuarioId(request.getTagIds(), user.getId());
            task.getTags().clear();
            task.getTags().addAll(tagsEncontradas);
        }

        Task saved = taskRepository.save(task);
        if (Boolean.TRUE.equals(saved.getConcluida())) {
            verificarGerarProximaRecorrencia(saved, user);
        }

        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse alternarStatus(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        boolean novoConcluida = !Boolean.TRUE.equals(task.getConcluida());
        task.sincronizarStatusComConcluida(novoConcluida);

        Task saved = taskRepository.save(task);
        if (Boolean.TRUE.equals(saved.getConcluida())) {
            verificarGerarProximaRecorrencia(saved, user);
        }

        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse atualizarStatus(Long id, boolean concluida) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        task.sincronizarStatusComConcluida(concluida);
        Task saved = taskRepository.save(task);
        if (concluida) {
            verificarGerarProximaRecorrencia(saved, user);
        }

        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse atualizarStatusKanban(Long id, StatusTarefa novoStatus) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        task.sincronizarStatus(novoStatus);
        Task saved = taskRepository.save(task);
        if (novoStatus == StatusTarefa.CONCLUIDA) {
            verificarGerarProximaRecorrencia(saved, user);
        }

        return toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse incrementarPomodoro(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        int atual = task.getPomodorosRealizados() != null ? task.getPomodorosRealizados() : 0;
        task.setPomodorosRealizados(atual + 1);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public void moverParaLixeira(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));

        task.setDeletada(true);
        task.setDataDelecao(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public void deletar(Long id) {
        moverParaLixeira(id);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse restaurarDaLixeira(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioId(id, user.getId())
                .filter(Task::getDeletada)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa na lixeira", id));

        task.setDeletada(false);
        task.setDataDelecao(null);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public void excluirDefinitivamente(Long id) {
        User user = authService.obterUsuarioAutenticado();
        if (!taskRepository.existsByIdAndUsuarioId(id, user.getId())) {
            throw new ResourceNotFoundException("Tarefa", id);
        }
        taskRepository.deleteById(id);
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public void esvaziarLixeira() {
        User user = authService.obterUsuarioAutenticado();
        taskRepository.esvaziarLixeiraDoUsuario(user.getId());
    }

    @Transactional(readOnly = true)
    public List<TaskResponse> listarLixeira() {
        User user = authService.obterUsuarioAutenticado();
        return taskRepository.findByUsuarioIdAndDeletadaTrueOrderByDataDelecaoDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // --- Subtarefas ---
    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse adicionarSubtarefa(Long taskId, SubtaskRequest request) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", taskId));

        Subtask subtask = Subtask.builder()
                .titulo(request.getTitulo())
                .concluida(Boolean.TRUE.equals(request.getConcluida()))
                .task(task)
                .build();

        task.getSubtarefas().add(subtask);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse alternarSubtarefa(Long taskId, Long subtaskId) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", taskId));

        Subtask subtask = task.getSubtarefas().stream()
                .filter(s -> s.getId().equals(subtaskId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Subtarefa", subtaskId));

        subtask.setConcluida(!Boolean.TRUE.equals(subtask.getConcluida()));
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    @CacheEvict(value = {"resumo", "estatisticas"}, allEntries = true)
    public TaskResponse deletarSubtarefa(Long taskId, Long subtaskId) {
        User user = authService.obterUsuarioAutenticado();
        Task task = taskRepository.findByIdAndUsuarioIdAndDeletadaFalse(taskId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", taskId));

        boolean removed = task.getSubtarefas().removeIf(s -> s.getId().equals(subtaskId));
        if (!removed) {
            throw new ResourceNotFoundException("Subtarefa", subtaskId);
        }

        return toResponse(taskRepository.save(task));
    }

    // --- Métricas e Resumo ---
    @Transactional(readOnly = true)
    @Cacheable(value = "resumo", key = "#root.target.obterUsuarioIdAtual()")
    public TaskSummaryResponse obterResumo() {
        User user = authService.obterUsuarioAutenticado();
        Long uid = user.getId();

        long total = taskRepository.countByUsuarioIdAndDeletadaFalse(uid);
        long concluidas = taskRepository.countByUsuarioIdAndDeletadaFalseAndConcluida(uid, true);
        long pendentes = total - concluidas;
        long aFazer = taskRepository.countByUsuarioIdAndDeletadaFalseAndStatus(uid, StatusTarefa.A_FAZER);
        long emAndamento = taskRepository.countByUsuarioIdAndDeletadaFalseAndStatus(uid, StatusTarefa.EM_ANDAMENTO);
        long atrasadas = taskRepository.countByUsuarioIdAndDeletadaFalseAndConcluidaFalseAndDataVencimentoBefore(uid, LocalDate.now());
        long naLixeira = taskRepository.countByUsuarioIdAndDeletadaTrue(uid);

        return TaskSummaryResponse.builder()
                .total(total)
                .concluidas(concluidas)
                .pendentes(pendentes)
                .aFazer(aFazer)
                .emAndamento(emAndamento)
                .atrasadas(atrasadas)
                .naLixeira(naLixeira)
                .build();
    }

    // --- Estatísticas Avançadas para Dashboard Chart.js ---
    @Transactional(readOnly = true)
    @Cacheable(value = "estatisticas", key = "#root.target.obterUsuarioIdAtual()")
    public TaskStatsResponse obterEstatisticas() {
        User user = authService.obterUsuarioAutenticado();
        List<Task> todasAtivas = taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(user.getId());

        // Por categoria
        Map<String, Long> porCategoria = new LinkedHashMap<>();
        for (Categoria cat : Categoria.values()) {
            long count = todasAtivas.stream().filter(t -> t.getCategoria() == cat).count();
            porCategoria.put(cat.name(), count);
        }

        // Por prioridade
        Map<String, Long> porPrioridade = new LinkedHashMap<>();
        for (Prioridade p : Prioridade.values()) {
            long count = todasAtivas.stream().filter(t -> t.getPrioridade() == p).count();
            porPrioridade.put(p.name(), count);
        }

        // Últimos 7 dias (rotulos, criadas, concluidas)
        List<String> rotulos = new ArrayList<>();
        List<Long> criadas = new ArrayList<>();
        List<Long> concluidas = new ArrayList<>();
        DateTimeFormatter dtfDia = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate hoje = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate dia = hoje.minusDays(i);
            rotulos.add(dia.format(dtfDia));

            long countCriadas = todasAtivas.stream()
                    .filter(t -> t.getDataCriacao() != null && t.getDataCriacao().toLocalDate().isEqual(dia))
                    .count();
            criadas.add(countCriadas);

            long countConcluidas = todasAtivas.stream()
                    .filter(t -> Boolean.TRUE.equals(t.getConcluida()) &&
                            t.getDataAtualizacao() != null && t.getDataAtualizacao().toLocalDate().isEqual(dia))
                    .count();
            concluidas.add(countConcluidas);
        }

        // Taxa de entrega no prazo
        List<Task> concluidasLista = todasAtivas.stream().filter(t -> Boolean.TRUE.equals(t.getConcluida())).toList();
        double taxaNoPrazo = 100.0;
        if (!concluidasLista.isEmpty()) {
            long comPrazo = concluidasLista.stream().filter(t -> t.getDataVencimento() != null).count();
            if (comPrazo > 0) {
                long noPrazo = concluidasLista.stream()
                        .filter(t -> t.getDataVencimento() != null &&
                                t.getDataAtualizacao() != null &&
                                !t.getDataAtualizacao().toLocalDate().isAfter(t.getDataVencimento()))
                        .count();
                taxaNoPrazo = Math.round(((double) noPrazo / comPrazo) * 1000.0) / 10.0;
            }
        }

        int totalPomodoros = todasAtivas.stream()
                .mapToInt(t -> t.getPomodorosRealizados() != null ? t.getPomodorosRealizados() : 0)
                .sum();

        long totalRecorrentes = todasAtivas.stream()
                .filter(t -> t.getRecorrencia() != null && t.getRecorrencia() != Recorrencia.NENHUMA)
                .count();

        return TaskStatsResponse.builder()
                .porCategoria(porCategoria)
                .porPrioridade(porPrioridade)
                .ultimos7DiasRotulos(rotulos)
                .ultimos7DiasCriadas(criadas)
                .ultimos7DiasConcluidas(concluidas)
                .taxaConclusaoNoPrazo(taxaNoPrazo)
                .totalPomodorosRealizados(totalPomodoros)
                .totalRecorrentes(totalRecorrentes)
                .build();
    }

    // --- Exportação CSV ---
    @Transactional(readOnly = true)
    public String exportarCsv() {
        User user = authService.obterUsuarioAutenticado();
        List<Task> tarefas = taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(user.getId());
        StringBuilder csv = new StringBuilder();
        csv.append("ID;Título;Descrição;Prioridade;Status;Categoria;Vencimento;Recorrência;Pomodoros;Concluída;Subtarefas;Criada Em\n");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DateTimeFormatter dfDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Task t : tarefas) {
            String subtarefasStr = "";
            if (t.getSubtarefas() != null && !t.getSubtarefas().isEmpty()) {
                long prontas = t.getSubtarefas().stream().filter(s -> Boolean.TRUE.equals(s.getConcluida())).count();
                subtarefasStr = prontas + "/" + t.getSubtarefas().size();
            }

            int pomRealizados = t.getPomodorosRealizados() != null ? t.getPomodorosRealizados() : 0;
            int pomEstimados = t.getPomodorosEstimados() != null ? t.getPomodorosEstimados() : 1;
            String pomodoroStr = pomRealizados + "/" + pomEstimados;

            csv.append(t.getId()).append(";")
                    .append(escapeCsv(t.getTitulo())).append(";")
                    .append(escapeCsv(t.getDescricao())).append(";")
                    .append(t.getPrioridade()).append(";")
                    .append(t.getStatus()).append(";")
                    .append(t.getCategoria()).append(";")
                    .append(t.getDataVencimento() != null ? t.getDataVencimento().format(dfDate) : "").append(";")
                    .append(t.getRecorrencia() != null ? t.getRecorrencia() : Recorrencia.NENHUMA).append(";")
                    .append(pomodoroStr).append(";")
                    .append(Boolean.TRUE.equals(t.getConcluida()) ? "Sim" : "Não").append(";")
                    .append(subtarefasStr).append(";")
                    .append(t.getDataCriacao() != null ? t.getDataCriacao().format(dtf) : "")
                    .append("\n");
        }
        return csv.toString();
    }

    private void verificarGerarProximaRecorrencia(Task task, User user) {
        if (task.getRecorrencia() != null && task.getRecorrencia() != Recorrencia.NENHUMA && Boolean.TRUE.equals(task.getConcluida())) {
            LocalDate baseDate = task.getDataVencimento() != null ? task.getDataVencimento() : LocalDate.now();
            LocalDate proximoVencimento = switch (task.getRecorrencia()) {
                case DIARIA -> baseDate.plusDays(1);
                case SEMANAL -> baseDate.plusWeeks(1);
                case MENSAL -> baseDate.plusMonths(1);
                default -> baseDate;
            };

            Task proxima = Task.builder()
                    .titulo(task.getTitulo())
                    .descricao(task.getDescricao())
                    .concluida(false)
                    .status(StatusTarefa.A_FAZER)
                    .prioridade(task.getPrioridade())
                    .categoria(task.getCategoria())
                    .dataVencimento(proximoVencimento)
                    .recorrencia(task.getRecorrencia())
                    .pomodorosEstimados(task.getPomodorosEstimados())
                    .pomodorosRealizados(0)
                    .deletada(false)
                    .usuario(user)
                    .subtarefas(new ArrayList<>())
                    .build();

            if (task.getSubtarefas() != null) {
                for (Subtask s : task.getSubtarefas()) {
                    proxima.getSubtarefas().add(Subtask.builder()
                            .titulo(s.getTitulo())
                            .concluida(false)
                            .task(proxima)
                            .build());
                }
            }

            taskRepository.save(proxima);
        }
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        String clean = val.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
        return "\"" + clean + "\"";
    }

    private TaskResponse toResponse(Task task) {
        boolean estaAtrasada = task.getDataVencimento() != null &&
                task.getDataVencimento().isBefore(LocalDate.now()) &&
                !Boolean.TRUE.equals(task.getConcluida());

        List<SubtaskResponse> subResponses = new ArrayList<>();
        int totalSub = 0;
        int concluidasSub = 0;

        if (task.getSubtarefas() != null) {
            totalSub = task.getSubtarefas().size();
            for (Subtask s : task.getSubtarefas()) {
                if (Boolean.TRUE.equals(s.getConcluida())) concluidasSub++;
                subResponses.add(SubtaskResponse.builder()
                        .id(s.getId())
                        .titulo(s.getTitulo())
                        .concluida(s.getConcluida())
                        .dataCriacao(s.getDataCriacao())
                        .build());
            }
        }

        List<TagResponse> tagsResp = new ArrayList<>();
        if (task.getTags() != null) {
            tagsResp = task.getTags().stream()
                    .map(t -> TagResponse.builder()
                            .id(t.getId())
                            .nome(t.getNome())
                            .cor(t.getCor())
                            .build())
                    .sorted((a, b) -> a.getNome().compareToIgnoreCase(b.getNome()))
                    .toList();
        }

        List<AttachmentResponse> anexosResp = new ArrayList<>();
        if (task.getAnexos() != null) {
            anexosResp = task.getAnexos().stream()
                    .map(attachmentService::toResponse)
                    .toList();
        }

        return TaskResponse.builder()
                .id(task.getId())
                .titulo(task.getTitulo())
                .descricao(task.getDescricao())
                .concluida(task.getConcluida())
                .prioridade(task.getPrioridade())
                .status(task.getStatus())
                .categoria(task.getCategoria())
                .dataVencimento(task.getDataVencimento())
                .recorrencia(task.getRecorrencia() != null ? task.getRecorrencia() : Recorrencia.NENHUMA)
                .pomodorosEstimados(task.getPomodorosEstimados() != null ? task.getPomodorosEstimados() : 1)
                .pomodorosRealizados(task.getPomodorosRealizados() != null ? task.getPomodorosRealizados() : 0)
                .estaAtrasada(estaAtrasada)
                .deletada(task.getDeletada())
                .dataDelecao(task.getDataDelecao())
                .dataCriacao(task.getDataCriacao())
                .dataAtualizacao(task.getDataAtualizacao())
                .subtarefas(subResponses)
                .totalSubtarefas(totalSub)
                .subtarefasConcluidas(concluidasSub)
                .tags(tagsResp)
                .anexos(anexosResp)
                .build();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> obterNotificacoes() {
        User user = authService.obterUsuarioAutenticado();
        List<Task> tasks = taskRepository.findByUsuarioIdAndDeletadaFalseOrderByDataCriacaoDesc(user.getId());
        LocalDate hoje = LocalDate.now();
        List<NotificationResponse> notificacoes = new ArrayList<>();

        for (Task t : tasks) {
            if (Boolean.TRUE.equals(t.getConcluida())) {
                continue;
            }

            if (t.getDataVencimento() != null) {
                if (t.getDataVencimento().isBefore(hoje)) {
                    notificacoes.add(NotificationResponse.builder()
                            .taskId(t.getId())
                            .titulo("Tarefa Atrasada: " + t.getTitulo())
                            .mensagem("Venceu em " + t.getDataVencimento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " e ainda não foi concluída.")
                            .tipo("ATRASADA")
                            .dataVencimento(t.getDataVencimento())
                            .prioridade(t.getPrioridade().name())
                            .build());
                } else if (t.getDataVencimento().isEqual(hoje)) {
                    notificacoes.add(NotificationResponse.builder()
                            .taskId(t.getId())
                            .titulo("Vence Hoje: " + t.getTitulo())
                            .mensagem("O prazo expira hoje! Prioridade: " + t.getPrioridade().name())
                            .tipo("VENCE_HOJE")
                            .dataVencimento(t.getDataVencimento())
                            .prioridade(t.getPrioridade().name())
                            .build());
                } else if (t.getDataVencimento().isEqual(hoje.plusDays(1))) {
                    notificacoes.add(NotificationResponse.builder()
                            .taskId(t.getId())
                            .titulo("Vence Amanhã: " + t.getTitulo())
                            .mensagem("Fique atento, esta tarefa vence amanhã.")
                            .tipo("VENCE_BREVE")
                            .dataVencimento(t.getDataVencimento())
                            .prioridade(t.getPrioridade().name())
                            .build());
                }
            } else if (t.getPrioridade() == Prioridade.URGENTE) {
                notificacoes.add(NotificationResponse.builder()
                        .taskId(t.getId())
                        .titulo("Atenção Urgente: " + t.getTitulo())
                        .mensagem("Esta tarefa possui prioridade URGENTE e requer sua atenção.")
                        .tipo("URGENTE")
                        .dataVencimento(null)
                        .prioridade(t.getPrioridade().name())
                        .build());
            }
        }

        return notificacoes;
    }
}
