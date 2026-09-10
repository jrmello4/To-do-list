package com.todolist.tarefas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.SubtarefaRequest;
import com.todolist.dto.TaskRequest;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ?sort= aceita só os campos da própria tarefa.
 *
 * A documentação prometia seis campos e nada era conferido: qualquer caminho
 * que o Spring Data conseguisse resolver passava. Dois desses caminhos fazem
 * estrago de verdade — o que navega para uma coleção, porque vira junção e
 * devolve a mesma tarefa repetida, e o que navega para o usuário, que expõe ao
 * ordenamento colunas que nenhuma resposta mostra.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Listagem: ordenação")
class OrdenacaoIntegrationTest {

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

    @Nested
    @DisplayName("aceita")
    class Aceita {

        @Test
        @DisplayName("os campos da tarefa, inclusive o que a interface usa")
        void camposDaTarefa() throws Exception {
            // `ordem` é o que a interface manda em toda listagem. Faltava na
            // lista documentada, que é o sinal de que a lista não era conferida
            // contra nada — nem contra o próprio cliente.
            for (String campo : new String[]{
                    "id", "ordem", "titulo", "prazo", "prioridade",
                    "concluida", "dataCriacao", "dataAtualizacao", "dataConclusao"}) {

                listar("?sort=" + campo + ",asc").andExpect(status().isOk());
            }
        }

        @Test
        @DisplayName("dois critérios de uma vez, que é como a interface pede")
        void doisCriterios() throws Exception {
            listar("?sort=ordem,asc&sort=id,asc").andExpect(status().isOk());
        }

        @Test
        @DisplayName("nenhum critério, caindo no padrão")
        void semCriterio() throws Exception {
            listar("").andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("recusa com 400")
    class Recusa {

        @Test
        @DisplayName("ordenar por uma coleção, que repetiria a tarefa por passo")
        void ordenarPorColecao() throws Exception {
            long tarefa = criarTarefa("Com passos");
            adicionarPasso(tarefa, "Primeiro");
            adicionarPasso(tarefa, "Segundo");
            adicionarPasso(tarefa, "Terceiro");

            // Sem a lista branca isto respondia 200 com a mesma tarefa três
            // vezes e totalElements contando junção, não tarefa.
            listar("?sort=subtarefas.titulo,asc")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem", containsString("subtarefas.titulo")));

            // A listagem correta traz a tarefa uma vez só.
            listar("")
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("ordenar por um campo do usuário")
        void ordenarPeloUsuario() throws Exception {
            listar("?sort=usuario.senhaHash,asc")
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("ordenar por um campo que não existe")
        void campoInexistente() throws Exception {
            listar("?sort=campoQueNaoExiste,asc")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros[0]", containsString("ordem")));
        }

        @Test
        @DisplayName("e a mensagem enumera os campos válidos")
        void mensagemListaOsCampos() throws Exception {
            listar("?sort=qualquer,asc")
                    .andExpect(jsonPath("$.erros[0]", containsString("Campos ordenáveis")))
                    .andExpect(jsonPath("$.erros[0]", containsString("prazo")));
        }
    }

    /* ----------------------------------------------------------- auxiliares */

    private org.springframework.test.web.servlet.ResultActions listar(String consulta)
            throws Exception {
        return mockMvc.perform(get("/api/tarefas" + consulta)
                .header("Authorization", "Bearer " + token));
    }

    private long criarTarefa(String titulo) throws Exception {
        String corpo = mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo(titulo).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private void adicionarPasso(long tarefa, String titulo) throws Exception {
        mockMvc.perform(post("/api/tarefas/" + tarefa + "/subtarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder().titulo(titulo).build())))
                .andExpect(status().isCreated());
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
