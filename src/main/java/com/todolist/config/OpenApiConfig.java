package com.todolist.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String DESCRICAO = """
            API REST para gerenciamento de tarefas construída com **Spring Boot 3.4.1**, \
            **MySQL** e **Flyway**.

            ### Recursos
            Cada tarefa possui título, descrição opcional, situação de conclusão e datas de \
            criação e atualização mantidas automaticamente pela aplicação.

            ### Tratamento de erros
            Todas as falhas retornam o mesmo formato (`ErrorResponse`), com o código HTTP, \
            uma mensagem legível, o momento do erro e a lista de validações que falharam:

            | Código | Quando acontece |
            |--------|-----------------|
            | `400`  | Corpo inválido — título vazio ou acima dos limites de tamanho |
            | `404`  | Nenhuma tarefa encontrada para o `id` informado |
            | `500`  | Erro inesperado no servidor |

            ### Interface web
            Uma interface pronta para uso está disponível na raiz da aplicação.
            """;

    @Value("${server.port:8080}")
    private int porta;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("To-do List API")
                        .description(DESCRICAO)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("jrmello4")
                                .url("https://github.com/jrmello4/to-do-list")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + porta)
                                .description("Ambiente local")));
    }
}
