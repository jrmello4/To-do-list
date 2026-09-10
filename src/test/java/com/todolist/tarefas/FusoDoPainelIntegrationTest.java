package com.todolist.tarefas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.PreferenciasRequest;
import com.todolist.dto.RegistroRequest;
import com.todolist.dto.TaskRequest;
import com.todolist.entity.Task;
import com.todolist.entity.Usuario;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.UsuarioRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * O dia em que uma conclusão cai depende do fuso de quem lê.
 *
 * A série do painel era montada com os dias de quem lê no eixo e os dias do
 * servidor nas barras. Enquanto o servidor rodou no mesmo fuso de quem usava,
 * ninguém percebeu; em UTC, tudo o que é concluído depois das 21h em São Paulo
 * já é do dia seguinte para o servidor e ia para a coluna errada.
 *
 * Os dois casos abaixo usam o mesmo instante gravado e esperam dias
 * diferentes, um para cada fuso — é isso que separa a conversão de um acerto
 * por acidente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Painel: o dia da conclusão é o dia de quem lê")
class FusoDoPainelIntegrationTest {

    /**
     * 10/03/2026 às 01h30 UTC.
     *
     * Em São Paulo (UTC-3) isso é 09/03 às 22h30 — a véspera. O instante foi
     * escolhido justamente por cair em dias diferentes nos dois fusos.
     */
    private static final LocalDateTime CONCLUSAO_UTC = LocalDateTime.of(2026, 3, 10, 1, 30);

    private static final String HOJE = "2026-03-10";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

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
    @DisplayName("em São Paulo, a conclusão de 01h30 UTC conta na véspera")
    void contaNaVesperaEmSaoPaulo() throws Exception {
        definirFuso("America/Sao_Paulo");
        concluirEm(CONCLUSAO_UTC);

        // Série de 7 dias terminando em 10/03: o índice 5 é o dia 09, o 6 é o 10.
        mockMvc.perform(get("/api/painel?dias=7&hoje=" + HOJE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluidasPorDia[5].data").value("2026-03-09"))
                .andExpect(jsonPath("$.concluidasPorDia[5].quantidade").value(1))
                .andExpect(jsonPath("$.concluidasPorDia[6].data").value("2026-03-10"))
                .andExpect(jsonPath("$.concluidasPorDia[6].quantidade").value(0));
    }

    @Test
    @DisplayName("em UTC, o mesmo instante conta no próprio dia 10")
    void contaNoDiaEmUtc() throws Exception {
        definirFuso("UTC");
        concluirEm(CONCLUSAO_UTC);

        mockMvc.perform(get("/api/painel?dias=7&hoje=" + HOJE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluidasPorDia[5].quantidade").value(0))
                .andExpect(jsonPath("$.concluidasPorDia[6].quantidade").value(1));
    }

    @Test
    @DisplayName("em Tóquio, a conclusão de 23h UTC conta já no dia seguinte")
    void contaNoDiaSeguinteEmToquio() throws Exception {
        definirFuso("Asia/Tokyo");
        // 09/03 às 23h UTC é 10/03 às 08h em Tóquio (UTC+9).
        concluirEm(LocalDateTime.of(2026, 3, 9, 23, 0));

        mockMvc.perform(get("/api/painel?dias=7&hoje=" + HOJE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.concluidasPorDia[5].quantidade").value(0))
                .andExpect(jsonPath("$.concluidasPorDia[6].quantidade").value(1));
    }

    @Test
    @DisplayName("sem ?hoje=, o dia de referência também é o da conta")
    void semHojeUsaOFusoDaConta() throws Exception {
        ZoneId zona = fusoEmOutroDia();
        definirFuso(zona.getId());

        String corpo = mockMvc.perform(get("/api/painel?dias=7")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode serie = objectMapper.readTree(corpo).get("concluidasPorDia");
        String ultimoDia = serie.get(serie.size() - 1).get("data").asText();

        assertThat(ultimoDia)
                .as("o último dia da série é o hoje da conta")
                .isEqualTo(LocalDate.now(zona).toString());

        // A outra metade da afirmação, e a que dá valor à primeira: o dia da
        // conta não é o do servidor. Sem ela o teste passaria sozinho nas
        // horas em que os dois coincidem — que é metade do dia.
        assertThat(ultimoDia)
                .as("e não é o hoje do servidor")
                .isNotEqualTo(LocalDate.now(ZoneOffset.UTC).toString());
    }

    /**
     * Um fuso que, agora, está num dia diferente do de UTC.
     *
     * Nenhum fuso serve às 24 horas: todo fuso coincide com UTC durante parte
     * do dia. Kiritimati (UTC+14) já virou o dia a partir das 10h UTC; Niue
     * (UTC-11) ainda está no dia anterior antes das 11h UTC. As duas faixas se
     * sobrepõem, então uma das duas sempre discorda — e o teste vale em
     * qualquer horário em que alguém rode a suíte, em vez de passar de manhã e
     * falhar à tarde.
     */
    private static ZoneId fusoEmOutroDia() {
        return LocalDateTime.now(ZoneOffset.UTC).getHour() >= 10
                ? ZoneId.of("Pacific/Kiritimati")
                : ZoneId.of("Pacific/Niue");
    }

    @Test
    @DisplayName("fuso inválido gravado por engano não derruba o painel")
    void fusoInvalidoNaoDerruba() throws Exception {
        concluirEm(CONCLUSAO_UTC);

        // Pela API não há como gravar um fuso inválido — ela recusa desde os
        // lembretes. O valor entra direto aqui porque é assim que ele
        // chegaria: numa linha gravada antes daquela validação existir.
        Usuario usuario = usuarioRepository.findByEmail("ana@exemplo.com").orElseThrow();
        usuario.setFusoHorario("Marte/Olympus");
        usuarioRepository.saveAndFlush(usuario);

        mockMvc.perform(get("/api/painel?dias=7&hoje=" + HOJE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                // Cai em UTC, então o dia 10 é o que recebe — deslocado para
                // quem está em São Paulo, mas a página abre.
                .andExpect(jsonPath("$.concluidasPorDia[6].quantidade").value(1));
    }

    /* ----------------------------------------------------------- auxiliares */

    private void definirFuso(String fuso) throws Exception {
        mockMvc.perform(put("/api/auth/preferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(PreferenciasRequest.builder().fusoHorario(fuso).build())))
                .andExpect(status().isOk());
    }

    /**
     * Cria uma tarefa e grava a conclusão num instante escolhido.
     *
     * O carimbo vai direto porque o que está em teste é a leitura, não a
     * gravação: pela API só se consegue concluir "agora", e "agora" não cai
     * num dia conhecido.
     */
    private void concluirEm(LocalDateTime instanteUtc) throws Exception {
        String corpo = mockMvc.perform(post("/api/tarefas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(TaskRequest.builder().titulo("Feita").build())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(corpo).get("id").asLong();

        Task task = taskRepository.findById(id).orElseThrow();
        task.setConcluida(true);
        task.setDataConclusao(instanteUtc);
        taskRepository.saveAndFlush(task);
    }

    private String json(Object valor) throws Exception {
        return objectMapper.writeValueAsString(valor);
    }
}
