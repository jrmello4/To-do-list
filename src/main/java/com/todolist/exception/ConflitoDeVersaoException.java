package com.todolist.exception;

/**
 * Edição enviada sobre uma versão que já não é a atual.
 *
 * Existe porque a coluna @Version sozinha não cobre o caso que motiva tudo
 * isto. Ela protege transações que se sobrepõem no tempo; duas abas, não —
 * cada PUT abre a sua transação, lê a versão de agora e grava por cima da
 * outra sem nunca ver conflito. Quem sabe qual versão estava na tela é o
 * cliente, então é ele quem precisa dizer, e é isso que esta exceção cobra.
 */
public class ConflitoDeVersaoException extends RuntimeException {

    public ConflitoDeVersaoException() {
        super("Esta tarefa foi alterada em outro lugar. Recarregue e refaça a edição.");
    }
}
