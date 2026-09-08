package com.todolist.service;

import com.todolist.dto.EtiquetaRequest;
import com.todolist.dto.EtiquetaResponse;
import com.todolist.entity.Etiqueta;
import com.todolist.exception.NomeDeEtiquetaEmUsoException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.EtiquetaRepository;
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
public class EtiquetaService {

    private static final String COR_PADRAO = "indigo";

    private final EtiquetaRepository etiquetaRepository;
    private final TaskRepository taskRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public List<EtiquetaResponse> listar(Long usuarioId) {
        Map<Long, Long> pendentes = pendentesPorEtiqueta(usuarioId);

        return etiquetaRepository.findByUsuarioIdOrderByNomeAsc(usuarioId)
                .stream()
                .map(etiqueta -> toResponse(etiqueta, pendentes.getOrDefault(etiqueta.getId(), 0L)))
                .toList();
    }

    @Transactional
    public EtiquetaResponse criar(Long usuarioId, EtiquetaRequest request) {
        String nome = request.getNome().trim();

        if (etiquetaRepository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, nome)) {
            throw new NomeDeEtiquetaEmUsoException(nome);
        }

        Etiqueta etiqueta = etiquetaRepository.save(Etiqueta.builder()
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .nome(nome)
                .cor(request.getCor() == null || request.getCor().isBlank()
                        ? COR_PADRAO : request.getCor().trim())
                .build());

        return toResponse(etiqueta, 0L);
    }

    @Transactional
    public EtiquetaResponse atualizar(Long usuarioId, Long id, EtiquetaRequest request) {
        Etiqueta etiqueta = etiquetaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", id));

        String nome = request.getNome().trim();

        if (etiquetaRepository.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(usuarioId, nome, id)) {
            throw new NomeDeEtiquetaEmUsoException(nome);
        }

        etiqueta.setNome(nome);
        if (request.getCor() != null && !request.getCor().isBlank()) {
            etiqueta.setCor(request.getCor().trim());
        }

        return toResponse(etiqueta, pendentesPorEtiqueta(usuarioId).getOrDefault(id, 0L));
    }

    /**
     * Excluir a etiqueta não apaga tarefa alguma — apenas a marcação some.
     *
     * A associação é desfeita pelo lado dono (Task) antes da exclusão. O
     * ON DELETE CASCADE da tabela de junção resolve no banco, mas não na
     * sessão do Hibernate: com uma tarefa gerenciada ainda apontando para a
     * etiqueta, o flush falha com TransientObjectException.
     */
    @Transactional
    public void deletar(Long usuarioId, Long id) {
        Etiqueta etiqueta = etiquetaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta", id));

        taskRepository.findByUsuarioIdAndEtiquetasId(usuarioId, id)
                .forEach(tarefa -> tarefa.getEtiquetas().remove(etiqueta));

        etiquetaRepository.delete(etiqueta);
    }

    private Map<Long, Long> pendentesPorEtiqueta(Long usuarioId) {
        Map<Long, Long> contagem = new HashMap<>();

        for (Object[] linha : etiquetaRepository.contarPendentesPorEtiqueta(usuarioId)) {
            contagem.put((Long) linha[0], (Long) linha[1]);
        }
        return contagem;
    }

    private static EtiquetaResponse toResponse(Etiqueta etiqueta, long pendentes) {
        return EtiquetaResponse.builder()
                .id(etiqueta.getId())
                .nome(etiqueta.getNome())
                .cor(etiqueta.getCor())
                .tarefasPendentes(pendentes)
                .build();
    }
}
