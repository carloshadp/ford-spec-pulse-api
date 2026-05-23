package com.ford.specpulse.autenticacao.api.dto;

import com.ford.specpulse.autenticacao.dominio.AutenticacaoServico.ResultadoAutenticacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;


@Schema(description = "Par de tokens emitido por login, registro ou refresh.")
public record TokenResposta(

        @Schema(description = "Tipo do token, sempre 'Bearer'.", example = "Bearer")
        String tipoToken,

        @Schema(description = "Token de acesso JWT (HS256). Use como Authorization: Bearer <token>.")
        String accessToken,

        @Schema(description = "Refresh token JWT (HS256). Use em POST /api/auth/refresh para rotacionar.")
        String refreshToken,

        @Schema(description = "Momento de expiracao do access token.")
        OffsetDateTime expiraEm,

        @Schema(description = "Momento de expiracao do refresh token.")
        OffsetDateTime refreshExpiraEm,

        @Schema(description = "Dados do usuario autenticado.")
        UsuarioResposta usuario
) {

    public static TokenResposta de(ResultadoAutenticacao resultado) {
        return new TokenResposta(
                "Bearer",
                resultado.acesso().valor(),
                resultado.refresh().valor(),
                resultado.acesso().expiraEm(),
                resultado.refresh().expiraEm(),
                UsuarioResposta.de(resultado.usuario())
        );
    }
}
