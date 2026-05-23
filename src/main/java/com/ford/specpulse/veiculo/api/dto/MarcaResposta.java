package com.ford.specpulse.veiculo.api.dto;

import com.ford.specpulse.veiculo.dominio.Marca;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Representacao de uma marca de veiculos.")
public record MarcaResposta(

        @Schema(description = "Identificador da marca.") UUID id,
        @Schema(description = "Nome comercial da marca.", example = "Ford") String nome,
        @Schema(description = "Pais de origem da marca.", example = "Estados Unidos") String paisOrigem
) {
    public static MarcaResposta de(Marca marca) {
        return new MarcaResposta(marca.getId(), marca.getNome(), marca.getPaisOrigem());
    }
}
