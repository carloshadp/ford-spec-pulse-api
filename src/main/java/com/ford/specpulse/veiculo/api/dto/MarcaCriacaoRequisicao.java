package com.ford.specpulse.veiculo.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload para criacao de uma marca.")
public record MarcaCriacaoRequisicao(

        @Schema(description = "Nome comercial da marca.", example = "Ford")
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120)
        String nome,

        @Schema(description = "Pais de origem da marca.", example = "Estados Unidos")
        @Size(max = 60)
        String paisOrigem
) {}
