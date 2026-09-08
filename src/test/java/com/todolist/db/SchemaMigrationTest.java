package com.todolist.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guarda as migrations do Flyway.
 *
 * Os demais testes não tocam no banco: TaskServiceTest usa mocks e
 * TaskControllerTest é uma fatia @WebMvcTest, que não carrega DataSource, JPA
 * nem Flyway. Sem esta classe o schema de produção não é exercitado por
 * nenhum teste, e uma migration quebrada passa despercebida.
 *
 * Por subir o contexto inteiro, este teste cobre dois erros de uma vez:
 * SQL inválido nas migrations (o Flyway falha) e divergência entre entidade e
 * schema (o ddl-auto: validate falha).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DisplayName("Migrations do Flyway")
class SchemaMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private Flyway flyway;

    @Test
    @DisplayName("aplicam-se com sucesso e ficam registradas no histórico")
    void aplicamSeComSucesso() {
        // Pela API do Flyway, e não por SQL na flyway_schema_history: o nome da
        // tabela muda de caixa conforme o banco, e o teste vale para os dois.
        MigrationInfo[] aplicadas = flyway.info().applied();

        assertThat(aplicadas)
                .as("nenhuma migration aplicada — o Flyway não rodou neste contexto")
                .isNotEmpty();

        assertThat(aplicadas)
                .allSatisfy(migration -> assertThat(migration.getState().isFailed())
                        .as("migration %s não pode estar em estado de falha", migration.getScript())
                        .isFalse());

        assertThat(aplicadas)
                .extracting(migration -> String.valueOf(migration.getVersion()))
                .as("todas as migrations de db/migration precisam constar como aplicadas")
                .contains("1", "2", "3", "4", "5", "6", "7", "8");
    }

    @Test
    @DisplayName("criam a tabela tasks com as colunas que a entidade Task espera")
    void criamAsColunasDeTasks() throws Exception {
        Set<String> colunas = new LinkedHashSet<>();

        try (Connection conexao = dataSource.getConnection();
             ResultSet resultado = conexao.getMetaData().getColumns(null, null, "%", "%")) {

            while (resultado.next()) {
                if ("tasks".equalsIgnoreCase(resultado.getString("TABLE_NAME"))) {
                    colunas.add(resultado.getString("COLUMN_NAME").toLowerCase());
                }
            }
        }

        assertThat(colunas).containsExactlyInAnyOrder(
                "id", "titulo", "descricao", "concluida",
                "data_criacao", "data_atualizacao", "usuario_id",
                "projeto_id", "prazo", "prioridade", "data_conclusao");
    }
}
