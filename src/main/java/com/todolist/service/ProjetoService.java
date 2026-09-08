package com.todolist.service;

import com.todolist.dto.ProjetoRequest;
import com.todolist.dto.ProjetoResponse;
import com.todolist.entity.Projeto;
import com.todolist.exception.NomeDeProjetoEmUsoException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.ProjetoRepository;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProjetoService {

    private static final String COR_PADRAO = "indigo";

    private final ProjetoRepository projetoRepository;
    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<ProjetoResponse> listar(Long usuarioId) {
        Map<Long, Long> pendentes = pendentesPorProjeto(usuarioId);

        return projetoRepository.findByUsuarioIdOrderByNomeAsc(usuarioId)
                .stream()
                .map(projeto -> toResponse(projeto, pendentes.getOrDefault(projeto.getId(), 0L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjetoResponse buscarPorId(Long usuarioId, Long id) {
        Projeto projeto = buscarDoUsuario(usuarioId, id);
        return toResponse(projeto, pendentesPorProjeto(usuarioId).getOrDefault(id, 0L));
    }

    @Transactional
    public ProjetoResponse criar(Long usuarioId, ProjetoRequest request) {
        String nome = request.getNome().trim();

        if (projetoRepository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, nome)) {
            throw new NomeDeProjetoEmUsoException(nome);
        }

        Projeto projeto = projetoRepository.save(Projeto.builder()
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .nome(nome)
                .cor(corOu(request.getCor()))
                .arquivado(Boolean.TRUE.equals(request.getArquivado()))
                .build());

        return toResponse(projeto, 0L);
    }

    @Transactional
    public ProjetoResponse atualizar(Long usuarioId, Long id, ProjetoRequest request) {
        Projeto projeto = buscarDoUsuario(usuarioId, id);
        String nome = request.getNome().trim();

        if (projetoRepository.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(usuarioId, nome, id)) {
            throw new NomeDeProjetoEmUsoException(nome);
        }

        projeto.setNome(nome);
        projeto.setCor(corOu(request.getCor()));

        if (request.getArquivado() != null) {
            projeto.setArquivado(request.getArquivado());
        }

        return toResponse(projeto, pendentesPorProjeto(usuarioId).getOrDefault(id, 0L));
    }

    /**
     * Apagar um projeto não apaga as tarefas: elas voltam para a caixa de
     * entrada. Levar as tarefas junto seria perda de dados silenciosa a partir
     * de um clique que parece organizacional.
     */
    @Transactional
    public void deletar(Long usuarioId, Long id) {
        Projeto projeto = buscarDoUsuario(usuarioId, id);

        taskRepository.findByUsuarioIdAndProjetoId(usuarioId, id)
                .forEach(tarefa -> tarefa.setProjeto(null));

        projetoRepository.delete(projeto);
    }

    private Projeto buscarDoUsuario(Long usuarioId, Long id) {
        return projetoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private Map<Long, Long> pendentesPorProjeto(Long usuarioId) {
        Map<Long, Long> contagem = new HashMap<>();

        for (Object[] linha : projetoRepository.contarPendentesPorProjeto(usuarioId)) {
            contagem.put((Long) linha[0], (Long) linha[1]);
        }
        return contagem;
    }

    private String corOu(String cor) {
        return cor == null || cor.isBlank() ? COR_PADRAO : cor.trim();
    }

    static ProjetoResponse toResponse(Projeto projeto, long pendentes) {
        return ProjetoResponse.builder()
                .id(projeto.getId())
                .nome(projeto.getNome())
                .cor(projeto.getCor())
                .arquivado(projeto.getArquivado())
                .tarefasPendentes(pendentes)
                .dataCriacao(projeto.getDataCriacao())
                .build();
    }
}
