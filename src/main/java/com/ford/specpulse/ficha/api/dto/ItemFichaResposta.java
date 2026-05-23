package com.ford.specpulse.ficha.api.dto;

import com.ford.specpulse.especificacao.dominio.NivelConfianca;
import com.ford.specpulse.ficha.dominio.StatusItemFicha;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;


@Schema(description = "Item da ficha tecnica consultada. Formato padronizado: " +
        "todo termo solicitado pelo usuario gera exatamente um item, mesmo quando o dado " +
        "nao existe (status indica como interpretar).")
public record ItemFichaResposta(

        @Schema(description = "Termo exato enviado pelo usuario na consulta.", example = "potencia")
        String termoSolicitado,

        @Schema(description = "Codigo canonico do atributo identificado. Nulo quando termo nao foi reconhecido.",
                example = "POTENCIA_CV")
        String codigoCanonico,

        @Schema(description = "Nome de exibicao do atributo identificado. Nulo quando termo nao foi reconhecido.",
                example = "Potencia maxima")
        String nomeExibicao,

        @Schema(description = "Categoria do atributo. Nulo quando termo nao foi reconhecido.",
                example = "MOTOR")
        String categoria,

        @Schema(description = "Valor formatado para exibicao. Nulo quando status != PRESENTE.",
                example = "397 cv")
        String valor,

        @Schema(description = "Unidade do valor, quando aplicavel.", example = "cv")
        String unidade,

        @Schema(description = "Status do item na ficha (PRESENTE, NAO_INFORMADO, NAO_DISPONIVEL, ATRIBUTO_DESCONHECIDO).")
        StatusItemFicha status,

        @Schema(description = "Confianca da fonte. Nulo quando status != PRESENTE.")
        NivelConfianca confianca,

        @Schema(description = "Nome da fonte oficial usada. Nulo quando status != PRESENTE.",
                example = "Site Oficial Ford")
        String fonte,

        @Schema(description = "Momento da captura do dado pela fonte. Nulo quando status != PRESENTE.")
        OffsetDateTime dataCaptura
) {

    public static ItemFichaResposta atributoDesconhecido(String termo) {
        return new ItemFichaResposta(termo, null, null, null, null, null,
                StatusItemFicha.ATRIBUTO_DESCONHECIDO, null, null, null);
    }
}
