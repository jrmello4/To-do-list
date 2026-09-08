# To-do List - Aplicação Fullstack, Quadro Kanban & API REST

Aplicação completa para gerenciamento de tarefas desenvolvida com **Java 17**, **Spring Boot 3.4.1**, **Flyway**, **MySQL / H2** e uma **Interface Web Moderna e Responsiva** com suporte a **Quadro Kanban (Drag & Drop)**, **Prioridades**, **Prazos**, **Subtarefas** e **Lixeira com Soft Delete**.

---

## Recursos & Funcionalidades

### 🖥️ Frontend Web Integrado (`/`)
- **3 Modos de Visualização:**
  - 📋 **Lista de Tarefas:** visão detalhada com filtros dinâmicos de status, categoria e prioridade, além de busca instantânea.
  - 📊 **Quadro Kanban:** 3 colunas (*A Fazer*, *Em Andamento*, *Concluída*) com **Drag & Drop** nativo para mover cartões entre fases do fluxo de trabalho.
  - 🗑️ **Lixeira (Soft Delete):** visualização de itens excluídos com opções de restauração individual e esvaziamento permanente.
- **Gestão de Prazos e Alertas:**
  - Prazos com cálculo dinâmico de status: *Atrasada* (com badge piscante em vermelho), *Vence Hoje* ou data formatada.
- **Prioridades Coloridas & Categorias:**
  - Prioridades: 🔴 Urgente, 🟠 Alta, 🟡 Média e 🟢 Baixa.
  - Categorias: 💼 Trabalho, 🎓 Estudos, 🏠 Pessoal, 💰 Finanças, 🏃 Saúde e 📁 Geral.
- **Subtarefas (Checklist Aninhada):**
  - Criação rápida de subtarefas dentro de cada card.
  - Alternância de conclusão de cada subtarefa com barra de progresso individual (*ex: 2/3 concluídas*).
- **Exportação para CSV:**
  - Botão no cabeçalho para download instantâneo de todas as tarefas ativas em formato CSV formatado para Excel ou Google Sheets.
- **Dark Mode / Light Mode:**
  - Alternância suave de tema escuro e claro com persistência em `localStorage`.
- **🍅 Modo Foco & Timer Pomodoro:**
  - Cronômetro Pomodoro interativo (25m foco, 5m pausa curta, 15m pausa longa) com sintetizador sonoro Web Audio API (sem dependências externas de áudio).
  - Vínculo direto de foco por tarefa ("🍅 Focar") e rastreamento de ciclos realizados vs estimados (`🍅 2/4`).
- **📊 Painel de Gráficos (Chart.js):**
  - Gráfico de Rosca por Categorias.
  - Gráfico de Barras por Prioridade.
  - Gráfico de Produtividade Semanal (Criadas vs Concluídas nos últimos 7 dias).
  - Indicadores de Taxa de Entrega no Prazo (%), Total de Pomodoros e Tarefas Recorrentes.
- **🔁 Tarefas Recorrentes Automatizadas:**
  - Frequências: `DIARIA`, `SEMANAL` e `MENSAL`.
  - Ao concluir a tarefa, o sistema clona e gera a próxima ocorrência agendada automaticamente com o prazo atualizado (+1 dia, +7 dias ou +1 mês).
- **🏷️ Sistema de Tags / Etiquetas Personalizadas:**
  - Criação de tags com cores hex personalizadas e paleta de atalhos rápidos.
  - Associação multi-tag por tarefa e filtro dinâmico de busca.
  - Cálculo de contraste luminoso dinâmico para garantir acessibilidade visual.
- **📎 Anexos de Arquivos & Upload de Imagens:**
  - Suporte a envio de imagens, documentos e PDFs de até 15MB via multipart.
  - Miniaturas (thumbnails) automáticas e download autenticado seguro.
- **📄 Relatórios Executivos em PDF (OpenPDF):**
  - Geração no backend de relatórios A4 corporativos com KPIs executivos e listagem operacional zebrada de tarefas.
