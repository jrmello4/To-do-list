package com.todolist.lembretes;

import com.todolist.dto.TaskRequest;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.Usuario;
import com.todolist.repository.UsuarioRepository;
import com.todolist.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

/**
 * A regra difícil dos lembretes não é mandar o e-mail, é decidir quando.
 * O servidor roda em UTC e cada conta tem o seu fuso, então "às 8 da manhã"
 * é um instante UTC diferente para cada uma. Estes testes tratam o envio
 * como um dublê e olham só para essa decisão.
 */
@SpringBootTest
@Transactional
@DisplayName("Varredura de lembretes")
class LembreteServiceTest {

    /** 11h UTC: 8h em São Paulo (UTC-3), 20h em Tóquio (UTC+9). */
    private static final ZonedDateTime ONZE_UTC =
            ZonedDateTime.parse("2026-09-09T11:00:00Z");

    /** 23h UTC do dia anterior: já é 8h do dia 9 em Tóquio. */
    private static final ZonedDateTime VINTE_E_TRES_UTC_DA_VESPERA =
            ZonedDateTime.parse("2026-09-08T23:00:00Z");

    private static final LocalDate HOJE = LocalDate.parse("2026-09-09");
    private static final LocalDate ANTEONTEM = LocalDate.parse("2026-09-07");

    @Autowired
    private LembreteService lembreteService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TaskService taskService;

    @MockitoBean
    private EnviadorDeLembrete enviador;

    private final List<Lembrete> enviados = new ArrayList<>();

    @BeforeEach
    void gravarOsEnvios() {
        enviados.clear();
        doAnswer(chamada -> enviados.add(chamada.getArgument(0)))
                .when(enviador).enviar(any());
    }

