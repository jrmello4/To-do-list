package com.todolist.tarefas;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.PosicaoRequest;
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

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Ordem manual sob uma listagem paginada e filtrada.
 *
 * O caso que decide o desenho é mover com filtro ligado: se a posição fosse um
 * índice da tela, mover o segundo dos pendentes para o primeiro colocaria a
 * tarefa num lugar qualquer da lista completa. Por isso a posição é dita por
 * um vizinho.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Reordenação manual")
class ReordenacaoIntegrationTest {

    /** A rota que a interface usa: ordem manual primeiro, id como desempate. */
    private static final String LISTAGEM = "/api/tarefas?sort=ordem,asc&sort=id,asc&size=50";

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
    @DisplayName("sem mover ninguém, a lista sai na ordem de criação")
    void ordemInicial() throws Exception {
        criar("A");
        criar("B");
        criar("C");

        assertThat(titulos()).containsExactly("A", "B", "C");
    }

    @Test
    @DisplayName("mover para antes de outra coloca a tarefa imediatamente antes dela")
    void moverParaAntes() throws Exception {
        long a = criar("A");
        criar("B");
        long c = criar("C");

        mover(c, PosicaoRequest.builder().antesDe(a).build());

        assertThat(titulos()).containsExactly("C", "A", "B");
    }

    @Test
    @DisplayName("mover para depois de outra coloca a tarefa imediatamente depois dela")
    void moverParaDepois() throws Exception {
        long a = criar("A");
        criar("B");
        long c = criar("C");

        mover(c, PosicaoRequest.builder().depoisDe(a).build());

        assertThat(titulos()).containsExactly("A", "C", "B");
    }

    @Test
    @DisplayName("mover para baixo funciona igual: a tarefa sai do lugar antigo")
    void moverParaBaixo() throws Exception {
        long a = criar("A");
        criar("B");
        long c = criar("C");

        mover(a, PosicaoRequest.builder().depoisDe(c).build());

        assertThat(titulos()).containsExactly("B", "C", "A");
    }

    @Test
    @DisplayName("as outras tarefas mantêm a ordem relativa entre si")
    void asOutrasNaoSeMexem() throws Exception {
        criar("A");
        criar("B");
        criar("C");
        criar("D");
        long e = criar("E");
        long b = idPorTitulo("B");

        mover(e, PosicaoRequest.builder().antesDe(b).build());

        // Só o E mudou de lugar; A, B, C e D seguem na mesma sequência.
        assertThat(titulos()).containsExactly("A", "E", "B", "C", "D");
    }

    @Test
    @DisplayName("mover com filtro ligado respeita a lista completa, e não a tela")
    void moverComFiltro() throws Exception {
        long a = criar("A");
        criar("B");
        long c = criar("C");
        criar("D");

        // A e C concluídas: filtrando por pendentes, a tela mostra só B e D.
        concluir(a);
        concluir(c);

        long d = idPorTitulo("D");
        long b = idPorTitulo("B");

        // Na tela filtrada, D é arrastado para cima de B — os únicos dois
        // visíveis. O que a conta pediu é "D antes de B", e é só isso que muda.
        mover(d, PosicaoRequest.builder().antesDe(b).build());

        assertThat(titulosCom("&concluida=false")).containsExactly("D", "B");

        // Na lista inteira, A continua antes e C continua entre B e o resto:
        // as concluídas não foram arrastadas junto nem pularam de lugar.
        assertThat(titulos()).containsExactly("A", "D", "B", "C");
    }

    @Test
    @DisplayName("tarefa nova entra no fim, mesmo depois de a lista ser reordenada")
    void novaEntraNoFim() throws Exception {
        long a = criar("A");
        long b = criar("B");

        mover(b, PosicaoRequest.builder().antesDe(a).build());
        criar("C");

        assertThat(titulos()).containsExactly("B", "A", "C");
    }

    @Test
    @DisplayName("a ordem sobrevive a mover a mesma tarefa várias vezes")
    void movimentosSeguidos() throws Exception {
        long a = criar("A");
        long b = criar("B");
        long c = criar("C");

        mover(c, PosicaoRequest.builder().antesDe(a).build());
        mover(a, PosicaoRequest.builder().antesDe(b).build());
        mover(b, PosicaoRequest.builder().antesDe(c).build());

        assertThat(titulos()).containsExactly("B", "C", "A");
    }

