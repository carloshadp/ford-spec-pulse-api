package com.ford.specpulse.ficha.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Contagem por status para leitura rapida do quao completa esta a ficha.")
public record ResumoFichaResposta(

        @Schema(description = "Itens com valor presente e confirmado.") int presentes,
        @Schema(description = "Itens cujo dado foi marcado como NAO_INFORMADO pela fonte.") int naoInformados,
        @Schema(description = "Itens sem especificacao cadastrada para a versao.") int naoDisponiveis,
        @Schema(description = "Termos que nao casaram com nenhum atributo conhecido.") int desconhecidos,
        @Schema(description = "Total de itens (= quantidade de atributos solicitados).") int total
) {}