    @Test
    @DisplayName("conta com lembretes desligados não recebe nada")
    void desligado() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, false);
        tarefa(ana, "Pagar a conta de luz", HOJE);

        assertThat(lembreteService.varrer(ONZE_UTC)).isZero();
        assertThat(enviados).isEmpty();
    }

    @Test
    @DisplayName("na hora local configurada, o resumo separa atrasadas de vencem hoje")
    void naHoraCerta() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        tarefa(ana, "Enviar o relatório", ANTEONTEM);
        tarefa(ana, "Pagar a conta de luz", HOJE);
        tarefa(ana, "Renovar o seguro", HOJE.plusDays(3));
        tarefa(ana, "Algum dia arrumar a gaveta", null);

        assertThat(lembreteService.varrer(ONZE_UTC)).isEqualTo(1);

        assertThat(enviados).hasSize(1);
        Lembrete lembrete = enviados.get(0);
        assertThat(lembrete.destinatario()).isEqualTo("ana@exemplo.com");
        assertThat(lembrete.nome()).isEqualTo("Ana");
        assertThat(titulos(lembrete.atrasadas())).containsExactly("Enviar o relatório");
        assertThat(titulos(lembrete.vencemHoje())).containsExactly("Pagar a conta de luz");
        assertThat(lembrete.assunto()).isEqualTo("1 tarefa passou do prazo");
    }

    @Test
    @DisplayName("tarefa já concluída não entra no resumo")
    void concluidaFicaDeFora() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        long feita = tarefa(ana, "Enviar o relatório", ANTEONTEM);
        taskService.definirConclusao(ana.getId(), feita, true);
        tarefa(ana, "Pagar a conta de luz", HOJE);

        lembreteService.varrer(ONZE_UTC);

        assertThat(enviados).hasSize(1);
        assertThat(enviados.get(0).atrasadas()).isEmpty();
        assertThat(titulos(enviados.get(0).vencemHoje())).containsExactly("Pagar a conta de luz");
    }

    @Test
    @DisplayName("fora da hora local configurada, nada sai")
    void foraDaHora() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 20, true);
        tarefa(ana, "Pagar a conta de luz", HOJE);

        // 11h UTC são 8h em São Paulo, e ela pediu às 20h.
        assertThat(lembreteService.varrer(ONZE_UTC)).isZero();
        assertThat(enviados).isEmpty();
        assertThat(ana.getUltimoLembreteEm()).isNull();
    }

    @Test
    @DisplayName("duas varreduras no mesmo dia local não repetem o lembrete")
    void naoRepeteNoMesmoDia() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        tarefa(ana, "Pagar a conta de luz", HOJE);

        assertThat(lembreteService.varrer(ONZE_UTC)).isEqualTo(1);
        assertThat(lembreteService.varrer(ONZE_UTC)).isZero();

        assertThat(enviados).hasSize(1);
        assertThat(ana.getUltimoLembreteEm()).isEqualTo(HOJE);
    }

    @Test
    @DisplayName("sem nada vencendo não manda e-mail, mas marca o dia como visto")
    void semNadaAdizer() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        tarefa(ana, "Renovar o seguro", HOJE.plusDays(3));

        assertThat(lembreteService.varrer(ONZE_UTC)).isZero();
        assertThat(enviados).isEmpty();

        // A marca evita reavaliar a mesma conta a cada varredura do dia.
        assertThat(ana.getUltimoLembreteEm()).isEqualTo(HOJE);
    }

    @Test
    @DisplayName("no mesmo instante UTC, só recebe a conta cujo relógio local marca a hora")
    void cadaContaNoSeuFuso() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        Usuario bruno = conta("bruno@exemplo.com", "Asia/Tokyo", 8, true);
        tarefa(ana, "Pagar a conta de luz", HOJE);
        tarefa(bruno, "Levar o carro na revisão", HOJE);

        // 23h UTC do dia 8 já são 8h do dia 9 em Tóquio, e ainda 20h do dia 8
        // em São Paulo.
        assertThat(lembreteService.varrer(VINTE_E_TRES_UTC_DA_VESPERA)).isEqualTo(1);
        assertThat(destinatarios()).containsExactly("bruno@exemplo.com");

        // Doze horas depois é a vez de São Paulo, e Tóquio não repete.
        assertThat(lembreteService.varrer(ONZE_UTC)).isEqualTo(1);
        assertThat(destinatarios()).containsExactly("bruno@exemplo.com", "ana@exemplo.com");

        assertThat(ana.getUltimoLembreteEm()).isEqualTo(HOJE);
        assertThat(bruno.getUltimoLembreteEm()).isEqualTo(HOJE);
    }

    @Test
    @DisplayName("o dia da conta é o dia local, não o do servidor")
    void oHojeEhOLocal() {
        Usuario bruno = conta("bruno@exemplo.com", "Asia/Tokyo", 8, true);
        tarefa(bruno, "Levar o carro na revisão", HOJE);

        // Em UTC ainda é dia 8, e uma tarefa para o dia 9 não venceria hoje.
        // Para quem está em Tóquio já é dia 9, e vence.
        lembreteService.varrer(VINTE_E_TRES_UTC_DA_VESPERA);

        assertThat(enviados).hasSize(1);
        assertThat(titulos(enviados.get(0).vencemHoje())).containsExactly("Levar o carro na revisão");
    }

    @Test
    @DisplayName("conta desativada não recebe")
    void contaDesativada() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        ana.setAtivo(false);
        tarefa(ana, "Pagar a conta de luz", HOJE);

        assertThat(lembreteService.varrer(ONZE_UTC)).isZero();
        assertThat(enviados).isEmpty();
    }

    @Test
    @DisplayName("fuso inválido gravado no banco não interrompe as outras contas")
    void fusoInvalidoNaoDerrubaAVarredura() {
        // A API valida o fuso antes de gravar, então isto só chega aqui por
        // escrita direta no banco. Ainda assim uma linha ruim não pode fazer
        // as contas seguintes deixarem de receber.
        Usuario quebrada = conta("quebrada@exemplo.com", "Marte/Olympus", 8, true);
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        tarefa(quebrada, "Nunca sai", HOJE);
        tarefa(ana, "Pagar a conta de luz", HOJE);

        assertThat(lembreteService.varrer(ONZE_UTC)).isEqualTo(1);
        assertThat(destinatarios()).containsExactly("ana@exemplo.com");
    }

    @Test
    @DisplayName("um envio que falha não leva junto as outras contas")
    void falhaDeEnvioNaoParaAVarredura() {
        Usuario ana = conta("ana@exemplo.com", "America/Sao_Paulo", 8, true);
        Usuario bruno = conta("bruno@exemplo.com", "America/Sao_Paulo", 8, true);
        tarefa(ana, "Pagar a conta de luz", HOJE);
        tarefa(bruno, "Levar o carro na revisão", HOJE);

        // SMTP fora do ar para a Ana, e só para ela.
        doAnswer(chamada -> {
            Lembrete lembrete = chamada.getArgument(0);
            if (lembrete.destinatario().equals("ana@exemplo.com")) {
                throw new IllegalStateException("SMTP fora do ar");
            }
            return enviados.add(lembrete);
        }).when(enviador).enviar(any());

        assertThat(lembreteService.varrer(ONZE_UTC)).isEqualTo(1);
        assertThat(destinatarios()).containsExactly("bruno@exemplo.com");

        // Sem a marca, a próxima varredura tenta de novo pela Ana.
        assertThat(ana.getUltimoLembreteEm()).isNull();
        assertThat(bruno.getUltimoLembreteEm()).isEqualTo(HOJE);
    }

    private Usuario conta(String email, String fuso, int hora, boolean lembretes) {
        return usuarioRepository.save(Usuario.builder()
                .nome(nomeDe(email) + " de Exemplo")
                .email(email)
                .senhaHash("$2a$10$hashQueNenhumTesteDaquiUsa")
                .fusoHorario(fuso)
                .horaLembrete(hora)
                .lembretesAtivos(lembretes)
                .build());
    }

    private long tarefa(Usuario dono, String titulo, LocalDate prazo) {
        return taskService.criar(dono.getId(),
                TaskRequest.builder().titulo(titulo).prazo(prazo).build()).getId();
    }

    private List<String> destinatarios() {
        return enviados.stream().map(Lembrete::destinatario).toList();
    }

    private static List<String> titulos(List<TaskResponse> tarefas) {
        return tarefas.stream().map(TaskResponse::getTitulo).toList();
    }

    private static String nomeDe(String email) {
        String usuario = email.substring(0, email.indexOf('@'));
        return Character.toUpperCase(usuario.charAt(0)) + usuario.substring(1);
    }
}
