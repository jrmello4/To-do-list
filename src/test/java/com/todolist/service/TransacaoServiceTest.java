package com.todolist.service;

import com.todolist.dto.TransacaoRequest;
import com.todolist.dto.TransacaoResponse;
import com.todolist.entity.*;
import com.todolist.repository.CategoriaTransacaoRepository;
import com.todolist.repository.ContaFinanceiraRepository;
import com.todolist.repository.TransacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransacaoServiceTest {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private ContaFinanceiraRepository contaRepository;

    @Mock
    private CategoriaTransacaoRepository categoriaRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TransacaoService transacaoService;

    private User usuario;
    private ContaFinanceira conta;
    private CategoriaTransacao categoria;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .id(1L)
                .nome("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();

        conta = ContaFinanceira.builder()
                .id(1L)
                .nome("Banco Inter")
                .tipo(TipoConta.CORRENTE)
                .saldoInicial(BigDecimal.valueOf(1000))
                .saldoAtual(BigDecimal.valueOf(1000))
                .cor("#ff7a00")
                .ativo(true)
                .usuario(usuario)
                .build();

        categoria = CategoriaTransacao.builder()
                .id(1L)
                .nome("Alimentação")
                .tipo(TipoTransacao.DESPESA)
                .icone("utensils")
                .cor("#f59e0b")
                .usuario(usuario)
                .build();

        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);
    }

    @Test
    @DisplayName("Deve criar transação simples pendente sem alterar saldo da conta")
    void deveCriarTransacaoSimplesPendente() {
        when(contaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });

        TransacaoRequest request = TransacaoRequest.builder()
                .descricao("Supermercado")
                .tipo(TipoTransacao.DESPESA)
                .valor(BigDecimal.valueOf(150))
                .dataVencimento(LocalDate.now().plusDays(3))
                .status(StatusTransacao.PENDENTE)
                .contaId(1L)
                .categoriaId(1L)
                .build();

        TransacaoResponse response = transacaoService.criar(request);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(StatusTransacao.PENDENTE);
        assertThat(conta.getSaldoAtual()).isEqualByComparingTo("1000"); // Saldo inalterado
        verify(contaRepository, never()).save(conta);
    }

    @Test
    @DisplayName("Deve criar transação paga e debitar do saldo da conta")
    void deveCriarTransacaoPagaEAtualizarSaldo() {
        when(contaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            t.setId(11L);
            return t;
        });

        TransacaoRequest request = TransacaoRequest.builder()
                .descricao("Restaurante")
                .tipo(TipoTransacao.DESPESA)
                .valor(BigDecimal.valueOf(200))
                .dataVencimento(LocalDate.now())
                .status(StatusTransacao.PAGO)
                .contaId(1L)
                .categoriaId(1L)
                .build();

        TransacaoResponse response = transacaoService.criar(request);

        assertThat(response.getId()).isEqualTo(11L);
        assertThat(response.getStatus()).isEqualTo(StatusTransacao.PAGO);
        assertThat(conta.getSaldoAtual()).isEqualByComparingTo("800"); // 1000 - 200
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("Deve criar parcelamento gerando N parcelas com vencimentos mensais")
    void deveCriarTransacaoParceladaComSucesso() {
        when(contaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(conta));
        when(categoriaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(categoria));
        when(transacaoRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        TransacaoRequest request = TransacaoRequest.builder()
                .descricao("Notebook")
                .tipo(TipoTransacao.DESPESA)
                .valor(BigDecimal.valueOf(300))
                .dataVencimento(LocalDate.of(2026, 1, 10))
                .status(StatusTransacao.PENDENTE)
                .parcelado(true)
                .totalParcelas(3)
                .contaId(1L)
                .categoriaId(1L)
                .build();

        TransacaoResponse response = transacaoService.criar(request);

        assertThat(response.getParcelado()).isTrue();
        assertThat(response.getTotalParcelas()).isEqualTo(3);
        verify(transacaoRepository).saveAll(argThat(iterable -> {
            List<Transacao> lista = (List<Transacao>) iterable;
            return lista.size() == 3 &&
                    lista.get(0).getDataVencimento().equals(LocalDate.of(2026, 1, 10)) &&
                    lista.get(1).getDataVencimento().equals(LocalDate.of(2026, 2, 10)) &&
                    lista.get(2).getDataVencimento().equals(LocalDate.of(2026, 3, 10));
        }));
    }

    @Test
    @DisplayName("Deve atualizar status de pendente para pago e debitar da conta")
    void deveAtualizarStatusTransacaoEModificarSaldo() {
        Transacao transacao = Transacao.builder()
                .id(1L)
                .descricao("Conta de Energia")
                .tipo(TipoTransacao.DESPESA)
                .valor(BigDecimal.valueOf(150))
                .status(StatusTransacao.PENDENTE)
                .dataVencimento(LocalDate.now())
                .conta(conta)
                .usuario(usuario)
                .build();

        when(transacaoRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(transacao));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(inv -> inv.getArgument(0));

        TransacaoResponse response = transacaoService.atualizarStatus(1L, StatusTransacao.PAGO);

        assertThat(response.getStatus()).isEqualTo(StatusTransacao.PAGO);
        assertThat(conta.getSaldoAtual()).isEqualByComparingTo("850"); // 1000 - 150
        verify(contaRepository).save(conta);
    }

    @Test
    @DisplayName("Deve excluir transação e estornar valor se já havia sido paga")
    void deveExcluirTransacaoRevertendoSaldoSePaga() {
        conta.setSaldoAtual(BigDecimal.valueOf(850));
        Transacao transacao = Transacao.builder()
                .id(1L)
                .descricao("Internet")
                .tipo(TipoTransacao.DESPESA)
                .valor(BigDecimal.valueOf(150))
                .status(StatusTransacao.PAGO)
                .dataVencimento(LocalDate.now())
                .conta(conta)
                .usuario(usuario)
                .build();

        when(transacaoRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(transacao));

        transacaoService.excluir(1L);

        assertThat(conta.getSaldoAtual()).isEqualByComparingTo("1000"); // Estornado 850 + 150
        verify(contaRepository).save(conta);
        verify(transacaoRepository).delete(transacao);
    }
}