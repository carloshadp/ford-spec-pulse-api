package com.ford.specpulse.especificacao.api.dto;

import com.ford.specpulse.especificacao.dominio.Fonte;
import com.ford.specpulse.especificacao.dominio.TipoFonte;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Fonte de evidencia para uma especificacao.")
public record FonteResposta(

        @Schema(description = "Identificador da fonte.") UUID id,
        @Schema(description = "Nome da fonte.") String nome,
        @Schema(description = "Categoria da fonte.") TipoFonte tipo,
        @Schema(description = "Endereco web da fonte, se aplicavel.") String url,
        @Schema(description = "Descricao adicional.") String descricao
) {
    public static FonteResposta de(Fonte f) {
        if (f == null) return null;
        return new FonteResposta(f.getId(), f.getNome(), f.getTipo(), f.getUrl(), f.getDescricao());
    }
}
