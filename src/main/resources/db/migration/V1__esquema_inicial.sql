
CREATE TABLE marcas (
    id                  UUID PRIMARY KEY,
    nome                VARCHAR(120) NOT NULL UNIQUE,
    pais_origem         VARCHAR(60),
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE veiculos (
    id                  UUID PRIMARY KEY,
    marca_id            UUID NOT NULL,
    modelo              VARCHAR(120) NOT NULL,
    segmento            VARCHAR(40) NOT NULL,
    mercado             VARCHAR(20) NOT NULL,
    ano_modelo          INT NOT NULL,
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_veiculos_marca FOREIGN KEY (marca_id) REFERENCES marcas(id),
    CONSTRAINT uq_veiculos_marca_modelo_ano UNIQUE (marca_id, modelo, ano_modelo, mercado)
);
CREATE INDEX ix_veiculos_segmento ON veiculos(segmento);
CREATE INDEX ix_veiculos_marca    ON veiculos(marca_id);

CREATE TABLE versoes (
    id                  UUID PRIMARY KEY,
    veiculo_id          UUID NOT NULL,
    nome                VARCHAR(120) NOT NULL,
    powertrain          VARCHAR(30) NOT NULL,
    tracao              VARCHAR(10) NOT NULL,
    cabine              VARCHAR(20),
    preco_sugerido      DECIMAL(12,2),
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_versoes_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos(id),
    CONSTRAINT uq_versoes_veiculo_nome UNIQUE (veiculo_id, nome)
);
CREATE INDEX ix_versoes_veiculo ON versoes(veiculo_id);

CREATE TABLE atributos_definicao (
    id                  UUID PRIMARY KEY,
    codigo_canonico     VARCHAR(80) NOT NULL UNIQUE,
    nome_exibicao       VARCHAR(160) NOT NULL,
    categoria           VARCHAR(40) NOT NULL,
    unidade             VARCHAR(20),
    tipo_dado           VARCHAR(15) NOT NULL,
    direcao_melhor      VARCHAR(15) NOT NULL,
    descricao           VARCHAR(500),
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX ix_atributos_categoria ON atributos_definicao(categoria);

CREATE TABLE fontes (
    id                  UUID PRIMARY KEY,
    nome                VARCHAR(160) NOT NULL,
    tipo                VARCHAR(40) NOT NULL,
    url                 VARCHAR(500),
    descricao           VARCHAR(500),
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE especificacoes (
    id                  UUID PRIMARY KEY,
    versao_id           UUID NOT NULL,
    atributo_id         UUID NOT NULL,
    fonte_id            UUID,
    valor_texto         VARCHAR(500),
    valor_numero        DECIMAL(18,4),
    valor_booleano      BOOLEAN,
    unidade             VARCHAR(20),
    status              VARCHAR(25) NOT NULL,
    confianca           VARCHAR(10) NOT NULL,
    data_captura        TIMESTAMP WITH TIME ZONE,
    observacao          VARCHAR(500),
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_especificacoes_versao   FOREIGN KEY (versao_id)   REFERENCES versoes(id),
    CONSTRAINT fk_especificacoes_atributo FOREIGN KEY (atributo_id) REFERENCES atributos_definicao(id),
    CONSTRAINT fk_especificacoes_fonte    FOREIGN KEY (fonte_id)    REFERENCES fontes(id),
    CONSTRAINT uq_especificacoes_versao_atributo UNIQUE (versao_id, atributo_id)
);
CREATE INDEX ix_especificacoes_versao    ON especificacoes(versao_id);
CREATE INDEX ix_especificacoes_atributo  ON especificacoes(atributo_id);
CREATE INDEX ix_especificacoes_status    ON especificacoes(status);

CREATE TABLE comparacoes (
    id                  UUID PRIMARY KEY,
    titulo              VARCHAR(160) NOT NULL,
    descricao           VARCHAR(500),
    versao_ford_id      UUID NOT NULL,
    perfil_cliente      VARCHAR(40),
    criado_por          VARCHAR(80) NOT NULL,
    data_criacao        TIMESTAMP WITH TIME ZONE NOT NULL,
    data_atualizacao    TIMESTAMP WITH TIME ZONE NOT NULL,
    versao_registro     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_comparacoes_versao_ford FOREIGN KEY (versao_ford_id) REFERENCES versoes(id)
);
CREATE INDEX ix_comparacoes_criado_por ON comparacoes(criado_por);

CREATE TABLE comparacao_versoes (
    id                  UUID PRIMARY KEY,
    comparacao_id       UUID NOT NULL,
    versao_id           UUID NOT NULL,
    ordem               INT NOT NULL,
    CONSTRAINT fk_comparacao_versoes_comp    FOREIGN KEY (comparacao_id) REFERENCES comparacoes(id) ON DELETE CASCADE,
    CONSTRAINT fk_comparacao_versoes_versao  FOREIGN KEY (versao_id)     REFERENCES versoes(id),
    CONSTRAINT uq_comparacao_versoes UNIQUE (comparacao_id, versao_id)
);
CREATE INDEX ix_comparacao_versoes_comp ON comparacao_versoes(comparacao_id);

CREATE TABLE comparacao_celulas (
    id                       UUID PRIMARY KEY,
    comparacao_id            UUID NOT NULL,
    versao_id                UUID NOT NULL,
    atributo_id              UUID NOT NULL,
    valor_texto              VARCHAR(500),
    valor_numero             DECIMAL(18,4),
    valor_booleano           BOOLEAN,
    unidade                  VARCHAR(20),
    confianca                VARCHAR(10) NOT NULL,
    status_celula            VARCHAR(20) NOT NULL,
    status_dado              VARCHAR(25) NOT NULL,
    fonte_nome_snapshot      VARCHAR(160),
    data_snapshot            TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_celulas_comp     FOREIGN KEY (comparacao_id) REFERENCES comparacoes(id) ON DELETE CASCADE,
    CONSTRAINT fk_celulas_versao   FOREIGN KEY (versao_id)     REFERENCES versoes(id),
    CONSTRAINT fk_celulas_atributo FOREIGN KEY (atributo_id)   REFERENCES atributos_definicao(id),
    CONSTRAINT uq_celulas UNIQUE (comparacao_id, versao_id, atributo_id)
);
CREATE INDEX ix_celulas_comp_atributo ON comparacao_celulas(comparacao_id, atributo_id);
