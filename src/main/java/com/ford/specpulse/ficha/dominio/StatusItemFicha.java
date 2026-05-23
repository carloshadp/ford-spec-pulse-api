package com.ford.specpulse.ficha.dominio;


public enum StatusItemFicha {

    /** Atributo reconhecido e valor encontrado para a versao. */
    PRESENTE,

    /** Atributo reconhecido, dado consta como NAO_INFORMADO pela fonte oficial. */
    NAO_INFORMADO,

    /** Atributo reconhecido, mas nao existe especificacao cadastrada para esta versao. */
    NAO_DISPONIVEL,

    /** Termo solicitado nao casa com nenhum codigo, nome ou sinonimo de atributo. */
    ATRIBUTO_DESCONHECIDO
}
