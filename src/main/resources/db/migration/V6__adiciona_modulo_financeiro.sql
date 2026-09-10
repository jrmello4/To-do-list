CREATE TABLE contas_financeiras (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    saldo_inicial DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    saldo_atual DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    cor VARCHAR(10) NOT NULL DEFAULT '#6366f1',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_contas_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE categorias_transacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    icone VARCHAR(50) DEFAULT 'tag',
    cor VARCHAR(10) NOT NULL DEFAULT '#6366f1',
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_cat_transacoes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE transacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    valor DECIMAL(15, 2) NOT NULL,
    data_vencimento DATE NOT NULL,
    data_pagamento DATE DEFAULT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    parcelado BOOLEAN NOT NULL DEFAULT FALSE,
    numero_parcela INT DEFAULT NULL,
    total_parcelas INT DEFAULT NULL,
    grupo_parcela_id VARCHAR(50) DEFAULT NULL,
    observacoes TEXT DEFAULT NULL,
    conta_id BIGINT NOT NULL,
    categoria_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_transacoes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_transacoes_conta FOREIGN KEY (conta_id) REFERENCES contas_financeiras(id) ON DELETE CASCADE,
    CONSTRAINT fk_transacoes_categoria FOREIGN KEY (categoria_id) REFERENCES categorias_transacoes(id) ON DELETE SET NULL
);