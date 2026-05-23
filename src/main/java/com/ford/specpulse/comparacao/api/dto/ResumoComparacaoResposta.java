package com.ford.specpulse.comparacao.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo executivo da comparacao: contagem de vantagens, riscos, paridades e celulas sem dado, sob a perspectiva Ford.")
public record ResumoComparacaoResposta(

        @Schema(description = "Quantidade de atributos em que a Ford apresenta vantagem.") int vantagens,
        @Schema(description = "Quantidade de atributos em que a Ford apresenta risco.") int riscos,
        @Schema(description = "Quantidade de atributos em paridade.") int paridades,
        @Schema(description = "Quantidade de atributos sem dado suficiente para comparacao.") int semDado,
        @Schema(description = "Total de atributos avaliados.") int totalAtributos
) {}
