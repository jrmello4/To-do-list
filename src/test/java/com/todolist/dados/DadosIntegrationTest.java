package com.todolist.dados;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.*;
import com.todolist.entity.Prioridade;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Exportar e importar")
class DadosIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("exporta tudo, referenciando projeto e etiquetas por nome")
    void exportaPorNome() throws Exception {
        String ana = registrar("ana@exemplo.com");
        long faculdade = novoProjeto(ana, "Faculdade");
        long urgente = novaEtiqueta(ana, "urgente");

        criarTarefa(ana, TaskRequest.builder()
                .titulo("Prova de cálculo")
                .projetoId(faculdade)
                .etiquetaIds(List.of(urgente))
                .prazo(LocalDate.of(2026, 10, 1))
                .prioridade(Prioridade.ALTA)
                .build());

        mockMvc.perform(get("/api/dados/exportar").header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(jsonPath("$.versao").value("1"))
                .andExpect(jsonPath("$.conta.email").value("ana@exemplo.com"))
                .andExpect(jsonPath("$.projetos[0].nome").value("Faculdade"))
                .andExpect(jsonPath("$.etiquetas[0].nome").value("urgente"))
                .andExpect(jsonPath("$.tarefas[0].titulo").value("Prova de cálculo"))
                // por nome, não por id
                .andExpect(jsonPath("$.tarefas[0].projeto").value("Faculdade"))
                .andExpect(jsonPath("$.tarefas[0].etiquetas[0]").value("urgente"))
                .andExpect(jsonPath("$.tarefas[0].prazo").value("2026-10-01"))
                .andExpect(jsonPath("$.tarefas[0].prioridade").value("ALTA"));
    }

    @Test
    @DisplayName("a exportação não vaza o hash da senha")
    void naoVazaSenha() throws Exception {
        String ana = registrar("ana@exemplo.com");

        String corpo = mockMvc.perform(get("/api/dados/exportar")
                        .header("Authorization", "Bearer " + ana))
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(corpo)
                .doesNotContain("senha")
                .doesNotContain("$2a$");
    }

    @Test
    @DisplayName("exportar de uma conta e importar noutra reconstrói os vínculos")
    void viagemDeIdaEVolta() throws Exception {
        String ana = registrar("ana@exemplo.com");
        long faculdade = novoProjeto(ana, "Faculdade");
        long urgente = novaEtiqueta(ana, "urgente");
        criarTarefa(ana, TaskRequest.builder().titulo("Prova").projetoId(faculdade)
                .etiquetaIds(List.of(urgente)).build());
        criarTarefa(ana, TaskRequest.builder().titulo("Solta").build());

        mockMvc.perform(post("/api/habitos")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(HabitoRequest.builder().nome("Ler").build())))
                .andExpect(status().isCreated());
        mockMvc.perform(put("/api/habitos/1/registros/" + LocalDate.now())
                        .header("Authorization", "Bearer " + ana));

        String arquivo = mockMvc.perform(get("/api/dados/exportar")
                        .header("Authorization", "Bearer " + ana))
                .andReturn().getResponse().getContentAsString();

        // Conta nova, do zero
        String bruno = registrar("bruno@exemplo.com");

        mockMvc.perform(post("/api/dados/importar")
                        .header("Authorization", "Bearer " + bruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projetos").value(1))
                .andExpect(jsonPath("$.etiquetas").value(1))
                .andExpect(jsonPath("$.tarefas").value(2))
                .andExpect(jsonPath("$.habitos").value(1));

        // O vínculo tarefa -> projeto -> etiqueta sobreviveu à viagem
        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + bruno))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].projeto.nome").value("Faculdade"))
                .andExpect(jsonPath("$.content[0].etiquetas[0].nome").value("urgente"))
                .andExpect(jsonPath("$.content[1].projeto").doesNotExist());
    }

    @Test
    @DisplayName("importar soma ao que existe e reaproveita nomes repetidos")
    void somaSemDuplicar() throws Exception {
        String ana = registrar("ana@exemplo.com");
        novoProjeto(ana, "Faculdade");
        criarTarefa(ana, TaskRequest.builder().titulo("Já existia").build());

        String arquivo = mockMvc.perform(get("/api/dados/exportar")
                        .header("Authorization", "Bearer " + ana))
                .andReturn().getResponse().getContentAsString();

        // Importa na própria conta: o projeto é reaproveitado, a tarefa duplica
        mockMvc.perform(post("/api/dados/importar")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(arquivo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projetos").value(0))
                .andExpect(jsonPath("$.tarefas").value(1))
                .andExpect(jsonPath("$.reaproveitados",
                        hasItem(containsString("Faculdade"))));

        mockMvc.perform(get("/api/projetos").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$", hasSize(1)));
        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + ana))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("versão desconhecida devolve 400 em vez de importar errado")
    void versaoIncompativel() throws Exception {
        String ana = registrar("ana@exemplo.com");

        mockMvc.perform(post("/api/dados/importar")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"versao\":\"99\",\"tarefas\":[]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem", containsString("versão 99")));
    }

    @Test
    @DisplayName("exportar exige autenticação")
    void exigeAutenticacao() throws Exception {
        mockMvc.perform(get("/api/dados/exportar")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/dados/importar")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("os passos viajam aninhados na tarefa e chegam na ordem")
    void passosNaViagemDeIdaEVolta() throws Exception {
        String origem = registrar("origem@exemplo.com");
        long tarefa = criarTarefa(origem, TaskRequest.builder().titulo("Escrever o artigo").build());
        novoPasso(origem, tarefa, "Levantar as referências", true);
        novoPasso(origem, tarefa, "Escrever o rascunho", false);

        String arquivo = mockMvc.perform(get("/api/dados/exportar")
                        .header("Authorization", "Bearer " + origem))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarefas[0].subtarefas", hasSize(2)))
                .andExpect(jsonPath("$.tarefas[0].subtarefas[0].titulo")
                        .value("Levantar as referências"))
                .andExpect(jsonPath("$.tarefas[0].subtarefas[0].concluida").value(true))
                .andReturn().getResponse().getContentAsString();

        String destino = registrar("destino@exemplo.com");
        mockMvc.perform(post("/api/dados/importar")
                        .header("Authorization", "Bearer " + destino)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(arquivo))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + destino))
                .andExpect(jsonPath("$.content[0].subtarefas", hasSize(2)))
                .andExpect(jsonPath("$.content[0].subtarefas[0].titulo")
                        .value("Levantar as referências"))
                .andExpect(jsonPath("$.content[0].subtarefas[1].titulo").value("Escrever o rascunho"))
                .andExpect(jsonPath("$.content[0].passosConcluidos").value(1))
                .andExpect(jsonPath("$.content[0].totalDePassos").value(2));
    }

    @Test
    @DisplayName("arquivo antigo, sem o campo de passos, continua entrando")
    void arquivoSemOCampoDePassos() throws Exception {
        String destino = registrar("destino@exemplo.com");

        // Foi por isso que a versão do formato não mudou: o campo é novo, mas
        // a ausência dele tem leitura óbvia — a tarefa não tem passo nenhum.
        String antigo = """
                {"versao":"1","tarefas":[{"titulo":"Tarefa de um arquivo antigo"}]}""";

        mockMvc.perform(post("/api/dados/importar")
                        .header("Authorization", "Bearer " + destino)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(antigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tarefas").value(1));

        mockMvc.perform(get("/api/tarefas").header("Authorization", "Bearer " + destino))
                .andExpect(jsonPath("$.content[0].subtarefas", hasSize(0)))
                .andExpect(jsonPath("$.content[0].totalDePassos").value(0));
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
        return idDe(mockMvc.perform(post("/api/projetos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ProjetoRequest.builder().nome(nome).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private long novaEtiqueta(String token, String nome) throws Exception {
        return idDe(mockMvc.perform(post("/api/etiquetas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(EtiquetaRequest.builder().nome(nome).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private long criarTarefa(String token, TaskRequest request) throws Exception {
        return idDe(mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private void novoPasso(String token, long tarefa, String titulo, boolean concluida)
            throws Exception {
        mockMvc.perform(post("/api/tarefas/" + tarefa + "/subtarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(SubtarefaRequest.builder()
                                .titulo(titulo).concluida(concluida).build())))
                .andExpect(status().isCreated());
    }

    private long idDe(String corpo) throws Exception {
        JsonNode no = objectMapper.readTree(corpo);
        return no.get("id").asLong();
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
