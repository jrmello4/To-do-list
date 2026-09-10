package com.todolist.service;

import com.todolist.dto.EsporteEventoResponse;
import com.todolist.dto.PreferenciaEsporteRequest;
import com.todolist.entity.EventoCalendario;
import com.todolist.entity.PreferenciaEsporte;
import com.todolist.entity.Role;
import com.todolist.entity.User;
import com.todolist.repository.EventoCalendarioRepository;
import com.todolist.repository.PreferenciaEsporteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EsporteServiceTest {

    @Mock
    private PreferenciaEsporteRepository preferenciaRepository;

    @Mock
    private EventoCalendarioRepository eventoCalendarioRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private EsporteService esporteService;

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
    @DisplayName("Deve inicializar preferencias padrao se usuario nao possuir nenhuma")
    void deveInicializarPreferenciasPadrao() {
        when(preferenciaRepository.countByUsuarioAndAtivoTrue(usuario)).thenReturn(0L);

        esporteService.obterPreferenciasDoUsuario();

        verify(preferenciaRepository).saveAll(any());
    }

    @Test
    @DisplayName("Deve adicionar nova preferencia de esporte com sucesso")
    void deveAdicionarPreferencia() {
        PreferenciaEsporteRequest req = PreferenciaEsporteRequest.builder()
                .esporte("UFC")
                .nomeInteresse("UFC / MMA")
                .build();

        when(preferenciaRepository.save(any(PreferenciaEsporte.class))).thenAnswer(inv -> {
            PreferenciaEsporte p = inv.getArgument(0);
            p.setId(10L);
            return p;
        });

        PreferenciaEsporte result = esporteService.adicionarPreferencia(req);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getIcone()).isEqualTo("🥊");
    }

    @Test
    @DisplayName("Deve salvar evento esportivo no calendario unificado")
    void deveSalvarEventoNoCalendario() {
        when(eventoCalendarioRepository.findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
                eq(usuario), any(), any())).thenReturn(List.of());

        when(eventoCalendarioRepository.save(any(EventoCalendario.class))).thenAnswer(inv -> {
            EventoCalendario ev = inv.getArgument(0);
            ev.setId(100L);
            return ev;
        });

        EsporteEventoResponse response = esporteService.salvarNoCalendario("UFC-315");

        assertThat(response).isNotNull();
        assertThat(response.getNoCalendario()).isTrue();
        assertThat(response.getEventoCalendarioId()).isEqualTo(100L);
        verify(eventoCalendarioRepository).save(any(EventoCalendario.class));
    }

    @Test
    @DisplayName("Deve filtrar eventos por modalidade esportiva")
    void deveFiltrarEventosPorModalidade() {
        when(preferenciaRepository.findByUsuarioAndAtivoTrue(usuario)).thenReturn(List.of());
        when(eventoCalendarioRepository.findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
                eq(usuario), any(), any())).thenReturn(List.of());

        List<EsporteEventoResponse> ufcEventos = esporteService.obterEventos("UFC", null);

        assertThat(ufcEventos).isNotEmpty();
        assertThat(ufcEventos).allMatch(e -> "UFC".equalsIgnoreCase(e.getEsporte()));
    }
}
