# To-do List API

API REST para gerenciamento de tarefas desenvolvida com Java 17, Spring Boot 3.4.1, MySQL e Flyway.

## Tecnologias

- **Java 17**
- **Spring Boot 3.4.1** (Web, Data JPA, Validation)
- **MySQL 8** (produção) / **H2** (testes)
- **Flyway** (migração de banco)
- **SpringDoc OpenAPI / Swagger** (documentação)
- **JUnit 5 + Mockito** (testes)
- **Maven 3.9.9**

## Pré-requisitos

- **Java 17+** instalado
- **MySQL 8** instalado e rodando
- **Maven 3.8+** (ou use o Maven Wrapper incluso na pasta `maven/`)

## Configuração do Banco

1. Acesse o MySQL e crie o banco de dados:

```sql
CREATE DATABASE todolist;
```

2. As credenciais padrão no `application.yml` são:

```yml
spring:
  datasource:
    username: root
    password: root
```

Altere em `src/main/resources/application.yml` conforme sua instalação.

## Como Executar

### Com Maven local

```bash
# Na raiz do projeto
mvn spring-boot:run
```

### Com o Maven empacotado (sem Maven global)

```bash
# Na raiz do projeto
./maven/bin/mvn spring-boot:run
```

### Gerar JAR e executar

```bash
./maven/bin/mvn package -DskipTests
java -jar target/to-do-list-1.0.0.jar
```

A aplicação iniciará em `http://localhost:8080`.

## Endpoints da API

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/tarefas` | Criar uma nova tarefa |
| `GET` | `/api/tarefas` | Listar todas as tarefas |
| `GET` | `/api/tarefas/{id}` | Buscar tarefa por ID |
| `PUT` | `/api/tarefas/{id}` | Atualizar uma tarefa |
| `DELETE` | `/api/tarefas/{id}` | Deletar uma tarefa |

### Exemplos de Requisição

**Criar tarefa** (`POST /api/tarefas`):

```json
{
  "titulo": "Estudar Spring Boot",
  "descricao": "Aprofundar em JPA e Flyway",
  "concluida": false
}
```

**Atualizar tarefa** (`PUT /api/tarefas/1`):

```json
{
  "titulo": "Estudar Spring Boot Avançado",
  "descricao": "Segurança com Spring Security",
  "concluida": true
}
```

### Respostas

**201 Created** (POST):

```json
{
  "id": 1,
  "titulo": "Estudar Spring Boot",
  "descricao": "Aprofundar em JPA e Flyway",
  "concluida": false,
  "dataCriacao": "2026-06-26T10:00:00",
  "dataAtualizacao": "2026-06-26T10:00:00"
}
```

**400 Bad Request** (validação):

```json
{
  "status": 400,
  "mensagem": "Erro de validação",
  "timestamp": "2026-06-26T10:00:00",
  "erros": ["titulo: O título é obrigatório"]
}
```

**404 Not Found**:

```json
{
  "status": 404,
  "mensagem": "Tarefa com id 99 não encontrado(a)",
  "timestamp": "2026-06-26T10:00:00",
  "erros": []
}
```

## Documentação Swagger

Com a aplicação rodando, acesse:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

## Executar Testes

```bash
./maven/bin/mvn test
```

Os testes usam banco H2 em memória (não é necessário MySQL rodando). O projeto possui **20 testes** cobrindo:

- **TaskService:** criação, listagem, busca por ID, atualização e exclusão
- **TaskController:** validação de status HTTP, corpo de resposta e tratamento de erros

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/todolist/
│   │   ├── config/          # Configurações (Swagger/OpenAPI)
│   │   ├── controller/      # Endpoints REST
│   │   ├── dto/             # Objetos de requisição/resposta
│   │   ├── entity/          # Entidade JPA
│   │   ├── exception/       # Tratamento global de erros
│   │   ├── repository/      # Camada de dados
│   │   └── service/         # Lógica de negócio
│   └── resources/
│       ├── db/migration/    # Migrações Flyway
│       └── application.yml  # Configurações da aplicação
└── test/
    └── java/com/todolist/
        ├── controller/      # Testes do controller (MockMvc)
        └── service/         # Testes do service (Mockito)
```
