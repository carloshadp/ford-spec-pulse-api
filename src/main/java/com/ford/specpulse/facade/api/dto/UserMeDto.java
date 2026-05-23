package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.autenticacao.dominio.Usuario;
import com.ford.specpulse.seguranca.Perfil;

import java.util.List;

public record UserMeDto(
        String id,
        String name,
        String email,
        List<String> roles
) {
    public static UserMeDto de(Usuario u) {
        return new UserMeDto(
                u.getId().toString(),
                u.getNome(),
                u.getEmail(),
                List.of(perfilIngles(u.getPerfil()))
        );
    }

    public static String perfilIngles(Perfil p) {
        if (p == null) return "read_only";
        return switch (p) {
            case SOMENTE_LEITURA -> "read_only";
            case ANALISTA -> "analyst";
            case GERENTE -> "manager";
            case VALIDADOR_DADOS -> "data_validator";
            case ADMINISTRADOR -> "admin";
        };
    }
}
