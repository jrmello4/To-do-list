package com.todolist.lembretes;

import com.todolist.dto.TaskFiltro;
import com.todolist.dto.TaskResponse;
import com.todolist.entity.Usuario;
import com.todolist.repository.UsuarioRepository;
import com.todolist.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LembreteService {

    private static final Logger log = LoggerFactory.getLogger(LembreteService.class);

    /** Teto de tarefas listadas no e-mail; o resto vira "e mais N". */
    private static final int LIMITE_NO_EMAIL = 20;

    private final UsuarioRepository usuarioRepository;
    private final TaskService taskService;
    private final EnviadorDeLembrete enviador;

    /**
     * Percorre as contas e envia para quem já chegou na hora configurada.
     *
     * A varredura roda de hora em hora porque cada conta tem o seu fuso: às
     * 11h UTC é 8h em São Paulo e 8h em outro lugar noutra hora. Quem decide é
     * o relógio local da conta, não o do servidor.
     *
     * ultimoLembreteEm guarda a data local já enviada, e é o que impede a
     * mesma conta receber duas vezes no mesmo dia.
     */
    @Transactional
    public int varrer(ZonedDateTime agoraUtc) {
        int enviados = 0;

        // Filtrado no banco, e não com findAll() seguido de um if: a varredura
        // roda de hora em hora, e carregar toda a base para descartar quase
        // tudo custa proporcional ao número de contas, não ao de lembretes.
        for (Usuario usuario : usuarioRepository.findByLembretesAtivosTrueAndAtivoTrue()) {
            ZonedDateTime local;
            try {
                local = agoraUtc.withZoneSameInstant(ZoneId.of(usuario.getFusoHorario()));
            } catch (Exception e) {
                // Fuso inválido gravado por engano não pode parar a varredura
                // das outras contas.
                log.warn("Fuso inválido na conta {}: {}", usuario.getId(), usuario.getFusoHorario());
                continue;
            }

            if (local.getHour() != usuario.getHoraLembrete()) {
                continue;
            }
            if (local.toLocalDate().equals(usuario.getUltimoLembreteEm())) {
                continue;
            }

            try {
                if (enviarPara(usuario, local.toLocalDate())) {
                    enviados++;
                }
            } catch (Exception e) {
                // Um endereço recusado ou o SMTP fora do ar não podem levar
                // junto as contas seguintes. Sem a marca de data, a próxima
                // varredura tenta de novo.
                log.error("Falha ao enviar lembrete para a conta {}", usuario.getId(), e);
                continue;
            }

            // Marca mesmo quando não há nada a dizer: senão a conta seria
            // reavaliada a cada varredura dentro da mesma hora.
            usuario.setUltimoLembreteEm(local.toLocalDate());
        }

        return enviados;
    }

    private boolean enviarPara(Usuario usuario, LocalDate hoje) {
        List<TaskResponse> atrasadas = buscar(usuario.getId(), hoje.minusDays(1));
        List<TaskResponse> vencemHoje = buscar(usuario.getId(), hoje).stream()
                .filter(tarefa -> hoje.equals(tarefa.getPrazo()))
                .toList();

        if (atrasadas.isEmpty() && vencemHoje.isEmpty()) {
            return false;
        }

        enviador.enviar(new Lembrete(
                usuario.getEmail(), primeiroNome(usuario.getNome()), atrasadas, vencemHoje));
        return true;
    }

    private List<TaskResponse> buscar(Long usuarioId, LocalDate ate) {
        return taskService.listar(
                        usuarioId,
                        new TaskFiltro(false, null, null, null, null, ate, null),
                        PageRequest.of(0, LIMITE_NO_EMAIL))
                .getContent();
    }

    private static String primeiroNome(String nome) {
        return nome == null || nome.isBlank() ? "você" : nome.trim().split("\\s+")[0];
    }
}
