-- Um passo dentro de uma tarefa, e não uma tarefa menor.
--
-- A diferença decide o resto do desenho. Se subtarefa fosse uma linha em
-- `tasks` apontando para a mãe, ela entraria na listagem — que é paginada e
-- ordenável no servidor — e uma filha poderia cair numa página diferente da
-- mãe, ou sumir num filtro que a mãe atende. Como tabela própria, ela nunca é
-- linha da listagem: viaja junto da mãe, sempre.
--
-- Por isso também não tem prazo, prioridade, projeto nem etiqueta. Um passo
-- com projeto diferente do da tarefa não quer dizer nada, e um passo com prazo
-- próprio apareceria em "atrasadas" contando duas vezes o mesmo compromisso.
CREATE TABLE subtarefas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id BIGINT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    concluida BOOLEAN NOT NULL DEFAULT FALSE,
    -- Posição na lista. Sem ela a ordem viria do banco, que não promete
    -- nenhuma: os passos apareceriam embaralhados entre um carregamento e
    -- outro, e passo fora de ordem é passo errado.
    ordem INT NOT NULL DEFAULT 0,
    data_criacao DATETIME NOT NULL,
    CONSTRAINT fk_subtarefas_task FOREIGN KEY (task_id)
        REFERENCES tasks (id) ON DELETE CASCADE
);

-- A consulta é sempre "os passos desta tarefa, em ordem".
CREATE INDEX idx_subtarefas_task ON subtarefas (task_id, ordem);
