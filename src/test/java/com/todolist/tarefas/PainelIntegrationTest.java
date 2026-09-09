package com.todolist.tarefas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.*;
import com.todolist.entity.Prioridade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Painel")
class PainelIntegrationTest {

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
    @DisplayName("conta vazia devolve tudo zerado, sem quebrar")
    void contaVazia() throws Exception {
        mockMvc.perform(get("/api/painel").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resumo.total").value(0))
                .andExpect(jsonPath("$.concluidasPorDia", hasSize(30)))
                .andExpect(jsonPath("$.pendentesPorProjeto", hasSize(0)))
                .andExpect(jsonPath("$.pendentesPorPrioridade", hasSize(0)))
                .andExpect(jsonPath("$.horasMediasParaConcluir").doesNotExist())
                .andExpect(jsonPath("$.habitos", hasSize(0)));
    }

    @Test
    @DisplayName("a série de dias é contínua: dias sem conclusão vêm com zero")
    void serieContinua() throws Exception {
        mockMvc.perform(get("/api/painel?dias=14").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.concluidasPorDia", hasSize(14)))
                .andExpect(jsonPath("$.concluidasPorDia[0].quantidade").value(0))
                .andExpect(jsonPath("$.concluidasPorDia[13].quantidade").value(0));
    }

    @Test
    @DisplayName("a janela de dias é limitada entre 7 e 365")
    void janelaLimitada() throws Exception {
        mockMvc.perform(get("/api/painel?dias=1").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.concluidasPorDia", hasSize(7)));

        mockMvc.perform(get("/api/painel?dias=9999").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.concluidasPorDia", hasSize(365)));
    }

    @Test
    @DisplayName("distribui as pendentes por projeto, com a caixa de entrada à parte")
    void distribuicaoPorProjeto() throws Exception {
        long faculdade = novoProjeto("Faculdade");
        criarTarefa(TaskRequest.builder().titulo("Prova").projetoId(faculdade).build());
        criarTarefa(TaskRequest.builder().titulo("Trabalho").projetoId(faculdade).build());
        criarTarefa(TaskRequest.builder().titulo("Comprar pão").build());

        mockMvc.perform(get("/api/painel").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.pendentesPorProjeto", hasSize(2)))
                // ordenado do maior para o menor
                .andExpect(jsonPath("$.pendentesPorProjeto[0].rotulo").value("Faculdade"))
                .andExpect(jsonPath("$.pendentesPorProjeto[0].quantidade").value(2))
                .andExpect(jsonPath("$.pendentesPorProjeto[1].rotulo").value("Sem projeto"))
                .andExpect(jsonPath("$.pendentesPorProjeto[1].quantidade").value(1));
    }

    @Test
    @DisplayName("distribui as pendentes por prioridade")
    void distribuicaoPorPrioridade() throws Exception {
        criarTarefa(TaskRequest.builder().titulo("A").prioridade(Prioridade.URGENTE).build());
        criarTarefa(TaskRequest.builder().titulo("B").prioridade(Prioridade.URGENTE).build());
        criarTarefa(TaskRequest.builder().titulo("C").prioridade(Prioridade.BAIXA).build());

        mockMvc.perform(get("/api/painel").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.pendentesPorPrioridade", hasSize(2)))
                .andExpect(jsonPath("$.pendentesPorPrioridade[*].rotulo",
                        containsInAnyOrder("URGENTE", "BAIXA")));
    }

    @Test
    @DisplayName("conta a conclusão de hoje na série e calcula o tempo médio")
    void concluidaDeHoje() throws Exception {
        long tarefa = criarTarefa(TaskRequest.builder().titulo("Feita agora").build());

        mockMvc.perform(patch("/api/tarefas/" + tarefa + "/conclusao")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ConclusaoRequest.builder().concluida(true).build())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/painel?dias=7").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.concluidasPorDia[6].quantidade").value(1))
                .andExpect(jsonPath("$.resumo.concluidas").value(1))
                // criada e concluída na mesma execução: praticamente zero horas
                .andExpect(jsonPath("$.horasMediasParaConcluir").value(0.0));
    }

    @Test
    @DisplayName("traz as sequências dos hábitos")
    void sequenciasDosHabitos() throws Exception {
        mockMvc.perform(post("/api/habitos")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(HabitoRequest.builder().nome("Ler").cor("violeta").build())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/painel").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.habitos", hasSize(1)))
                .andExpect(jsonPath("$.habitos[0].nome").value("Ler"))
                .andExpect(jsonPath("$.habitos[0].cor").value("violeta"))
                .andExpect(jsonPath("$.habitos[0].atual").value(0));
    }

    @Test
    @DisplayName("o painel de uma conta não enxerga os dados de outra")
    void naoVazaEntreContas() throws Exception {
        novoProjeto("Faculdade");
        criarTarefa(TaskRequest.builder().titulo("Da Ana").build());

        String bruno = registrar("bruno@exemplo.com");

        mockMvc.perform(get("/api/painel").header("Authorization", "Bearer " + bruno))
                .andExpect(jsonPath("$.resumo.total").value(0))
                .andExpect(jsonPath("$.pendentesPorProjeto", hasSize(0)))
                .andExpect(jsonPath("$.habitos", hasSize(0)));
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

    private long novoProjeto(String nome) throws Exception {
        String corpo = mockMvc.perform(post("/api/projetos")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ProjetoRequest.builder().nome(nome).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private long criarTarefa(TaskRequest request) throws Exception {
        String corpo = mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
