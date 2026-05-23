package com.ford.specpulse.especificacao.api.dto;

import com.ford.specpulse.especificacao.dominio.AtributoDefinicao;
import com.ford.specpulse.especificacao.dominio.Categoria;
import com.ford.specpulse.especificacao.dominio.DirecaoMelhor;
import com.ford.specpulse.especificacao.dominio.TipoDado;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Definicao canonica de um atributo tecnico.")
public record AtributoDefinicaoResposta(

        @Schema(description = "Identificador do atributo.") UUID id,
        @Schema(description = "Codigo canonico unico.", example = "POTENCIA_CV") String codigoCanonico,
        @Schema(description = "Nome para exibicao.", example = "Potencia maxima") String nomeExibicao,
        @Schema(description = "Categoria do atributo.") Categoria categoria,
        @Schema(description = "Unidade de medida.", example = "cv") String unidade,
        @Schema(description = "Tipo do valor armazenado.") TipoDado tipoDado,
        @Schema(description = "Direcao considerada melhor para comparacao.") DirecaoMelhor direcaoMelhor,
        @Schema(description = "Descricao explicativa.") String descricao
) {
    public static AtributoDefinicaoResposta de(AtributoDefinicao a) {
        return new AtributoDefinicaoResposta(
                a.getId(), a.getCodigoCanonico(), a.getNomeExibicao(), a.getCategoria(),
                a.getUnidade(), a.getTipoDado(), a.getDirecaoMelhor(), a.getDescricao());
    }
}
