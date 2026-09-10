package com.todolist.controller;

import com.todolist.dto.TransferenciaRequest;
import com.todolist.entity.StatusTransacao;
import com.todolist.entity.TipoTransacao;
import com.todolist.service.AuthService;
import com.todolist.service.TransacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todolist.dto.ContaRequest;
import com.todolist.dto.ContaResponse;
import com.todolist.dto.RegisterRequest;
import com.todolist.entity.TipoConta;
import com.todolist.service.AuthService;
import com.todolist.service.ContaFinanceiraService;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FinancasAvancadasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService authService;

    @Autowired
    private ContaFinanceiraService contaService;

    @Autowired
    private TransacaoService transacaoService;

    private String token;

    @BeforeEach
    void setup() throws Exception {
        String email = "fin" + System.nanoTime() + "@teste.com";
        RegisterRequest reg = new RegisterRequest("Teste Fin", email, "SenhaForte123");
        token = authService.cadastrar(reg).getToken();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(email, null, java.util.List.of()));
    }

    @Test
    @DisplayName("Transferência move saldo entre contas")
    void transferenciaMoveSaldo() {
        ContaResponse a = contaService.criar(ContaRequest.builder()
                .nome("Origem").tipo(TipoConta.CORRENTE).saldoInicial(new BigDecimal("1000")).build());
        ContaResponse b = contaService.criar(ContaRequest.builder()
                .nome("Destino").tipo(TipoConta.DINHEIRO).saldoInicial(new BigDecimal("100")).build());

        TransferenciaRequest req = TransferenciaRequest.builder()
                .contaOrigemId(a.getId())
                .contaDestinoId(b.getId())
                .valor(new BigDecimal("250"))
                .build();
        var resp = transacaoService.transferir(req);

        assertThat(new BigDecimal(String.valueOf(resp.get("saldoOrigem")))).isEqualByComparingTo("750");
        assertThat(new BigDecimal(String.valueOf(resp.get("saldoDestino")))).isEqualByComparingTo("350");
    }

    @Test
    @DisplayName("Projeção via API retorna payload válido")
    void projecaoRetornaPayload() throws Exception {
        mockMvc.perform(get("/api/financas/transacoes/projecao?dias=30")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
                    assertThat(node.has("saldoAtual")).isTrue();
                    assertThat(node.has("pontos")).isTrue();
                });
    }

    @Test
    @DisplayName("Orçamento responde lista")
    void orcamentoResponde() throws Exception {
        mockMvc.perform(get("/api/financas/transacoes/orcamento")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Alertas de esporte respondem lista")
    void alertasEsporte() throws Exception {
        mockMvc.perform(get("/api/esportes/alertas")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
