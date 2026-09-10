package com.todolist.service;

import com.todolist.dto.MetaAporteRequest;
import com.todolist.dto.MetaRequest;
import com.todolist.dto.MetaResponse;
import com.todolist.entity.Meta;
import com.todolist.entity.Role;
import com.todolist.entity.User;
import com.todolist.repository.MetaRepository;
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
class MetaServiceTest {

    @Mock
    private MetaRepository metaRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private MetaService metaService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .id(1L)
                .nome("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("Deve criar meta com sucesso e calcular percentual")
    void deveCriarMetaComSucesso() {
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);

        MetaRequest request = MetaRequest.builder()
                .titulo("Comprar Notebook")
                .valorAlvo(new BigDecimal("5000.00"))
                .valorAtual(new BigDecimal("1000.00"))
                .unidade("R$")
                .categoria("EQUIPAMENTO")
                .prazo(LocalDate.now().plusMonths(3))
                .build();

        when(metaRepository.save(any(Meta.class))).thenAnswer(invocation -> {
            Meta m = invocation.getArgument(0);
            m.setId(10L);
            return m;
        });

        MetaResponse response = metaService.criarMeta(request);

        assertThat(response).isNotNull();
        assertThat(response.getTitulo()).isEqualTo("Comprar Notebook");
        assertThat(response.getPercentualConcluido()).isEqualTo(20.0);
        assertThat(response.getConcluida()).isFalse();
    }

    @Test
    @DisplayName("Deve registrar aporte e concluir meta quando valor atual atingir o alvo")
    void deveRegistrarAporteEConcluirMetaQuandoAtingirAlvo() {
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);

        Meta meta = Meta.builder()
                .id(1L)
                .titulo("Viagem")
                .valorAlvo(new BigDecimal("3000.00"))
                .valorAtual(new BigDecimal("2500.00"))
                .concluida(false)
                .ativo(true)
                .usuario(usuario)
                .build();

        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));
        when(metaRepository.save(any(Meta.class))).thenAnswer(inv -> inv.getArgument(0));

        MetaAporteRequest aporte = MetaAporteRequest.builder()
                .valorAporte(new BigDecimal("600.00"))
                .build();

        MetaResponse response = metaService.registrarAporte(1L, aporte);

        assertThat(response.getValorAtual()).isEqualByComparingTo(new BigDecimal("3100.00"));
        assertThat(response.getConcluida()).isTrue();
        assertThat(response.getPercentualConcluido()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Deve excluir meta logicamente")
    void deveExcluirMetaLogicamente() {
        when(authService.obterUsuarioAutenticado()).thenReturn(usuario);

        Meta meta = Meta.builder()
                .id(1L)
                .titulo("Meta Teste")
                .ativo(true)
                .usuario(usuario)
                .build();

        when(metaRepository.findById(1L)).thenReturn(Optional.of(meta));

        metaService.excluirMeta(1L);

        assertThat(meta.getAtivo()).isFalse();
        verify(metaRepository).save(meta);
    }
}
