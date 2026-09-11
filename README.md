# LifeHub — Plataforma Fullstack (To-do List & Vida Pessoal)

Aplicação **Java 17 + Spring Boot 3.4.1** com interface web integrada, evoluída de um To-do List (Kanban, Pomodoro, tags, anexos, PDF) para um **LifeHub**: finanças, hábitos, notas, metas, calendário unificado e radar multi-esportes. Também roda como **app desktop** (Electron + servidor local com login automático).

---

## Recursos

### Produtividade
- Lista, **Kanban** (drag & drop) e lixeira (soft delete)
- Prioridades, prazos, categorias, subtarefas, tags, anexos
- Pomodoro, tarefas recorrentes, export CSV/PDF
- Dark mode, notificações, modo convidado, PWA (`manifest.json` + Service Worker)

### LifeHub
- **Finanças:** contas, categorias, transações (avista/parcelado), saldo impactado em pagamento
- **Hábitos:** registro diário e streak
- **Notas rápidas:** fixar, cor
- **Metas:** aportes e % de conclusão
- **Calendário unificado:** tarefas, eventos e agenda esportiva
- **Esportes:** catálogo real via **TheSportsDB** (free key) com fallback offline

### Segurança & qualidade
- JWT stateless + BCrypt, **`JWT_SECRET` obrigatório** (mín. 32 bytes)
- Rate limit em login/cadastro (Bucket4j)
- Isolamento por usuário em todas as entidades
- CORS configurável (`CORS_ALLOWED_ORIGINS`)
- Flyway **em todos os ambientes** (dev H2 e prod MySQL) + índices (V10)
- GETs sem efeitos colaterais (sem seed silencioso)

---

## Como executar

### Desktop (app local, sem login) — recomendado para uso pessoal
O app roda como um programa desktop: uma janela nativa (Electron) que inicia o servidor Spring Boot **apenas em 127.0.0.1:8791**, com banco **persistente** em `~/.lifehub/` e **login automático** (não há tela de autenticação).

```powershell
# 1) Gere o JAR do servidor
.\mvnw.cmd package -DskipTests

# 2) Rode a janela desktop
cd desktop
npm install        # só na primeira vez
npm start
```

- Perfil usado: `desktop` (`application-desktop.yml`) — H2 em arquivo, sem Swagger/H2 console, CORS só para localhost.
- O endpoint `POST /api/auth/desktop` existe **apenas** nesse perfil e emite o token do proprietário local automaticamente.
- Para gerar um instalador Windows: `npm run dist` (produz `output/desktop`).

Requisitos: Java 17+ (no PATH ou `JAVA_HOME`); Node/npm apenas para empacotar/executar o Electron.

### Dev (H2 em memória)
```powershell
.\mvnw.cmd spring-boot:run
```
- UI: http://localhost:8080  
- Swagger: http://localhost:8080/swagger-ui.html  
- H2 console: http://localhost:8080/h2-console (`jdbc:h2:mem:todolist`)

O perfil `dev` já traz `jwt.secret` local e Flyway habilitado.

### Prod (MySQL + Docker)
```bash
docker compose up -d
```

```powershell
$env:JWT_SECRET = "<chave-com-32-bytes-ou-mais>"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "<senha>"
$env:CORS_ALLOWED_ORIGINS = "https://seu-dominio.com"
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod
```

Variáveis relevantes:

| Variável | Obrigatória em prod | Descrição |
|----------|:-------------------:|-----------|
| `JWT_SECRET` | Sim | Assinatura dos tokens (mín. 32 bytes) |
| `DB_USERNAME` / `DB_PASSWORD` | Sim | Credenciais MySQL (sem default `root`) |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | Não | Defaults: localhost / 3306 / todolist |
| `CORS_ALLOWED_ORIGINS` | Não | Origins permitidas, separadas por vírgula |
| `SPORTS_API_KEY` | Não | Chave TheSportsDB (default free `123`) |
| `SPORTS_API_BASE_URL` | Não | Base da API de esportes |

### Testes
```powershell
.\mvnw.cmd test
```

### JAR
```powershell
.\mvnw.cmd package -DskipTests
java -jar target/to-do-list-1.0.0.jar
```

---

## API (resumo)

### Auth (`/api/auth`)
| Método | Rota | Auth |
|--------|------|:----:|
| POST | `/api/auth/cadastro` | Não |
| POST | `/api/auth/login` | Não |
| POST | `/api/auth/convidado` | Não |
| POST | `/api/auth/desktop` | Não (apenas perfil `desktop`) |
| GET | `/api/auth/me` | Sim |

### Módulos LifeHub (todos exigem Bearer)
| Prefixo | Domínio |
|---------|---------|
| `/api/tarefas` | Tarefas, Kanban, Pomodoro, lixeira, export |
| `/api/tags`, `/api/tarefas/{id}/anexos` | Tags e anexos |
| `/api/contas`, `/api/categorias-transacoes`, `/api/transacoes` | Finanças |
| `/api/habitos` | Hábitos e streak |
| `/api/notas` | Notas rápidas |
| `/api/metas` | Metas e aportes |
| `/api/calendario` | Agenda unificada |
| `/api/esportes` | Preferências + eventos (TheSportsDB) |
| `/api/dashboard` | Resumo consolidado |

Exemplos de esportes:
- `GET /api/esportes/eventos?esporte=FUTEBOL&busca=Flamengo`
- `POST /api/esportes/eventos/{id}/salvar-calendario` (IDs no formato `tsdb-<idEvent>`)

---

## Hub de esportes (TheSportsDB)

- Integração com a API v1 pública (`eventsday`, `eventsnextleague`, `lookupevent`)
- Cache em memória (`esportes`) para respeitar o rate limit free (~30 req/min)
- Limites free retornam poucos itens por endpoint; **Premium** amplia o volume
- Se a API falhar, a UI recebe um **catálogo de demonstração** (IDs `DEMO-*`)

Configuração em `application.yml` → `app.esportes` (ligas e esportes do dia).

---

## Migrations Flyway

| Versão | Conteúdo |
|--------|----------|
| V1–V5 | Tasks, usuários, subtarefas, pomodoro, tags/anexos |
| V6–V9 | Finanças, hábitos/notas, metas/calendário, preferências esportivas |
| V10 | Índices de consulta (user_id, datas, status) |
| V11–V12 | Limite mensal por categoria, alertas esportivos e auto-aporte de metas |
| V13 | Flag de recorrência já gerada (anti-duplicação) e flag de transferência (exclusão de relatórios) |

Dev e prod usam o mesmo caminho de migrations (`ddl-auto: validate`).

---

## Stack

Java 17 · Spring Boot 3.4.1 · Spring Security + JJWT · Bucket4j · Flyway · MySQL/H2 · OpenPDF · springdoc · Chart.js (frontend) · TheSportsDB
