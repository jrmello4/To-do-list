-- prazo é DATE, não DATETIME: "quinta-feira" é uma decisão, "quinta às 14h32"
-- é ruído que ninguém preenche com honestidade.
ALTER TABLE tasks ADD COLUMN prazo DATE NULL;

ALTER TABLE tasks ADD COLUMN prioridade VARCHAR(10) NOT NULL DEFAULT 'MEDIA';

ALTER TABLE tasks ADD COLUMN data_conclusao DATETIME NULL;

-- As tarefas já concluídas não têm data de conclusão registrada. A última
-- atualização é a melhor aproximação disponível — sem isso elas ficariam
-- de fora de qualquer métrica histórica.
UPDATE tasks
SET data_conclusao = data_atualizacao
WHERE concluida = TRUE AND data_conclusao IS NULL;
