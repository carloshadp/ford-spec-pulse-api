package com.ford.specpulse.veiculo.api.dto;

import com.ford.specpulse.veiculo.dominio.Mercado;
import com.ford.specpulse.veiculo.dominio.Segmento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Payload para criacao de um modelo de veiculo.")
public record VeiculoCriacaoRequisicao(

        @Schema(description = "Identificador da marca.")
        @NotNull(message = "marcaId e obrigatorio")
        UUID marcaId,

        @Schema(description = "Nome do modelo.", example = "Ranger")
        @NotBlank(message = "modelo e obrigatorio")
        @Size(max = 120)
        String modelo,

        @Schema(description = "Segmento de mercado.")
        @NotNull(message = "segmento e obrigatorio")
        Segmento segmento,

        @Schema(description = "Mercado em que e comercializado.")
        @NotNull(message = "mercado e obrigatorio")
        Mercado mercado,

        @Schema(description = "Ano-modelo do veiculo.", example = "2024")
        @NotNull(message = "anoModelo e obrigatorio")
        @Min(value = 1980, message = "anoModelo deve ser >= 1980")
        @Max(value = 2100, message = "anoModelo deve ser <= 2100")
        Integer anoModelo
) {}
