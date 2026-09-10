package com.todolist.tarefas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.ConclusaoRequest;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.TaskRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Duas telas editando a mesma tarefa.
 *
 * A tarefa carrega uma versão, e quem edita pode devolvê-la para dizer sobre
 * qual estado a edição foi feita. Sem isso, a segunda gravação apagava a
 * primeira em silêncio — o caso comum de quem deixa a conta aberta no celular
 * e no computador.
 *
 * A coluna @Version sozinha não resolveria: ela protege transações que se
 * sobrepõem, e dois PUTs sequenciais não se sobrepõem. Cada um lê a versão de
 * agora e grava. Por isso a versão sobe até o cliente e volta.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Edição concorrente")
class ConcorrenciaIntegrationTest {

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
    @DisplayName("a tarefa nasce na versão 0 e a versão sobe a cada edição")
    void versaoSobeACadaEdicao() throws Exception {
        long id = criarTarefa("Original");

        mockMvc.perform(get("/api/tarefas/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.versao").value(0));

        editar(id, "Primeira alteração", null).andExpect(jsonPath("$.versao").value(1));
        editar(id, "Segunda alteração", null).andExpect(jsonPath("$.versao").value(2));
    }

    @Test
    @DisplayName("a segunda aba, com versão velha, é recusada com 409")
    void segundaAbaRecusada() throws Exception {
        long id = criarTarefa("Original");

        // Duas abas abrem a tarefa e ambas leem a versão 0.
        int versaoNaAbaA = versaoAtual(id);
        int versaoNaAbaB = versaoAtual(id);

        editar(id, "Escrito pela aba A", versaoNaAbaA)
                .andExpect(status().isOk());

        // A aba B ainda acha que está na versão 0. Sem a conferência, isto
        // respondia 200 e o texto da aba A desaparecia sem ninguém saber.
        editar(id, "Escrito pela aba B", versaoNaAbaB)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem", containsString("alterada em outro lugar")));

        mockMvc.perform(get("/api/tarefas/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.titulo").value("Escrito pela aba A"));
    }

    @Test
    @DisplayName("depois de recarregar, a mesma edição passa")
    void recarregarResolve() throws Exception {
        long id = criarTarefa("Original");
        int velha = versaoAtual(id);

        editar(id, "Alteração de outro aparelho", velha).andExpect(status().isOk());
        editar(id, "Minha alteração", velha).andExpect(status().isConflict());

        // É o que a interface faz ao receber o 409: recarrega e refaz.
        editar(id, "Minha alteração", versaoAtual(id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Minha alteração"));
    }

    @Test
    @DisplayName("sem enviar versão, grava como sempre gravou")
    void semVersaoGravaComoAntes() throws Exception {
        long id = criarTarefa("Original");
        editar(id, "Primeira", null).andExpect(status().isOk());

        // Compatibilidade: a importação não tem versão para mandar, e um
        // cliente antigo não deve parar de funcionar por causa deste campo.
        editar(id, "Segunda, sem versão", null)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Segunda, sem versão"));
    }

    @Test
    @DisplayName("concluir pela rota própria não exige versão")
    void conclusaoNaoExigeVersao() throws Exception {
        long id = criarTarefa("Original");

        // Marcar como feita é uma alternância, não uma edição de texto: não há
        // conteúdo de outra aba para ser apagado por baixo.
        mockMvc.perform(patch("/api/tarefas/" + id + "/conclusao")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ConclusaoRequest.builder().concluida(true).build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluida").value(true));
    }

    @Test
    @DisplayName("mover a tarefa muda a versão de quem foi empurrado")
    void moverEmpurraEMudaVersao() throws Exception {
        long primeira = criarTarefa("Primeira");
        long segunda = criarTarefa("Segunda");

        int versaoDaPrimeira = versaoAtual(primeira);

        // Mover a segunda para antes da primeira empurra a primeira, e o
        // UPDATE em massa precisa levar a versão junto — senão uma tarefa
        // mudaria de posição sem que ninguém que a tivesse em mãos soubesse.
        mockMvc.perform(patch("/api/tarefas/" + segunda + "/posicao")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"antesDe\":" + primeira + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tarefas/" + primeira).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.versao").value(versaoDaPrimeira + 1));
    }

    /* ----------------------------------------------------------- auxiliares */

    private long criarTarefa(String titulo) throws Exception {
        String corpo = mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo(titulo).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private int versaoAtual(long id) throws Exception {
        String corpo = mockMvc.perform(get("/api/tarefas/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("versao").asInt();
    }

    private org.springframework.test.web.servlet.ResultActions editar(
            long id, String titulo, Integer versao) throws Exception {

        return mockMvc.perform(put("/api/tarefas/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(TaskRequest.builder().titulo(titulo).versao(versao).build())));
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
