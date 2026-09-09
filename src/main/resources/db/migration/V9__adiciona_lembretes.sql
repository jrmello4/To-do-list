-- O servidor roda em UTC e quem usa não. Sem guardar o fuso da conta, um
-- lembrete "das 8 da manhã" chegaria às 5 para quem está em São Paulo.
ALTER TABLE usuarios ADD COLUMN fuso_horario VARCHAR(60) NOT NULL DEFAULT 'America/Sao_Paulo';

ALTER TABLE usuarios ADD COLUMN lembretes_ativos BOOLEAN NOT NULL DEFAULT FALSE;

-- Hora local em que o resumo deve chegar (0 a 23).
ALTER TABLE usuarios ADD COLUMN hora_lembrete INT NOT NULL DEFAULT 8;

-- Data local do último envio. É o que impede mandar duas vezes no mesmo dia
-- quando a varredura roda de hora em hora.
ALTER TABLE usuarios ADD COLUMN ultimo_lembrete_em DATE NULL;
