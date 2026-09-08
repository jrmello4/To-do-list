CREATE TABLE projetos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    cor VARCHAR(20) NOT NULL DEFAULT 'indigo',
    arquivado BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao DATETIME NOT NULL,
    CONSTRAINT fk_projetos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    -- Nomes de projeto são únicos por conta, não globalmente: duas pessoas
    -- podem ter um projeto "Casa" sem conflito.
    CONSTRAINT uk_projetos_usuario_nome UNIQUE (usuario_id, nome)
);

-- Nulo de propósito: tarefa sem projeto é a caixa de entrada.
ALTER TABLE tasks ADD COLUMN projeto_id BIGINT NULL;

ALTER TABLE tasks ADD CONSTRAINT fk_tasks_projeto
    FOREIGN KEY (projeto_id) REFERENCES projetos (id);
