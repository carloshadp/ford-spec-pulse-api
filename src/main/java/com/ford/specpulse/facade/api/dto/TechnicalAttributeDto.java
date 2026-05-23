package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.especificacao.dominio.AtributoDefinicao;
import com.ford.specpulse.especificacao.dominio.Categoria;

import java.util.List;

public record TechnicalAttributeDto(
        String id,
        String canonicalName,
        String category,
        String unit,
        double strategicWeight,
        List<String> synonyms
) {
    public static TechnicalAttributeDto de(AtributoDefinicao a) {
        return de(a, List.of());
    }

    public static TechnicalAttributeDto de(AtributoDefinicao a, List<String> sinonimos) {
        return new TechnicalAttributeDto(
                SlugUtil.slugAtributo(a.getCodigoCanonico()),
                a.getNomeExibicao(),
                categoriaIngles(a.getCategoria()),
                a.getUnidade(),
                0.5,
                sinonimos
        );
    }

    public static String categoriaIngles(Categoria c) {
        if (c == null) return "unknown";
        return switch (c) {
            case MOTOR -> "engine_transmission";
            case UTILIDADE -> "capacity_use";
            case SEGURANCA -> "safety";
            case OFFROAD -> "traction_offroad";
            case CONECTIVIDADE -> "connectivity";
            case CONFORTO -> "comfort";
            case EFICIENCIA -> "engine_transmission";
            case ADAS -> "adas";
            case PAINEL_DIGITAL -> "digital_cockpit";
            case ACABAMENTO -> "visual_finish";
        };
    }
}
