CREATE TABLE usuario (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome       VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(100) NOT NULL,
    perfil     VARCHAR(20)  NOT NULL,
    unidade_id BIGINT REFERENCES unidade (id),
    ativo      BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT ck_usuario_perfil CHECK (perfil IN ('MORADOR', 'PORTEIRO', 'SINDICO')),
    CONSTRAINT ck_usuario_morador_unidade CHECK (perfil <> 'MORADOR' OR unidade_id IS NOT NULL)
);

CREATE INDEX idx_usuario_unidade ON usuario (unidade_id);