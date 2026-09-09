package com.todolist.tarefas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.SubtarefaRequest;
import com.todolist.dto.TaskRequest;
import com.todolist.entity.Subtarefa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Subtarefas")
class SubtarefasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ana;
    private long tarefa;

    @BeforeEach
    void preparar() throws Exception {
        ana = registrar("ana@exemplo.com");
        tarefa = criarTarefa(ana, "Escrever o artigo");
    }

    @Test
    @DisplayName("adicionar devolve 201 com a tarefa inteira e a contagem de passos")
    void adicionar() throws Exception {
        mockMvc.perform(post("/api/tarefas/" + tarefa + "/subtarefas")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder()
                                .titulo("Levantar as referências").build())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value((int) tarefa))
                .andExpect(jsonPath("$.subtarefas", hasSize(1)))
                .andExpect(jsonPath("$.subtarefas[0].titulo").value("Levantar as referências"))
                .andExpect(jsonPath("$.subtarefas[0].concluida").value(false))
                .andExpect(jsonPath("$.totalDePassos").value(1))
                .andExpect(jsonPath("$.passosConcluidos").value(0));
    }

    @Test
    @DisplayName("os passos saem na ordem em que entraram")
    void mantemAOrdem() throws Exception {
        novoPasso(tarefa, "Primeiro");
        novoPasso(tarefa, "Segundo");
        novoPasso(tarefa, "Terceiro");

        mockMvc.perform(get("/api/tarefas/" + tarefa).header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.subtarefas[0].titulo").value("Primeiro"))
                .andExpect(jsonPath("$.subtarefas[1].titulo").value("Segundo"))
                .andExpect(jsonPath("$.subtarefas[2].titulo").value("Terceiro"))
                .andExpect(jsonPath("$.subtarefas[0].ordem").value(0))
                .andExpect(jsonPath("$.subtarefas[2].ordem").value(2));
    }

    @Test
    @DisplayName("título em branco devolve 400")
    void tituloEmBranco() throws Exception {
        mockMvc.perform(post("/api/tarefas/" + tarefa + "/subtarefas")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder().titulo("   ").build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("passando do teto de passos devolve 400")
    void teto() throws Exception {
        for (int i = 0; i < Subtarefa.LIMITE_POR_TAREFA; i++) {
            novoPasso(tarefa, "Passo " + i);
        }

        mockMvc.perform(post("/api/tarefas/" + tarefa + "/subtarefas")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder().titulo("Um a mais").build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("marcar um passo atualiza a contagem, sem mexer nos outros")
    void marcar() throws Exception {
        long primeiro = novoPasso(tarefa, "Primeiro");
        novoPasso(tarefa, "Segundo");

        mockMvc.perform(patch("/api/tarefas/" + tarefa + "/subtarefas/" + primeiro)
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder().concluida(true).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.passosConcluidos").value(1))
                .andExpect(jsonPath("$.totalDePassos").value(2))
                .andExpect(jsonPath("$.subtarefas[0].concluida").value(true))
                .andExpect(jsonPath("$.subtarefas[1].concluida").value(false));
    }

    @Test
    @DisplayName("alterar só a situação mantém o texto")
    void campoOmitidoNaoApaga() throws Exception {
        long passo = novoPasso(tarefa, "Levantar as referências");

        mockMvc.perform(patch("/api/tarefas/" + tarefa + "/subtarefas/" + passo)
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder().concluida(true).build())))
                .andExpect(jsonPath("$.subtarefas[0].titulo").value("Levantar as referências"));
    }

    @Test
    @DisplayName("remover um passo tira só ele")
    void remover() throws Exception {
        long primeiro = novoPasso(tarefa, "Primeiro");
        novoPasso(tarefa, "Segundo");

        mockMvc.perform(delete("/api/tarefas/" + tarefa + "/subtarefas/" + primeiro)
                        .header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtarefas", hasSize(1)))
                .andExpect(jsonPath("$.subtarefas[0].titulo").value("Segundo"))
                .andExpect(jsonPath("$.totalDePassos").value(1));
    }

    @Test
    @DisplayName("concluir a tarefa não mexe nos passos que ficaram pendentes")
    void concluirNaoArrastaOsPassos() throws Exception {
        novoPasso(tarefa, "Primeiro");
        novoPasso(tarefa, "Segundo");

        // Marcar tudo por baixo seria destruir informação: depois de reabrir a
        // tarefa não haveria como saber quais passos tinham sido feitos mesmo.
        mockMvc.perform(patch("/api/tarefas/" + tarefa + "/conclusao")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"concluida\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(true))
                .andExpect(jsonPath("$.passosConcluidos").value(0))
                .andExpect(jsonPath("$.totalDePassos").value(2));
    }

    @Test
    @DisplayName("os passos aparecem na listagem, junto da tarefa")
    void aparecemNaListagem() throws Exception {
        novoPasso(tarefa, "Primeiro");

        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.content[0].subtarefas", hasSize(1)))
                .andExpect(jsonPath("$.content[0].totalDePassos").value(1));
    }

    @Test
    @DisplayName("tarefa sem passos traz lista vazia e zeros, não nulos")
    void semPassos() throws Exception {
        mockMvc.perform(get("/api/tarefas/" + tarefa).header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.subtarefas", hasSize(0)))
                .andExpect(jsonPath("$.totalDePassos").value(0))
                .andExpect(jsonPath("$.passosConcluidos").value(0));
    }

    @Test
    @DisplayName("apagar a tarefa leva os passos junto")
    void apagarATarefaLevaOsPassos() throws Exception {
        novoPasso(tarefa, "Primeiro");
        novoPasso(tarefa, "Segundo");

        mockMvc.perform(delete("/api/tarefas/" + tarefa).header("Authorization", "Bearer " + ana))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/tarefas/" + tarefa).header("Authorization", "Bearer " + ana))
                .andExpect(status().isNotFound());
    }

    @Nested
    @DisplayName("Isolamento entre contas")
    class Isolamento {

        @Test
        @DisplayName("outra conta não adiciona passo na tarefa alheia, e recebe 404")
        void naoAdicionaNaTarefaDeOutro() throws Exception {
            String bruno = registrar("bruno@exemplo.com");

            mockMvc.perform(post("/api/tarefas/" + tarefa + "/subtarefas")
                            .header("Authorization", "Bearer " + bruno)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(SubtarefaRequest.builder().titulo("Invasor").build())))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/tarefas/" + tarefa).header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.subtarefas", hasSize(0)));
        }

        @Test
        @DisplayName("outra conta não marca nem apaga passo alheio")
        void naoMexeNoPassoDeOutro() throws Exception {
            long passo = novoPasso(tarefa, "Primeiro");
            String bruno = registrar("bruno@exemplo.com");

            mockMvc.perform(patch("/api/tarefas/" + tarefa + "/subtarefas/" + passo)
                            .header("Authorization", "Bearer " + bruno)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(SubtarefaRequest.builder().concluida(true).build())))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/api/tarefas/" + tarefa + "/subtarefas/" + passo)
                            .header("Authorization", "Bearer " + bruno))
                    .andExpect(status().isNotFound());

            mockMvc.perform(get("/api/tarefas/" + tarefa).header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.subtarefas[0].concluida").value(false));
        }

        @Test
        @DisplayName("passo de uma tarefa não é alcançável pela outra tarefa da mesma conta")
        void passoNaoAtravessaTarefas() throws Exception {
            long passo = novoPasso(tarefa, "Primeiro");
            long outra = criarTarefa(ana, "Outra tarefa");

            // O passo existe e a conta é a dona das duas tarefas. Ainda assim
            // o id só vale dentro da tarefa a que ele pertence.
            mockMvc.perform(patch("/api/tarefas/" + outra + "/subtarefas/" + passo)
                            .header("Authorization", "Bearer " + ana)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(SubtarefaRequest.builder().concluida(true).build())))
                    .andExpect(status().isNotFound());
        }
    }

    /* ----------------------------------------------------------- auxiliares */

    private String registrar(String email) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Ana Ribeiro", email, "senhaSegura1"))))
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

    private long novoPasso(long tarefaId, String titulo) throws Exception {
        String corpo = mockMvc.perform(post("/api/tarefas/" + tarefaId + "/subtarefas")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder().titulo(titulo).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var passos = objectMapper.readTree(corpo).get("subtarefas");
        return passos.get(passos.size() - 1).get("id").asLong();
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
