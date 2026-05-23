package com.ford.specpulse.compartilhado;

import java.text.Normalizer;

public final class SlugUtil {

    private SlugUtil() {
    }

    public static String normalizar(String texto) {
        if (texto == null || texto.isBlank()) return "unknown";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
    }

    public static String slugMarca(String nome) {
        return "brand-" + normalizar(nome);
    }

    public static String slugVeiculo(String marcaNome, String modelo, int anoModelo) {
        return "vehicle-" + normalizar(marcaNome) + "-" + normalizar(modelo) + "-" + anoModelo;
    }

    public static String slugVersao(String marcaNome, String modelo, int anoModelo, String nomeVersao) {
        return "version-" + normalizar(marcaNome) + "-" + normalizar(modelo) + "-"
                + normalizar(nomeVersao) + "-" + anoModelo;
    }

    public static String slugAtributo(String codigoCanonico) {
        return "attr_" + normalizar(codigoCanonico).replace("-", "_");
    }

    public static String slugFonte(String nome) {
        return "source-" + normalizar(nome);
    }
}
