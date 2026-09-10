CREATE TABLE metas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    categoria VARCHAR(50) NOT NULL DEFAULT 'GERAL',
    valor_alvo DECIMAL(12, 2) NOT NULL,
    valor_atual DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    unidade VARCHAR(20) NOT NULL DEFAULT 'R$',
    prazo DATE,
    cor VARCHAR(10) NOT NULL DEFAULT '#10b981',
    icone VARCHAR(50) DEFAULT 'target',
    concluida BOOLEAN NOT NULL DEFAULT FALSE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao DATETIME NOT NULL,
    data_atualizacao DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_metas_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE eventos_calendario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    descricao TEXT,
    data_evento DATE NOT NULL,
    hora_inicio TIME,
    hora_fim TIME,
    cor VARCHAR(10) NOT NULL DEFAULT '#6366f1',
    categoria VARCHAR(50) NOT NULL DEFAULT 'Geral',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao DATETIME NOT NULL,
    data_atualizacao DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_eventos_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
