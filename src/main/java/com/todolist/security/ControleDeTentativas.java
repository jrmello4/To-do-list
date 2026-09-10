package com.todolist.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Freia tentativas de senha repetidas.
 *
 * O BCrypt encarece cada tentativa, mas não impede o volume — e é justamente o
 * custo dele que torna o endpoint um bom alvo: cada tentativa errada consome
 * CPU do servidor, então o mesmo laço que procura a senha também derruba a
 * aplicação para todo mundo.
 *
 * <p><b>Por que a contagem é por IP, e não por e-mail.</b> Contar por e-mail é
 * o primeiro impulso e cria um problema maior: qualquer um bloqueia a conta de
 * qualquer pessoa só errando a senha dela algumas vezes. O ataque deixa de ser
 * descobrir a senha e passa a ser trancar o dono do lado de fora. Por isso o
 * teto é por origem, com um segundo teto por (IP, e-mail) que corta o laço
 * numa conta específica sem dar a ninguém o poder de trancar outra pessoa.
 *
 * <p><b>Limite conhecido.</b> A contagem é de memória, então vale por
 * instância: atrás de um balanceador com N instâncias, o teto efetivo é N
 * vezes o configurado. Continua reduzindo o ataque em ordens de grandeza, e a
 * alternativa — contar no banco — cobraria uma escrita por tentativa errada,
 * que é exatamente o que um ataque produz em volume. Um ataque distribuído por
 * muitos IPs também passa por baixo disto; para esse caso o que serve é um
 * segundo fator, não um contador.
 */
@Component
public class ControleDeTentativas {

    /** Tentativas erradas da mesma origem antes de recusar. */
    @Value("${LOGIN_MAX_POR_IP:20}")
    private int maxPorIp;

    /** Tentativas erradas da mesma origem contra a mesma conta. */
    @Value("${LOGIN_MAX_POR_CONTA:8}")
    private int maxPorConta;

    /** Quanto tempo a contagem dura, e quanto tempo o bloqueio dura com ela. */
    @Value("${LOGIN_JANELA_MINUTOS:15}")
    private long janelaMinutos;

    /**
     * Teto de chaves guardadas.
     *
     * Sem ele, um laço variando o e-mail a cada tentativa criaria uma entrada
     * por tentativa e o controle contra abuso viraria o próprio vazamento de
     * memória. Ao encher, a limpeza corre antes de aceitar chave nova.
     */
    private static final int MAX_CHAVES = 20_000;

    private final Map<String, Contagem> contagens = new ConcurrentHashMap<>();

    /** Contador com validade. Reiniciado, e não zerado, ao vencer a janela. */
    private static final class Contagem {
        private final AtomicInteger erros = new AtomicInteger();
        private volatile Instant expiraEm;

        Contagem(Instant expiraEm) {
            this.expiraEm = expiraEm;
        }
    }

    /** Recusa antes de a senha ser conferida, para o hash nem chegar a rodar. */
    public boolean bloqueado(String ip, String email) {
        return excedeu(chaveDeIp(ip), maxPorIp) || excedeu(chaveDeConta(ip, email), maxPorConta);
    }

    public void registrarFalha(String ip, String email) {
        somar(chaveDeIp(ip));
        somar(chaveDeConta(ip, email));
    }

    /**
     * Acerto limpa a contagem da conta, mas não a do IP.
     *
     * Se limpasse as duas, bastaria intercalar uma entrada legítima a cada
     * poucas tentativas para o teto por origem nunca ser alcançado.
     */
    public void registrarAcerto(String ip, String email) {
        contagens.remove(chaveDeConta(ip, email));
    }

    public Duration janela() {
        return Duration.ofMinutes(janelaMinutos);
    }

    private boolean excedeu(String chave, int teto) {
        Contagem contagem = contagens.get(chave);

        if (contagem == null) {
            return false;
        }
        if (Instant.now().isAfter(contagem.expiraEm)) {
            contagens.remove(chave);
            return false;
        }
        return contagem.erros.get() >= teto;
    }

    private void somar(String chave) {
        Instant agora = Instant.now();

        if (contagens.size() >= MAX_CHAVES) {
            limpar(agora);
        }

        contagens.compute(chave, (ignorada, contagem) -> {
            if (contagem == null || agora.isAfter(contagem.expiraEm)) {
                Contagem nova = new Contagem(agora.plus(janela()));
                nova.erros.set(1);
                return nova;
            }
            contagem.erros.incrementAndGet();
            return contagem;
        });
    }

    private void limpar(Instant agora) {
        contagens.entrySet().removeIf(entrada -> agora.isAfter(entrada.getValue().expiraEm));

        // Ainda cheio depois de tirar o que venceu significa ataque em curso
        // com chaves ainda válidas. Descartar tudo é melhor do que crescer sem
        // limite: o pior caso é um punhado de tentativas a mais.
        if (contagens.size() >= MAX_CHAVES) {
            contagens.clear();
        }
    }

    private static String chaveDeIp(String ip) {
        return "ip:" + ip;
    }

    private static String chaveDeConta(String ip, String email) {
        return "conta:" + ip + "|" + (email == null ? "" : email.trim().toLowerCase());
    }
}