- **🔔 Central de Notificações & Alertas em Tempo Real:**
  - Sino de alertas no cabeçalho com badge pulsante para tarefas atrasadas, vencendo hoje ou urgentes.
  - Integração com a API de notificações nativas da área de trabalho no navegador.
- **👤 Modo Convidado (Acesso Imediato para Testes):**
  - Login em 1 clique sem preenchimento de formulário, com dados e métricas pré-populadas para teste instantâneo.

### 🔐 Segurança, Rate Limiting & Performance
- **Spring Security 6 + JJWT 0.12.6:** arquitetura stateless moderna com senhas hasheadas via `BCryptPasswordEncoder`.
- **Proteção contra Força Bruta (Rate Limiting com Bucket4j):** limita tentativas em `/api/auth/login` e `/api/auth/cadastro` a 10 requisições por minuto por IP, retornando HTTP 429 Too Many Requests.
- **Spring Cache:** aceleração em memória para estatísticas e resumos com invalidação sob demanda (`@CacheEvict`).
- **Isolamento de Dados por Usuário:** cada usuário possui seu próprio quadro Kanban, lista de tarefas, subtarefas, lixeira e métricas de resumo. Um usuário nunca vê ou acessa tarefas de outro.
- **Rotas Públicas e Protegidas:** `/`, arquivos estáticos, `/swagger-ui/**` e `/api/auth/**` são públicos; todos os endpoints de tarefas (`/api/tarefas/**`) exigem cabeçalho `Authorization: Bearer <token>`.

---

### 🚀 API REST Completa

#### 🔑 Autenticação (`/api/auth`)

| Método | Rota | Descrição | Requer Auth |
|--------|------|-----------|:-----------:|
| `POST` | `/api/auth/cadastro` | Cadastra novo usuário (Rate limited: máx 10 req/min) | Não |
| `POST` | `/api/auth/login` | Autentica usuário e retorna token JWT (Rate limited: máx 10 req/min) | Não |
| `POST` | `/api/auth/convidado` | Acessa como convidado imediato com dados de demonstração | Não |
| `GET` | `/api/auth/me` | Retorna os dados do perfil do usuário autenticado | Sim (Bearer) |

#### 📝 Tarefas & Subtarefas (`/api/tarefas`)

| Método | Rota | Descrição | Requer Auth |
|--------|------|-----------|:-----------:|
| `POST` | `/api/tarefas` | Cria nova tarefa (com prioridade, prazo, categoria, recorrência, pomodoros e tags) | Sim (Bearer) |
| `GET` | `/api/tarefas` | Lista tarefas do usuário com filtros (`concluida`, `status`, `prioridade`, `categoria`, `tagId`, `busca`) | Sim (Bearer) |
| `GET` | `/api/tarefas/resumo` | Retorna métricas do usuário (com cache) | Sim (Bearer) |
| `GET` | `/api/tarefas/estatisticas` | Retorna estatísticas avançadas para gráficos Chart.js (com cache) | Sim (Bearer) |
| `GET` | `/api/tarefas/notificacoes` | Retorna alertas e tarefas em atraso ou vencendo em breve | Sim (Bearer) |
| `GET` | `/api/tarefas/export/csv` | Download do arquivo CSV com todas as tarefas ativas do usuário | Sim (Bearer) |
| `GET` | `/api/tarefas/export/pdf` | Download de Relatório Executivo em PDF formatado (OpenPDF) | Sim (Bearer) |
| `GET` | `/api/tarefas/{id}` | Busca detalhes da tarefa por ID | Sim (Bearer) |
| `PUT` | `/api/tarefas/{id}` | Atualiza tarefa, status, recorrência, estimativas e tags | Sim (Bearer) |
| `PATCH` | `/api/tarefas/{id}/toggle` | Alterna conclusão (se recorrente, gera automaticamente a próxima) | Sim (Bearer) |
| `PATCH` | `/api/tarefas/{id}/status-kanban` | Altera status Kanban (`A_FAZER`, `EM_ANDAMENTO`, `CONCLUIDA`) | Sim (Bearer) |
| `PATCH` | `/api/tarefas/{id}/pomodoro/increment` | Incrementa a contagem de ciclos pomodoro realizados | Sim (Bearer) |
| `POST` | `/api/tarefas/{id}/subtarefas` | Adiciona uma nova subtarefa | Sim (Bearer) |
| `PATCH` | `/api/tarefas/{id}/subtarefas/{subId}/toggle` | Alterna status de uma subtarefa | Sim (Bearer) |
| `DELETE` | `/api/tarefas/{id}/subtarefas/{subId}` | Remove uma subtarefa | Sim (Bearer) |
| `DELETE` | `/api/tarefas/{id}` | Move a tarefa para a lixeira (*Soft Delete*) | Sim (Bearer) |
| `GET` | `/api/tarefas/lixeira` | Lista todas as tarefas do usuário presentes na lixeira | Sim (Bearer) |
| `PATCH` | `/api/tarefas/{id}/restaurar` | Restaura uma tarefa da lixeira | Sim (Bearer) |
| `DELETE` | `/api/tarefas/{id}/definitivo` | Remove permanentemente uma tarefa do banco | Sim (Bearer) |
| `DELETE` | `/api/tarefas/lixeira/esvaziar` | Esvazia toda a lixeira do usuário | Sim (Bearer) |

