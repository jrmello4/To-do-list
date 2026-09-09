<div align="center">

# To-do List

**API REST de gerenciamento de tarefas com interface web integrada.**

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?style=flat-square&logo=swagger&logoColor=black)](https://swagger.io/)
[![Testes](https://img.shields.io/badge/testes-121-success?style=flat-square)](#executar-testes)
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
| **Organização** | Projetos, etiquetas, prazos, prioridade e busca — filtrados no servidor |
| **Hábitos** | Rotina recorrente com sequências e grade dos últimos 14 dias |
| **Painel** | Série de conclusões, distribuição por projeto e prioridade, tempo médio |
| **Lembretes** | Resumo diário por e-mail, na hora local de cada conta |
| **Seus dados** | Exportar e importar tudo em JSON; instalável no celular |
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
| `PUT` | `/api/auth/preferencias` | Liga os lembretes e define hora e fuso |

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
| `SERVER_PORT` | `8080` (`PORT` tem precedência, para os serviços de hospedagem) |

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
- Abas de tarefas, hábitos e painel, com atalhos de teclado
- Projetos e etiquetas coloridos, com contagem de pendentes e painel para criar e excluir
- Etiquetas escolhidas por chips alternáveis, em vez de um select múltiplo
- Prazo com destaque para atrasadas e para as que vencem hoje, e prioridade
- Busca e filtros resolvidos no servidor, com "carregar mais" paginado
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

### Tarefas

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/tarefas` | Criar uma nova tarefa |
| `GET` | `/api/tarefas` | Listar tarefas — **paginado e filtrável** |
| `GET` | `/api/tarefas/resumo` | Contagens da conta |
| `GET` | `/api/tarefas/{id}` | Buscar tarefa por ID |
| `PUT` | `/api/tarefas/{id}` | Atualizar uma tarefa |
| `PATCH` | `/api/tarefas/{id}/conclusao` | Concluir ou reabrir |
| `DELETE` | `/api/tarefas/{id}` | Deletar uma tarefa |

### Projetos

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/projetos` | Listar, com a contagem de pendentes de cada um |
| `POST` | `/api/projetos` | Criar projeto |
| `GET` | `/api/projetos/{id}` | Buscar projeto por ID |
| `PUT` | `/api/projetos/{id}` | Atualizar projeto |
| `DELETE` | `/api/projetos/{id}` | Excluir — as tarefas voltam à caixa de entrada |

### Etiquetas

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/etiquetas` | Listar, com a contagem de pendentes de cada uma |
| `POST` | `/api/etiquetas` | Criar etiqueta |
| `PUT` | `/api/etiquetas/{id}` | Atualizar etiqueta |
| `DELETE` | `/api/etiquetas/{id}` | Excluir — as tarefas continuam, só a marcação some |

Etiquetas são transversais aos projetos: uma tarefa pertence a um projeto só,
mas pode ter várias etiquetas. Em `POST`/`PUT` de tarefa, `etiquetaIds`
**substitui** as etiquetas atuais — lista vazia remove todas, campo omitido
mantém as que já existem.

### Hábitos

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/habitos` | Listar, com sequência atual, recorde e grade de 14 dias |
| `POST` | `/api/habitos` | Criar hábito |
| `PUT` | `/api/habitos/{id}` | Atualizar hábito |
| `DELETE` | `/api/habitos/{id}` | Excluir — leva o histórico junto |
| `PUT` | `/api/habitos/{id}/registros/{data}` | Marcar o dia como cumprido |
| `DELETE` | `/api/habitos/{id}/registros/{data}` | Desmarcar o dia |

Tarefa e hábito são modelos diferentes de propósito: tarefa termina, hábito se
repete. A pergunta que interessa deixa de ser "está concluída?" e passa a ser
"em quantos dos últimos dias eu fiz?".

Cada hábito define em que dias da semana vale (`diasSemana`, padrão ISO). Sem
isso não dá para calcular sequência com honestidade: num hábito de segunda,
quarta e sexta, não fazer na terça não é falha.

A sequência é **derivada dos registros a cada consulta**, nunca de um contador
guardado — um contador dessincroniza no primeiro dia apagado ou marcado com
atraso, e não há como perceber. O dia de hoje é o único com tolerância: ainda
não acabou, então não tê-lo cumprido não quebra a sequência.

Marcar e desmarcar são idempotentes; a existência da linha em
`habito_registros` é o registro, e desmarcar apaga a linha.

### Painel

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/painel` | Números agregados da conta (`?dias=30&hoje=...`) |

Tudo agregado no banco, em vez de mandar as tarefas todas e somar no navegador
— o que funciona com 50 tarefas e derrete com 5 mil. A série de conclusões vem
contínua: dias sem conclusão aparecem com zero, para o gráfico não comprimir os
intervalos vazios.

**Sobre os gráficos.** A paleta de acento do app foi submetida ao validador de
daltonismo, e seis tons escolhidos pelo usuário não passam numa checagem de
todos os pares — sempre há um par que alguma forma de daltonismo colapsa. Como
mexer na paleta para agradar um gráfico seria a troca errada, o painel usa
formas em que **cor não é canal de identidade**: barras horizontais, uma por
linha, com nome e valor sempre visíveis. Não há legenda nem gráfico de pizza,
porque não existe cor para casar com nome.

As barras de distribuição são normalizadas pelo **total** (a barra é a fatia do
conjunto); as de sequência, pelo **máximo** (o que interessa é qual é a maior).
A série diária tem tooltip por barra e um botão que revela a tabela com os
números.

### Exportar e importar

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/dados/exportar` | Baixa projetos, etiquetas, tarefas e hábitos num JSON |
| `POST` | `/api/dados/importar` | Importa um arquivo exportado |

Projetos e etiquetas são referenciados **por nome**, não por id: ids só valem
dentro do banco de origem, e por nome o arquivo pode ser importado noutra
instalação ou noutra conta — que é o ponto de existir uma exportação.

A importação **soma** ao que já existe, em vez de substituir. Substituir
exigiria apagar tudo antes, e um arquivo errado levaria a conta inteira junto.
Nomes já existentes de projeto, etiqueta e hábito são reaproveitados e voltam
listados em `reaproveitados`; tarefas são sempre criadas, porque não há como
saber se uma de mesmo título é a mesma ou outra parecida.

### Lembretes por e-mail

| Método | Rota | Descrição |
|--------|------|-----------|
| `PUT` | `/api/auth/preferencias` | `lembretesAtivos`, `horaLembrete` (0–23) e `fusoHorario` |

Um resumo do que passou do prazo e do que vence hoje, uma vez por dia. Só sai
quando há algo a dizer — um e-mail diário avisando que não há nada vira ruído e
deixa de ser lido.

**O fuso é da conta, não do servidor.** O servidor roda em UTC; quem escolhe
"8 da manhã" quer as 8 do relógio da parede dele. A varredura roda de hora em
hora e compara a hora *local* de cada conta: às 11h UTC é 8h em São Paulo, e 8h
em Tóquio é 23h UTC do dia anterior. A interface envia o fuso do próprio
navegador (`Intl.DateTimeFormat().resolvedOptions().timeZone`), e um fuso
desconhecido é recusado com **400** na hora de salvar — gravado, ele viraria
uma conta que simplesmente nunca recebe.

O campo `ultimo_lembrete_em` guarda a **data local** já enviada, e é o que
impede o mesmo resumo sair duas vezes no mesmo dia. Uma conta é marcada mesmo
quando não havia nada a dizer; uma falha de envio **não** marca, para a próxima
varredura tentar de novo. Um endereço recusado não interrompe as contas
seguintes.

O envio fica **desligado por padrão**:

| Variável | Efeito |
|----------|--------|
| `LEMBRETES_ATIVOS=true` | Liga a varredura de hora em hora. Numa só instância — com duas, o resumo sai duas vezes |
| `SPRING_MAIL_HOST` | Sem ela, `EnviadorEmLog` registra o lembrete no log em vez de enviar |
| `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` | Credenciais do SMTP |
| `LEMBRETE_REMETENTE` | Endereço no campo *De* |

`spring.mail.host` não tem valor padrão no `application.yml` de propósito: uma
string vazia conta como definida para o `@ConditionalOnProperty` e ligaria o
envio real sem servidor nenhum atrás. `EscolhaDoEnviadorTest` cobre esse caso.

### Publicar

| Variável | Para quê |
|----------|----------|
| `PORT` | Nome que os serviços de hospedagem injetam; tem precedência sobre `SERVER_PORT` |
| `JWT_SECRET` | Obrigatória fora de desenvolvimento — sem ela os tokens caem a cada reinício |
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` | Banco |

`GET /actuator/health` responde **sem autenticação**, para o *health check* de
quem hospeda não ler a aplicação como fora do ar. É o único endpoint do
actuator exposto: `/actuator/env` listaria as variáveis de ambiente, senha de
banco inclusive.

### Instalável no celular

`manifest.webmanifest` e `sw.js` deixam a aplicação instalável e abrível sem
rede. O service worker usa **rede primeiro, cache como reserva**: cache primeiro
serviria a interface antiga depois de cada deploy, e uma interface velha
conversando com uma API nova quebra de formas difíceis de diagnosticar. Chamadas
de `/api/` nunca vêm do cache — uma lista de tarefas velha apresentada como
atual é pior do que um erro de rede honesto.

### Atalhos de teclado

| Tecla | Ação |
|-------|------|
| `n` | Nova tarefa |
| `/` | Buscar |
| `1` `2` `3` | Tarefas, hábitos, painel |
| `Esc` | Fechar o que estiver aberto |

Ignorados enquanto se digita num campo, com um modal aberto, ou em combinação
com Ctrl/Cmd/Alt — essas pertencem ao navegador.

### Filtros da listagem

Combináveis, resolvidos no banco:

| Parâmetro | Exemplo | O que faz |
|-----------|---------|-----------|
| `concluida` | `true` | Filtra por situação |
| `projeto` | `3` | Tarefas de um projeto |
| `etiqueta` | `2` | Tarefas com uma etiqueta |
| `semProjeto` | `true` | Apenas a caixa de entrada |
| `prioridade` | `ALTA` | `BAIXA`, `MEDIA`, `ALTA` ou `URGENTE` |
| `prazoAte` | `2026-09-30` | Vencem até a data (sem prazo fica de fora) |
| `busca` | `flyway` | Título e descrição, sem diferenciar maiúsculas |
| `page` / `size` / `sort` | `0` / `50` / `prazo,asc` | Paginação e ordenação |

```bash
curl -s "http://localhost:8080/api/tarefas?prioridade=ALTA&concluida=false&sort=prazo,asc" \
  -H "Authorization: Bearer $TOKEN"
```

**A listagem é paginada.** A resposta é um objeto com `content`, `totalElements` e
`totalPages` — não um array. É também por isso que existe `/api/tarefas/resumo`:
com a lista paginada, somar a página no cliente daria contagens erradas.

`prazo` é uma data (`AAAA-MM-DD`), sem hora, porque prazo é uma decisão sobre o
dia. O cálculo de "atrasada" usa a data enviada em `?hoje=`, e não a do
servidor — quem usa pode estar em outro fuso.

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

O projeto possui **121 testes**. A maioria roda contra H2 em memória, sem
precisar de MySQL:

| Classe | Cobre |
|--------|-------|
| `TaskServiceTest` | Regras de negócio com mocks |
| `TaskControllerTest` | Status HTTP, corpo de resposta e as regras de segurança reais |
| `SchemaMigrationTest` | As migrations aplicam e produzem as colunas que as entidades esperam |
| `TaskRepositoryTest` | Persistência contra o schema criado pelo Flyway |
| `AutenticacaoIntegrationTest` | Cadastro, login, token e **isolamento entre contas** |
| `PlanejamentoIntegrationTest` | Projetos, etiquetas, prazos, prioridade, filtros, paginação e resumo |
| `CalculoDeSequenciaTest` | Casos de borda das sequências de hábitos |
| `HabitosIntegrationTest` | Hábitos, registros, dias da semana e isolamento |
| `PainelIntegrationTest` | Agregações do painel, série contínua e isolamento |
| `DadosIntegrationTest` | Exportar, importar, viagem de ida e volta entre contas |
| `LembreteServiceTest` | Quando o lembrete sai: fuso de cada conta, uma vez por dia, falha isolada |
| `EscolhaDoEnviadorTest` | Sem SMTP configurado, o enviador ativo é o que só registra no log |
| `AgendadorDeLembretesTest` | A varredura só é agendada com `LEMBRETES_ATIVOS=true` |
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
│   │   ├── lembretes/       # Quando e como o resumo diário sai
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
        ├── lembretes/       # Varredura, fusos e escolha do enviador
        ├── security/        # Autenticação e isolamento entre contas
        └── service/         # Testes do service (Mockito)
```
