-- ── CARGOS ──────────────────────────────────────────
CREATE TABLE cargo_pro (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(120) NOT NULL UNIQUE,
    orgao      VARCHAR(120),
    ativo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── TEMAS ───────────────────────────────────────────
CREATE TABLE tema (
    id         BIGSERIAL PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL UNIQUE,
    ativo      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── SUBTEMAS ────────────────────────────────────────
CREATE TABLE subtema (
    id       BIGSERIAL PRIMARY KEY,
    tema_id  BIGINT NOT NULL REFERENCES tema(id),
    nome     VARCHAR(200) NOT NULL,
    ativo    BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (tema_id, nome)
);

-- ── QUESTÕES ────────────────────────────────────────
CREATE TABLE questao_pro (
    id               BIGSERIAL PRIMARY KEY,
    cargo_id         BIGINT REFERENCES cargo_pro(id),
    tema_id          BIGINT REFERENCES tema(id),
    subtema_id       BIGINT REFERENCES subtema(id),
    enunciado        TEXT NOT NULL,
    gabarito         BOOLEAN NOT NULL,  -- true=CERTO false=ERRADO
    explicacao       TEXT NOT NULL,
    tipo_pegadinha   VARCHAR(80),
    artigo_ref       VARCHAR(200),
    nivel_dificuldade VARCHAR(10) NOT NULL DEFAULT 'MEDIO',
                     -- FACIL | MEDIO | DIFICIL
    ativa            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_qp_tema     ON questao_pro(tema_id);
CREATE INDEX idx_qp_subtema  ON questao_pro(subtema_id);
CREATE INDEX idx_qp_nivel    ON questao_pro(nivel_dificuldade);
CREATE INDEX idx_qp_cargo    ON questao_pro(cargo_id);

-- ── SESSÕES DE PROVA ────────────────────────────────
CREATE TABLE sessao_prova (
    id             BIGSERIAL PRIMARY KEY,
    cargo_id       BIGINT REFERENCES cargo_pro(id),
    config_json    TEXT NOT NULL,   -- JSON com filtros, % por nível etc.
    status         VARCHAR(20) NOT NULL DEFAULT 'EM_ANDAMENTO',
                   -- EM_ANDAMENTO | CONCLUIDA | ABANDONADA
    total_questoes INT NOT NULL,
    acertos        INT NOT NULL DEFAULT 0,
    erros          INT NOT NULL DEFAULT 0,
    tempo_total_ms BIGINT,          -- tempo total em ms
    created_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    finalizada_at  TIMESTAMP
);

-- ── RESPOSTAS ────────────────────────────────────────
CREATE TABLE resposta_pro (
    id           BIGSERIAL PRIMARY KEY,
    sessao_id    BIGINT NOT NULL REFERENCES sessao_prova(id),
    questao_id   BIGINT NOT NULL REFERENCES questao_pro(id),
    tema_id      BIGINT REFERENCES tema(id),
    subtema_id   BIGINT REFERENCES subtema(id),
    nivel        VARCHAR(10) NOT NULL,
    resposta     BOOLEAN NOT NULL,
    acertou      BOOLEAN NOT NULL,
    tempo_ms     BIGINT NOT NULL,    -- tempo nesta questão
    created_at   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rp_sessao   ON resposta_pro(sessao_id);
CREATE INDEX idx_rp_questao  ON resposta_pro(questao_id);
CREATE INDEX idx_rp_tema     ON resposta_pro(tema_id);
CREATE INDEX idx_rp_subtema  ON resposta_pro(subtema_id);

-- ── DADOS INICIAIS ───────────────────────────────────
INSERT INTO cargo_pro (nome, orgao) VALUES
    ('Técnico Administrativo', 'Geral'),
    ('Analista Administrativo', 'Geral'),
    ('Técnico em TI', 'Geral'),
    ('Analista em TI', 'Geral'),
    ('Auditor Fiscal', 'Receita Federal');