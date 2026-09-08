package com.todolist.tarefas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.*;
import com.todolist.entity.Prioridade;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Projetos, prazos, prioridade, filtros, paginação e resumo. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Planejamento de tarefas")
class PlanejamentoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String ana;

    @BeforeEach
    void criarConta() throws Exception {
        ana = registrar("ana@exemplo.com");
    }

    /* ------------------------------------------------------------ projetos */

    @Nested
    @DisplayName("Projetos")
    class Projetos {

        @Test
        @DisplayName("criar devolve 201 e começa sem tarefas pendentes")
        void criarProjeto() throws Exception {
            mockMvc.perform(post("/api/projetos")
                            .header("Authorization", "Bearer " + ana)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(ProjetoRequest.builder().nome("Faculdade").cor("verde").build())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nome").value("Faculdade"))
                    .andExpect(jsonPath("$.cor").value("verde"))
                    .andExpect(jsonPath("$.arquivado").value(false))
                    .andExpect(jsonPath("$.tarefasPendentes").value(0));
        }

        @Test
        @DisplayName("nome repetido na mesma conta devolve 409")
        void nomeRepetido() throws Exception {
            novoProjeto(ana, "Faculdade");

            mockMvc.perform(post("/api/projetos")
                            .header("Authorization", "Bearer " + ana)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(ProjetoRequest.builder().nome("faculdade").build())))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("o mesmo nome em outra conta é permitido")
        void mesmoNomeEmOutraConta() throws Exception {
            novoProjeto(ana, "Faculdade");
            String bruno = registrar("bruno@exemplo.com");

            mockMvc.perform(post("/api/projetos")
                            .header("Authorization", "Bearer " + bruno)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(ProjetoRequest.builder().nome("Faculdade").build())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("conta a quantidade de tarefas pendentes")
        void contaPendentes() throws Exception {
            long projeto = novoProjeto(ana, "Faculdade");
            criarTarefa(ana, TaskRequest.builder().titulo("Prova").projetoId(projeto).build());
            long feita = criarTarefa(ana, TaskRequest.builder().titulo("Trabalho").projetoId(projeto).build());
            concluir(ana, feita, true);

            mockMvc.perform(get("/api/projetos").header("Authorization", "Bearer " + ana))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].tarefasPendentes").value(1));
        }

        @Test
        @DisplayName("excluir o projeto devolve as tarefas à caixa de entrada, sem apagá-las")
        void excluirNaoApagaTarefas() throws Exception {
            long projeto = novoProjeto(ana, "Faculdade");
            long tarefa = criarTarefa(ana, TaskRequest.builder().titulo("Prova").projetoId(projeto).build());

            mockMvc.perform(delete("/api/projetos/" + projeto).header("Authorization", "Bearer " + ana))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/api/tarefas/" + tarefa).header("Authorization", "Bearer " + ana))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.projeto").doesNotExist());
        }

        @Test
        @DisplayName("projeto de outra conta devolve 404")
        void projetoDeOutraConta() throws Exception {
            long daAna = novoProjeto(ana, "Faculdade");
            String bruno = registrar("bruno@exemplo.com");

            mockMvc.perform(get("/api/projetos/" + daAna).header("Authorization", "Bearer " + bruno))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("não dá para pendurar a própria tarefa no projeto de outra conta")
        void naoUsaProjetoDeOutraConta() throws Exception {
            long daAna = novoProjeto(ana, "Faculdade");
            String bruno = registrar("bruno@exemplo.com");

            // A tarefa é do Bruno, mas o projeto não. Sem validar o dono do
            // projeto, a chave estrangeira aceitaria a associação.
            mockMvc.perform(post("/api/tarefas")
                            .header("Authorization", "Bearer " + bruno)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json(TaskRequest.builder().titulo("Invasora").projetoId(daAna).build())))
                    .andExpect(status().isNotFound());
        }
    }

    /* --------------------------------------------------- prazos e conclusão */

    @Nested
    @DisplayName("Prazo, prioridade e conclusão")
    class Planejamento {

        @Test
        @DisplayName("guarda prazo e prioridade")
        void guardaPrazoEPrioridade() throws Exception {
            long id = criarTarefa(ana, TaskRequest.builder()
                    .titulo("Entregar relatório")
                    .prazo(LocalDate.of(2026, 9, 30))
                    .prioridade(Prioridade.URGENTE)
                    .build());

            mockMvc.perform(get("/api/tarefas/" + id).header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.prazo").value("2026-09-30"))
                    .andExpect(jsonPath("$.prioridade").value("URGENTE"));
        }

        @Test
        @DisplayName("sem prioridade informada, assume MEDIA")
        void prioridadePadrao() throws Exception {
            long id = criarTarefa(ana, TaskRequest.builder().titulo("Sem prioridade").build());

            mockMvc.perform(get("/api/tarefas/" + id).header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.prioridade").value("MEDIA"))
                    .andExpect(jsonPath("$.prazo").doesNotExist());
        }

        @Test
        @DisplayName("concluir registra a data, reabrir a limpa")
        void registraELimpaDataDeConclusao() throws Exception {
            long id = criarTarefa(ana, TaskRequest.builder().titulo("Ciclo").build());

            mockMvc.perform(get("/api/tarefas/" + id).header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.dataConclusao").doesNotExist());

            concluir(ana, id, true);
            mockMvc.perform(get("/api/tarefas/" + id).header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.concluida").value(true))
                    .andExpect(jsonPath("$.dataConclusao").isNotEmpty());

            concluir(ana, id, false);
            mockMvc.perform(get("/api/tarefas/" + id).header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.concluida").value(false))
                    .andExpect(jsonPath("$.dataConclusao").doesNotExist());
        }

        @Test
        @DisplayName("prioridade inválida na URL devolve 400, não 500")
        void prioridadeInvalida() throws Exception {
            mockMvc.perform(get("/api/tarefas?prioridade=URGENTISSIMA")
                            .header("Authorization", "Bearer " + ana))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem", containsString("prioridade")));
        }

        @Test
        @DisplayName("ordenar por campo inexistente devolve 400, não 500")
        void ordenacaoInvalida() throws Exception {
            mockMvc.perform(get("/api/tarefas?sort=campoInventado")
                            .header("Authorization", "Bearer " + ana))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.erros[0]", containsString("Campos ordenáveis")));
        }
    }

    /* ------------------------------------------------ filtros e paginação */

    @Nested
    @DisplayName("Filtros, paginação e resumo")
    class Filtros {

        @Test
        @DisplayName("filtra por situação, projeto, caixa de entrada e prioridade")
        void filtraPorTudo() throws Exception {
            long faculdade = novoProjeto(ana, "Faculdade");

            long comProjeto = criarTarefa(ana, TaskRequest.builder()
                    .titulo("Prova de cálculo").projetoId(faculdade)
                    .prioridade(Prioridade.ALTA).build());
            criarTarefa(ana, TaskRequest.builder().titulo("Comprar pão").build());
            concluir(ana, comProjeto, true);

            mockMvc.perform(get("/api/tarefas?concluida=true").header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].titulo").value("Prova de cálculo"));

            mockMvc.perform(get("/api/tarefas?projeto=" + faculdade)
                            .header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(1)));

            mockMvc.perform(get("/api/tarefas?semProjeto=true").header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].titulo").value("Comprar pão"));

            mockMvc.perform(get("/api/tarefas?prioridade=ALTA").header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("busca no título e na descrição, sem diferenciar maiúsculas")
        void busca() throws Exception {
            criarTarefa(ana, TaskRequest.builder().titulo("Estudar Flyway").build());
            criarTarefa(ana, TaskRequest.builder().titulo("Revisar código")
                    .descricao("Conferir as migrations do FLYWAY").build());
            criarTarefa(ana, TaskRequest.builder().titulo("Comprar pão").build());

            mockMvc.perform(get("/api/tarefas?busca=flyway").header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(2)));
        }

        @Test
        @DisplayName("filtra por prazo, deixando de fora as tarefas sem prazo")
        void filtraPorPrazo() throws Exception {
            criarTarefa(ana, TaskRequest.builder().titulo("Vence cedo")
                    .prazo(LocalDate.of(2026, 9, 10)).build());
            criarTarefa(ana, TaskRequest.builder().titulo("Vence tarde")
                    .prazo(LocalDate.of(2026, 12, 1)).build());
            criarTarefa(ana, TaskRequest.builder().titulo("Sem prazo").build());

            mockMvc.perform(get("/api/tarefas?prazoAte=2026-09-30")
                            .header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].titulo").value("Vence cedo"));
        }

        @Test
        @DisplayName("pagina os resultados")
        void pagina() throws Exception {
            for (int i = 1; i <= 7; i++) {
                criarTarefa(ana, TaskRequest.builder().titulo("Tarefa " + i).build());
            }

            mockMvc.perform(get("/api/tarefas?page=0&size=3").header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(3)))
                    .andExpect(jsonPath("$.totalElements").value(7))
                    .andExpect(jsonPath("$.totalPages").value(3));

            mockMvc.perform(get("/api/tarefas?page=2&size=3").header("Authorization", "Bearer " + ana))
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("resumo conta no banco, e não na página")
        void resumoContaTudo() throws Exception {
            for (int i = 1; i <= 7; i++) {
                criarTarefa(ana, TaskRequest.builder().titulo("Tarefa " + i).build());
            }
            long atrasada = criarTarefa(ana, TaskRequest.builder()
                    .titulo("Atrasada").prazo(LocalDate.of(2020, 1, 1)).build());
            long hoje = criarTarefa(ana, TaskRequest.builder()
                    .titulo("Vence hoje").prazo(LocalDate.of(2026, 9, 8)).build());
            concluir(ana, atrasada, false);

            long feita = criarTarefa(ana, TaskRequest.builder().titulo("Feita").build());
            concluir(ana, feita, true);

            // size=2 de propósito: as contagens não podem sair da página
            mockMvc.perform(get("/api/tarefas/resumo?hoje=2026-09-08")
                            .header("Authorization", "Bearer " + ana))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total").value(10))
                    .andExpect(jsonPath("$.concluidas").value(1))
                    .andExpect(jsonPath("$.pendentes").value(9))
                    .andExpect(jsonPath("$.atrasadas").value(1))
                    .andExpect(jsonPath("$.vencemHoje").value(1))
                    .andExpect(jsonPath("$.percentualConcluido").value(10));

            assert hoje > 0;
        }

        @Test
        @DisplayName("o resumo de uma conta não inclui as tarefas de outra")
        void resumoNaoVazaEntreContas() throws Exception {
            criarTarefa(ana, TaskRequest.builder().titulo("Da Ana").build());
            String bruno = registrar("bruno@exemplo.com");

            mockMvc.perform(get("/api/tarefas/resumo").header("Authorization", "Bearer " + bruno))
                    .andExpect(jsonPath("$.total").value(0));
        }
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

    private long novoProjeto(String token, String nome) throws Exception {
        String corpo = mockMvc.perform(post("/api/projetos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ProjetoRequest.builder().nome(nome).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private long criarTarefa(String token, TaskRequest request) throws Exception {
        String corpo = mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private void concluir(String token, long id, boolean concluida) throws Exception {
        mockMvc.perform(patch("/api/tarefas/" + id + "/conclusao")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ConclusaoRequest.builder().concluida(concluida).build())))
                .andExpect(status().isOk());
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
