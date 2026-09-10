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
    private final com.todolist.repository.MetaRepository metaRepository;
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
                .filter(t -> contaId == null || (t.getConta() != null && contaId.equals(t.getConta().getId())))
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
            if (transacao.getTipo() == TipoTransacao.RECEITA) {
                aplicarAutoAporteEmMetas(user, transacao.getValor());
            }
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
                if (transacao.getTipo() == TipoTransacao.RECEITA) {
                    aplicarAutoAporteEmMetas(user, transacao.getValor());
                }
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

    /** Desvia um % de receitas pagas para metas com auto-aporte ativo. */
    private void aplicarAutoAporteEmMetas(User user, BigDecimal valorReceita) {
        List<com.todolist.entity.Meta> metas = metaRepository.findByUsuarioAndAtivoTrueOrderByConcluidaAscPrazoAsc(user);
        for (com.todolist.entity.Meta meta : metas) {
            if (!Boolean.TRUE.equals(meta.getAutoAporteAtivo()) || meta.getConcluida()) {
                continue;
            }
            if (meta.getAutoAportePercentual() == null
                    || meta.getAutoAportePercentual().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal aporte = valorReceita.multiply(meta.getAutoAportePercentual())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            if (aporte.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            meta.setValorAtual(meta.getValorAtual().add(aporte));
            if (meta.getValorAtual().compareTo(meta.getValorAlvo()) >= 0) {
                meta.setConcluida(true);
            }
            metaRepository.save(meta);
        }
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

    /** Transferência imediata entre contas do mesmo usuário (afeta saldos na hora). */
    @Transactional
    public java.util.Map<String, Object> transferir(com.todolist.dto.TransferenciaRequest request) {
        User user = authService.obterUsuarioAutenticado();
        if (request.getContaOrigemId().equals(request.getContaDestinoId())) {
            throw new IllegalArgumentException("Conta de origem e destino devem ser diferentes.");
        }
        ContaFinanceira origem = contaRepository.findByIdAndUsuarioId(request.getContaOrigemId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", request.getContaOrigemId()));
        ContaFinanceira destino = contaRepository.findByIdAndUsuarioId(request.getContaDestinoId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Conta", request.getContaDestinoId()));

        String desc = (request.getDescricao() != null && !request.getDescricao().isBlank())
                ? request.getDescricao().trim()
                : ("Transferência " + origem.getNome() + " → " + destino.getNome());

        LocalDate hoje = LocalDate.now();

        Transacao saida = Transacao.builder()
                .descricao(desc)
                .tipo(TipoTransacao.DESPESA)
                .valor(request.getValor())
                .dataVencimento(hoje)
                .dataPagamento(hoje)
                .status(StatusTransacao.PAGO)
                .conta(origem)
                .usuario(user)
                .observacoes("Transferência para " + destino.getNome())
                .build();

        Transacao entrada = Transacao.builder()
                .descricao(desc)
                .tipo(TipoTransacao.RECEITA)
                .valor(request.getValor())
                .dataVencimento(hoje)
                .dataPagamento(hoje)
                .status(StatusTransacao.PAGO)
                .conta(destino)
                .usuario(user)
                .observacoes("Transferência de " + origem.getNome())
                .build();

        aplicarImpactoSaldo(origem, TipoTransacao.DESPESA, request.getValor());
        aplicarImpactoSaldo(destino, TipoTransacao.RECEITA, request.getValor());
        contaRepository.save(origem);
        contaRepository.save(destino);
        transacaoRepository.save(saida);
        transacaoRepository.save(entrada);

        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("saida", paraResponse(saida));
        resp.put("entrada", paraResponse(entrada));
        resp.put("saldoOrigem", origem.getSaldoAtual());
        resp.put("saldoDestino", destino.getSaldoAtual());
        return resp;
    }

    /** Orçamento do mês: gasto real vs limite por categoria de despesa. */
    @Transactional(readOnly = true)
    public List<com.todolist.dto.OrcamentoItemResponse> listarOrcamento(Integer ano, Integer mes) {
        User user = authService.obterUsuarioAutenticado();
        LocalDate hoje = LocalDate.now();
        int a = (ano != null) ? ano : hoje.getYear();
        int m = (mes != null) ? mes : hoje.getMonthValue();
        LocalDate inicio = LocalDate.of(a, m, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<Transacao> gastos = transacaoRepository.findByUsuarioIdAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                user.getId(), inicio, fim).stream()
                .filter(t -> t.getStatus() == StatusTransacao.PAGO && t.getTipo() == TipoTransacao.DESPESA)
                .collect(Collectors.toList());

        java.util.Map<Long, com.todolist.dto.OrcamentoItemResponse> porCat = new java.util.LinkedHashMap<>();
        for (Transacao t : gastos) {
            if (t.getCategoria() == null) {
                continue;
            }
            Long catId = t.getCategoria().getId();
            com.todolist.dto.OrcamentoItemResponse item = porCat.get(catId);
            if (item == null) {
                item = com.todolist.dto.OrcamentoItemResponse.builder()
                        .categoriaId(catId)
                        .categoriaNome(t.getCategoria().getNome())
                        .categoriaCor(t.getCategoria().getCor())
                        .categoriaIcone(t.getCategoria().getIcone())
                        .limite(t.getCategoria().getLimiteMensal())
                        .gasto(BigDecimal.ZERO)
                        .build();
                porCat.put(catId, item);
            }
            item.setGasto(item.getGasto().add(t.getValor()));
        }

        // Categorias com limite mas sem gasto ainda
        List<CategoriaTransacao> cats = categoriaRepository.findByUsuarioIdAndTipoOrderByNomeAsc(
                user.getId(), TipoTransacao.DESPESA);
        for (CategoriaTransacao c : cats) {
            if (c.getLimiteMensal() != null && !porCat.containsKey(c.getId())) {
                porCat.put(c.getId(), com.todolist.dto.OrcamentoItemResponse.builder()
                        .categoriaId(c.getId())
                        .categoriaNome(c.getNome())
                        .categoriaCor(c.getCor())
                        .categoriaIcone(c.getIcone())
                        .limite(c.getLimiteMensal())
                        .gasto(BigDecimal.ZERO)
                        .build());
            }
        }

        List<com.todolist.dto.OrcamentoItemResponse> lista = new ArrayList<>(porCat.values());
        for (com.todolist.dto.OrcamentoItemResponse it : lista) {
            if (it.getLimite() != null && it.getLimite().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal restante = it.getLimite().subtract(it.getGasto());
                it.setRestante(restante);
                double pct = it.getGasto().multiply(BigDecimal.valueOf(100))
                        .divide(it.getLimite(), 2, java.math.RoundingMode.HALF_UP).doubleValue();
                it.setPercentualUsado(pct);
                it.setEstourado(it.getGasto().compareTo(it.getLimite()) > 0);
            } else {
                it.setRestante(null);
                it.setPercentualUsado(null);
                it.setEstourado(false);
            }
        }
        lista.sort((x, y) -> {
            if (x.getLimite() == null && y.getLimite() == null) return x.getCategoriaNome().compareTo(y.getCategoriaNome());
            if (x.getLimite() == null) return 1;
            if (y.getLimite() == null) return -1;
            return y.getGasto().compareTo(x.getGasto());
        });
        return lista;
    }

    /** Projeção de saldo para os próximos N dias a partir das transações pendentes/pagas. */
    @Transactional(readOnly = true)
    public com.todolist.dto.ProjecaoResponse projetarSaldo(Integer dias) {
        User user = authService.obterUsuarioAutenticado();
        int n = (dias != null && dias > 0 && dias <= 365) ? dias : 90;
        LocalDate inicio = LocalDate.now();
        LocalDate fim = inicio.plusDays(n);

        List<ContaFinanceira> contas = contaRepository.findByUsuarioIdAndAtivoTrueOrderByNomeAsc(user.getId());
        BigDecimal saldoAtual = contas.stream()
                .map(ContaFinanceira::getSaldoAtual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Transacao> todas = transacaoRepository.findByUsuarioIdAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                user.getId(), inicio, fim);

        java.util.Map<LocalDate, BigDecimal> receitasDia = new java.util.TreeMap<>();
        java.util.Map<LocalDate, BigDecimal> despesasDia = new java.util.TreeMap<>();
        BigDecimal recTotal = BigDecimal.ZERO;
        BigDecimal despTotal = BigDecimal.ZERO;

        for (Transacao t : todas) {
            LocalDate d = t.getDataVencimento();
            if (d == null) continue;
            // Pago já entrou no saldo atual — só conta pendentes na projeção
            if (t.getStatus() == StatusTransacao.PAGO) {
                continue;
            }
            if (t.getTipo() == TipoTransacao.RECEITA) {
                receitasDia.merge(d, t.getValor(), BigDecimal::add);
                recTotal = recTotal.add(t.getValor());
            } else {
                despesasDia.merge(d, t.getValor(), BigDecimal::add);
                despTotal = despTotal.add(t.getValor());
            }
        }

        BigDecimal acumulado = saldoAtual;
        List<com.todolist.dto.ProjecaoResponse.Ponto> pontos = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            LocalDate d = inicio.plusDays(i);
            BigDecimal rec = receitasDia.getOrDefault(d, BigDecimal.ZERO);
            BigDecimal desp = despesasDia.getOrDefault(d, BigDecimal.ZERO);
            acumulado = acumulado.add(rec).subtract(desp);
            pontos.add(com.todolist.dto.ProjecaoResponse.Ponto.builder()
                    .data(d)
                    .receitasDoDia(rec)
                    .despesasDoDia(desp)
                    .saldoAcumulado(acumulado)
                    .build());
        }

        return com.todolist.dto.ProjecaoResponse.builder()
                .inicio(inicio)
                .fim(fim)
                .saldoAtual(saldoAtual)
                .receitasPrevistas(recTotal)
                .despesasPrevistas(despTotal)
                .saldoProjetado(acumulado)
                .pontos(pontos)
                .build();
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