==============================================================

CREATE TABLE usuarios (
    id                  UUID PRIMARY KEY,
    nome                VARCHAR(120) NOT NULL,
    email               VARCHAR(160) NOT NULL UNIQUE,
    senha_hash          VARCHAR(120) NOT NULL,
    perfil              VARCHAR(40) NOT NULL,
    ativo               BOOLEAN NOT NULL DEFAULT TRUE,
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_usuarios_email ON usuarios(email);
CREATE INDEX ix_usuarios_perfil ON usuarios(perfil);

CREATE TABLE refresh_tokens (
    id                  UUID PRIMARY KEY,
    usuario_id          UUID NOT NULL,
    expira_em           TIMESTAMP WITH TIME ZONE NOT NULL,
    revogado_em         TIMESTAMP WITH TIME ZONE,
    motivo_revogacao    VARCHAR(60),
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_refresh_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);
CREATE INDEX ix_refresh_usuario ON refresh_tokens(usuario_id);
CREATE INDEX ix_refresh_expira ON refresh_tokens(expira_em);
