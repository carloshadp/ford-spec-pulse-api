package com.ford.specpulse.autenticacao.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


@Schema(description = "Credenciais para autenticacao por email + senha.")
public record LoginRequisicao(

        @Schema(description = "Email do usuario.", example = "ana@ford.internal")
        @NotBlank(message = "email e obrigatorio")
        @Email(message = "email com formato invalido")
        @Size(max = 160)
        String email,

        @Schema(description = "Senha em texto puro.")
        @NotBlank(message = "senha e obrigatoria")
        @Size(min = 1, max = 80)
        String senha
) {
}
