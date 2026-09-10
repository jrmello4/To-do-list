package com.todolist.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.LoginRequest;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.TrocaDeSenhaRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * O que protege a entrada da conta.
 *
 * Duas coisas que faltavam e se apoiam uma na outra: um teto de tentativas,
 * para que descobrir uma senha por força bruta não seja só questão de tempo, e
 * a revogação de token, para que a senha trocada depois de um vazamento sirva
 * para alguma coisa.
 *
 * <p>Cada caso usa um IP próprio de propósito. O contador é por origem e vive
 * na aplicação, não na transação — sem endereços distintos, um teste esgotaria
 * o teto do seguinte, e a suíte passaria a depender da ordem de execução.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Entrada da conta")
class EndurecimentoDeLoginIntegrationTest {

    private static final String SENHA = "senha-segura";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void criarConta() throws Exception {
        token = registrar("ana@exemplo.com");
    }

    @Nested
    @DisplayName("limite de tentativas")
    class LimiteDeTentativas {

        @Test
        @DisplayName("erros demais da mesma origem passam a devolver 429")
        void bloqueiaDepoisDoTeto() throws Exception {
            String ip = "203.0.113.10";

            // O teto por (origem, conta) é 8. As oito primeiras são 401.
            for (int tentativa = 1; tentativa <= 8; tentativa++) {
                entrar("ana@exemplo.com", "senha-errada", ip)
                        .andExpect(status().isUnauthorized());
            }

            entrar("ana@exemplo.com", "senha-errada", ip)
                    .andExpect(status().isTooManyRequests())
                    .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                    .andExpect(jsonPath("$.mensagem", containsString("Tentativas demais")));
        }

        @Test
        @DisplayName("e a senha certa também é recusada enquanto o bloqueio dura")
        void bloqueiaAteASenhaCerta() throws Exception {
            String ip = "203.0.113.11";

            for (int tentativa = 1; tentativa <= 8; tentativa++) {
                entrar("ana@exemplo.com", "senha-errada", ip);
            }

            // Aceitar a senha certa aqui devolveria ao ataque a chance de
            // testar mais uma a cada bloqueio; e é justamente a última que
            // interessa a quem está adivinhando.
            entrar("ana@exemplo.com", SENHA, ip)
                    .andExpect(status().isTooManyRequests());
        }

        @Test
        @DisplayName("outra origem não herda o bloqueio")
        void naoBloqueiaOutraOrigem() throws Exception {
            for (int tentativa = 1; tentativa <= 8; tentativa++) {
                entrar("ana@exemplo.com", "senha-errada", "203.0.113.12");
            }

            // É o que impede a conta de ser trancada por terceiros: se o
            // contador fosse por e-mail, qualquer um trancaria qualquer pessoa
            // errando a senha dela algumas vezes.
            entrar("ana@exemplo.com", SENHA, "203.0.113.13")
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("entrar certo limpa a contagem daquela conta")
        void acertoLimpaAContagem() throws Exception {
            String ip = "203.0.113.14";

            for (int tentativa = 1; tentativa <= 7; tentativa++) {
                entrar("ana@exemplo.com", "senha-errada", ip);
            }

            entrar("ana@exemplo.com", SENHA, ip).andExpect(status().isOk());

            // Sem a limpeza, quem errou a senha sete vezes e acertou na oitava
            // ficaria a um engano do bloqueio pelos próximos quinze minutos.
            for (int tentativa = 1; tentativa <= 7; tentativa++) {
                entrar("ana@exemplo.com", "senha-errada", ip)
                        .andExpect(status().isUnauthorized());
            }
        }
    }

    @Nested
    @DisplayName("troca de senha")
    class TrocaDeSenha {

        @Test
        @DisplayName("derruba os tokens antigos e devolve um novo")
        void derrubaTokensAntigos() throws Exception {
            String corpo = trocarSenha(SENHA, "outra-senha-boa")
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            String tokenNovo = objectMapper.readTree(corpo).get("token").asText();

            // Sem o incremento da versão, este token seguiria valendo por até
            // 24h depois da troca — que é exatamente quando alguém troca a
            // senha, e exatamente o que a troca deveria impedir.
            mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + tokenNovo))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("a nova senha passa a valer e a antiga não")
        void aNovaSenhaPassaAValer() throws Exception {
            trocarSenha(SENHA, "outra-senha-boa").andExpect(status().isOk());

            entrar("ana@exemplo.com", SENHA, "203.0.113.20")
                    .andExpect(status().isUnauthorized());
            entrar("ana@exemplo.com", "outra-senha-boa", "203.0.113.21")
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("exige a senha atual, para um token roubado não bastar")
        void exigeASenhaAtual() throws Exception {
            trocarSenha("chute", "outra-senha-boa")
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.mensagem").value("Senha atual incorreta"));

            // E a senha continua sendo a de antes.
            entrar("ana@exemplo.com", SENHA, "203.0.113.22").andExpect(status().isOk());
        }

        @Test
        @DisplayName("recusa senha nova curta demais")
        void recusaSenhaCurta() throws Exception {
            trocarSenha(SENHA, "curta").andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("sair de todos os aparelhos")
    class SairDeTodos {

        @Test
        @DisplayName("invalida o token que fez a chamada")
        void invalidaOProprioToken() throws Exception {
            mockMvc.perform(post("/api/auth/sair-de-todos")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + token))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("invalida também os tokens emitidos antes")
        void invalidaTokensAnteriores() throws Exception {
            String outroAparelho = entrarComSucesso("ana@exemplo.com");

            mockMvc.perform(post("/api/auth/sair-de-todos")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNoContent());

            // O ponto todo: apagar o token do próprio navegador não faz nada
            // contra quem já tem uma cópia dele.
            mockMvc.perform(get("/api/auth/eu")
                            .header("Authorization", "Bearer " + outroAparelho))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("e entrar de novo continua funcionando")
        void entrarDeNovoFunciona() throws Exception {
            mockMvc.perform(post("/api/auth/sair-de-todos")
                    .header("Authorization", "Bearer " + token));

            String novo = entrarComSucesso("ana@exemplo.com");

            mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + novo))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("não derruba o token de outra conta")
        void naoDerrubaOutraConta() throws Exception {
            String bruno = registrar("bruno@exemplo.com");

            mockMvc.perform(post("/api/auth/sair-de-todos")
                    .header("Authorization", "Bearer " + token));

            mockMvc.perform(get("/api/auth/eu").header("Authorization", "Bearer " + bruno))
                    .andExpect(status().isOk());
        }
    }

    /* ----------------------------------------------------------- auxiliares */

    private String registrar(String email) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Pessoa", email, SENHA))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }

    private String entrarComSucesso(String email) throws Exception {
        String corpo = entrar(email, SENHA, "198.51.100.1")
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }

    /** O IP entra explícito porque é a chave da contagem de tentativas. */
    private ResultActions entrar(String email, String senha, String ip) throws Exception {
        MockHttpServletRequestBuilder requisicao = post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(new LoginRequest(email, senha)));

        requisicao.with(bruta -> {
            bruta.setRemoteAddr(ip);
            return bruta;
        });

        return mockMvc.perform(requisicao);
    }

    private ResultActions trocarSenha(String atual, String nova) throws Exception {
        return mockMvc.perform(put("/api/auth/senha")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(TrocaDeSenhaRequest.builder()
                        .senhaAtual(atual)
                        .novaSenha(nova)
                        .build())));
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
