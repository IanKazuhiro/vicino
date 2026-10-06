CREATE TABLE espaco (
    id                          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome                        VARCHAR(100) NOT NULL UNIQUE,
    descricao                   VARCHAR(500),
    hora_abertura               TIME         NOT NULL,
    hora_fechamento             TIME         NOT NULL,
    duracao_minima_minutos      INTEGER      NOT NULL,
    duracao_maxima_minutos      INTEGER      NOT NULL,
    antecedencia_minima_horas   INTEGER      NOT NULL,
    antecedencia_maxima_dias    INTEGER      NOT NULL,
    limite_reservas_por_unidade INTEGER      NOT NULL,
    exige_aprovacao             BOOLEAN      NOT NULL DEFAULT FALSE,
    ativo                       BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_espaco_horario      CHECK (hora_abertura < hora_fechamento),
    CONSTRAINT ck_espaco_duracao      CHECK (duracao_minima_minutos > 0 AND duracao_minima_minutos <= duracao_maxima_minutos),
    CONSTRAINT ck_espaco_antecedencia CHECK (antecedencia_minima_horas >= 0 AND antecedencia_maxima_dias > 0),
    CONSTRAINT ck_espaco_limite       CHECK (limite_reservas_por_unidade > 0)
);