package com.ford.specpulse.compartilhado;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "Resposta padronizada para erros da API.")
public record RespostaErro(

        @Schema(description = "Momento em que o erro foi gerado.")
        OffsetDateTime timestamp,

        @Schema(description = "Status HTTP numerico.", example = "404")
        int status,

        @Schema(description = "Código curto do erro.", example = "NOT_FOUND")
        String code,

        @Schema(description = "Mensagem amigável descrevendo o erro.")
        String message,

        @Schema(description = "Path da requisição que originou o erro.")
        String path,

        @Schema(description = "Identificador rastreável da requisição.")
        String requestId,

        @Schema(description = "Detalhes de validação por campo, quando aplicável.")
        List<FieldError> details
) {

    @Schema(description = "Erro de validação em um campo específico.")
    public record FieldError(
            @Schema(description = "Nome do campo inválido.") String field,
            @Schema(description = "Mensagem do erro.") String message
    ) {
    }
}
