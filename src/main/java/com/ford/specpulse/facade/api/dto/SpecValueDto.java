package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.especificacao.dominio.Especificacao;
import com.ford.specpulse.especificacao.dominio.NivelConfianca;
import com.ford.specpulse.especificacao.dominio.StatusEspecificacao;
import com.ford.specpulse.versao.dominio.Versao;

import java.time.OffsetDateTime;
import java.util.List;

public record SpecValueDto(
        String versionId,
        String attributeId,
        Object value,
        String unit,
        String status,
        double confidence,
        String confidenceLevel,
        String sourceLabel,
        List<String> evidenceIds,
        OffsetDateTime updatedAt
) {
    public static SpecValueDto de(Especificacao e) {
        Object valor = e.getValorBooleano() != null ? e.getValorBooleano()
                : e.getValorNumero() != null ? e.getValorNumero()
                : e.getValorTexto();

        String sourceLabel = e.getFonte() != null ? e.getFonte().getNome() : null;
        List<String> evidenceIds = e.getFonte() != null
                ? List.of(SlugUtil.slugFonte(e.getFonte().getNome()))
                : List.of();

        Versao v = e.getVersao();
        String versionId = SlugUtil.slugVersao(
                v.getVeiculo().getMarca().getNome(),
                v.getVeiculo().getModelo(),
                v.getVeiculo().getAnoModelo(),
                v.getNome()
        );

        return new SpecValueDto(
                versionId,
                SlugUtil.slugAtributo(e.getAtributo().getCodigoCanonico()),
                valor,
                e.getUnidade() != null ? e.getUnidade() : e.getAtributo().getUnidade(),
                statusIngles(e.getStatus()),
                confidenceFloat(e.getConfianca()),
                confidenceLevel(e.getConfianca()),
                sourceLabel,
                evidenceIds,
                e.getDataAtualizacao()
        );
    }

    private static String statusIngles(StatusEspecificacao s) {
        if (s == null) return "not_informed";
        return switch (s) {
            case CONFIRMADO -> "found";
            case PENDENTE_VALIDACAO -> "pending_validation";
            case NAO_INFORMADO -> "not_informed";
            case CONFLITO -> "conflict";
        };
    }

    private static double confidenceFloat(NivelConfianca n) {
        if (n == null) return 0.0;
        return switch (n) {
            case ALTA -> 0.9;
            case MEDIA -> 0.65;
            case BAIXA -> 0.35;
        };
    }

    private static String confidenceLevel(NivelConfianca n) {
        if (n == null) return "unknown";
        return switch (n) {
            case ALTA -> "high";
            case MEDIA -> "medium";
            case BAIXA -> "low";
        };
    }
}
