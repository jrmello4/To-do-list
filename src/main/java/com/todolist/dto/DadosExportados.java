package com.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Tudo o que a conta tem, num arquivo.
 *
 * Projetos e etiquetas são referenciados **por nome**, não por id. Ids só
 * fazem sentido dentro do banco de origem; por nome o arquivo pode ser
 * importado noutra instalação ou noutra conta — que é o ponto de existir uma
 * exportação.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "DadosExportados", description = "Exportação completa da conta")
public class DadosExportados {

    /** Muda quando o formato deixar de ser compatível com esta versão. */
    public static final String VERSAO_ATUAL = "1";

    @Schema(description = "Versão do formato", example = "1")
    @Builder.Default
    private String versao = VERSAO_ATUAL;

    @Schema(description = "Quando a exportação foi gerada")
    private LocalDateTime exportadoEm;

    @Schema(description = "Conta de origem. Ignorado na importação.")
    private UsuarioResponse conta;

    private List<ProjetoExportado> projetos;
    private List<EtiquetaExportada> etiquetas;
    private List<TarefaExportada> tarefas;
    private List<HabitoExportado> habitos;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(name = "ProjetoExportado")
    public static class ProjetoExportado {
        private String nome;
        private String cor;
        private Boolean arquivado;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(name = "EtiquetaExportada")
    public static class EtiquetaExportada {
        private String nome;
        private String cor;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(name = "TarefaExportada")
    public static class TarefaExportada {
        private String titulo;
        private String descricao;
        private Boolean concluida;

        @Schema(description = "Nome do projeto, ou nulo para a caixa de entrada")
        private String projeto;

        @Schema(description = "Nomes das etiquetas")
        private List<String> etiquetas;

        private LocalDate prazo;
        private String prioridade;
        private LocalDateTime dataCriacao;
        private LocalDateTime dataConclusao;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    @Schema(name = "HabitoExportado")
    public static class HabitoExportado {
        private String nome;
        private String cor;
        private List<Integer> diasSemana;
        private Boolean ativo;

        @Schema(description = "Dias em que o hábito foi cumprido")
        private List<LocalDate> registros;
    }
}
