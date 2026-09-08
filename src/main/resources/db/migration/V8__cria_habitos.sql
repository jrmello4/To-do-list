CREATE TABLE habitos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(120) NOT NULL,
    cor VARCHAR(20) NOT NULL DEFAULT 'verde',
    -- Dias da semana em que o hábito vale, no padrão ISO (1 = segunda,
    -- 7 = domingo). Sem isso não dá para calcular sequência com honestidade:
    -- num hábito de segunda/quarta/sexta, não fazer na terça não é falha.
    dias_semana VARCHAR(20) NOT NULL DEFAULT '1,2,3,4,5,6,7',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao DATETIME NOT NULL,
    CONSTRAINT fk_habitos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT uk_habitos_usuario_nome UNIQUE (usuario_id, nome)
);

-- A existência da linha é o registro: o dia foi cumprido. Desmarcar apaga a
-- linha, em vez de gravar uma coluna "concluido = false".
--
-- Isto desvia do plano original de propósito. Com a coluna booleana existiriam
-- três estados para a mesma pergunta — linha ausente, linha com false, linha
-- com true — e os dois primeiros significariam a mesma coisa. A restrição de
-- unicidade impede marcar o mesmo dia duas vezes.
CREATE TABLE habito_registros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    habito_id BIGINT NOT NULL,
    data DATE NOT NULL,
    CONSTRAINT fk_habito_registros_habito FOREIGN KEY (habito_id)
        REFERENCES habitos (id) ON DELETE CASCADE,
    CONSTRAINT uk_habito_registros_dia UNIQUE (habito_id, data)
);

CREATE INDEX idx_habitos_usuario ON habitos (usuario_id);
CREATE INDEX idx_habito_registros_habito_data ON habito_registros (habito_id, data);
