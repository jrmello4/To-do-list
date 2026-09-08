-- As tarefas já existentes não têm dono. Adicionar a coluna como NOT NULL de
-- uma vez falharia, porque não há valor para preencher as linhas atuais.
-- Daí os três passos: entra permitindo nulo, adota as órfãs, e só então aperta.

-- 1. a coluna entra permitindo nulo
ALTER TABLE tasks ADD COLUMN usuario_id BIGINT NULL;

-- 2. cria o dono de resgate apenas se houver tarefas órfãs, e as adota.
--    O senha_hash '!' é inválido de propósito: nenhum hash BCrypt tem esse
--    formato, então a verificação sempre falha e ninguém entra nessa conta.
INSERT INTO usuarios (nome, email, senha_hash, ativo, data_criacao)
SELECT 'Dono original', 'dono-original@local', '!', TRUE, NOW()
FROM (SELECT 1) AS origem
WHERE EXISTS (SELECT 1 FROM tasks WHERE usuario_id IS NULL);

UPDATE tasks
SET usuario_id = (SELECT id FROM usuarios WHERE email = 'dono-original@local')
WHERE usuario_id IS NULL;

-- 3. agora sim a restrição e a chave estrangeira
ALTER TABLE tasks MODIFY COLUMN usuario_id BIGINT NOT NULL;

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_usuario
    FOREIGN KEY (usuario_id) REFERENCES usuarios (id);

CREATE INDEX idx_tasks_usuario ON tasks (usuario_id);
