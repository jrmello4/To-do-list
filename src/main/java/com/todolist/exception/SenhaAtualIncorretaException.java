package com.todolist.exception;

/**
 * Senha atual errada na troca de senha.
 *
 * Não reaproveita BadCredentialsException porque o tratamento dela responde
 * "E-mail ou senha incorretos" — mensagem certa no login, onde não se deve
 * revelar qual dos dois campos falhou, e errada aqui, onde quem chama já está
 * autenticado e o único campo em jogo é a senha atual.
 */
public class SenhaAtualIncorretaException extends RuntimeException {

    public SenhaAtualIncorretaException() {
        super("Senha atual incorreta");
    }
}
