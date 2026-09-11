ALTER TABLE tasks ADD COLUMN recorrencia_gerada BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE transacoes ADD COLUMN transferencia BOOLEAN NOT NULL DEFAULT FALSE;
CREATE INDEX idx_transacoes_transferencia ON transacoes (user_id, transferencia);
