CREATE INDEX idx_tasks_user_ativa ON tasks(user_id, concluida);
CREATE INDEX idx_tasks_user_vencimento ON tasks(user_id, data_vencimento);

CREATE INDEX idx_contas_user ON contas_financeiras(user_id, ativo);

CREATE INDEX idx_categorias_user ON categorias_transacoes(user_id);

CREATE INDEX idx_transacoes_user_vencimento ON transacoes(user_id, data_vencimento);
CREATE INDEX idx_transacoes_user_status ON transacoes(user_id, status);
CREATE INDEX idx_transacoes_grupo_parcela ON transacoes(grupo_parcela_id, user_id);

CREATE INDEX idx_habitos_user ON habitos(user_id, ativo);
CREATE INDEX idx_registros_habito_data ON registros_habitos(habito_id, data_registro);

CREATE INDEX idx_notas_user ON notas_rapidas(user_id, fixada);

CREATE INDEX idx_metas_user ON metas(user_id, ativo);

CREATE INDEX idx_eventos_cal_user_data ON eventos_calendario(user_id, data_evento, ativo);

CREATE INDEX idx_pref_esportes_user ON preferencias_esportivas(user_id, ativo);
