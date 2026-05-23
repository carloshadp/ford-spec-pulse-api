package com.ford.specpulse.comparacao.api.dto;

import com.ford.specpulse.comparacao.dominio.PerfilCliente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Payload para criacao de uma analise comparativa.")
public record ComparacaoCriacaoRequisicao(

        @Schema(description = "Titulo curto da analise.", example = "Ranger XLT vs Hilux SRX vs S10 High Country")
        @NotBlank(message = "titulo e obrigatorio")
        @Size(max = 160)
        String titulo,

        @Schema(description = "Descricao livre (objetivo, observacoes).")
        @Size(max = 500)
        String descricao,

        @Schema(description = "Identificador da versao Ford de referencia.")
        @NotNull(message = "versaoFordId e obrigatorio")
        UUID versaoFordId,

        @Schema(description = "Lista de 1 a 3 versoes concorrentes para comparar com a referencia.")
        @NotEmpty(message = "informe ao menos um concorrente")
        @Size(min = 1, max = 3, message = "a comparacao aceita entre 1 e 3 concorrentes")
        List<UUID> versoesConcorrentesIds,

        @Schema(description = "Perfil de cliente para contextualizar a leitura da analise.")
        PerfilCliente perfilCliente
) {}
