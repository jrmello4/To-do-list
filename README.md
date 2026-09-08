<div align="center">

# To-do List

**API REST de gerenciamento de tarefas com interface web integrada.**

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?style=flat-square&logo=swagger&logoColor=black)](https://swagger.io/)
[![Testes](https://img.shields.io/badge/testes-43-success?style=flat-square)](#executar-testes)
[![Segurança](https://img.shields.io/badge/auth-JWT-000000?style=flat-square&logo=jsonwebtokens&logoColor=white)](#autenticação)
[![Docker](https://img.shields.io/badge/Docker-compose-2496ED?style=flat-square&logo=docker&logoColor=white)](#subir-com-docker)

<img src="docs/screenshot.png" alt="Interface web da To-do List" width="640">

</div>

---

## Sobre

Aplicação Spring Boot que expõe um CRUD de tarefas por uma API REST documentada com OpenAPI e
acompanha uma **interface web pronta para uso**, servida pela própria aplicação em
`http://localhost:8080` — sem build de frontend, sem dependência externa.

| | |
|---|---|
| **Contas** | Cadastro e login com JWT; cada conta enxerga apenas as próprias tarefas |
| **Interface web** | Criar, concluir, editar, excluir e filtrar tarefas, com anel de progresso e resumo do dia |
| **Personalização** | Saudação com o seu nome, seis cores de destaque e tema claro/escuro/sistema |
| **API REST** | Cinco endpoints em `/api/tarefas`, com validação de entrada e erros padronizados |
| **Documentação** | Swagger UI com exemplos, descrições de campo e schemas de erro |
| **Banco** | MySQL em produção e H2 em memória nos testes, com schema versionado pelo Flyway |

## Tecnologias

- **Java 17**
- **Spring Boot 3.4.1** (Web, Data JPA, Validation)
- **MySQL 8** (produção) / **H2** (testes)
- **Flyway** (migração de banco)
- **SpringDoc OpenAPI / Swagger** (documentação)
- **JUnit 5 + Mockito** (testes)
- **Spring Security + JWT** (jjwt)
- **Testcontainers** (testes contra MySQL real)
- **Docker / Docker Compose**
- **Maven 3.9.9**
- **HTML, CSS e JavaScript puro** (interface web, sem framework nem build)

## Subir com Docker

O caminho mais curto: sobe aplicação e banco juntos, sem instalar Java nem MySQL.

```bash
cp .env.example .env    # ajuste DB_PASSWORD e JWT_SECRET
docker compose up --build
```

A aplicação fica em `http://localhost:8080` e os dados do MySQL persistem no
volume `dados-mysql`. A aplicação só inicia depois que o banco responde ao
*healthcheck*, então o Flyway nunca tenta migrar um banco ainda subindo.

## Autenticação

<div align="center">
<img src="docs/entrada.png" alt="Tela de entrada" width="420">
</div>

Todas as rotas de `/api/tarefas` exigem um token JWT.

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/auth/registrar` | Cria a conta e já devolve o token |
| `POST` | `/api/auth/login` | Autentica e devolve o token |
| `GET` | `/api/auth/eu` | Perfil da conta autenticada |

```bash
# cadastrar e guardar o token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/registrar \
  -H 'Content-Type: application/json' \
  -d '{"nome":"Ana","email":"ana@exemplo.com","senha":"senha-segura"}' \
  | python3 -c 'import json,sys; print(json.load(sys.stdin)["token"])')

curl -s http://localhost:8080/api/tarefas -H "Authorization: Bearer $TOKEN"
```

No Swagger, clique em **Authorize** e cole apenas o token.

### Isolamento entre contas

Nenhuma consulta busca tarefa só por `id`: `TaskRepository` expõe
`findByIdAndUsuarioId` e `findByUsuarioIdOrderByIdAsc`, e o `TaskService` chega
a qualquer tarefa por um único caminho, que sempre recebe o dono. Pedir a tarefa
de outra conta devolve **404**, e não 403 — um 403 confirmaria que aquele `id`
existe.

O comportamento é coberto por testes: trocar `findByIdAndUsuarioId` por
`findById` derruba o build.

### Chave dos tokens

`JWT_SECRET` precisa ter no mínimo 32 caracteres. Se não for definida, a
aplicação **gera uma chave aleatória a cada inicialização** e registra um aviso,
em vez de cair num valor padrão embutido no código — que estaria neste
repositório e permitiria forjar tokens de qualquer instalação. O efeito prático
é que os tokens deixam de valer a cada reinício.

## Pré-requisitos

- **Java 17+** instalado
- **MySQL 8** instalado e rodando
- **Maven 3.8+** (ou use o Maven Wrapper incluso na pasta `maven/`)

## Configuração do Banco

1. Acesse o MySQL e crie o banco de dados:

```sql
CREATE DATABASE todolist;
```

2. A conexão é configurada por variáveis de ambiente, com valores padrão para
desenvolvimento local (`root`/`root` em `localhost:3306`). Para usar outras credenciais,
copie o modelo e ajuste:

```bash
cp .env.example .env
```

| Variável | Padrão |
|----------|--------|
| `DB_URL` | `jdbc:mysql://localhost:3306/todolist?...` |
| `DB_USERNAME` | `root` |
| `DB_PASSWORD` | `root` |
| `SERVER_PORT` | `8080` |

O `.env` é ignorado pelo Git. Nenhuma credencial precisa ser editada dentro do
`application.yml`, o que permite publicar a aplicação sem alterar o código.

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

Com a aplicação no ar:

| Endereço | O que é |
|----------|---------|
| http://localhost:8080 | Interface web |
| http://localhost:8080/swagger-ui.html | Swagger UI |
| http://localhost:8080/api-docs | Especificação OpenAPI (JSON) |

## Interface Web

Os arquivos ficam em `src/main/resources/static/` e são servidos automaticamente pelo Spring Boot:
não há passo de build, instalação de pacotes nem servidor separado. A página consome os mesmos
endpoints REST documentados abaixo e traz:

- Saudação que muda com o horário e o nome configurado, com a data do dia
- Anel de progresso e resumo contextual ("faltam 3 tarefas para zerar o dia")
- Formulário de criação com validação e contador de caracteres
- Lista com conclusão em um clique, edição em modal e exclusão com confirmação
- Filtros por situação, com indicador deslizante
- Mensagens de erro vindas da API exibidas na tela
- Layout responsivo e animações que respeitam `prefers-reduced-motion`

### Personalização

<div align="center">
<img src="docs/personalizar.png" alt="Painel de personalização" width="420">
</div>

O painel de personalização define **nome**, **cor de destaque** (seis opções) e **tema**
(claro, escuro ou seguindo o sistema). Toda a paleta da interface deriva de dois valores —
`--accent-h` e `--accent-s` — então trocar a cor repinta o hero, os botões, os filtros e os
estados de foco de uma vez só.

As preferências ficam em `localStorage`: valem só naquele navegador e nunca são enviadas à API.
São aplicadas por `js/prefs.js`, carregado de forma síncrona no `<head>` justamente para que o
tema esteja definido antes da primeira pintura — sem piscar a tela no tema errado.

O contraste do texto sobre o hero foi medido nas seis cores e nos dois temas: o pior caso fica
em **4,68:1**, acima do mínimo de 4,5:1 exigido pelo WCAG AA.

<div align="center">
<img src="docs/screenshot-dark.png" alt="Interface web no tema escuro" width="420">
</div>

## Endpoints da API

Todos exigem o cabeçalho `Authorization: Bearer <token>` e operam apenas sobre
as tarefas da conta autenticada.

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

**401 Unauthorized** (sem token ou token inválido):

```json
{
  "status": 401,
  "mensagem": "Autenticação necessária. Envie o cabeçalho Authorization: Bearer <token>.",
  "timestamp": "2026-06-26T10:00:00",
  "erros": []
}
```

**404 Not Found** (inexistente — ou de outra conta):

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

Cada campo dos DTOs traz descrição e exemplo, e as respostas de erro apontam para o schema
`ErrorResponse`, então dá para testar os endpoints direto pelo navegador.

## Executar Testes

```bash
./maven/bin/mvn test
```

O projeto possui **43 testes**. A maioria roda contra H2 em memória, sem
precisar de MySQL:

| Classe | Cobre |
|--------|-------|
| `TaskServiceTest` | Regras de negócio com mocks |
| `TaskControllerTest` | Status HTTP, corpo de resposta e as regras de segurança reais |
| `SchemaMigrationTest` | As migrations aplicam e produzem as colunas que as entidades esperam |
| `TaskRepositoryTest` | Persistência contra o schema criado pelo Flyway |
| `AutenticacaoIntegrationTest` | Cadastro, login, token e **isolamento entre contas** |
| `MigrationsNoMySQLTest` | As migrations contra **MySQL de verdade**, via Testcontainers |

`MigrationsNoMySQLTest` é pulada automaticamente onde não há Docker, e executa
no CI. As demais rodam sempre.

O perfil de teste usa `ddl-auto: validate` com Flyway ligado: o schema vem das
migrations e o Hibernate apenas confere as entidades contra ele. Uma migration
quebrada, ou um campo sem migration correspondente, derruba o build.

### Integração contínua

`.github/workflows/ci.yml` roda `mvn verify` e constrói a imagem Docker a cada
push e pull request.

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
│   │   ├── security/        # JWT, filtro e regras de acesso
│   │   └── service/         # Lógica de negócio
│   └── resources/
│       ├── db/migration/    # Migrações Flyway
│       ├── static/          # Interface web
│       │   ├── css/         #   estilos (tema e cor dinâmicos)
│       │   ├── js/          #   prefs.js (preferências) e app.js (cliente da API)
│       │   └── index.html
│       └── application.yml  # Configurações da aplicação
└── test/
    └── java/com/todolist/
        ├── controller/      # Testes do controller (MockMvc)
        ├── db/              # Migrations e persistência (Flyway, JPA, Testcontainers)
        ├── security/        # Autenticação e isolamento entre contas
        └── service/         # Testes do service (Mockito)
```
