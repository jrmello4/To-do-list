CREATE TABLE etiquetas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nome VARCHAR(60) NOT NULL,
    cor VARCHAR(20) NOT NULL DEFAULT 'indigo',
    data_criacao DATETIME NOT NULL,
    CONSTRAINT fk_etiquetas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT uk_etiquetas_usuario_nome UNIQUE (usuario_id, nome)
);

-- Junção N:N. A chave primária composta impede a mesma etiqueta duas vezes na
-- mesma tarefa sem precisar de verificação na aplicação.
--
-- O ON DELETE CASCADE é rede de segurança no banco. A aplicação desfaz as
-- associações antes de excluir, porque o cascade do banco não conta para a
-- sessão do Hibernate.
CREATE TABLE task_etiquetas (
    task_id BIGINT NOT NULL,
    etiqueta_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, etiqueta_id),
    CONSTRAINT fk_task_etiquetas_task FOREIGN KEY (task_id)
        REFERENCES tasks (id) ON DELETE CASCADE,
    CONSTRAINT fk_task_etiquetas_etiqueta FOREIGN KEY (etiqueta_id)
        REFERENCES etiquetas (id) ON DELETE CASCADE
);
