CREATE TABLE autorizacao_visitante (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    unidade_id BIGINT NOT NULL REFERENCES unidade (id),
    criado_por BIGINT NOT NULL REFERENCES usuario (id),
    nome VARCHAR(150),
    documento VARCHAR(20),
    telefone VARCHAR(20),
    placa VARCHAR(10),
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    anonimizado_em TIMESTAMPTZ,
    CONSTRAINT ck_autorizacao_periodo CHECK (inicio < fim),
    CONSTRAINT ck_autorizacao_status CHECK (status IN ('ATIVA', 'CANCELADA')),
    CONSTRAINT ck_autorizacao_dados CHECK (anonimizado_em IS NOT NULL OR (nome IS NOT NULL AND documento IS NOT NULL))
);

CREATE INDEX idx_autorizacao_unidade ON autorizacao_visitante (unidade_id);
CREATE INDEX idx_autorizacao_periodo ON autorizacao_visitante (inicio, fim);

CREATE TABLE acesso_visitante (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    autorizacao_id BIGINT NOT NULL REFERENCES autorizacao_visitante (id),
    entrada_em TIMESTAMP NOT NULL,
    saida_em TIMESTAMP,
    registrado_entrada_por BIGINT NOT NULL REFERENCES usuario (id),
    registrado_saida_por BIGINT REFERENCES usuario (id),
    CONSTRAINT ck_acesso_periodo CHECK (saida_em IS NULL OR saida_em >= entrada_em),
    CONSTRAINT ck_acesso_saida CHECK ((saida_em IS NULL) = (registrado_saida_por IS NULL))
);

CREATE UNIQUE INDEX ux_acesso_aberto ON acesso_visitante (autorizacao_id) WHERE saida_em IS NULL;