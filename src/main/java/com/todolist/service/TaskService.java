package com.todolist.service;

import com.todolist.dto.*;
import com.todolist.entity.Prioridade;
import com.todolist.entity.Etiqueta;
import com.todolist.entity.Projeto;
import com.todolist.entity.Task;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.EtiquetaRepository;
import com.todolist.repository.ProjetoRepository;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.TaskSpecifications;
import com.todolist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjetoRepository projetoRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public TaskResponse criar(Long usuarioId, TaskRequest request) {
        Task task = Task.builder()
                // getReferenceById devolve uma referência preguiçosa: grava a
                // chave estrangeira sem ir ao banco buscar o usuário inteiro.
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .projeto(resolverProjeto(usuarioId, request.getProjetoId()))
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .prazo(request.getPrazo())
                .prioridade(request.getPrioridade() != null ? request.getPrioridade() : Prioridade.MEDIA)
                .concluida(false)
                .etiquetas(resolverEtiquetas(usuarioId, request.getEtiquetaIds()))
                .build();

        if (Boolean.TRUE.equals(request.getConcluida())) {
            aplicarConclusao(task, true);
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listar(Long usuarioId, TaskFiltro filtro, Pageable paginacao) {
        // A especificação do dono vem primeiro e não é opcional: é ela que
        // mantém o isolamento entre contas também nesta rota.
        Specification<Task> criterios = Specification.allOf(
                TaskSpecifications.doUsuario(usuarioId),
                TaskSpecifications.concluida(filtro.concluida()),
                TaskSpecifications.doProjeto(filtro.projetoId()),
                TaskSpecifications.comEtiqueta(filtro.etiquetaId()),
                TaskSpecifications.semProjeto(filtro.semProjeto()),
                TaskSpecifications.comPrioridade(filtro.prioridade()),
                TaskSpecifications.prazoAte(filtro.prazoAte()),
                TaskSpecifications.busca(filtro.busca()));

        return taskRepository.findAll(criterios, paginacao).map(TaskService::toResponse);
    }

    /**
     * Contagens vindas do banco, e não de somar a lista no cliente: com a
     * listagem paginada, o cliente só enxerga uma página por vez.
     *
     * A data de referência vem de fora porque "atrasada" depende do hoje de
     * quem usa, que pode não ser o do servidor.
     */
    @Transactional(readOnly = true)
    public ResumoResponse resumo(Long usuarioId, LocalDate referencia) {
        LocalDate hoje = referencia != null ? referencia : LocalDate.now();

        long total = taskRepository.countByUsuarioId(usuarioId);
        long concluidas = taskRepository.countByUsuarioIdAndConcluidaTrue(usuarioId);

        return ResumoResponse.builder()
                .total(total)
                .concluidas(concluidas)
                .pendentes(total - concluidas)
                .atrasadas(taskRepository.countByUsuarioIdAndConcluidaFalseAndPrazoBefore(usuarioId, hoje))
                .vencemHoje(taskRepository.countByUsuarioIdAndConcluidaFalseAndPrazo(usuarioId, hoje))
                .percentualConcluido(total == 0 ? 0 : Math.round(concluidas * 100f / total))
                .build();
    }

    @Transactional(readOnly = true)
    public TaskResponse buscarPorId(Long usuarioId, Long id) {
        return toResponse(buscarDoUsuario(usuarioId, id));
    }

    @Transactional
    public TaskResponse atualizar(Long usuarioId, Long id, TaskRequest request) {
        Task task = buscarDoUsuario(usuarioId, id);

        task.setTitulo(request.getTitulo());
        task.setDescricao(request.getDescricao());
        task.setProjeto(resolverProjeto(usuarioId, request.getProjetoId()));
        task.setPrazo(request.getPrazo());

        if (request.getPrioridade() != null) {
            task.setPrioridade(request.getPrioridade());
        }
        if (request.getEtiquetaIds() != null) {
            task.setEtiquetas(resolverEtiquetas(usuarioId, request.getEtiquetaIds()));
        }
        if (request.getConcluida() != null) {
            aplicarConclusao(task, request.getConcluida());
        }

        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse definirConclusao(Long usuarioId, Long id, boolean concluida) {
        Task task = buscarDoUsuario(usuarioId, id);
        aplicarConclusao(task, concluida);
        return toResponse(taskRepository.save(task));
    }

    @Transactional
    public void deletar(Long usuarioId, Long id) {
        taskRepository.delete(buscarDoUsuario(usuarioId, id));
    }

    /**
     * Único caminho para chegar a uma tarefa por id. Tarefa inexistente e
     * tarefa de outro usuário produzem o mesmo 404 de propósito: um 403
     * confirmaria que aquele id existe.
     */
    private Task buscarDoUsuario(Long usuarioId, Long id) {
        return taskRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Tarefa", id));
    }

    /**
     * O projeto também é validado contra o dono. Sem isto, bastaria enviar o
     * projetoId de outra conta no corpo da requisição para pendurar uma tarefa
     * lá dentro — a tarefa é sua, mas o projeto não.
     */
    private Projeto resolverProjeto(Long usuarioId, Long projetoId) {
        if (projetoId == null) {
            return null;
        }
        return projetoRepository.findByIdAndUsuarioId(projetoId, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", projetoId));
    }

    /**
     * As etiquetas também são validadas contra o dono — mesmo motivo do
     * projeto. Um id que não pertence à conta vira 404 em vez de ser ignorado
     * em silêncio: ignorar faria a tarefa ser salva sem a etiqueta pedida,
     * sem ninguém saber.
     */
    private Set<Etiqueta> resolverEtiquetas(Long usuarioId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new LinkedHashSet<>();
        }

        List<Etiqueta> encontradas = etiquetaRepository.findByUsuarioIdAndIdIn(usuarioId, ids);

        if (encontradas.size() != ids.stream().distinct().count()) {
            throw new ResourceNotFoundException("Etiqueta", null);
        }

        return new LinkedHashSet<>(encontradas);
    }

    /** Mantém dataConclusao coerente com concluida, sem mexer no que não mudou. */
    private void aplicarConclusao(Task task, boolean concluida) {
        if (concluida == Boolean.TRUE.equals(task.getConcluida())) {
            return;
        }

        task.setConcluida(concluida);
        task.setDataConclusao(concluida ? LocalDateTime.now() : null);
    }

    private static TaskResponse toResponse(Task task) {
        Projeto projeto = task.getProjeto();

        return TaskResponse.builder()
                .id(task.getId())
                .titulo(task.getTitulo())
                .descricao(task.getDescricao())
                .concluida(task.getConcluida())
                .projeto(projeto == null ? null : ProjetoResumoResponse.builder()
                        .id(projeto.getId())
                        .nome(projeto.getNome())
                        .cor(projeto.getCor())
                        .build())
                .etiquetas(task.getEtiquetas().stream()
                        .map(etiqueta -> EtiquetaResumoResponse.builder()
                                .id(etiqueta.getId())
                                .nome(etiqueta.getNome())
                                .cor(etiqueta.getCor())
                                .build())
                        .toList())
                .prazo(task.getPrazo())
                .prioridade(task.getPrioridade())
                .dataConclusao(task.getDataConclusao())
                .dataCriacao(task.getDataCriacao())
                .dataAtualizacao(task.getDataAtualizacao())
                .build();
    }
}
