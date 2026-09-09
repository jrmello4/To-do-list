package com.todolist.lembretes;

/**
 * Como o lembrete sai daqui.
 *
 * Interface porque a regra de quando enviar precisa ser testável sem servidor
 * de e-mail: nos testes entra um dublê que guarda o que foi enviado.
 */
public interface EnviadorDeLembrete {

    void enviar(Lembrete lembrete);
}
