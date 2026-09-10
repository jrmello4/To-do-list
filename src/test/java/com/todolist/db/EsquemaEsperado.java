package com.todolist.db;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * O que as migrations precisam produzir, num lugar só.
 *
 * Duas conferências leem estas listas: SchemaMigrationTest, contra o H2, e
 * MigrationsNoMySQLTest, contra um MySQL de verdade — que só roda onde há
 * Docker. Antes cada uma trazia a sua própria cópia das colunas, com um
 * comentário pedindo que quem mexesse numa lembrasse da outra.
 *
 * O pedido não funcionou: a coluna `ordem` entrou só na segunda e passou até o
 * CI. E o modo de errar é sistemático, não distração — a conferência que não
 * roda na máquina de quem desenvolve é justamente a que fica para trás. Com
 * uma lista só não há a segunda para esquecer.
 */
final class EsquemaEsperado {

    /** Colunas de `tasks`, na ordem em que foram sendo acrescentadas. */
    static final List<String> COLUNAS_DE_TASKS = List.of(
            "id", "titulo", "descricao", "concluida",
            "data_criacao", "data_atualizacao", "usuario_id",
            "projeto_id", "prazo", "prioridade", "data_conclusao", "ordem", "versao");

    static final List<String> TABELAS = List.of(
            "tasks", "usuarios", "projetos", "etiquetas", "task_etiquetas",
            "habitos", "habito_registros", "subtarefas");

    private static final Pattern VERSAO = Pattern.compile("^V(\\d+)__");

    /**
     * Versões que o Flyway precisa reportar como aplicadas.
     *
     * Lida dos próprios arquivos em db/migration, e não escrita à mão. Uma
     * lista fixa aqui teria o mesmo defeito das colunas duplicadas: quem
     * acrescentasse uma migration precisaria lembrar de um segundo lugar, e a
     * conferência passaria a verificar o que alguém digitou em vez do que
     * existe no diretório.
     */
    static List<String> versoesDeMigration() {
        try {
            Resource[] arquivos = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:db/migration/V*__*.sql");

            List<String> versoes = Arrays.stream(arquivos)
                    .map(Resource::getFilename)
                    .map(VERSAO::matcher)
                    .filter(Matcher::find)
                    .map(encontrado -> encontrado.group(1))
                    .toList();

            if (versoes.isEmpty()) {
                throw new IllegalStateException(
                        "Nenhuma migration encontrada em db/migration — a conferência "
                                + "passaria sem verificar coisa alguma.");
            }

            return versoes;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private EsquemaEsperado() {
    }
}
