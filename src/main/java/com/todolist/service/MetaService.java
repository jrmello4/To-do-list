package com.todolist.service;

import com.todolist.dto.MetaAporteRequest;
import com.todolist.dto.MetaRequest;
import com.todolist.dto.MetaResponse;
import com.todolist.entity.Meta;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.MetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaRepository metaRepository;
    private final AuthService authService;

    @Transactional
    public List<MetaResponse> listarMetas() {
        User usuario = authService.obterUsuarioAutenticado();
        if (metaRepository.countByUsuarioAndAtivoTrue(usuario) == 0) {
            inicializarMetasPadrao(usuario);
        }
        return metaRepository.findByUsuarioAndAtivoTrueOrderByConcluidaAscPrazoAsc(usuario)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MetaResponse criarMeta(MetaRequest request) {
        User usuario = authService.obterUsuarioAutenticado();

        BigDecimal atual = request.getValorAtual() != null ? request.getValorAtual() : BigDecimal.ZERO;
        boolean concluida = atual.compareTo(request.getValorAlvo()) >= 0;

        Meta meta = Meta.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .categoria(request.getCategoria() != null ? request.getCategoria().toUpperCase() : "GERAL")
                .valorAlvo(request.getValorAlvo())
                .valorAtual(atual)
                .unidade(request.getUnidade() != null ? request.getUnidade() : "R$")
                .prazo(request.getPrazo())
                .cor(request.getCor() != null ? request.getCor() : "#10b981")
                .icone(request.getIcone() != null ? request.getIcone() : "target")
                .concluida(concluida)
                .ativo(true)
                .usuario(usuario)
                .build();

        return toResponse(metaRepository.save(meta));
    }

    @Transactional
    public MetaResponse registrarAporte(Long metaId, MetaAporteRequest request) {
        User usuario = authService.obterUsuarioAutenticado();
        Meta meta = buscarMetaDoUsuario(metaId, usuario);

        BigDecimal novoAtual = meta.getValorAtual().add(request.getValorAporte());
        meta.setValorAtual(novoAtual);

        if (novoAtual.compareTo(meta.getValorAlvo()) >= 0) {
            meta.setConcluida(true);
        }

        return toResponse(metaRepository.save(meta));
    }

    @Transactional
    public MetaResponse atualizarMeta(Long metaId, MetaRequest request) {
        User usuario = authService.obterUsuarioAutenticado();
        Meta meta = buscarMetaDoUsuario(metaId, usuario);

        meta.setTitulo(request.getTitulo());
        meta.setDescricao(request.getDescricao());
        if (request.getCategoria() != null) meta.setCategoria(request.getCategoria().toUpperCase());
        meta.setValorAlvo(request.getValorAlvo());
        if (request.getValorAtual() != null) {
            meta.setValorAtual(request.getValorAtual());
            meta.setConcluida(request.getValorAtual().compareTo(request.getValorAlvo()) >= 0);
        }
        if (request.getUnidade() != null) meta.setUnidade(request.getUnidade());
        meta.setPrazo(request.getPrazo());
        if (request.getCor() != null) meta.setCor(request.getCor());
        if (request.getIcone() != null) meta.setIcone(request.getIcone());

        return toResponse(metaRepository.save(meta));
    }

    @Transactional
    public void excluirMeta(Long metaId) {
        User usuario = authService.obterUsuarioAutenticado();
        Meta meta = buscarMetaDoUsuario(metaId, usuario);
        meta.setAtivo(false);
        metaRepository.save(meta);
    }

    private Meta buscarMetaDoUsuario(Long id, User usuario) {
        return metaRepository.findById(id)
                .filter(m -> m.getAtivo() && m.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Meta", id));
    }

    private void inicializarMetasPadrao(User usuario) {
        Meta reserva = Meta.builder()
                .titulo("Reserva de Emergência")
                .descricao("Guardar 6 meses de custo fixo para segurança financeira")
                .categoria("FINANCEIRA")
                .valorAlvo(new BigDecimal("10000.00"))
                .valorAtual(new BigDecimal("3500.00"))
                .unidade("R$")
                .prazo(LocalDate.now().plusMonths(6))
                .cor("#10b981")
                .icone("shield")
                .concluida(false)
                .ativo(true)
                .usuario(usuario)
                .build();

        Meta leitura = Meta.builder()
                .titulo("Ler 12 Livros no Ano")
                .descricao("Manter o hábito constante de leitura e auto-desenvolvimento")
                .categoria("ESTUDO")
                .valorAlvo(new BigDecimal("12.00"))
                .valorAtual(new BigDecimal("4.00"))
                .unidade("livros")
                .prazo(LocalDate.now().plusMonths(4))
                .cor("#6366f1")
                .icone("book")
                .concluida(false)
                .ativo(true)
                .usuario(usuario)
                .build();

        metaRepository.saveAll(List.of(reserva, leitura));
    }

    public MetaResponse toResponse(Meta m) {
        double pct = 0.0;
        if (m.getValorAlvo() != null && m.getValorAlvo().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal proporcao = m.getValorAtual().divide(m.getValorAlvo(), 4, RoundingMode.HALF_UP);
            pct = Math.min(100.0, proporcao.multiply(BigDecimal.valueOf(100)).doubleValue());
        }

        return MetaResponse.builder()
                .id(m.getId())
                .titulo(m.getTitulo())
                .descricao(m.getDescricao())
                .categoria(m.getCategoria())
                .valorAlvo(m.getValorAlvo())
                .valorAtual(m.getValorAtual())
                .unidade(m.getUnidade())
                .prazo(m.getPrazo())
                .cor(m.getCor())
                .icone(m.getIcone())
                .concluida(m.getConcluida())
                .percentualConcluido(pct)
                .dataCriacao(m.getDataCriacao())
                .dataAtualizacao(m.getDataAtualizacao())
                .build();
    }
}
