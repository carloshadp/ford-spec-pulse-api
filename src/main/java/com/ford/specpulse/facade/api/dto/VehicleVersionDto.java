package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.versao.dominio.Powertrain;
import com.ford.specpulse.versao.dominio.Versao;

import java.math.BigDecimal;

public record VehicleVersionDto(
        String id,
        String vehicleId,
        String name,
        String powertrain,
        String drivetrain,
        String versionLevel,
        double dataCompleteness,
        PriceRange priceRange
) {

    public record PriceRange(String currency, BigDecimal min, BigDecimal max) {
    }

    public static VehicleVersionDto de(Versao v) {
        String marcaNome = v.getVeiculo().getMarca().getNome();
        String modelo = v.getVeiculo().getModelo();
        int ano = v.getVeiculo().getAnoModelo();
        return new VehicleVersionDto(
                SlugUtil.slugVersao(marcaNome, modelo, ano, v.getNome()),
                SlugUtil.slugVeiculo(marcaNome, modelo, ano),
                v.getNome(),
                powertrainIngles(v.getPowertrain()),
                v.getTracao() != null ? v.getTracao().toLowerCase() : "unknown",
                "unknown",
                0.8,
                new PriceRange("BRL", v.getPrecoSugerido(), v.getPrecoSugerido())
        );
    }

    private static String powertrainIngles(Powertrain p) {
        if (p == null) return "unknown";
        return switch (p) {
            case DIESEL -> "diesel";
            case GASOLINA -> "gasoline";
            case FLEX -> "flex";
            case HIBRIDO -> "hybrid";
            case ELETRICO -> "electric";
            default -> "unknown";
        };
    }
}
