package com.todolist.service;

import com.todolist.dto.TransacaoRequest;
import com.todolist.dto.TransacaoResponse;
import com.todolist.entity.*;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.CategoriaTransacaoRepository;
import com.todolist.repository.ContaFinanceiraRepository;
import com.todolist.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransacaoService {

    private final TransacaoRepository transacaoRepository;
    private final ContaFinanceiraRepository contaRepository;
    private final CategoriaTransacaoRepository categoriaRepository;
    private final AuthService authService;

    public List<TransacaoResponse> listarTodas(LocalDate inicio, LocalDate fim, StatusTransacao status, TipoTransacao tipo, Long contaId) {
        User user = authService.obterUsuarioAutenticado();

        if (inicio == null) {
            inicio = LocalDate.now().withDayOfMonth(1);
        }
        if (fim == null) {
            fim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        List<Transacao> transacoes = transacaoRepository.findByUsuarioIdAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                user.getId(), inicio, fim);

        return transacoes.stream()
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> tipo == null || t.getTipo() == tipo)
                .filter(t -> contaId == null || t.getConta().getId().equals(contaId))
                .map(this::paraResponse)
                .collect(Collectors.toList());
    }

    public TransacaoResponse buscarPorId(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Transacao transacao = transacaoRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));
        return paraResponse(transacao);
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request) {
        User user = authService.obterUsuarioAutenticado();

        ContaFinanceira conta = contaRepository.findByIdAndUsuarioId(request.getContaId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", request.getContaId()));

        CategoriaTransacao categoria = null;
        if (request.getCategoriaId() != null) {
            categoria = categoriaRepository.findByIdAndUsuarioId(request.getCategoriaId(), user.getId())
                    .orElse(null);
        }

        StatusTransacao status = request.getStatus() != null ? request.getStatus() : StatusTransacao.PENDENTE;
        boolean parcelado = Boolean.TRUE.equals(request.getParcelado())
                && request.getTotalParcelas() != null
                && request.getTotalParcelas() > 1;

        if (parcelado) {
            String grupoId = UUID.randomUUID().toString();
            int totalParcelas = request.getTotalParcelas();
            List<Transacao> listaParcelas = new ArrayList<>();

            for (int i = 1; i <= totalParcelas; i++) {
                LocalDate vencimentoParcela = request.getDataVencimento().plusMonths(i - 1);
                StatusTransacao statusParcela = (i == 1 && status == StatusTransacao.PAGO) ? StatusTransacao.PAGO : StatusTransacao.PENDENTE;
                LocalDate dataPagamentoParcela = (statusParcela == StatusTransacao.PAGO)
                        ? (request.getDataPagamento() != null ? request.getDataPagamento() : LocalDate.now())
                        : null;

                Transacao parcela = Transacao.builder()
                        .descricao(request.getDescricao().trim() + " (" + i + "/" + totalParcelas + ")")
                        .tipo(request.getTipo())
                        .valor(request.getValor())
                        .dataVencimento(vencimentoParcela)
                        .dataPagamento(dataPagamentoParcela)
                        .status(statusParcela)
                        .parcelado(true)
                        .numeroParcela(i)
                        .totalParcelas(totalParcelas)
                        .grupoParcelaId(grupoId)
                        .observacoes(request.getObservacoes())
                        .conta(conta)
                        .categoria(categoria)
                        .usuario(user)
                        .build();

                listaParcelas.add(parcela);

                if (statusParcela == StatusTransacao.PAGO) {
                    aplicarImpactoSaldo(conta, parcela.getTipo(), parcela.getValor());
                }
            }

            contaRepository.save(conta);
            List<Transacao> salvas = transacaoRepository.saveAll(listaParcelas);
            return paraResponse(salvas.get(0));
        }

        LocalDate dataPagamento = (status == StatusTransacao.PAGO)
                ? (request.getDataPagamento() != null ? request.getDataPagamento() : LocalDate.now())
                : null;

        Transacao transacao = Transacao.builder()
                .descricao(request.getDescricao().trim())
                .tipo(request.getTipo())
                .valor(request.getValor())
                .dataVencimento(request.getDataVencimento())
                .dataPagamento(dataPagamento)
                .status(status)
                .parcelado(false)
                .observacoes(request.getObservacoes())
                .conta(conta)
                .categoria(categoria)
                .usuario(user)
                .build();

        if (status == StatusTransacao.PAGO) {
            aplicarImpactoSaldo(conta, transacao.getTipo(), transacao.getValor());
            contaRepository.save(conta);
        }

        Transacao salva = transacaoRepository.save(transacao);
        return paraResponse(salva);
    }

    @Transactional
    public TransacaoResponse atualizarStatus(Long id, StatusTransacao novoStatus) {
        User user = authService.obterUsuarioAutenticado();
        Transacao transacao = transacaoRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));

        if (transacao.getStatus() != novoStatus) {
            ContaFinanceira conta = transacao.getConta();
            if (novoStatus == StatusTransacao.PAGO) {
                transacao.setStatus(StatusTransacao.PAGO);
                transacao.setDataPagamento(LocalDate.now());
                aplicarImpactoSaldo(conta, transacao.getTipo(), transacao.getValor());
            } else {
                transacao.setStatus(StatusTransacao.PENDENTE);
                transacao.setDataPagamento(null);
                reverterImpactoSaldo(conta, transacao.getTipo(), transacao.getValor());
            }
            contaRepository.save(conta);
            transacao = transacaoRepository.save(transacao);
        }

        return paraResponse(transacao);
    }

    @Transactional
    public void excluir(Long id) {
        User user = authService.obterUsuarioAutenticado();
        Transacao transacao = transacaoRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transação", id));

        if (transacao.getStatus() == StatusTransacao.PAGO) {
            ContaFinanceira conta = transacao.getConta();
            reverterImpactoSaldo(conta, transacao.getTipo(), transacao.getValor());
            contaRepository.save(conta);
        }

        transacaoRepository.delete(transacao);
    }

    private void aplicarImpactoSaldo(ContaFinanceira conta, TipoTransacao tipo, BigDecimal valor) {
        if (tipo == TipoTransacao.RECEITA) {
            conta.setSaldoAtual(conta.getSaldoAtual().add(valor));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(valor));
        }
    }

    private void reverterImpactoSaldo(ContaFinanceira conta, TipoTransacao tipo, BigDecimal valor) {
        if (tipo == TipoTransacao.RECEITA) {
            conta.setSaldoAtual(conta.getSaldoAtual().subtract(valor));
        } else {
            conta.setSaldoAtual(conta.getSaldoAtual().add(valor));
        }
    }

    public TransacaoResponse paraResponse(Transacao t) {
        LocalDate hoje = LocalDate.now();
        boolean atrasada = t.getStatus() == StatusTransacao.PENDENTE && t.getDataVencimento().isBefore(hoje);

        return TransacaoResponse.builder()
                .id(t.getId())
                .descricao(t.getDescricao())
                .tipo(t.getTipo())
                .valor(t.getValor())
                .dataVencimento(t.getDataVencimento())
                .dataPagamento(t.getDataPagamento())
                .status(t.getStatus())
                .estaAtrasada(atrasada)
                .parcelado(t.getParcelado())
                .numeroParcela(t.getNumeroParcela())
                .totalParcelas(t.getTotalParcelas())
                .grupoParcelaId(t.getGrupoParcelaId())
                .observacoes(t.getObservacoes())
                .contaId(t.getConta().getId())
                .contaNome(t.getConta().getNome())
                .contaCor(t.getConta().getCor())
                .categoriaId(t.getCategoria() != null ? t.getCategoria().getId() : null)
                .categoriaNome(t.getCategoria() != null ? t.getCategoria().getNome() : "Sem categoria")
                .categoriaIcone(t.getCategoria() != null ? t.getCategoria().getIcone() : "tag")
                .categoriaCor(t.getCategoria() != null ? t.getCategoria().getCor() : "#64748b")
                .build();
    }
}