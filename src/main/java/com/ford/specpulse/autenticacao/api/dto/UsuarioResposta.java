package com.ford.specpulse.autenticacao.api.dto;

import com.ford.specpulse.autenticacao.dominio.Usuario;
import com.ford.specpulse.seguranca.Perfil;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;


@Schema(description = "Representacao publica de um usuario.")
public record UsuarioResposta(

        @Schema(description = "Identificador unico.")
        UUID id,

        @Schema(description = "Nome completo.")
        String nome,

        @Schema(description = "Email.")
        String email,

        @Schema(description = "Perfil / role.")
        Perfil perfil,

        @Schema(description = "Indica se o usuario esta ativo.")
        boolean ativo
) {

    public static UsuarioResposta de(Usuario usuario) {
        return new UsuarioResposta(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo());
    }
}
