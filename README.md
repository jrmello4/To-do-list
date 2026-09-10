<div align="center">

# To-do List

**API REST de gerenciamento de tarefas com interface web integrada.**

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.1-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Flyway](https://img.shields.io/badge/Flyway-migrations-CC0200?style=flat-square&logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Swagger](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?style=flat-square&logo=swagger&logoColor=black)](https://swagger.io/)
[![Testes](https://img.shields.io/badge/testes-150-success?style=flat-square)](#executar-testes)
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
| **Passos** | Uma tarefa se abre em subtarefas, com progresso "2 de 5" |
| **Ordem própria** | A lista se arrasta na ordem que fizer sentido, com mouse, dedo ou teclado |
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
| `PUT` | `/api/auth/senha` | Troca a senha e derruba os tokens antigos |
| `POST` | `/api/auth/sair-de-todos` | Invalida todos os tokens da conta |
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

### Tentativas de senha

Errar a senha muitas vezes seguidas da mesma origem passa a devolver **429**,
com `Retry-After`. O BCrypt já encarece cada tentativa, mas não impede o
volume — e é justamente esse custo que torna o login um bom alvo: cada tentativa
errada gasta CPU do servidor, então o mesmo laço que procura a senha também tira
a aplicação do ar para todo mundo.

A contagem é **por origem**, e não por e-mail. Contar por e-mail é o primeiro
impulso e cria um problema maior: qualquer um trancaria a conta de qualquer
pessoa só errando a senha dela algumas vezes. O ataque deixaria de ser descobrir
a senha e passaria a ser trancar o dono do lado de fora. Há um segundo teto por
(origem, e-mail), que corta o laço contra uma conta específica sem dar a ninguém
esse poder.

Dois limites conhecidos, e nenhum deles é acidente: a contagem vive na memória
da instância, então atrás de um balanceador o teto efetivo é multiplicado pelo
número de instâncias; e um ataque distribuído por muitos IPs passa por baixo —
para esse caso o que serve é um segundo fator, não um contador. Ver
`FORWARD_HEADERS_STRATEGY` em `.env.example`: atrás de um proxy, sem ela, todos
chegam com o mesmo endereço e o limite tranca todo mundo de uma vez.

### Revogar tokens

O token não tem sessão no servidor, então apagá-lo do navegador não impede quem
já tenha uma cópia. Duas rotas resolvem isso: `PUT /api/auth/senha`, que exige a
senha atual — um token roubado não deve bastar para tomar a conta —, e
`POST /api/auth/sair-de-todos`.

As duas incrementam uma versão guardada na conta. O token carrega a versão que
existia quando foi emitido, e o filtro compara: o que ficou para trás para de
valer na requisição seguinte. Sem isso, trocar a senha depois de um vazamento
não fazia nada — o token vazado seguia válido por até 24 horas, que é
exatamente quando alguém troca a senha.

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
- Passos por tarefa, com "2 de 5" que expande a lista na própria linha
- Reordenação por arraste (mouse e toque) ou pelas setas do teclado
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
| `PATCH` | `/api/tarefas/{id}/posicao` | Mover na lista, relativo a outra tarefa |
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

### Ordem manual

| Método | Rota | Descrição |
|--------|------|-----------|
| `PATCH` | `/api/tarefas/{id}/posicao` | Corpo: `{"antesDe": 12}` ou `{"depoisDe": 12}` |

**A posição é dita por um vizinho, e não por um número.** "Posição 7" não
significaria nada estável: a listagem é paginada e filtrada, então o sétimo da
tela raramente é o sétimo da conta, e o sétimo de hoje não é o de amanhã. Um
vizinho concreto é o mesmo em qualquer filtro, e é sempre alguém que quem
arrastou estava vendo.

É isso que resolve o caso difícil — **mover com filtro ligado**. Filtrando por
pendentes, arrastar D para cima de B significa exatamente "D imediatamente
antes de B" na lista completa; as concluídas que estavam entre os dois não são
arrastadas junto nem pulam de lugar, porque as demais tarefas mantêm a ordem
relativa entre si. Quem foi arrastado é o único que muda de lugar.

A ordem mora numa coluna `ordem` em `tasks`, semeada com o próprio `id` na
migration — que é exatamente a ordem que a listagem mostrava antes, então
ninguém vê a lista embaralhar ao atualizar. Empates são desfeitos pelo `id`,
então a ordem é sempre total mesmo que dois valores coincidam. Mover é um
único `UPDATE` que empurra para baixo o que estava do destino em diante, e não
uma reescrita de todas as posições: mexer numa tarefa não deveria custar uma
linha alterada por tarefa da conta. O preço é que as posições ficam esparsas
com o tempo, o que não importa — a listagem usa a ordem relativa, nunca o valor.

O `UPDATE` é filtrado por dono, e o vizinho também é buscado com o dono junto:
sem isso, daria para descobrir a posição de uma tarefa alheia mandando o id
dela como referência.

Na interface a alça aparece à esquerda do cartão. O arraste usa **Pointer
Events**, e não a API de drag-and-drop do HTML, que não vale no toque —
reordenar só no desktop seria meia funcionalidade. E funciona pelo teclado:
com a alça em foco, <kbd>↑</kbd> e <kbd>↓</kbd> movem a tarefa, com o foco
acompanhando o cartão e um aviso ("posição 2 de 4") para leitor de tela.

Hoje a listagem não oferece outra ordenação na interface, então a ordem manual
é a ordem. Se um seletor de ordenação for adicionado depois, o arraste precisa
ser desabilitado enquanto outra ordenação estiver ativa — arrastar sob
"ordenar por prazo" não teria como significar nada.

### Passos (subtarefas)

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/tarefas/{id}/subtarefas` | Adiciona um passo no fim da lista |
| `PATCH` | `/api/tarefas/{id}/subtarefas/{subId}` | Muda o texto, a situação, ou os dois |
| `DELETE` | `/api/tarefas/{id}/subtarefas/{subId}` | Remove o passo |

**Um passo é um degrau dentro da tarefa, não uma tarefa menor** — e essa
escolha resolve o problema que adiou a funcionalidade. Se a subtarefa fosse
uma linha em `tasks` apontando para a mãe, ela entraria na listagem, que é
paginada e ordenada no servidor: uma filha poderia cair numa página diferente
da mãe, ou sumir num filtro que a mãe atende. Em tabela própria, ela nunca é
linha da listagem — viaja junto da mãe, sempre.

Pelo mesmo motivo o passo não tem prazo, prioridade, projeto nem etiqueta: um
passo com projeto diferente do da tarefa não quer dizer nada, e um passo com
prazo próprio apareceria em "atrasadas" contando duas vezes o mesmo
compromisso. As contagens do resumo e do painel seguem contando **tarefas**.

**Não existe `SubtarefaRepository`, e a ausência é o desenho.** Com um
repositório, mais cedo ou mais tarde alguém escreveria `findById(subId)` e o
passo de outra conta estaria a um id de distância. Aqui o único caminho até um
passo é a tarefa mãe, encontrada por `findByIdAndUsuarioId`. O `Subtarefa`
também não tem coluna `usuario_id`: o dono é o da tarefa, e sem essa coluna não
há atalho a construir. Um teste confirma que um passo não é alcançável nem pela
outra tarefa da mesma conta.

Concluir a tarefa **não** marca os passos pendentes. Marcar tudo por baixo
destruiria informação: depois de reabrir a tarefa não haveria como saber quais
passos tinham sido feitos mesmo. Apagar a tarefa, sim, leva os passos junto —
por `orphanRemoval` no Hibernate e `ON DELETE CASCADE` no banco, porque
confiar só no segundo já custou caro aqui: o banco apaga a linha, mas a sessão
do Hibernate segue com o objeto na mão.

Teto de **50 passos por tarefa**, na entidade e não em quem escreve: a API e a
importação precisam concordar, senão um arquivo cria uma tarefa que a própria
API teria recusado.

Na interface, o primeiro passo se cria pela edição da tarefa — numa lista onde
a maioria das tarefas não tem passo nenhum, um controle por linha seria ruído.
A partir do primeiro, a linha ganha um "2 de 5" que expande a lista ali mesmo,
com marcar, remover e adicionar sem sair do lugar.

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

**O dia é o de quem lê.** As conclusões são gravadas em UTC e convertidas para
o fuso da conta antes de serem agrupadas. Sem essa conversão — que é como o
painel nasceu — o eixo vinha nos dias de quem lê e as barras nos dias do
servidor: uma tarefa concluída às 22h em São Paulo já é do dia seguinte em UTC e
ia para a coluna errada. É a mesma correção que os lembretes já faziam, aplicada
onde faltava.

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

A exportação sai **na ordem que a conta arrumou**, e não na de criação: a
arrumação é trabalho de quem usa, e um backup que a perde não é backup. A
importação renumera na sequência do arquivo, o que continua valendo mesmo
importando para uma conta que já tem tarefas.

Os passos vêm **aninhados na tarefa**, e não numa lista à parte com referência
cruzada: passo não existe fora da tarefa, e uma lista separada permitiria um
arquivo com passos órfãos. O campo é novo, mas a versão do formato não mudou —
a ausência dele tem leitura óbvia, e um arquivo antigo continua entrando.

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

**Ordenação.** `sort` aceita `id`, `ordem`, `titulo`, `prazo`, `prioridade`,
`concluida`, `dataCriacao`, `dataAtualizacao` e `dataConclusao`. Qualquer outro
campo devolve **400** com a lista. A restrição não é burocracia: ordenar por uma
coleção — `sort=subtarefas.titulo` — viraria junção, e a página voltaria com a
mesma tarefa repetida uma vez por passo, com `totalElements` contando junção em
vez de tarefa.

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

Enviar `versao` é opcional e recomendado: com ela, uma edição feita sobre dado
desatualizado volta **409** em vez de apagar em silêncio o que outra tela salvou.
A versão vem em toda leitura da tarefa. Sem o campo, grava como sempre gravou —
é o que mantém a importação e clientes antigos funcionando.

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
  "versao": 0,
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

O projeto possui **183 testes**. A maioria roda contra H2 em memória, sem
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
| `SubtarefasIntegrationTest` | Passos: ordem, contagem, teto e **isolamento entre contas** |
| `ReordenacaoIntegrationTest` | Ordem manual, inclusive movendo com filtro ligado |
| `LembreteServiceTest` | Quando o lembrete sai: fuso de cada conta, uma vez por dia, falha isolada |
| `EscolhaDoEnviadorTest` | Sem SMTP configurado, o enviador ativo é o que só registra no log |
| `AgendadorDeLembretesTest` | A varredura só é agendada com `LEMBRETES_ATIVOS=true` |
| `FusoDoPainelIntegrationTest` | O dia de cada conclusão no fuso de quem lê, não no do servidor |
| `OrdenacaoIntegrationTest` | O que `?sort=` aceita e o que recusa |
| `ConcorrenciaIntegrationTest` | Duas telas editando a mesma tarefa |
| `EndurecimentoDeLoginIntegrationTest` | Teto de tentativas, troca de senha e revogação de token |
| `LimiteDeCorpoIntegrationTest` | Corpo grande demais recusado antes de ser desserializado |
| `MigrationsNoMySQLTest` | As migrations contra **MySQL de verdade**, via Testcontainers |

`MigrationsNoMySQLTest` é pulada automaticamente onde não há Docker, e executa
no CI. As demais rodam sempre.

O perfil de teste usa `ddl-auto: validate` com Flyway ligado: o schema vem das
migrations e o Hibernate apenas confere as entidades contra ele. Uma migration
quebrada, ou um campo sem migration correspondente, derruba o build.

### Cobertura

`mvn verify` gera o relatório do JaCoCo em `target/site/jacoco/index.html` e
confere um piso: **82% de instruções e 68% de ramos**, medidos sem os DTOs e as
entidades — que são getters gerados pelo Lombok e só inflariam o número.

O piso está abaixo do que o projeto tem hoje (87% e 73%) de propósito. Ele não é
meta: existe para a cobertura não cair sem ninguém perceber. Subir o número é
decisão de quem escreve os testes; deixá-lo despencar, não.

### Verificação da interface

A interface é perto de 40% do código do projeto e não passava por verificação
nenhuma — um erro de digitação em `app.js` atravessava o CI verde e só aparecia
para quem abrisse a página.

```bash
npm install   # só na primeira vez
npm run lint
```

Não substitui teste de fluxo, mas pega a classe de erro que mais custa num
arquivo sem compilação por trás: nome errado, variável que não existe, `case`
sem `break`.

### Integração contínua

`.github/workflows/ci.yml` roda três trabalhos em paralelo a cada push e pull
request: `mvn verify` com os testes e o piso de cobertura, o ESLint da
interface, e a construção da imagem Docker.

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/todolist/
│   │   ├── config/          # Configurações (Swagger/OpenAPI)
│   │   ├── controller/      # Endpoints REST
│   │   ├── dto/             # Objetos de requisição/resposta
│   │   ├── entity/          # Entidades JPA
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
        ├── dados/           # Exportação, importação e teto de corpo
        ├── db/              # Migrations e persistência (Flyway, JPA, Testcontainers)
        ├── habitos/         # Hábitos, registros e sequências
        ├── lembretes/       # Varredura, fusos e escolha do enviador
        ├── security/        # Autenticação, tentativas de senha e revogação
        ├── service/         # Testes do service (Mockito)
        └── tarefas/         # Planejamento, ordem, passos, painel e concorrência
```

Na raiz, `eslint.config.mjs` e `package.json` existem só para a verificação
estática da interface — a aplicação em si não tem dependência de Node e continua
sendo servida como está pelo Spring Boot.
