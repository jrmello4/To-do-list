-- Posição manual da tarefa na lista da conta.
--
-- Ordem manual é uma ordem total sobre as tarefas do dono, e por isso mora
-- numa coluna e não numa tabela à parte: é um atributo de cada tarefa, e uma
-- tabela de posições só acrescentaria uma junção e a chance de uma tarefa
-- ficar sem linha lá.
ALTER TABLE tasks ADD COLUMN ordem INT NOT NULL DEFAULT 0;

-- Semeia com o próprio id: é exatamente a ordem que a listagem mostrava antes
-- desta migration (sort=id,asc), então ninguém vê a lista embaralhar ao
-- atualizar. Semear com zero deixaria tudo empatado e a ordem passaria a
-- depender do desempate por id — mesma coisa por acidente, e não por desenho.
UPDATE tasks SET ordem = id;

-- A listagem ordena por (ordem, id) dentro de uma conta. Sem este índice, cada
-- página faria uma ordenação em memória sobre todas as tarefas do dono.
CREATE INDEX idx_tasks_usuario_ordem ON tasks (usuario_id, ordem);
