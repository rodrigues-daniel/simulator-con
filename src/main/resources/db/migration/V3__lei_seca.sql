-- ── MATÉRIAS (agrupador maior: "Lei 8.112/90", "CF/88 - Art. 37") ────
CREATE TABLE materia (
    id          BIGSERIAL PRIMARY KEY,
    cargo_id    BIGINT REFERENCES cargo_pro(id),
    nome        VARCHAR(200) NOT NULL,
    descricao   TEXT,
    fonte_legal VARCHAR(300),        -- "Lei 8.112/90", "CF/88"
    ordem       INT NOT NULL DEFAULT 0,
    ativa       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_materia_cargo ON materia(cargo_id);

-- ── TÓPICOS (subitens: "Art. 37 - Princípios", "Capítulo II - Servidores")
CREATE TABLE topico (
    id          BIGSERIAL PRIMARY KEY,
    materia_id  BIGINT NOT NULL REFERENCES materia(id),
    nome        VARCHAR(300) NOT NULL,
    artigo_base VARCHAR(200),        -- "Art. 37", "Arts. 1-5"
    resumo      TEXT,                -- resumo em markdown para estudo
    ordem       INT NOT NULL DEFAULT 0,
    ativo       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_topico_materia ON topico(materia_id);

-- ── ARTIGOS (texto literal da lei) ────────────────────────────────────
CREATE TABLE artigo_lei (
    id          BIGSERIAL PRIMARY KEY,
    topico_id   BIGINT NOT NULL REFERENCES topico(id),
    identificador VARCHAR(100) NOT NULL,  -- "Art. 37, caput"
    texto       TEXT NOT NULL,
    destaque    BOOLEAN NOT NULL DEFAULT FALSE,  -- muito cobrado
    ordem       INT NOT NULL DEFAULT 0
);

CREATE INDEX idx_artigo_topico ON artigo_lei(topico_id);

-- ── QUESTÕES DE LEI SECA ──────────────────────────────────────────────
CREATE TABLE questao_lei (
    id              BIGSERIAL PRIMARY KEY,
    topico_id       BIGINT NOT NULL REFERENCES topico(id),
    artigo_id       BIGINT REFERENCES artigo_lei(id),
    enunciado       TEXT NOT NULL,
    gabarito        BOOLEAN NOT NULL,
    comentario      TEXT NOT NULL,      -- análise linha a linha da questão
    resumo_estudo   TEXT,               -- tópico resumido para revisar
    tipo_pegadinha  VARCHAR(80),
    artigo_ref      VARCHAR(200),
    nivel           VARCHAR(10) NOT NULL DEFAULT 'MEDIO',
    ano_prova       INT,
    orgao_prova     VARCHAR(120),       -- "TCU 2024", "TCE-SP 2023"
    recorrencia     INT NOT NULL DEFAULT 1,  -- quantas vezes o tema caiu
    ativa           BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_qlei_topico    ON questao_lei(topico_id);
CREATE INDEX idx_qlei_artigo    ON questao_lei(artigo_id);
CREATE INDEX idx_qlei_nivel     ON questao_lei(nivel);

-- ── PROGRESSO DO USUÁRIO ──────────────────────────────────────────────
CREATE TABLE progresso_topico (
    id              BIGSERIAL PRIMARY KEY,
    topico_id       BIGINT NOT NULL REFERENCES topico(id) UNIQUE,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
                    -- PENDENTE | EM_ESTUDO | CONCLUIDO
    acertos         INT NOT NULL DEFAULT 0,
    erros           INT NOT NULL DEFAULT 0,
    ultima_revisao  TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ── ANOTAÇÕES PESSOAIS ────────────────────────────────────────────────
CREATE TABLE anotacao (
    id          BIGSERIAL PRIMARY KEY,
    topico_id   BIGINT REFERENCES topico(id),
    artigo_id   BIGINT REFERENCES artigo_lei(id),
    conteudo    TEXT NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW()
);