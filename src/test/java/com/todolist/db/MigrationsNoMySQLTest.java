package com.todolist.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * As migrations contra o MySQL de verdade.
 *
 * Os outros testes usam H2 em MODE=MySQL, que aceita a sintaxe mas não é MySQL:
 * tipos, colação e detalhes de DDL divergem. Só aqui se confirma que
 * AUTO_INCREMENT, TEXT, DATETIME e o ALTER ... MODIFY da V3 funcionam onde a
 * aplicação de fato roda.
 *
 * disabledWithoutDocker = true faz a classe ser pulada onde não há Docker, em
 * vez de quebrar o build de quem roda os testes na própria máquina. No CI,
 * onde há Docker, ela executa.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers(disabledWithoutDocker = true)
@DisplayName("Migrations contra MySQL real")
class MigrationsNoMySQLTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0");

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Test
    @DisplayName("aplicam-se em ordem e sem falha")
    void aplicamSeNoMySQL() {
        MigrationInfo[] aplicadas = flyway.info().applied();

        assertThat(aplicadas).isNotEmpty();
        assertThat(aplicadas)
                .allSatisfy(migration -> assertThat(migration.getState().isFailed()).isFalse());
        assertThat(aplicadas)
                .extracting(migration -> String.valueOf(migration.getVersion()))
                .contains("1", "2", "3", "4", "5", "6", "7", "8");
    }

    @Test
    @DisplayName("produzem as tabelas e a chave estrangeira de dono")
    void produzemOSchemaEsperado() throws Exception {
        Set<String> tabelas = new LinkedHashSet<>();
        Set<String> colunasDeTasks = new LinkedHashSet<>();
        Set<String> chavesEstrangeiras = new LinkedHashSet<>();

        try (Connection conexao = dataSource.getConnection()) {
            try (ResultSet resultado = conexao.getMetaData()
                    .getTables(conexao.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (resultado.next()) {
                    tabelas.add(resultado.getString("TABLE_NAME").toLowerCase());
                }
            }

            try (ResultSet resultado = conexao.getMetaData()
                    .getColumns(conexao.getCatalog(), null, "tasks", "%")) {
                while (resultado.next()) {
                    colunasDeTasks.add(resultado.getString("COLUMN_NAME").toLowerCase());
                }
            }

            try (ResultSet resultado = conexao.getMetaData()
                    .getImportedKeys(conexao.getCatalog(), null, "tasks")) {
                while (resultado.next()) {
                    chavesEstrangeiras.add(resultado.getString("FKCOLUMN_NAME").toLowerCase()
                            + " -> " + resultado.getString("PKTABLE_NAME").toLowerCase());
                }
            }
        }

        assertThat(tabelas).contains("tasks", "usuarios", "projetos", "etiquetas", "task_etiquetas",
                "habitos", "habito_registros");
        assertThat(colunasDeTasks).containsExactlyInAnyOrder(
                "id", "titulo", "descricao", "concluida",
                "data_criacao", "data_atualizacao", "usuario_id",
                "projeto_id", "prazo", "prioridade", "data_conclusao");
        assertThat(chavesEstrangeiras).contains("usuario_id -> usuarios");
    }
}
