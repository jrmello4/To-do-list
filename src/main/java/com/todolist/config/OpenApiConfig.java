package com.todolist.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
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

            ### Autenticação
            Todas as rotas de tarefas exigem um token JWT. Obtenha um em             `POST /api/auth/registrar` ou `POST /api/auth/login`, clique em **Authorize**             aqui em cima e cole o token — cada conta enxerga apenas as próprias tarefas.

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
                                .description("Ambiente local")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Cole aqui apenas o token; o prefixo \"Bearer \" "
                                        + "é adicionado automaticamente.")))
                // Exigido por padrão em tudo. As rotas de cadastro e login se
                // isentam com @SecurityRequirements no próprio controller.
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
