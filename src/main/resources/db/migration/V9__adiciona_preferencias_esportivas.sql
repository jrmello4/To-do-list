CREATE TABLE preferencias_esportivas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    esporte VARCHAR(50) NOT NULL,
    nome_interesse VARCHAR(100) NOT NULL,
    icone VARCHAR(20) DEFAULT '⚽',
    cor VARCHAR(10) DEFAULT '#10b981',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao DATETIME NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_pref_esporte_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
