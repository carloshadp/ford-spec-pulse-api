package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.veiculo.dominio.Marca;

public record BrandDto(
        String id,
        String name,
        String country,
        boolean isFord
) {
    public static BrandDto de(Marca m) {
        return new BrandDto(
                SlugUtil.slugMarca(m.getNome()),
                m.getNome(),
                m.getPaisOrigem(),
                "Ford".equalsIgnoreCase(m.getNome())
        );
    }
}
