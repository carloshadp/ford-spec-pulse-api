CREATE TABLE auditoria (
    id            UUID         PRIMARY KEY,
    request_id    VARCHAR(80),
    usuario_id    UUID,
    acao          VARCHAR(80)  NOT NULL,
    entidade      VARCHAR(80),
    entidade_id   VARCHAR(80),
    dados_json    TEXT,
    ip            VARCHAR(50),
    ocorrido_em   TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_auditoria_usuario   ON auditoria (usuario_id);
CREATE INDEX idx_auditoria_acao      ON auditoria (acao);
CREATE INDEX idx_auditoria_ocorrido  ON auditoria (ocorrido_em);
