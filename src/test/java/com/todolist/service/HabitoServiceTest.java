package com.todolist.service;

import com.todolist.dto.HabitoRequest;
import com.todolist.dto.HabitoResponse;
import com.todolist.entity.Habito;
import com.todolist.entity.RegistroHabito;
import com.todolist.entity.Role;
import com.todolist.entity.User;
import com.todolist.repository.HabitoRepository;
import com.todolist.repository.RegistroHabitoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitoServiceTest {

    @Mock
    private HabitoRepository habitoRepository;

    @Mock
    private RegistroHabitoRepository registroHabitoRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private HabitoService habitoService;

    private User usuario;

    @BeforeEach
    void setUp() {
        usuario = User.builder()
                .id(1L)
                .nome("Test User")
                .email("test@example.com")
                .role(Role.ROLE_USER)
                .build();
        lenient().when(authService.obterUsuarioAutenticado()).thenReturn(usuario);
    }

    @Test
    @DisplayName("Deve calcular streak consecutivo corretamente")
    void deveCalcularStreakCorretamente() {
        Habito h = Habito.builder().id(1L).nome("Ler Livro").ativo(true).usuario(usuario).build();

        LocalDate hoje = LocalDate.now();
        List<RegistroHabito> registros = List.of(
                RegistroHabito.builder().id(1L).habito(h).dataRegistro(hoje).concluido(true).build(),
                RegistroHabito.builder().id(2L).habito(h).dataRegistro(hoje.minusDays(1)).concluido(true).build(),
                RegistroHabito.builder().id(3L).habito(h).dataRegistro(hoje.minusDays(2)).concluido(true).build()
        );

        when(registroHabitoRepository.findByHabitoIdOrderByDataRegistroDesc(1L)).thenReturn(registros);

        HabitoResponse resp = habitoService.paraResponse(h);

        assertThat(resp.getConcluidoHoje()).isTrue();
        assertThat(resp.getStreakDias()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deve alternar conclusão de hoje de pendente para concluído")
    void deveAlternarConclusaoDeHoje() {
        Habito h = Habito.builder().id(1L).nome("Beber Água").ativo(true).usuario(usuario).build();
        LocalDate hoje = LocalDate.now();

        when(habitoRepository.findByIdAndUsuarioId(1L, 1L)).thenReturn(Optional.of(h));
        when(registroHabitoRepository.findByHabitoIdAndDataRegistro(1L, hoje)).thenReturn(Optional.empty());

        habitoService.toggleHoje(1L);

        verify(registroHabitoRepository).save(argThat(r ->
                r.getDataRegistro().isEqual(hoje) && Boolean.TRUE.equals(r.getConcluido())
        ));
    }

    @Test
    @DisplayName("Deve criar novo hábito com sucesso")
    void deveCriarNovoHabito() {
        HabitoRequest req = HabitoRequest.builder().nome("Meditar").cor("#8b5cf6").icone("sun").build();

        when(habitoRepository.save(any(Habito.class))).thenAnswer(inv -> {
            Habito salvo = inv.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        HabitoResponse resp = habitoService.criar(req);

        assertThat(resp.getId()).isEqualTo(10L);
        assertThat(resp.getNome()).isEqualTo("Meditar");
    }
}