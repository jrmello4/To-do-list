package com.todolist.service;

import com.todolist.dto.*;
import com.todolist.entity.Prioridade;
import com.todolist.entity.Etiqueta;
import com.todolist.entity.Projeto;
import com.todolist.entity.Subtarefa;
import com.todolist.entity.Task;
import com.todolist.exception.ConflitoDeVersaoException;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.EtiquetaRepository;
import com.todolist.repository.OrdenacaoDeTarefas;
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
import java.time.ZoneOffset;
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
    private final FusoDaConta fusoDaConta;

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
                // Nova tarefa entra no fim, que é onde estava antes de existir
                // ordem manual (a listagem era por id crescente).
                .ordem(taskRepository.maiorOrdem(usuarioId) + 1)
                .build();

        if (Boolean.TRUE.equals(request.getConcluida())) {
            aplicarConclusao(task, true);
        }

        return toResponse(taskRepository.saveAndFlush(task));
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> listar(Long usuarioId, TaskFiltro filtro, Pageable paginacao) {
        // Ordenação conferida aqui, e não no controlador: LembreteService
        // também chama esta rota, e a regra vale para todo mundo que lista.
        OrdenacaoDeTarefas.verificar(paginacao.getSort());

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
        // Sem a data informada, o hoje é o da conta e não o do servidor: em
        // UTC, "atrasada" mudaria de significado três horas antes da meia-noite
        // de quem está em São Paulo.
        LocalDate hoje = fusoDaConta.hoje(usuarioId, referencia);

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
        conferirVersao(task, request.getVersao());

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

        return toResponse(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse definirConclusao(Long usuarioId, Long id, boolean concluida) {
        Task task = buscarDoUsuario(usuarioId, id);
        aplicarConclusao(task, concluida);
        return toResponse(taskRepository.saveAndFlush(task));
    }

    /**
     * Move a tarefa para imediatamente antes ou depois de outra.
     *
     * A posição é dita por um vizinho porque a lista é paginada e filtrada:
     * um índice numérico da tela não corresponde a lugar nenhum da conta. Com
     * um vizinho, o resultado é o mesmo esteja a lista filtrada por pendentes,
     * por projeto ou inteira — o que muda é só quem está visível entre os dois.
     *
     * As demais tarefas mantêm a ordem relativa entre si. Quem foi arrastado
     * é o único que muda de lugar.
     */
    @Transactional
    public TaskResponse mover(Long usuarioId, Long id, PosicaoRequest request) {
        boolean depois = request.getDepoisDe() != null;
        Long referenciaId = depois ? request.getDepoisDe() : request.getAntesDe();

        if (referenciaId == null) {
            throw new IllegalArgumentException("Informe antesDe ou depoisDe");
        }
        if (referenciaId.equals(id)) {
            throw new IllegalArgumentException("Uma tarefa não pode ser posicionada em relação a si mesma");
        }

        Task tarefa = buscarDoUsuario(usuarioId, id);
        Task referencia = buscarDoUsuario(usuarioId, referenciaId);

        int destino = referencia.getOrdem() + (depois ? 1 : 0);

        // Abre espaço primeiro e só depois grava: fazer o contrário empurraria
        // a própria tarefa junto com as outras.
        taskRepository.abrirEspaco(usuarioId, destino);

        // O UPDATE em massa não passa pela sessão, então a entidade em mãos
        // está desatualizada: busca de novo antes de gravar a posição.
        Task movida = buscarDoUsuario(usuarioId, id);
        movida.setOrdem(destino);

        return toResponse(taskRepository.saveAndFlush(movida));
    }

    /* --------------------------------------------------------- Subtarefas */

    /*
     * Não existe SubtarefaRepository, e a ausência é o desenho.
     *
     * Com um repositório, mais cedo ou mais tarde alguém escreveria
     * findById(subId) e o passo de outra conta estaria a um id de distância.
     * Aqui o único caminho até um passo é a tarefa mãe, e a tarefa mãe só é
     * encontrada por buscarDoUsuario — que sempre recebe o dono. A falha não
     * é evitada por disciplina: ela não tem como ser escrita.
     */

    @Transactional
    public TaskResponse adicionarSubtarefa(Long usuarioId, Long tarefaId, SubtarefaRequest request) {
        Task task = buscarDoUsuario(usuarioId, tarefaId);

        if (request.getTitulo() == null || request.getTitulo().isBlank()) {
            throw new IllegalArgumentException("O passo precisa de um título");
        }
        // Sem teto, a subtarefa viaja junto da mãe em toda resposta e uma
        // página de 50 tarefas carregaria dezenas de milhares de linhas.
        if (task.getSubtarefas().size() >= Subtarefa.LIMITE_POR_TAREFA) {
            throw new IllegalArgumentException(
                    "Uma tarefa comporta no máximo " + Subtarefa.LIMITE_POR_TAREFA + " passos");
        }

        task.getSubtarefas().add(Subtarefa.builder()
                .task(task)
                .titulo(request.getTitulo().trim())
                .concluida(Boolean.TRUE.equals(request.getConcluida()))
                .ordem(proximaOrdem(task))
                .build());

        return toResponse(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse atualizarSubtarefa(Long usuarioId, Long tarefaId, Long subtarefaId,
                                           SubtarefaRequest request) {
        Task task = buscarDoUsuario(usuarioId, tarefaId);
        Subtarefa passo = passoDaTarefa(task, subtarefaId);

        if (request.getTitulo() != null && !request.getTitulo().isBlank()) {
            passo.setTitulo(request.getTitulo().trim());
        }
        if (request.getConcluida() != null) {
            passo.setConcluida(request.getConcluida());
        }

        return toResponse(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse removerSubtarefa(Long usuarioId, Long tarefaId, Long subtarefaId) {
        Task task = buscarDoUsuario(usuarioId, tarefaId);
        Subtarefa passo = passoDaTarefa(task, subtarefaId);

        // Tirar da coleção da mãe é o que apaga: com orphanRemoval, o
        // Hibernate emite o DELETE. Apagar por um repositório à parte deixaria
        // o objeto vivo na sessão — foi assim que a exclusão de etiqueta
        // quebrou antes.
        task.getSubtarefas().remove(passo);

        return toResponse(taskRepository.saveAndFlush(task));
    }

    /**
     * Procura o passo dentro da tarefa que já foi validada contra o dono, e
     * não no banco inteiro. Um id de outra tarefa dá o mesmo 404 de sempre.
     */
    private Subtarefa passoDaTarefa(Task task, Long subtarefaId) {
        return task.getSubtarefas().stream()
                .filter(passo -> passo.getId().equals(subtarefaId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Passo", subtarefaId));
    }

    /** Novo passo vai para o fim da lista, que é onde quem escreve espera. */
    private static int proximaOrdem(Task task) {
        return task.getSubtarefas().stream()
                .mapToInt(passo -> passo.getOrdem() == null ? 0 : passo.getOrdem())
                .max()
                .orElse(-1) + 1;
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

    /**
     * Recusa a edição feita sobre uma versão que já foi substituída.
     *
     * A conferência é aqui, e não na coluna @Version sozinha: cada PUT abre a
     * sua própria transação e lê a versão atual, então duas abas nunca
     * colidiriam no banco — a segunda simplesmente apagaria o que a primeira
     * escreveu. Quem sabe o que estava na tela é o cliente.
     *
     * Sem o campo, grava como sempre gravou: a importação não tem versão para
     * mandar, e um cliente antigo não deve parar de funcionar por causa disto.
     */
    private void conferirVersao(Task task, Integer versaoEnviada) {
        if (versaoEnviada != null && !versaoEnviada.equals(task.getVersao())) {
            throw new ConflitoDeVersaoException();
        }
    }

    /** Mantém dataConclusao coerente com concluida, sem mexer no que não mudou. */
    private void aplicarConclusao(Task task, boolean concluida) {
        if (concluida == Boolean.TRUE.equals(task.getConcluida())) {
            return;
        }

        task.setConcluida(concluida);
        // UTC, pelo mesmo motivo dos carimbos da entidade: é o painel que
        // depois converte para o fuso de quem lê.
        task.setDataConclusao(concluida ? LocalDateTime.now(ZoneOffset.UTC) : null);
    }

    /*
     * As gravações usam saveAndFlush, e não save.
     *
     * A versão é incrementada pelo Hibernate no flush, que sem isto só
     * acontece no commit — depois de a resposta já ter sido montada. O corpo
     * saía com a versão anterior, e um cliente que a guardasse seria recusado
     * na edição seguinte por estar "desatualizado" em relação a uma alteração
     * que foi ele mesmo quem fez. O flush aconteceria de qualquer forma um
     * instante depois; o que muda é só a resposta contar a verdade.
     */

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
                .subtarefas(task.getSubtarefas().stream()
                        .map(passo -> SubtarefaResponse.builder()
                                .id(passo.getId())
                                .titulo(passo.getTitulo())
                                .concluida(passo.getConcluida())
                                .ordem(passo.getOrdem())
                                .build())
                        .toList())
                .totalDePassos(task.getSubtarefas().size())
                .passosConcluidos((int) task.getSubtarefas().stream()
                        .filter(passo -> Boolean.TRUE.equals(passo.getConcluida()))
                        .count())
                .prazo(task.getPrazo())
                .prioridade(task.getPrioridade())
                .ordem(task.getOrdem())
                .versao(task.getVersao())
                .dataConclusao(task.getDataConclusao())
                .dataCriacao(task.getDataCriacao())
                .dataAtualizacao(task.getDataAtualizacao())
                .build();
    }
}
