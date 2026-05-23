package com.ford.specpulse.comparacao.api.dto;

import com.ford.specpulse.comparacao.dominio.ComparacaoCelula;
import com.ford.specpulse.comparacao.dominio.StatusCelula;
import com.ford.specpulse.especificacao.dominio.NivelConfianca;
import com.ford.specpulse.especificacao.dominio.StatusEspecificacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Celula da matriz comparativa para uma combinacao versao x atributo.")
public record CelulaMatrizResposta(

        @Schema(description = "Identificador da versao a que a celula pertence.") UUID versaoId,
        @Schema(description = "Valor texto, quando aplicavel.") String valorTexto,
        @Schema(description = "Valor numerico, quando aplicavel.") BigDecimal valorNumero,
        @Schema(description = "Valor booleano, quando aplicavel.") Boolean valorBooleano,
        @Schema(description = "Unidade do valor.") String unidade,
        @Schema(description = "Status da celula em relacao a Ford.") StatusCelula statusCelula,
        @Schema(description = "Status do dado de origem (governanca).") StatusEspecificacao statusDado,
        @Schema(description = "Nivel de confianca do valor.") NivelConfianca confianca,
        @Schema(description = "Nome da fonte capturada no momento do snapshot.") String fonte
) {
    public static CelulaMatrizResposta de(ComparacaoCelula c) {
        return new CelulaMatrizResposta(
                c.getVersao().getId(),
                c.getValorTexto(),
                c.getValorNumero(),
                c.getValorBooleano(),
                c.getUnidade(),
                c.getStatusCelula(),
                c.getStatusDado(),
                c.getConfianca(),
                c.getFonteNomeSnapshot()
        );
    }
}
