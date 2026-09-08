package com.todolist.habitos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.HabitoRequest;
import com.todolist.dto.RegistroRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Hábitos")
class HabitosIntegrationTest {

    /** Uma quarta-feira, fixada para os casos com dias da semana. */
    private static final String QUARTA = "2026-09-09";
    private static final String TERCA = "2026-09-08";
    private static final String SEGUNDA = "2026-09-07";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ana;

    @BeforeEach
    void criarConta() throws Exception {
        ana = registrar("ana@exemplo.com");
    }

    @Test
    @DisplayName("criar devolve 201, com sequência zerada e grade de 14 dias")
    void criar() throws Exception {
        mockMvc.perform(post("/api/habitos?hoje=" + QUARTA)
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(HabitoRequest.builder()
                                .nome("Ler 20 páginas").cor("verde").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("Ler 20 páginas"))
                .andExpect(jsonPath("$.sequenciaAtual").value(0))
                .andExpect(jsonPath("$.maiorSequencia").value(0))
                .andExpect(jsonPath("$.feitoHoje").value(false))
                .andExpect(jsonPath("$.aplicavelHoje").value(true))
                .andExpect(jsonPath("$.ultimosDias", hasSize(14)))
                .andExpect(jsonPath("$.diasSemana", hasSize(7)));
    }

    @Test
    @DisplayName("nome repetido devolve 409")
    void nomeRepetido() throws Exception {
        novoHabito("Ler", null);

        mockMvc.perform(post("/api/habitos")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(HabitoRequest.builder().nome("LER").build())))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("marcar dias seguidos forma a sequência")
    void sequenciaCresce() throws Exception {
        long habito = novoHabito("Ler", null);

        marcar(habito, SEGUNDA);
        marcar(habito, TERCA);
        marcar(habito, QUARTA);

        mockMvc.perform(get("/api/habitos?hoje=" + QUARTA).header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$[0].sequenciaAtual").value(3))
                .andExpect(jsonPath("$[0].maiorSequencia").value(3))
                .andExpect(jsonPath("$[0].feitoHoje").value(true));
    }

    @Test
    @DisplayName("não ter feito hoje ainda não zera a sequência")
    void hojeNaoQuebra() throws Exception {
        long habito = novoHabito("Ler", null);

        marcar(habito, SEGUNDA);
        marcar(habito, TERCA);

        mockMvc.perform(get("/api/habitos?hoje=" + QUARTA).header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$[0].sequenciaAtual").value(2))
                .andExpect(jsonPath("$[0].feitoHoje").value(false));
    }

    @Test
    @DisplayName("marcar é idempotente e desmarcar desfaz")
    void marcarEDesmarcar() throws Exception {
        long habito = novoHabito("Ler", null);

        marcar(habito, QUARTA);
        marcar(habito, QUARTA);

        mockMvc.perform(get("/api/habitos?hoje=" + QUARTA).header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$[0].sequenciaAtual").value(1));

        mockMvc.perform(delete("/api/habitos/" + habito + "/registros/" + QUARTA + "?hoje=" + QUARTA)
                        .header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.feitoHoje").value(false));

        // desmarcar de novo também não é erro
        mockMvc.perform(delete("/api/habitos/" + habito + "/registros/" + QUARTA + "?hoje=" + QUARTA)
                        .header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("marcar um dia futuro devolve 400")
    void diaFuturoDevolve400() throws Exception {
        long habito = novoHabito("Ler", null);

        mockMvc.perform(put("/api/habitos/" + habito + "/registros/2026-12-31?hoje=" + QUARTA)
                        .header("Authorization", "Bearer " + ana))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem",
                        org.hamcrest.Matchers.containsString("ainda não chegou")));
    }

    @Test
    @DisplayName("dias em que o hábito não vale não contam como falha")
    void diasNaoAplicaveis() throws Exception {
        // segunda, quarta e sexta
        long habito = novoHabito("Academia", List.of(1, 3, 5));

        marcar(habito, SEGUNDA);
        marcar(habito, QUARTA);

        // A terça ficou sem registro, mas o hábito não valia nela.
        mockMvc.perform(get("/api/habitos?hoje=" + QUARTA).header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$[0].sequenciaAtual").value(2))
                .andExpect(jsonPath("$[0].diasSemana", hasSize(3)));
    }

    @Test
    @DisplayName("hábito de outra conta devolve 404")
    void habitoDeOutraConta() throws Exception {
        long daAna = novoHabito("Ler", null);
        String bruno = registrar("bruno@exemplo.com");

        mockMvc.perform(put("/api/habitos/" + daAna + "/registros/" + QUARTA)
                        .header("Authorization", "Bearer " + bruno))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/habitos").header("Authorization", "Bearer " + bruno))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("excluir o hábito leva o histórico junto")
    void excluirApagaRegistros() throws Exception {
        long habito = novoHabito("Ler", null);
        marcar(habito, QUARTA);

        mockMvc.perform(delete("/api/habitos/" + habito).header("Authorization", "Bearer " + ana))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/habitos").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /* ----------------------------------------------------------- auxiliares */

    private String registrar(String email) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Pessoa", email, "senha-segura"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("token").asText();
    }

    private long novoHabito(String nome, List<Integer> dias) throws Exception {
        String corpo = mockMvc.perform(post("/api/habitos?hoje=" + QUARTA)
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(HabitoRequest.builder().nome(nome).diasSemana(dias).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private void marcar(long habito, String data) throws Exception {
        mockMvc.perform(put("/api/habitos/" + habito + "/registros/" + data + "?hoje=" + QUARTA)
                        .header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk());
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
