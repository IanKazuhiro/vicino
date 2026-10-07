CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE reserva (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    espaco_id BIGINT NOT NULL REFERENCES espaco(id),
    unidade_id BIGINT NOT NULL REFERENCES unidade(id),
    criado_por BIGINT NOT NULL REFERENCES usuario(id),
    inicio TIMESTAMP NOT NULL,
    fim TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ck_reserva_status CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'RECUSADA', 'CANCELADA')),
    CONSTRAINT ck_reserva_periodo CHECK (inicio < fim),
    CONSTRAINT ex_reserva_sem_conflito EXCLUDE USING gist (
        espaco_id WITH =,
        tsrange(inicio, fim) WITH &&
    ) WHERE (status IN ('PENDENTE', 'CONFIRMADA'))
);

CREATE INDEX idx_reserva_unidade ON reserva (unidade_id);