package com.todolist.service;

import com.todolist.dto.EventoCalendarioRequest;
import com.todolist.dto.ItemCalendarioResponse;
import com.todolist.entity.EventoCalendario;
import com.todolist.entity.Task;
import com.todolist.entity.Transacao;
import com.todolist.entity.User;
import com.todolist.exception.ResourceNotFoundException;
import com.todolist.repository.EventoCalendarioRepository;
import com.todolist.repository.TaskRepository;
import com.todolist.repository.TransacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarioService {

    private final TaskRepository taskRepository;
    private final TransacaoRepository transacaoRepository;
    private final EventoCalendarioRepository eventoRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public List<ItemCalendarioResponse> obterItensDoMes(int ano, int mes) {
        User usuario = authService.obterUsuarioAutenticado();
        LocalDate inicio = LocalDate.of(ano, mes, 1);
        LocalDate fim = inicio.withDayOfMonth(inicio.lengthOfMonth());

        List<ItemCalendarioResponse> itens = new ArrayList<>();

        // 1. Tarefas com vencimento no mês
        List<Task> tarefas = taskRepository.findByUsuarioIdAndDeletadaFalseAndDataVencimentoBetween(
                usuario.getId(), inicio, fim
        );
        for (Task t : tarefas) {
            itens.add(ItemCalendarioResponse.builder()
                    .id("TAREFA-" + t.getId())
                    .origemId(t.getId())
                    .tipo("TAREFA")
                    .titulo(t.getTitulo())
                    .data(t.getDataVencimento())
                    .status(t.getConcluida() ? "CONCLUIDA" : "PENDENTE")
                    .cor("#3b82f6")
                    .detalhe("Prioridade: " + t.getPrioridade() + " | " + t.getCategoria())
                    .build());
        }

        // 2. Transações financeiras com vencimento no mês
        List<Transacao> transacoes = transacaoRepository.findByUsuarioIdAndDataVencimentoBetweenOrderByDataVencimentoAsc(
                usuario.getId(), inicio, fim
        );
        for (Transacao tr : transacoes) {
            boolean isReceita = "RECEITA".equalsIgnoreCase(tr.getTipo().name());
            itens.add(ItemCalendarioResponse.builder()
                    .id("TRANSACAO-" + tr.getId())
                    .origemId(tr.getId())
                    .tipo(isReceita ? "RECEITA" : "DESPESA")
                    .titulo(tr.getDescricao())
                    .data(tr.getDataVencimento())
                    .valor(tr.getValor())
                    .status(tr.getStatus().name())
                    .cor(isReceita ? "#10b981" : "#ef4444")
                    .detalhe((tr.getConta() != null ? tr.getConta().getNome() : "") + 
                             (tr.getCategoria() != null ? " • " + tr.getCategoria().getNome() : ""))
                    .build());
        }

        // 3. Eventos próprios do calendário
        List<EventoCalendario> eventos = eventoRepository.findByUsuarioAndAtivoTrueAndDataEventoBetweenOrderByDataEventoAscHoraInicioAsc(
                usuario, inicio, fim
        );
        for (EventoCalendario ev : eventos) {
            itens.add(ItemCalendarioResponse.builder()
                    .id("EVENTO-" + ev.getId())
                    .origemId(ev.getId())
                    .tipo("EVENTO")
                    .titulo(ev.getTitulo())
                    .data(ev.getDataEvento())
                    .hora(ev.getHoraInicio())
                    .status("AGENDADO")
                    .cor(ev.getCor() != null ? ev.getCor() : "#8b5cf6")
                    .detalhe(ev.getCategoria() != null ? ev.getCategoria() : "Compromisso")
                    .build());
        }

        itens.sort(Comparator.comparing(ItemCalendarioResponse::getData));
        return itens;
    }

    @Transactional
    public ItemCalendarioResponse criarEvento(EventoCalendarioRequest request) {
        User usuario = authService.obterUsuarioAutenticado();

        EventoCalendario evento = EventoCalendario.builder()
                .titulo(request.getTitulo())
                .descricao(request.getDescricao())
                .dataEvento(request.getDataEvento())
                .horaInicio(request.getHoraInicio())
                .horaFim(request.getHoraFim())
                .cor(request.getCor() != null ? request.getCor() : "#8b5cf6")
                .categoria(request.getCategoria() != null ? request.getCategoria() : "Geral")
                .ativo(true)
                .usuario(usuario)
                .build();

        EventoCalendario salvo = eventoRepository.save(evento);

        return ItemCalendarioResponse.builder()
                .id("EVENTO-" + salvo.getId())
                .origemId(salvo.getId())
                .tipo("EVENTO")
                .titulo(salvo.getTitulo())
                .data(salvo.getDataEvento())
                .hora(salvo.getHoraInicio())
                .status("AGENDADO")
                .cor(salvo.getCor())
                .detalhe(salvo.getCategoria())
                .build();
    }

    @Transactional
    public void excluirEvento(Long eventoId) {
        User usuario = authService.obterUsuarioAutenticado();
        EventoCalendario evento = eventoRepository.findById(eventoId)
                .filter(e -> e.getAtivo() && e.getUsuario().getId().equals(usuario.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Evento", eventoId));
        evento.setAtivo(false);
        eventoRepository.save(evento);
    }
}
