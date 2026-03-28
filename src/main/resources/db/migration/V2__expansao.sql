-- Rastreia questões já respondidas corretamente (não voltam ao pool)
CREATE TABLE questao_respondida (
    id          BIGSERIAL PRIMARY KEY,
    questao_id  BIGINT NOT NULL REFERENCES questao_pro(id),
    respondida_em TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (questao_id)
);

-- Preferências do usuário (tema visual, grupo ativo, etc.)
CREATE TABLE user_preferencia (
    chave  VARCHAR(80) PRIMARY KEY,
    valor  TEXT NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Adiciona campo grupo nas questões
ALTER TABLE questao_pro
    ADD COLUMN IF NOT EXISTS grupo VARCHAR(20) NOT NULL DEFAULT 'EDITAL';
    -- EDITAL = assuntos do edital
    -- BANCA  = provas reais da CEBRASPE

-- Índice para filtrar por grupo
CREATE INDEX IF NOT EXISTS idx_qp_grupo ON questao_pro(grupo);

-- Seed preferências padrão
INSERT INTO user_preferencia (chave, valor) VALUES
    ('tema_visual', 'escuro'),
    ('grupo_ativo', 'EDITAL')
ON CONFLICT DO NOTHING;