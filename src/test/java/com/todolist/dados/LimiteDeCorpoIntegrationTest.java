package com.todolist.dados;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.TaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * O corpo é recusado antes de ser desserializado.
 *
 * A importação já tinha um teto de 5000 itens por tipo, mas conferido depois
 * de o Jackson montar a lista inteira em memória — o que só protege o banco,
 * nunca a aplicação. Um arquivo grande o bastante derrubava o processo sem
 * jamais chegar à conferência.
 *
 * O limite aqui é reduzido por propriedade para o teste não precisar montar
 * dez megabytes de JSON só para provar o ponto.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(properties = "LIMITE_CORPO_BYTES=2048")
@DisplayName("Limite de corpo da requisição")
class LimiteDeCorpoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void criarConta() throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Ana", "ana@exemplo.com", "senha-segura"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(corpo).get("token").asText();
    }

    @Test
    @DisplayName("recusa a importação de um corpo acima do teto")
    void recusaImportacaoGrande() throws Exception {
        mockMvc.perform(post("/api/dados/importar")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonGrande()))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.mensagem", containsString("grande demais")));
    }

    @Test
    @DisplayName("o teto vale para qualquer rota com corpo, não só a importação")
    void valeParaQualquerRota() throws Exception {
        // Recusar só na importação deixaria de fora o registro e o login, que
        // são públicos — os que qualquer um alcança sem sequer ter conta.
        mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonGrande()))
                .andExpect(status().isPayloadTooLarge());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonGrande()))
                .andExpect(status().isPayloadTooLarge());
    }

    @Test
    @DisplayName("um corpo de tamanho normal segue passando")
    void corpoNormalPassa() throws Exception {
        // O teto não pode ser tão zeloso a ponto de barrar o uso comum: se
        // barrasse, o teste acima passaria por acidente.
        mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo("Comprar pão").build())))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET não é afetado pelo teto")
    void getNaoEhAfetado() throws Exception {
        mockMvc.perform(get("/api/dados/exportar").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    /** JSON sintaticamente válido e acima do teto de 2048 bytes do teste. */
    private String jsonGrande() {
        return "{\"titulo\":\"" + "a".repeat(4096) + "\"}";
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
