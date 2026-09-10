package com.todolist.service;

import com.todolist.dto.CategoriaTransacaoRequest;
import com.todolist.dto.CategoriaTransacaoResponse;
import com.todolist.entity.CategoriaTransacao;
import com.todolist.entity.TipoTransacao;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.CategoriaTransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaTransacaoService {

    private final CategoriaTransacaoRepository categoriaRepository;
    private final AuthService authService;

    public List<CategoriaTransacaoResponse> listarTodas() {
        User user = authService.obterUsuarioAutenticado();
        garantirCategoriasPadrao(user);
        return categoriaRepository.findByUsuarioIdOrderByNomeAsc(user.getId())
                .stream().map(this::paraResponse).collect(Collectors.toList());
    }

    public List<CategoriaTransacaoResponse> listarPorTipo(TipoTransacao tipo) {
        User user = authService.obterUsuarioAutenticado();
        garantirCategoriasPadrao(user);
        return categoriaRepository.findByUsuarioIdAndTipoOrderByNomeAsc(user.getId(), tipo)
                .stream().map(this::paraResponse).collect(Collectors.toList());
    }

    @Transactional
    public CategoriaTransacaoResponse criar(CategoriaTransacaoRequest request) {
        User user = authService.obterUsuarioAutenticado();
        String icone = (request.getIcone() != null && !request.getIcone().isBlank()) ? request.getIcone() : "tag";
        String cor = (request.getCor() != null && !request.getCor().isBlank()) ? request.getCor() : "#6366f1";

        CategoriaTransacao categoria = CategoriaTransacao.builder()
                .nome(request.getNome().trim())
                .tipo(request.getTipo())
                .icone(icone)
                .cor(cor)
                .usuario(user)
                .build();

        return paraResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        User user = authService.obterUsuarioAutenticado();
        CategoriaTransacao categoria = categoriaRepository.findByIdAndUsuarioId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", id));
        categoriaRepository.delete(categoria);
    }

    @Transactional
    public void garantirCategoriasPadrao(User user) {
        List<CategoriaTransacao> existentes = categoriaRepository.findByUsuarioIdOrderByNomeAsc(user.getId());
        if (!existentes.isEmpty()) {
            return;
        }

        List<CategoriaTransacao> padroes = new ArrayList<>();
        // Despesas
        padroes.add(CategoriaTransacao.builder().nome("Alimentação").tipo(TipoTransacao.DESPESA).icone("utensils").cor("#f59e0b").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Moradia").tipo(TipoTransacao.DESPESA).icone("home").cor("#3b82f6").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Transporte").tipo(TipoTransacao.DESPESA).icone("car").cor("#8b5cf6").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Lazer").tipo(TipoTransacao.DESPESA).icone("gamepad").cor("#ec4899").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Saúde").tipo(TipoTransacao.DESPESA).icone("heartbeat").cor("#ef4444").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Educação").tipo(TipoTransacao.DESPESA).icone("graduation-cap").cor("#10b981").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Outras Despesas").tipo(TipoTransacao.DESPESA).icone("tags").cor("#64748b").usuario(user).build());

        // Receitas
        padroes.add(CategoriaTransacao.builder().nome("Salário").tipo(TipoTransacao.RECEITA).icone("money-bill-wave").cor("#10b981").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Freelance / Extra").tipo(TipoTransacao.RECEITA).icone("laptop-code").cor("#06b6d4").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Rendimentos").tipo(TipoTransacao.RECEITA).icone("chart-line").cor("#8b5cf6").usuario(user).build());
        padroes.add(CategoriaTransacao.builder().nome("Outras Receitas").tipo(TipoTransacao.RECEITA).icone("wallet").cor("#64748b").usuario(user).build());

        categoriaRepository.saveAll(padroes);
    }

    public CategoriaTransacaoResponse paraResponse(CategoriaTransacao categoria) {
        if (categoria == null) return null;
        return CategoriaTransacaoResponse.builder()
                .id(categoria.getId())
                .nome(categoria.getNome())
                .tipo(categoria.getTipo())
                .icone(categoria.getIcone())
                .cor(categoria.getCor())
                .build();
    }
}