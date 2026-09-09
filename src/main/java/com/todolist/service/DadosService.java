package com.todolist.service;

import com.todolist.dto.DadosExportados;
import com.todolist.dto.ImportacaoResponse;
import com.todolist.dto.UsuarioResponse;
import com.todolist.entity.*;
import com.todolist.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DadosService {

    /**
     * Teto por tipo na importação. Um arquivo enorme, de propósito ou por
     * engano, não deveria conseguir encher o banco numa requisição só.
     */
    private static final int LIMITE_POR_TIPO = 5000;

    private final UsuarioRepository usuarioRepository;
    private final ProjetoRepository projetoRepository;
    private final EtiquetaRepository etiquetaRepository;
    private final TaskRepository taskRepository;
    private final HabitoRepository habitoRepository;
    private final HabitoRegistroRepository registroRepository;

    /* -------------------------------------------------------- Exportação */

    @Transactional(readOnly = true)
    public DadosExportados exportar(Long usuarioId) {
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        return DadosExportados.builder()
                .exportadoEm(LocalDateTime.now())
                .conta(UsuarioResponse.builder()
                        .id(usuario.getId())
                        .nome(usuario.getNome())
                        .email(usuario.getEmail())
                        .dataCriacao(usuario.getDataCriacao())
                        .build())
                .projetos(projetoRepository.findByUsuarioIdOrderByNomeAsc(usuarioId).stream()
                        .map(projeto -> DadosExportados.ProjetoExportado.builder()
                                .nome(projeto.getNome())
                                .cor(projeto.getCor())
                                .arquivado(projeto.getArquivado())
                                .build())
                        .toList())
                .etiquetas(etiquetaRepository.findByUsuarioIdOrderByNomeAsc(usuarioId).stream()
                        .map(etiqueta -> DadosExportados.EtiquetaExportada.builder()
                                .nome(etiqueta.getNome())
                                .cor(etiqueta.getCor())
                                .build())
                        .toList())
                // Na ordem que a conta arrumou, não na de criação: a arrumação
                // é trabalho de quem usa, e um backup que a perde não é backup.
                .tarefas(taskRepository.findByUsuarioIdOrderByOrdemAscIdAsc(usuarioId).stream()
                        .map(DadosService::exportarTarefa)
                        .toList())
                .habitos(exportarHabitos(usuarioId))
                .build();
    }

    private static DadosExportados.TarefaExportada exportarTarefa(Task task) {
        return DadosExportados.TarefaExportada.builder()
                .titulo(task.getTitulo())
                .descricao(task.getDescricao())
                .concluida(task.getConcluida())
                .projeto(task.getProjeto() == null ? null : task.getProjeto().getNome())
                .etiquetas(task.getEtiquetas().stream().map(Etiqueta::getNome).sorted().toList())
                .prazo(task.getPrazo())
                .prioridade(task.getPrioridade() == null ? null : task.getPrioridade().name())
                .dataCriacao(task.getDataCriacao())
                .dataConclusao(task.getDataConclusao())
                .subtarefas(task.getSubtarefas().stream()
                        .map(passo -> DadosExportados.SubtarefaExportada.builder()
                                .titulo(passo.getTitulo())
                                .concluida(passo.getConcluida())
                                .build())
                        .toList())
                .build();
    }

    private List<DadosExportados.HabitoExportado> exportarHabitos(Long usuarioId) {
        // Uma consulta para todos os registros, agrupados depois em memória.
        Map<Long, List<LocalDate>> registros = new HashMap<>();
        for (HabitoRegistro registro
                : registroRepository.buscarDaConta(usuarioId, LocalDate.of(1970, 1, 1))) {
            registros.computeIfAbsent(registro.getHabito().getId(), chave -> new ArrayList<>())
                    .add(registro.getData());
        }

        return habitoRepository.findByUsuarioIdOrderByNomeAsc(usuarioId).stream()
                .map(habito -> DadosExportados.HabitoExportado.builder()
                        .nome(habito.getNome())
                        .cor(habito.getCor())
                        .diasSemana(habito.diasComoConjunto().stream()
                                .map(java.time.DayOfWeek::getValue).sorted().toList())
                        .ativo(habito.getAtivo())
                        .registros(registros.getOrDefault(habito.getId(), List.of())
                                .stream().sorted().toList())
                        .build())
                .toList();
    }

    /* -------------------------------------------------------- Importação */

    /**
     * Soma ao que já existe, em vez de substituir.
     *
     * Substituir exigiria apagar tudo antes, e um arquivo errado levaria a
     * conta inteira junto. Projetos, etiquetas e hábitos com nome já existente
     * são reaproveitados; tarefas são sempre criadas, porque não há como saber
     * se uma tarefa de mesmo título é a mesma ou outra parecida.
     */
    @Transactional
    public ImportacaoResponse importar(Long usuarioId, DadosExportados dados) {
        verificarVersao(dados.getVersao());

        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        List<String> reaproveitados = new ArrayList<>();

        Resultado<Projeto> projetos = importarProjetos(
                usuarioId, usuario, dados.getProjetos(), reaproveitados);
        Resultado<Etiqueta> etiquetas = importarEtiquetas(
                usuarioId, usuario, dados.getEtiquetas(), reaproveitados);

        int tarefas = importarTarefas(
                usuario, dados.getTarefas(), projetos.porNome(), etiquetas.porNome());
        int[] habitos = importarHabitos(usuarioId, usuario, dados.getHabitos(), reaproveitados);

        return ImportacaoResponse.builder()
                .projetos(projetos.criados())
                .etiquetas(etiquetas.criados())
                .tarefas(tarefas)
                .habitos(habitos[0])
                .registrosDeHabito(habitos[1])
                .reaproveitados(reaproveitados)
                .build();
    }

    /** Mapa por nome mais a contagem do que foi realmente criado. */
    private record Resultado<T>(Map<String, T> porNome, int criados) {
    }

    private void verificarVersao(String versao) {
        if (versao != null && !DadosExportados.VERSAO_ATUAL.equals(versao)) {
            throw new IllegalArgumentException(
                    "Arquivo na versão " + versao + "; esta instalação lê a versão "
                            + DadosExportados.VERSAO_ATUAL);
        }
    }

    private void verificarLimite(List<?> itens, String tipo) {
        if (itens != null && itens.size() > LIMITE_POR_TIPO) {
            throw new IllegalArgumentException(
                    "O arquivo traz " + itens.size() + " " + tipo + "; o limite por importação é "
                            + LIMITE_POR_TIPO);
        }
    }

    private Resultado<Projeto> importarProjetos(Long usuarioId, Usuario usuario,
                                                List<DadosExportados.ProjetoExportado> vindos,
                                                List<String> reaproveitados) {
        verificarLimite(vindos, "projetos");

        Map<String, Projeto> porNome = new LinkedHashMap<>();
        for (Projeto projeto : projetoRepository.findByUsuarioIdOrderByNomeAsc(usuarioId)) {
            porNome.put(chave(projeto.getNome()), projeto);
        }

        if (vindos == null) {
            return new Resultado<>(porNome, 0);
        }

        int criados = 0;
        for (DadosExportados.ProjetoExportado vindo : vindos) {
            if (vindo.getNome() == null || vindo.getNome().isBlank()) {
                continue;
            }
            if (porNome.containsKey(chave(vindo.getNome()))) {
                reaproveitados.add("Projeto \"" + vindo.getNome() + "\"");
                continue;
            }

            Projeto criado = projetoRepository.save(Projeto.builder()
                    .usuario(usuario)
                    .nome(vindo.getNome().trim())
                    .cor(vindo.getCor() == null ? "indigo" : vindo.getCor())
                    .arquivado(Boolean.TRUE.equals(vindo.getArquivado()))
                    .build());
            porNome.put(chave(criado.getNome()), criado);
            criados++;
        }

        return new Resultado<>(porNome, criados);
    }

    private Resultado<Etiqueta> importarEtiquetas(Long usuarioId, Usuario usuario,
                                                  List<DadosExportados.EtiquetaExportada> vindas,
                                                  List<String> reaproveitadas) {
        verificarLimite(vindas, "etiquetas");

        Map<String, Etiqueta> porNome = new LinkedHashMap<>();
        for (Etiqueta etiqueta : etiquetaRepository.findByUsuarioIdOrderByNomeAsc(usuarioId)) {
            porNome.put(chave(etiqueta.getNome()), etiqueta);
        }

        if (vindas == null) {
            return new Resultado<>(porNome, 0);
        }

        int criadas = 0;
        for (DadosExportados.EtiquetaExportada vinda : vindas) {
            if (vinda.getNome() == null || vinda.getNome().isBlank()) {
                continue;
            }
            if (porNome.containsKey(chave(vinda.getNome()))) {
                reaproveitadas.add("Etiqueta \"" + vinda.getNome() + "\"");
                continue;
            }

            Etiqueta criada = etiquetaRepository.save(Etiqueta.builder()
                    .usuario(usuario)
                    .nome(vinda.getNome().trim())
                    .cor(vinda.getCor() == null ? "indigo" : vinda.getCor())
                    .build());
            porNome.put(chave(criada.getNome()), criada);
            criadas++;
        }

        return new Resultado<>(porNome, criadas);
    }

    private int importarTarefas(Usuario usuario, List<DadosExportados.TarefaExportada> vindas,
                                Map<String, Projeto> projetos, Map<String, Etiqueta> etiquetas) {
        verificarLimite(vindas, "tarefas");

        if (vindas == null) {
            return 0;
        }

        int criadas = 0;
        // A posição vem da sequência do arquivo, e não de um campo gravado:
        // é a mesma ordem que a exportação escreveu, e continua válida mesmo
        // importando para uma conta que já tem tarefas.
        int ordem = taskRepository.maiorOrdem(usuario.getId()) + 1;

        for (DadosExportados.TarefaExportada vinda : vindas) {
            if (vinda.getTitulo() == null || vinda.getTitulo().isBlank()) {
                continue;
            }

            Set<Etiqueta> daTarefa = new LinkedHashSet<>();
            for (String nome : Optional.ofNullable(vinda.getEtiquetas()).orElse(List.of())) {
                Etiqueta etiqueta = etiquetas.get(chave(nome));
                if (etiqueta != null) {
                    daTarefa.add(etiqueta);
                }
            }

            Task task = Task.builder()
                    .usuario(usuario)
                    .titulo(vinda.getTitulo().trim())
                    .descricao(vinda.getDescricao())
                    .concluida(Boolean.TRUE.equals(vinda.getConcluida()))
                    .projeto(vinda.getProjeto() == null ? null : projetos.get(chave(vinda.getProjeto())))
                    .etiquetas(daTarefa)
                    .prazo(vinda.getPrazo())
                    .prioridade(prioridadeOu(vinda.getPrioridade()))
                    .dataConclusao(vinda.getDataConclusao())
                    .ordem(ordem++)
                    .build();

            importarPassos(task, vinda.getSubtarefas());

            taskRepository.save(task);
            criadas++;
        }

        return criadas;
    }

    /**
     * Passos entram junto com a tarefa, pelo cascade. A ordem vem da posição
     * no arquivo: é a única que o arquivo carrega, e é a que quem exportou
     * estava vendo.
     */
    private void importarPassos(Task task, List<DadosExportados.SubtarefaExportada> vindos) {
        if (vindos == null) {
            return;
        }

        // O mesmo teto da API, e não o de 5000 dos outros tipos: um arquivo
        // não pode criar uma tarefa que a própria API recusaria montar.
        if (vindos.size() > Subtarefa.LIMITE_POR_TAREFA) {
            throw new IllegalArgumentException(
                    "A tarefa \"" + task.getTitulo() + "\" traz " + vindos.size()
                            + " passos; o máximo é " + Subtarefa.LIMITE_POR_TAREFA);
        }

        int ordem = 0;
        for (DadosExportados.SubtarefaExportada vindo : vindos) {
            if (vindo.getTitulo() == null || vindo.getTitulo().isBlank()) {
                continue;
            }

            task.getSubtarefas().add(Subtarefa.builder()
                    .task(task)
                    .titulo(vindo.getTitulo().trim())
                    .concluida(Boolean.TRUE.equals(vindo.getConcluida()))
                    .ordem(ordem++)
                    .build());
        }
    }

    private int[] importarHabitos(Long usuarioId, Usuario usuario,
                                  List<DadosExportados.HabitoExportado> vindos,
                                  List<String> reaproveitados) {
        verificarLimite(vindos, "hábitos");

        if (vindos == null) {
            return new int[]{0, 0};
        }

        Set<String> existentes = new HashSet<>();
        for (Habito habito : habitoRepository.findByUsuarioIdOrderByNomeAsc(usuarioId)) {
            existentes.add(chave(habito.getNome()));
        }

        int criados = 0;
        int registros = 0;

        for (DadosExportados.HabitoExportado vindo : vindos) {
            if (vindo.getNome() == null || vindo.getNome().isBlank()) {
                continue;
            }
            if (existentes.contains(chave(vindo.getNome()))) {
                reaproveitados.add("Hábito \"" + vindo.getNome() + "\"");
                continue;
            }

            List<Integer> dias = Optional.ofNullable(vindo.getDiasSemana()).orElse(List.of());
            Habito habito = habitoRepository.save(Habito.builder()
                    .usuario(usuario)
                    .nome(vindo.getNome().trim())
                    .cor(vindo.getCor() == null ? "verde" : vindo.getCor())
                    .diasSemana(dias.isEmpty() ? "1,2,3,4,5,6,7"
                            : dias.stream().distinct().sorted()
                                    .map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("1"))
                    .ativo(vindo.getAtivo() == null || vindo.getAtivo())
                    .build());
            existentes.add(chave(habito.getNome()));
            criados++;

            for (LocalDate dia : Optional.ofNullable(vindo.getRegistros()).orElse(List.of())) {
                registroRepository.save(
                        HabitoRegistro.builder().habito(habito).data(dia).build());
                registros++;
            }
        }

        return new int[]{criados, registros};
    }

    private static Prioridade prioridadeOu(String valor) {
        if (valor == null) {
            return Prioridade.MEDIA;
        }
        try {
            return Prioridade.valueOf(valor);
        } catch (IllegalArgumentException e) {
            // Prioridade desconhecida não invalida a tarefa inteira.
            return Prioridade.MEDIA;
        }
    }

    /** Comparação por nome sem diferenciar maiúsculas, como no resto do sistema. */
    private static String chave(String nome) {
        return nome == null ? "" : nome.trim().toLowerCase();
    }
}
