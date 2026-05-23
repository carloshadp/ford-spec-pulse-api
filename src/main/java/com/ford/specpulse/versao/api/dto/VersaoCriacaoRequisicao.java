package com.ford.specpulse.versao.api.dto;

import com.ford.specpulse.versao.dominio.Powertrain;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Payload para criacao de uma versao de veiculo.")
public record VersaoCriacaoRequisicao(

        @Schema(description = "Identificador do veiculo (modelo).")
        @NotNull(message = "veiculoId e obrigatorio")
        UUID veiculoId,

        @Schema(description = "Nome comercial da versao.", example = "XLT 3.0 V6 Diesel 4x4 Cabine Dupla")
        @NotBlank(message = "nome e obrigatorio")
        @Size(max = 120)
        String nome,

        @Schema(description = "Tipo de powertrain.")
        @NotNull(message = "powertrain e obrigatorio")
        Powertrain powertrain,

        @Schema(description = "Configuracao de tracao.", example = "4x4")
        @NotBlank(message = "tracao e obrigatoria")
        @Size(max = 10)
        String tracao,

        @Schema(description = "Configuracao de cabine.", example = "DUPLA")
        @Size(max = 20)
        String cabine,

        @Schema(description = "Preco sugerido em BRL.", example = "339990.00")
        @DecimalMin(value = "0.0", inclusive = false, message = "precoSugerido deve ser positivo")
        BigDecimal precoSugerido
) {}