#### 🏷️ Tags & Etiquetas Personalizadas (`/api/tags`)

| Método | Rota | Descrição | Requer Auth |
|--------|------|-----------|:-----------:|
| `GET` | `/api/tags` | Lista todas as tags personalizadas do usuário | Sim (Bearer) |
| `POST` | `/api/tags` | Cria uma nova tag com cor hex customizada | Sim (Bearer) |
| `DELETE` | `/api/tags/{id}` | Remove uma tag do usuário | Sim (Bearer) |

#### 📎 Anexos de Tarefas (`/api/tarefas/{id}/anexos`)

| Método | Rota | Descrição | Requer Auth |
|--------|------|-----------|:-----------:|
| `GET` | `/api/tarefas/{id}/anexos` | Lista todos os anexos vinculados a uma tarefa | Sim (Bearer) |
| `POST` | `/api/tarefas/{id}/anexos` | Upload multipart de arquivo ou imagem (até 15MB) | Sim (Bearer) |
| `GET` | `/api/tarefas/{id}/anexos/{anexoId}` | Download / visualização segura do anexo | Sim (Bearer) |
| `DELETE` | `/api/tarefas/{id}/anexos/{anexoId}` | Remove o anexo física e logicamente | Sim (Bearer) |

---

## Como Executar

### 1. Execução Rápida (H2 Database em Memória)

A aplicação já vem configurada com perfil `dev` por padrão.

**No Windows:**
```powershell
.\mvnw.cmd spring-boot:run
```

**No Linux / macOS:**
```bash
./mvnw spring-boot:run
```

Acesse:
- **Interface Web:** [http://localhost:8080](http://localhost:8080)
- **Documentação Swagger:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **H2 Console:** [http://localhost:8080/h2-console](http://localhost:8080/h2-console) (JDBC URL: `jdbc:h2:mem:todolist`)

---

### 2. Execução com MySQL 8 (Docker Compose)

1. Inicie o banco MySQL e o Adminer:
```bash
docker compose up -d
```

2. Execute no perfil de produção:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

O Adminer estará acessível em `http://localhost:8081`.

---

## Testes Automatizados

Para executar os testes unitários e de integração:

```powershell
.\mvnw.cmd test
```

---

## Gerar Pacote JAR

```powershell
.\mvnw.cmd package -DskipTests
java -jar target/to-do-list-1.0.0.jar
```
