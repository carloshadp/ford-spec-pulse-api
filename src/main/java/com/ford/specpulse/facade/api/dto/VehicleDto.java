package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.veiculo.dominio.Segmento;
import com.ford.specpulse.veiculo.dominio.Veiculo;

import java.time.OffsetDateTime;

public record VehicleDto(
        String id,
        String brandId,
        String model,
        String segment,
        String market,
        int year,
        String sourceRevision,
        OffsetDateTime updatedAt
) {
    public static VehicleDto de(Veiculo v) {
        String marcaNome = v.getMarca().getNome();
        return new VehicleDto(
                SlugUtil.slugVeiculo(marcaNome, v.getModelo(), v.getAnoModelo()),
                SlugUtil.slugMarca(marcaNome),
                v.getModelo(),
                segmentoIngles(v.getSegmento()),
                v.getMercado().name(),
                v.getAnoModelo(),
                v.getMercado().name().toLowerCase() + "-q1-" + v.getAnoModelo(),
                v.getDataAtualizacao()
        );
    }

    private static String segmentoIngles(Segmento s) {
        if (s == null) return "unknown";
        return switch (s) {
            case PICAPE_MEDIA -> "midsize_pickup";
            case PICAPE_GRANDE -> "midsize_pickup";
            case SUV_MEDIO -> "suv";
            case SUV_GRANDE -> "suv";
            default -> "unknown";
        };
    }
}
