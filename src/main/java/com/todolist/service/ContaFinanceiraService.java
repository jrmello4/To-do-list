package com.todolist.service;

import com.todolist.dto.ContaRequest;
import com.todolist.dto.ContaResponse;
import com.todolist.entity.ContaFinanceira;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.ContaFinanceiraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContaFinanceiraService {

    private final ContaFinanceiraRepository contaRepository;
    private final AuthService authService;

    public List<ContaResponse> listarTodas() {
        User user = authService.obterUsuarioAutenticado();
        List<ContaFinanceira> contas = contaRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(user.getId());
        if (contas.isEmpty()) {
            // Cria uma conta padrão inicial se o usuário não tiver nenhuma
            ContaFinanceira padrao = ContaFinanceira.builder()
                    .nome("Conta Principal")
                    .tipo(com.todolist.entity.TipoConta.CORRENTE)
                    .saldoInicial(BigDecimal.ZERO)
                    .saldoAtual(BigDecimal.ZERO)
                    .cor("#6366f1")
                    .ativo(true)
                    .usuario(user)
                    .build();
            contaRepository.save(padrao);
            contas = List.of(padrao);
        }
        return contas.stream().map(this::paraResponse).collect(Collectors.toList());
    }

    public ContaResponse buscarPorId(Long id) {
        User user = authService.obterUsuarioAutenticado();
        ContaFinanceira conta = contaRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
        return paraResponse(conta);
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        User user = authService.obterUsuarioAutenticado();
        BigDecimal saldoInicial = request.getSaldoInicial() != null ? request.getSaldoInicial() : BigDecimal.ZERO;
        String cor = (request.getCor() != null && !request.getCor().isBlank()) ? request.getCor() : "#6366f1";

        ContaFinanceira conta = ContaFinanceira.builder()
                .nome(request.getNome().trim())
                .tipo(request.getTipo())
                .saldoInicial(saldoInicial)
                .saldoAtual(saldoInicial)
                .cor(cor)
                .ativo(true)
                .usuario(user)
                .build();

        ContaFinanceira salva = contaRepository.save(conta);
        return paraResponse(salva);
    }

    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest request) {
        User user = authService.obterUsuarioAutenticado();
        ContaFinanceira conta = contaRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));

        conta.setNome(request.getNome().trim());
        conta.setTipo(request.getTipo());
        if (request.getCor() != null && !request.getCor().isBlank()) {
            conta.setCor(request.getCor());
        }

        ContaFinanceira atualizada = contaRepository.save(conta);
        return paraResponse(atualizada);
    }

    @Transactional
    public void excluir(Long id) {
        User user = authService.obterUsuarioAutenticado();
        ContaFinanceira conta = contaRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", id));
        // Desativação lógica para não quebrar integridade referencial com transações existentes
        conta.setAtivo(false);
        contaRepository.save(conta);
    }

    public ContaResponse paraResponse(ContaFinanceira conta) {
        return ContaResponse.builder()
                .id(conta.getId())
                .nome(conta.getNome())
                .tipo(conta.getTipo())
                .saldoInicial(conta.getSaldoInicial())
                .saldoAtual(conta.getSaldoAtual())
                .cor(conta.getCor())
                .ativo(conta.getAtivo())
                .build();
    }
}