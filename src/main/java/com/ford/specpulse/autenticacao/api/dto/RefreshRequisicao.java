package com.ford.specpulse.autenticacao.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Requisicao para trocar um refresh token valido por um novo par de tokens.")
public record RefreshRequisicao(

        @Schema(description = "Refresh token recebido no login anterior.")
        @NotBlank(message = "refreshToken e obrigatorio")
        @Size(max = 2048)
        String refreshToken
) {
}