    @Test
    @DisplayName("sem antesDe nem depoisDe devolve 400")
    void semVizinho() throws Exception {
        long a = criar("A");

        mockMvc.perform(patch("/api/tarefas/" + a + "/posicao")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("posicionar em relação a si mesma devolve 400")
    void emRelacaoASiMesma() throws Exception {
        long a = criar("A");

        mockMvc.perform(patch("/api/tarefas/" + a + "/posicao")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(PosicaoRequest.builder().antesDe(a).build())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("o vizinho de outra conta devolve 404, e nada se move")
    void vizinhoDeOutraConta() throws Exception {
        long minha = criar("Minha");

        String bruno = registrar("bruno@exemplo.com");
        long dele = idNoCorpo(mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + bruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo("Dele").build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());

        // Sem a checagem do dono no vizinho, daria para descobrir a posição de
        // uma tarefa alheia — e mexer na ordem da conta do outro.
        mockMvc.perform(patch("/api/tarefas/" + minha + "/posicao")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(PosicaoRequest.builder().antesDe(dele).build())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("outra conta não move a tarefa alheia")
    void naoMoveTarefaDeOutro() throws Exception {
        long a = criar("A");
        long b = criar("B");
        String bruno = registrar("bruno@exemplo.com");

        mockMvc.perform(patch("/api/tarefas/" + b + "/posicao")
                        .header("Authorization", "Bearer " + bruno)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(PosicaoRequest.builder().antesDe(a).build())))
                .andExpect(status().isNotFound());

        assertThat(titulos()).containsExactly("A", "B");
    }

    @Test
    @DisplayName("mover numa conta não escreve nas linhas da outra")
    void naoEscreveNasLinhasDaOutraConta() throws Exception {
        long a = criar("A");
        criar("B");
        long c = criar("C");

        String bruno = registrar("bruno@exemplo.com");
        criarPara(bruno, "Dele 1");
        criarPara(bruno, "Dele 2");
        criarPara(bruno, "Dele 3");

        List<Integer> antes = ordens(bruno);

        mover(c, PosicaoRequest.builder().antesDe(a).build());

        // O UPDATE que abre espaço é filtrado por dono. Sem o filtro, a ordem
        // relativa do Bruno até sobreviveria — somar 1 a todo mundo acima de um
        // ponto é monotônico e não troca ninguém de lugar —, mas as linhas dele
        // seriam reescritas por uma ação que não é dele. É isso que se verifica
        // aqui, e não a ordem: a ordem não detectaria a falha.
        assertThat(ordens(bruno)).isEqualTo(antes);
        assertThat(titulosDaConta(bruno)).containsExactly("Dele 1", "Dele 2", "Dele 3");
    }

    /* ----------------------------------------------------------- auxiliares */

    private List<String> titulos() throws Exception {
        return titulosCom("");
    }

    private List<String> titulosCom(String extra) throws Exception {
        String corpo = mockMvc.perform(get(LISTAGEM + extra)
                        .header("Authorization", "Bearer " + ana))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<String> nomes = new ArrayList<>();
        objectMapper.readTree(corpo).get("content")
                .forEach(no -> nomes.add(no.get("titulo").asText()));
        return nomes;
    }

    private void criarPara(String token, String titulo) throws Exception {
        mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo(titulo).build())))
                .andExpect(status().isCreated());
    }

    private List<Integer> ordens(String token) throws Exception {
        List<Integer> valores = new ArrayList<>();
        conteudo(token).forEach(no -> valores.add(no.get("ordem").asInt()));
        return valores;
    }

    private List<String> titulosDaConta(String token) throws Exception {
        List<String> nomes = new ArrayList<>();
        conteudo(token).forEach(no -> nomes.add(no.get("titulo").asText()));
        return nomes;
    }

    private com.fasterxml.jackson.databind.JsonNode conteudo(String token) throws Exception {
        String corpo = mockMvc.perform(get(LISTAGEM).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo).get("content");
    }

    private long idNoCorpo(String corpo) throws Exception {
        return objectMapper.readTree(corpo).get("id").asLong();
    }

    private long idPorTitulo(String titulo) throws Exception {
        String corpo = mockMvc.perform(get(LISTAGEM).header("Authorization", "Bearer " + ana))
                .andReturn().getResponse().getContentAsString();

        for (var no : objectMapper.readTree(corpo).get("content")) {
            if (no.get("titulo").asText().equals(titulo)) {
                return no.get("id").asLong();
            }
        }
        throw new AssertionError("Tarefa não encontrada: " + titulo);
    }

    private void mover(long id, PosicaoRequest destino) throws Exception {
        mockMvc.perform(patch("/api/tarefas/" + id + "/posicao")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(destino)))
                .andExpect(status().isOk());
    }

    private void concluir(long id) throws Exception {
        mockMvc.perform(patch("/api/tarefas/" + id + "/conclusao")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"concluida\":true}"))
                .andExpect(status().isOk());
    }

    private long criar(String titulo) throws Exception {
        return idNoCorpo(mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + ana)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo(titulo).build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString());
    }

    private String registrar(String email) throws Exception {
        String corpo = mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new RegistroRequest("Ana Ribeiro", email, "senhaSegura1"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(corpo).get("token").asText();
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
