package com.ford.specpulse.veiculo.api.dto;

import com.ford.specpulse.veiculo.dominio.Mercado;
import com.ford.specpulse.veiculo.dominio.Segmento;
import com.ford.specpulse.veiculo.dominio.Veiculo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Representacao de um modelo de veiculo.")
public record VeiculoResposta(

        @Schema(description = "Identificador do veiculo.") UUID id,
        @Schema(description = "Marca a qual o veiculo pertence.") MarcaResposta marca,
        @Schema(description = "Nome do modelo.", example = "Ranger") String modelo,
        @Schema(description = "Segmento de mercado.") Segmento segmento,
        @Schema(description = "Mercado em que e comercializado.") Mercado mercado,
        @Schema(description = "Ano-modelo do veiculo.", example = "2024") Integer anoModelo
) {
    public static VeiculoResposta de(Veiculo veiculo) {
        return new VeiculoResposta(
                veiculo.getId(),
                MarcaResposta.de(veiculo.getMarca()),
                veiculo.getModelo(),
                veiculo.getSegmento(),
                veiculo.getMercado(),
                veiculo.getAnoModelo()
        );
    }
}
