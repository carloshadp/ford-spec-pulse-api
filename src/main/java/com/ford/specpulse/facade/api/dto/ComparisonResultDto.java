package com.ford.specpulse.facade.api.dto;

import com.ford.specpulse.comparacao.dominio.Comparacao;
import com.ford.specpulse.comparacao.dominio.ComparacaoCelula;
import com.ford.specpulse.comparacao.dominio.ComparacaoVersao;
import com.ford.specpulse.compartilhado.SlugUtil;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

public record ComparisonResultDto(
        String id,
        String status,
        OffsetDateTime createdAt,
        String createdBy,
        RequestInfo request,
        List<VehicleVersionDto> comparedVersions,
        List<MatrixRowDto> rows,
        List<Object> gaps,
        SummaryDto summary
) {

    public record RequestInfo(
            String referenceVersionId,
            List<String> competitorVersionIds
    ) {
    }

    public record MatrixRowDto(
            String attributeId,
            String canonicalName,
            String category,
            List<CellDto> cells
    ) {
    }

    public record CellDto(
            String versionId,
            Object value,
            String unit,
            String difference,
            double confidence,
            String confidenceLevel
    ) {
    }

    public record SummaryDto(
            double confidence,
            String executiveSummary,
            List<String> keyAdvantages,
            List<String> keyGaps,
            List<String> validationWarnings
    ) {
    }

    public static ComparisonResultDto de(Comparacao c) {
        String refVersionId = SlugUtil.slugVersao(
                c.getVersaoFord().getVeiculo().getMarca().getNome(),
                c.getVersaoFord().getVeiculo().getModelo(),
                c.getVersaoFord().getVeiculo().getAnoModelo(),
                c.getVersaoFord().getNome()
        );

        List<String> competitorIds = c.getConcorrentes().stream()
                .sorted(Comparator.comparingInt(ComparacaoVersao::getOrdem))
                .map(cv -> SlugUtil.slugVersao(
                        cv.getVersao().getVeiculo().getMarca().getNome(),
                        cv.getVersao().getVeiculo().getModelo(),
                        cv.getVersao().getVeiculo().getAnoModelo(),
                        cv.getVersao().getNome()
                ))
                .toList();

        List<VehicleVersionDto> versions = new java.util.ArrayList<>();
        versions.add(VehicleVersionDto.de(c.getVersaoFord()));
        c.getConcorrentes().stream()
                .sorted(Comparator.comparingInt(ComparacaoVersao::getOrdem))
                .forEach(cv -> versions.add(VehicleVersionDto.de(cv.getVersao())));

        List<MatrixRowDto> rows = montarLinhas(c);

        int vantagens = 0, riscos = 0, paridades = 0;
        for (ComparacaoCelula cel : c.getCelulas()) {
            if (cel.getVersao().getId().equals(c.getVersaoFord().getId())) continue;
            switch (cel.getStatusCelula()) {
                case VANTAGEM -> vantagens++;
                case RISCO -> riscos++;
                case PARIDADE -> paridades++;
                default -> { }
            }
        }

        SummaryDto summary = new SummaryDto(
                0.75,
                String.format("Análise comparativa com %d vantagem(ns), %d risco(s) e %d paridade(s) identificados.",
                        vantagens, riscos, paridades),
                vantagens > 0 ? List.of("Ford apresenta vantagem em " + vantagens + " atributo(s).") : List.of(),
                riscos > 0 ? List.of("Ford apresenta risco em " + riscos + " atributo(s).") : List.of(),
                List.of("Dados marcados como not_informed não implicam ausência confirmada.")
        );

        return new ComparisonResultDto(
                c.getId().toString(),
                "ready",
                c.getDataCriacao(),
                c.getCriadoPor(),
                new RequestInfo(refVersionId, competitorIds),
                versions,
                rows,
                List.of(),
                summary
        );
    }

    private static List<MatrixRowDto> montarLinhas(Comparacao c) {
        java.util.Map<java.util.UUID, com.ford.specpulse.especificacao.dominio.AtributoDefinicao> atributos =
                new java.util.LinkedHashMap<>();
        java.util.Map<java.util.UUID, java.util.Map<java.util.UUID, ComparacaoCelula>> porAtributo =
                new java.util.LinkedHashMap<>();

        for (ComparacaoCelula cel : c.getCelulas()) {
            java.util.UUID aId = cel.getAtributo().getId();
            atributos.putIfAbsent(aId, cel.getAtributo());
            porAtributo.computeIfAbsent(aId, k -> new java.util.LinkedHashMap<>())
                    .put(cel.getVersao().getId(), cel);
        }

        java.util.Map<java.util.UUID, String> slugPorVersao = new java.util.HashMap<>();
        slugPorVersao.put(c.getVersaoFord().getId(), slugVersao(c.getVersaoFord()));
        c.getConcorrentes().forEach(cv -> slugPorVersao.put(cv.getVersao().getId(), slugVersao(cv.getVersao())));

        List<java.util.UUID> ordemVersoes = new java.util.ArrayList<>();
        ordemVersoes.add(c.getVersaoFord().getId());
        c.getConcorrentes().stream()
                .sorted(Comparator.comparingInt(ComparacaoVersao::getOrdem))
                .forEach(cv -> ordemVersoes.add(cv.getVersao().getId()));

        List<MatrixRowDto> linhas = new java.util.ArrayList<>();
        for (var entrada : atributos.entrySet()) {
            var a = entrada.getValue();
            var celsPorVersao = porAtributo.get(entrada.getKey());
            List<CellDto> celulas = ordemVersoes.stream().map(vId -> {
                String slug = slugPorVersao.getOrDefault(vId, vId.toString());
                ComparacaoCelula cel = celsPorVersao != null ? celsPorVersao.get(vId) : null;
                if (cel == null) return new CellDto(slug, null, a.getUnidade(), "unknown", 0.0, "unknown");
                Object val = cel.getValorBooleano() != null ? cel.getValorBooleano()
                        : cel.getValorNumero() != null ? cel.getValorNumero()
                        : cel.getValorTexto();
                return new CellDto(
                        slug,
                        val,
                        cel.getUnidade(),
                        statusDiff(cel.getStatusCelula()),
                        confFloat(cel.getConfianca()),
                        confLevel(cel.getConfianca())
                );
            }).toList();
            linhas.add(new MatrixRowDto(
                    SlugUtil.slugAtributo(a.getCodigoCanonico()),
                    a.getNomeExibicao(),
                    TechnicalAttributeDto.de(a).category(),
                    celulas
            ));
        }
        return linhas;
    }

    private static String slugVersao(com.ford.specpulse.versao.dominio.Versao v) {
        return SlugUtil.slugVersao(
                v.getVeiculo().getMarca().getNome(),
                v.getVeiculo().getModelo(),
                v.getVeiculo().getAnoModelo(),
                v.getNome()
        );
    }

    private static String statusDiff(com.ford.specpulse.comparacao.dominio.StatusCelula s) {
        if (s == null) return "unknown";
        return switch (s) {
            case VANTAGEM -> "advantage";
            case RISCO -> "risk";
            case PARIDADE -> "parity";
            case REFERENCIA -> "reference";
            default -> "unknown";
        };
    }

    private static double confFloat(com.ford.specpulse.especificacao.dominio.NivelConfianca n) {
        if (n == null) return 0.0;
        return switch (n) {
            case ALTA -> 0.9;
            case MEDIA -> 0.65;
            case BAIXA -> 0.35;
        };
    }

    private static String confLevel(com.ford.specpulse.especificacao.dominio.NivelConfianca n) {
        if (n == null) return "unknown";
        return switch (n) {
            case ALTA -> "high";
            case MEDIA -> "medium";
            case BAIXA -> "low";
        };
    }
}
