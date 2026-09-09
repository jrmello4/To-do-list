package com.todolist.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.TaskRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Percorre a autenticação de ponta a ponta, com a aplicação inteira no ar.
 *
 * O grupo "isolamento entre contas" é o motivo principal desta classe:
 * autenticar não é autorizar, e um token válido não pode dar acesso ao que
 * pertence a outra pessoa.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Autenticação e isolamento entre contas")
class AutenticacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /* ------------------------------------------------------------ cadastro */

    @Test
    @DisplayName("cadastro devolve 201 com token e sem expor a senha")
    void cadastroDevolveToken() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Ana Ribeiro", "ana@exemplo.com", "senha-segura"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.usuario.senhaHash").doesNotExist())
                .andExpect(jsonPath("$.usuario.senha").doesNotExist());
    }

    @Test
    @DisplayName("cadastro com e-mail repetido devolve 409")
    void cadastroDuplicadoDevolve409() throws Exception {
        registrar("ana@exemplo.com", "senha-segura");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Outra Ana", "ana@exemplo.com", "outra-senha"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("e-mail é normalizado, então a mesma conta em maiúsculas colide")
    void emailNormalizado() throws Exception {
        registrar("ana@exemplo.com", "senha-segura");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Ana", "ANA@Exemplo.COM", "senha-segura"))))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("senha curta devolve 400")
    void senhaCurtaDevolve400() throws Exception {
        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Ana", "ana@exemplo.com", "1234"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros").isNotEmpty());
    }

    /* --------------------------------------------------------------- login */

    @Test
    @DisplayName("login com senha errada devolve 401 sem dizer qual campo falhou")
    void loginComSenhaErrada() throws Exception {
        registrar("ana@exemplo.com", "senha-segura");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("ana@exemplo.com", "senha-errada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos"));
    }

    @Test
    @DisplayName("login de conta inexistente devolve a mesma mensagem de senha errada")
    void loginDeContaInexistente() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new LoginRequest("ninguem@exemplo.com", "qualquer-senha"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos"));
    }

    /* --------------------------------------------------------------- token */

    @Test
    @DisplayName("sem token as tarefas devolvem 401")
    void semTokenDevolve401() throws Exception {
        mockMvc.perform(get("/api/tarefas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("token adulterado devolve 401")
    void tokenAdulteradoDevolve401() throws Exception {
        String token = registrar("ana@exemplo.com", "senha-segura");

        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + token + "x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("perfil devolve a conta do token")
    void perfilDevolveAConta() throws Exception {
        String token = registrar("ana@exemplo.com", "senha-segura");

        mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.nome").value("Ana Ribeiro"));
    }

    @Test
    @DisplayName("a interface e os arquivos do PWA continuam públicos")
    void arquivosPublicos() throws Exception {
        for (String caminho : new String[]{
                "/", "/index.html", "/css/style.css", "/js/app.js", "/js/prefs.js",
                "/favicon.svg", "/sw.js", "/manifest.webmanifest"}) {
            mockMvc.perform(get(caminho))
                    .andExpect(status().isOk());
        }
    }

    /* -------------------------------------------- isolamento entre contas */

    @Test
    @DisplayName("uma conta não enxerga a tarefa de outra, e recebe 404 — não 403")
    void naoEnxergaTarefaDeOutraConta() throws Exception {
        String ana = registrar("ana@exemplo.com", "senha-segura");
        String bruno = registrar("bruno@exemplo.com", "senha-segura");

        long idDaAna = criarTarefa(ana, "Tarefa da Ana");

        // 404 e não 403: um 403 confirmaria que essa tarefa existe.
        mockMvc.perform(get("/api/tarefas/" + idDaAna).header("Authorization", "Bearer " + bruno))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/tarefas/" + idDaAna).header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tarefa da Ana"));
    }

    @Test
    @DisplayName("uma conta não altera nem apaga a tarefa de outra")
    void naoAlteraNemApagaTarefaDeOutraConta() throws Exception {
        String ana = registrar("ana@exemplo.com", "senha-segura");
        String bruno = registrar("bruno@exemplo.com", "senha-segura");

        long idDaAna = criarTarefa(ana, "Tarefa da Ana");

        mockMvc.perform(put("/api/tarefas/" + idDaAna)
                        .header("Authorization", "Bearer " + bruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo("Sequestrada").build())))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/tarefas/" + idDaAna).header("Authorization", "Bearer " + bruno))
                .andExpect(status().isNotFound());

        // e continua intacta para a dona
        mockMvc.perform(get("/api/tarefas/" + idDaAna).header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Tarefa da Ana"));
    }

    @Test
    @DisplayName("a listagem traz apenas as tarefas da própria conta")
    void listagemTrazApenasAsProprias() throws Exception {
        String ana = registrar("ana@exemplo.com", "senha-segura");
        String bruno = registrar("bruno@exemplo.com", "senha-segura");

        criarTarefa(ana, "Primeira da Ana");
        criarTarefa(ana, "Segunda da Ana");
        criarTarefa(bruno, "Única do Bruno");

        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].titulo").value("Primeira da Ana"))
                .andExpect(jsonPath("$.content[1].titulo").value("Segunda da Ana"));

        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + bruno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].titulo").value("Única do Bruno"));
    }

    /* ----------------------------------------------------------- auxiliares */

    private String registrar(String email, String senha) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Ana Ribeiro", email, senha))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }

    private long criarTarefa(String token, String titulo) throws Exception {
        String corpo = mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo(titulo).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
