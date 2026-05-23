package com.ford.specpulse.especificacao.api.dto;

import com.ford.specpulse.especificacao.dominio.Especificacao;
import com.ford.specpulse.especificacao.dominio.NivelConfianca;
import com.ford.specpulse.especificacao.dominio.StatusEspecificacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Valor de um atributo para uma versao, com fonte e confianca.")
public record EspecificacaoResposta(

        @Schema(description = "Identificador da especificacao.") UUID id,
        @Schema(description = "Versao a qual a especificacao pertence.") UUID versaoId,
        @Schema(description = "Atributo descrito.") AtributoDefinicaoResposta atributo,
        @Schema(description = "Fonte da evidencia (pode ser nula quando NAO_INFORMADO).") FonteResposta fonte,
        @Schema(description = "Valor texto, quando aplicavel.") String valorTexto,
        @Schema(description = "Valor numerico, quando aplicavel.") BigDecimal valorNumero,
        @Schema(description = "Valor booleano, quando aplicavel.") Boolean valorBooleano,
        @Schema(description = "Unidade do valor, quando aplicavel.") String unidade,
        @Schema(description = "Status do dado.") StatusEspecificacao status,
        @Schema(description = "Nivel de confianca atribuido.") NivelConfianca confianca,
        @Schema(description = "Momento em que o valor foi capturado da fonte.") OffsetDateTime dataCaptura,
        @Schema(description = "Observacao livre.") String observacao
) {
    public static EspecificacaoResposta de(Especificacao e) {
        return new EspecificacaoResposta(
                e.getId(),
                e.getVersao().getId(),
                AtributoDefinicaoResposta.de(e.getAtributo()),
                FonteResposta.de(e.getFonte()),
                e.getValorTexto(),
                e.getValorNumero(),
                e.getValorBooleano(),
                e.getUnidade(),
                e.getStatus(),
                e.getConfianca(),
                e.getDataCaptura(),
                e.getObservacao()
        );
    }
}
