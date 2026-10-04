CREATE TABLE unidade (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    bloco_id BIGINT NOT NULL REFERENCES bloco(id),
    numero VARCHAR(20) NOT NULL,
    CONSTRAINT uk_unidade_bloco_numero UNIQUE (bloco_id, numero)
);