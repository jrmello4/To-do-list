package com.todolist.service;

import com.todolist.dto.NotaRapidaRequest;
import com.todolist.dto.NotaRapidaResponse;
import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.NotaRapida;
import com.todolist.entity.Prioridade;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.NotaRapidaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotaRapidaService {

    private final NotaRapidaRepository notaRapidaRepository;
    private final TaskService taskService;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<NotaRapidaResponse> listarTodas() {
        User user = authService.obterUsuarioAutenticado();
        return notaRapidaRepository.findByUsuarioIdOrderByFixadaDescDataAtualizacaoDesc(user.getId())
                .stream()
                .map(this::paraResponse)
                .collect(Collectors.toList());
    }

    public NotaRapidaResponse buscarPorId(Long id) {
        User user = authService.obterUsuarioAutenticado();
        NotaRapida nota = notaRapidaRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nota", id));
        return paraResponse(nota);
    }

    @Transactional
    public NotaRapidaResponse salvarOuAtualizar(Long id, NotaRapidaRequest request) {
        User user = authService.obterUsuarioAutenticado();

        NotaRapida nota;
        if (id != null) {
            nota = notaRapidaRepository.findByIdAndUsuarioId(id, user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Nota", id));
            nota.setTitulo(request.getTitulo());
            nota.setConteudo(request.getConteudo());
            if (request.getCor() != null) nota.setCor(request.getCor());
            if (request.getFixada() != null) nota.setFixada(request.getFixada());
        } else {
            nota = NotaRapida.builder()
                    .titulo(request.getTitulo())
                    .conteudo(request.getConteudo())
                    .cor(request.getCor() != null ? request.getCor() : "#ffffff")
                    .fixada(Boolean.TRUE.equals(request.getFixada()))
                    .usuario(user)
                    .build();
        }

        return paraResponse(notaRapidaRepository.save(nota));
    }

    @Transactional
    public void excluir(Long id) {
        User user = authService.obterUsuarioAutenticado();
        NotaRapida nota = notaRapidaRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Nota", id));
        notaRapidaRepository.delete(nota);
    }

    @Transactional
    public TaskResponse converterEmTarefa(Long id) {
        NotaRapidaResponse nota = buscarPorId(id);

        String titulo = (nota.getTitulo() != null && !nota.getTitulo().isBlank())
                ? nota.getTitulo().trim()
                : (nota.getConteudo() != null && !nota.getConteudo().isBlank()
                    ? nota.getConteudo().split("\n")[0].trim()
                    : "Tarefa convertida de anotação");

        if (titulo.length() > 200) {
            titulo = titulo.substring(0, 197) + "...";
        }

        TaskRequest taskRequest = TaskRequest.builder()
                .titulo(titulo)
                .descricao(nota.getConteudo())
                .prioridade(Prioridade.MEDIA)
                .build();

        return taskService.criar(taskRequest);
    }

    public NotaRapidaResponse paraResponse(NotaRapida n) {
        return NotaRapidaResponse.builder()
                .id(n.getId())
                .titulo(n.getTitulo())
                .conteudo(n.getConteudo())
                .cor(n.getCor())
                .fixada(n.getFixada())
                .dataCriacao(n.getDataCriacao())
                .dataAtualizacao(n.getDataAtualizacao())
                .build();
    }
}