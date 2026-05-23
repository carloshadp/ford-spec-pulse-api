package com.ford.specpulse.versao.api.dto;

import com.ford.specpulse.veiculo.api.dto.VeiculoResposta;
import com.ford.specpulse.versao.dominio.Powertrain;
import com.ford.specpulse.versao.dominio.Versao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Representacao de uma versao comercial de veiculo.")
public record VersaoResposta(

        @Schema(description = "Identificador da versao.") UUID id,
        @Schema(description = "Veiculo (modelo) ao qual a versao pertence.") VeiculoResposta veiculo,
        @Schema(description = "Nome comercial da versao.") String nome,
        @Schema(description = "Tipo de powertrain.") Powertrain powertrain,
        @Schema(description = "Configuracao de tracao.", example = "4x4") String tracao,
        @Schema(description = "Configuracao de cabine.", example = "DUPLA") String cabine,
        @Schema(description = "Preco sugerido em BRL.", example = "339990.00") BigDecimal precoSugerido
) {
    public static VersaoResposta de(Versao versao) {
        return new VersaoResposta(
                versao.getId(),
                VeiculoResposta.de(versao.getVeiculo()),
                versao.getNome(),
                versao.getPowertrain(),
                versao.getTracao(),
                versao.getCabine(),
                versao.getPrecoSugerido()
        );
    }
}
