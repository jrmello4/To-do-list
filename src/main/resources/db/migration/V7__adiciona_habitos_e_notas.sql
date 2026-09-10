CREATE TABLE habitos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    icone VARCHAR(50) DEFAULT 'check',
    cor VARCHAR(10) NOT NULL DEFAULT '#6366f1',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_habitos_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE registros_habitos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    data_registro DATE NOT NULL,
    concluido BOOLEAN NOT NULL DEFAULT TRUE,
    habito_id BIGINT NOT NULL,
    CONSTRAINT fk_reg_habitos_habito FOREIGN KEY (habito_id) REFERENCES habitos(id) ON DELETE CASCADE,
    CONSTRAINT uk_habito_data UNIQUE (habito_id, data_registro)
);

CREATE TABLE notas_rapidas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150),
    conteudo TEXT,
    cor VARCHAR(10) NOT NULL DEFAULT '#ffffff',
    fixada BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao DATETIME NOT NULL,
    data_atualizacao DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_notas_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);