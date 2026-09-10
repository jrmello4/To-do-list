package com.todolist.service;

import com.todolist.dto.ContaRequest;
import com.todolist.dto.ContaResponse;
import com.todolist.entity.ContaFinanceira;
import com.todolist.entity.Role;
import com.todolist.entity.TipoConta;
import com.todolist.entity.User;
import com.todolist.repository.ContaFinanceiraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaFinanceiraServiceTest {

    @Mock
    private ContaFinanceiraRepository contaRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ContaFinanceiraService contaService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .id(1L)
                .nome("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);
    }

    @Test
    @DisplayName("Deve listar contas existentes do usuário")
    void deveListarContasDoUsuario() {
        ContaFinanceira c1 = ContaFinanceira.builder()
                .id(1L)
                .nome("Nubank")
                .tipo(TipoConta.CORRENTE)
                .saldoInicial(BigDecimal.valueOf(100))
                .saldoAtual(BigDecimal.valueOf(500))
                .cor("#820ad1")
                .ativo(true)
                .usuario(usuario)
                .build();

        when(contaRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(1L)).thenReturn(List.of(c1));

        List<ContaResponse> contas = contaService.listarTodas();

        assertThat(contas).hasSize(1);
        assertThat(contas.get(0).getNome()).isEqualTo("Nubank");
        assertThat(contas.get(0).getSaldoAtual()).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("Deve criar conta padrão se o usuário não possuir nenhuma conta")
    void deveCriarContaPadraoSeNaoHouver() {
        when(contaRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(1L)).thenReturn(new ArrayList<>());
        when(contaRepository.save(any(ContaFinanceira.class))).thenAnswer(inv -> {
            ContaFinanceira c = inv.getArgument(0);
            c.setId(10L);
            return c;
        });

        List<ContaResponse> contas = contaService.listarTodas();

        assertThat(contas).hasSize(1);
        assertThat(contas.get(0).getNome()).isEqualTo("Conta Principal");
        verify(contaRepository).save(any(ContaFinanceira.class));
    }

    @Test
    @DisplayName("Deve criar nova conta financeira com saldo inicial")
    void deveCriarNovaConta() {
        ContaRequest request = ContaRequest.builder()
                .nome("Carteira")
                .tipo(TipoConta.DINHEIRO)
                .saldoInicial(BigDecimal.valueOf(250))
                .cor("#10b981")
                .build();

        when(contaRepository.save(any(ContaFinanceira.class))).thenAnswer(inv -> {
            ContaFinanceira c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        ContaResponse response = contaService.criar(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getNome()).isEqualTo("Carteira");
        assertThat(response.getSaldoAtual()).isEqualByComparingTo("250");
    }

    @Test
    @DisplayName("Deve atualizar conta financeira")
    void deveAtualizarConta() {
        ContaFinanceira conta = ContaFinanceira.builder()
                .id(1L)
                .nome("Antigo Nome")
                .tipo(TipoConta.CORRENTE)
                .saldoInicial(BigDecimal.ZERO)
                .saldoAtual(BigDecimal.ZERO)
                .cor("#6366f1")
                .ativo(true)
                .usuario(usuario)
                .build();

        when(contaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(conta));
        when(contaRepository.save(any(ContaFinanceira.class))).thenAnswer(inv -> inv.getArgument(0));

        ContaRequest request = ContaRequest.builder()
                .nome("Novo Nome")
                .tipo(TipoConta.INVESTIMENTO)
                .cor("#3b82f6")
                .build();

        ContaResponse response = contaService.atualizar(1L, request);

        assertThat(response.getNome()).isEqualTo("Novo Nome");
        assertThat(response.getTipo()).isEqualTo(TipoConta.INVESTIMENTO);
    }

    @Test
    @DisplayName("Deve desativar logicamente conta ao excluir")
    void deveDesativarContaAoExcluir() {
        ContaFinanceira conta = ContaFinanceira.builder()
                .id(1L)
                .nome("Nubank")
                .tipo(TipoConta.CORRENTE)
                .ativo(true)
                .usuario(usuario)
                .build();

        when(contaRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(conta));

        contaService.excluir(1L);

        assertThat(conta.getAtivo()).isFalse();
        verify(contaRepository).save(conta);
    }
}