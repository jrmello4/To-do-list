package com.todolist.service;

import com.todolist.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;

/**
 * Em que fuso o dia de uma conta começa e termina.
 *
 * Existe num lugar só porque a pergunta aparece em mais de um: o painel, para
 * saber em que coluna cada conclusão cai, e o resumo, para saber o que já está
 * atrasado. Nos dois, a resposta errada é a mesma — usar o relógio do
 * servidor, que roda em UTC e não é o de ninguém.
 *
 * Quando quem chama informa a data, ela vence: é o dia de quem está com a tela
 * aberta, e o navegador sabe disso melhor do que a preferência gravada. O fuso
 * da conta é a reserva para quando a data não vem, que é o caso de qualquer
 * cliente que use a API direto.
 */
@Component
@RequiredArgsConstructor
public class FusoDaConta {

    private static final Logger log = LoggerFactory.getLogger(FusoDaConta.class);

    private final UsuarioRepository usuarioRepository;

    /**
     * Um fuso inválido gravado por engano não pode derrubar a página inteira,
     * então cai em UTC: a tela fica deslocada, mas continua abrindo. A conta
     * nem sempre existe — o token pode se referir a uma já apagada.
     */
    @Transactional(readOnly = true)
    public ZoneId de(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(usuario -> {
                    try {
                        return ZoneId.of(usuario.getFusoHorario());
                    } catch (Exception e) {
                        log.warn("Fuso inválido na conta {}: {}",
                                usuarioId, usuario.getFusoHorario());
                        return (ZoneId) ZoneOffset.UTC;
                    }
                })
                .orElse(ZoneOffset.UTC);
    }

    /** A data informada, ou o hoje da conta quando ela não vem. */
    public LocalDate hoje(Long usuarioId, LocalDate informada) {
        return informada != null ? informada : LocalDate.now(de(usuarioId));
    }
}
