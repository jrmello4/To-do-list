CREATE TABLE tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    concluida BOOLEAN NOT NULL DEFAULT FALSE,
    data_criacao DATETIME NOT NULL,
    data_atualizacao DATETIME
);
