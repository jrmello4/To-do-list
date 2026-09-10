-- Três coisas pequenas que não se justificavam sozinhas numa migration cada.

-- 1. Revogação de token.
--
-- O token vale 24h e não havia como cortá-lo antes disso: sem sessão no
-- servidor, um token vazado seguia valendo até expirar, e trocar a senha não
-- mudava nada. Agora o token carrega a versão que a conta tinha quando foi
-- emitido, e o filtro compara com a versão atual. Trocar a senha ou sair de
-- todos os aparelhos incrementa a coluna, e todo token emitido antes para de
-- valer na requisição seguinte.
--
-- Contas existentes começam em 0, que é o que os tokens já emitidos não têm —
-- por isso a ausência da claim é lida como 0, e não como inválida.
ALTER TABLE usuarios ADD COLUMN token_version INT NOT NULL DEFAULT 0;

-- 2. Bloqueio otimista nas tarefas.
--
-- Duas abas abertas na mesma tarefa gravavam uma por cima da outra em
-- silêncio: a última a salvar vencia e a edição da outra sumia sem aviso.
-- Com a coluna de versão, a segunda gravação falha e o cliente recarrega.
--
-- DEFAULT 0 para as linhas que já existem: sem isso o Hibernate leria NULL
-- como entidade nova e tentaria inserir.
ALTER TABLE tasks ADD COLUMN versao INT NOT NULL DEFAULT 0;

-- 3. Índice da varredura de lembretes.
--
-- A varredura roda de hora em hora e antes carregava todas as contas para
-- filtrar em Java. Agora a consulta filtra no banco, e este índice é o que
-- evita que ela vire uma varredura de tabela cheia de hora em hora.
CREATE INDEX idx_usuarios_lembretes ON usuarios (lembretes_ativos, ativo);
