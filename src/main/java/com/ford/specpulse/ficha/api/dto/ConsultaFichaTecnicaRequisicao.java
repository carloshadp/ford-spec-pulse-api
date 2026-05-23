package com.ford.specpulse.ficha.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;


@Schema(description = "Entrada da consulta de ficha tecnica: identifica o veiculo e os atributos desejados.")
public record ConsultaFichaTecnicaRequisicao(

        @Schema(description = "Nome da marca (case-insensitive).", example = "Ford")
        @NotBlank(message = "marca e obrigatoria")
        @Size(max = 120)
        String marca,

        @Schema(description = "Modelo do veiculo (case-insensitive).", example = "Ranger Raptor")
        @NotBlank(message = "modelo e obrigatorio")
        @Size(max = 120)
        String modelo,

        @Schema(description = "Versao desejada. Aceita nome exato ou trecho que identifique unicamente uma versao.",
                example = "Raptor 3.0 V6 Biturbo Gasolina 4x4 Cabine Dupla")
        @NotBlank(message = "versao e obrigatoria")
        @Size(max = 160)
        String versao,

        @Schema(description = "Lista livre de atributos a pesquisar. Aceita codigo canonico, nome de exibicao ou sinonimo.",
                example = "[\"potencia\", \"torque\", \"pneus\", \"angulo de ataque\", \"controle de descida\"]")
        @NotEmpty(message = "informe ao menos um atributo")
        @Size(max = 50, message = "no maximo 50 atributos por consulta")
        List<@NotBlank @Size(max = 120) String> atributos
) {}
