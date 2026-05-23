package com.ford.specpulse.ficha.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;


@Schema(description = "Ficha tecnica padronizada de um veiculo. Formato sempre igual independente do veiculo: " +
        "uma lista de itens, um por atributo solicitado, com status explicito quando o dado nao existe.")
public record FichaTecnicaResposta(

        @Schema(description = "Identificador da versao resolvida no banco.") UUID versaoId,
        @Schema(description = "Marca do veiculo identificado.", example = "Ford") String marca,
        @Schema(description = "Modelo do veiculo identificado.", example = "Ranger Raptor") String modelo,
        @Schema(description = "Versao do veiculo identificado.", example = "Raptor 3.0 V6 Biturbo Gasolina 4x4 Cabine Dupla") String versao,
        @Schema(description = "Ano-modelo.", example = "2024") Integer anoModelo,
        @Schema(description = "Mercado da versao.", example = "BR") String mercado,
        @Schema(description = "Momento em que a consulta foi processada.") OffsetDateTime consultadoEm,
        @Schema(description = "Itens da ficha, na mesma ordem em que os atributos foram solicitados.") List<ItemFichaResposta> itens,
        @Schema(description = "Resumo de quantos itens cairam em cada status.") ResumoFichaResposta resumo
) {}
