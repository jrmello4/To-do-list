-- Todos começam por usuario_id porque toda consulta filtra por dono antes de
-- qualquer outra coisa: um índice que não comece por ele fica de fora do plano
-- de execução na maioria dos casos.

-- Listagem por situação — o filtro mais usado
CREATE INDEX idx_tasks_usuario_concluida ON tasks (usuario_id, concluida);

-- Atrasadas, vencem hoje e ordenação por prazo
CREATE INDEX idx_tasks_usuario_prazo ON tasks (usuario_id, prazo);

-- Contagem de pendentes por projeto e filtro por projeto
CREATE INDEX idx_tasks_projeto ON tasks (projeto_id);

-- Barra lateral de projetos
CREATE INDEX idx_projetos_usuario ON projetos (usuario_id);

-- Listagem de etiquetas da conta
CREATE INDEX idx_etiquetas_usuario ON etiquetas (usuario_id);

-- O caminho inverso da junção: quais tarefas têm esta etiqueta.
-- O sentido task -> etiqueta já é servido pela chave primária composta.
CREATE INDEX idx_task_etiquetas_etiqueta ON task_etiquetas (etiqueta_id);
