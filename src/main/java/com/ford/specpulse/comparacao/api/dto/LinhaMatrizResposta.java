package com.ford.specpulse.comparacao.api.dto;

import com.ford.specpulse.especificacao.api.dto.AtributoDefinicaoResposta;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Linha da matriz: um atributo com as celulas de cada versao participante.")
public record LinhaMatrizResposta(

        @Schema(description = "Definicao do atributo.") AtributoDefinicaoResposta atributo,
        @Schema(description = "Celulas (Ford + concorrentes), na ordem definida em ordensVersoes.") List<CelulaMatrizResposta> celulas
) {}
