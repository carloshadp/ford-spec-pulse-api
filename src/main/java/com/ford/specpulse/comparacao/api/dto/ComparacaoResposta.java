package com.ford.specpulse.comparacao.api.dto;

import com.ford.specpulse.comparacao.dominio.Comparacao;
import com.ford.specpulse.comparacao.dominio.ComparacaoCelula;
import com.ford.specpulse.comparacao.dominio.ComparacaoVersao;
import com.ford.specpulse.comparacao.dominio.PerfilCliente;
import com.ford.specpulse.comparacao.dominio.StatusCelula;
import com.ford.specpulse.especificacao.api.dto.AtributoDefinicaoResposta;
import com.ford.specpulse.especificacao.dominio.AtributoDefinicao;
import com.ford.specpulse.versao.api.dto.VersaoResposta;
import com.ford.specpulse.versao.dominio.Versao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Schema(description = "Resultado completo de uma analise comparativa.")
public record ComparacaoResposta(

        @Schema(description = "Identificador da comparacao.") UUID id,
        @Schema(description = "Titulo da analise.") String titulo,
        @Schema(description = "Descricao livre.") String descricao,
        @Schema(description = "Versao Ford de referencia.") VersaoResposta versaoFord,
        @Schema(description = "Versoes concorrentes, na ordem de inclusao.") List<VersaoResposta> concorrentes,
        @Schema(description = "Perfil de cliente associado.") PerfilCliente perfilCliente,
        @Schema(description = "Usuario que criou a analise.") String criadoPor,
        @Schema(description = "Momento de criacao.") OffsetDateTime dataCriacao,
        @Schema(description = "Resumo executivo (contagens por status).") ResumoComparacaoResposta resumo,
        @Schema(description = "Matriz: uma linha por atributo, uma celula por versao.") List<LinhaMatrizResposta> matriz
) {

    public static ComparacaoResposta de(Comparacao comparacao) {
        VersaoResposta versaoFord = VersaoResposta.de(comparacao.getVersaoFord());
        List<VersaoResposta> concorrentes = comparacao.getConcorrentes().stream()
                .sorted(Comparator.comparingInt(ComparacaoVersao::getOrdem))
                .map(cv -> VersaoResposta.de(cv.getVersao()))
                .toList();

        List<UUID> ordemVersoes = new ArrayList<>();
        ordemVersoes.add(comparacao.getVersaoFord().getId());
        comparacao.getConcorrentes().stream()
                .sorted(Comparator.comparingInt(ComparacaoVersao::getOrdem))
                .forEach(cv -> ordemVersoes.add(cv.getVersao().getId()));

        Map<UUID, AtributoDefinicao> atributoPorId = new LinkedHashMap<>();
        Map<UUID, Map<UUID, ComparacaoCelula>> celulasPorAtributo = new LinkedHashMap<>();

        for (ComparacaoCelula celula : comparacao.getCelulas()) {
            UUID atrId = celula.getAtributo().getId();
            atributoPorId.putIfAbsent(atrId, celula.getAtributo());
            celulasPorAtributo
                    .computeIfAbsent(atrId, k -> new LinkedHashMap<>())
                    .put(celula.getVersao().getId(), celula);
        }

        List<LinhaMatrizResposta> matriz = new ArrayList<>();
        for (Map.Entry<UUID, AtributoDefinicao> entrada : atributoPorId.entrySet()) {
            Map<UUID, ComparacaoCelula> porVersao = celulasPorAtributo.get(entrada.getKey());
            List<CelulaMatrizResposta> celulas = new ArrayList<>();
            for (UUID versaoId : ordemVersoes) {
                ComparacaoCelula celula = porVersao.get(versaoId);
                celulas.add(celula != null ? CelulaMatrizResposta.de(celula) : null);
            }
            matriz.add(new LinhaMatrizResposta(AtributoDefinicaoResposta.de(entrada.getValue()), celulas));
        }

        ResumoComparacaoResposta resumo = montarResumo(comparacao);

        return new ComparacaoResposta(
                comparacao.getId(),
                comparacao.getTitulo(),
                comparacao.getDescricao(),
                versaoFord,
                concorrentes,
                comparacao.getPerfilCliente(),
                comparacao.getCriadoPor(),
                comparacao.getDataCriacao(),
                resumo,
                matriz
        );
    }

    private static ResumoComparacaoResposta montarResumo(Comparacao comparacao) {
        Versao ford = comparacao.getVersaoFord();
        int vantagens = 0, riscos = 0, paridades = 0, semDado = 0;
        java.util.Set<UUID> atributosVistos = new java.util.HashSet<>();
        for (ComparacaoCelula c : comparacao.getCelulas()) {
            // contamos apenas celulas dos concorrentes (excluimos a referencia Ford)
            if (c.getVersao().getId().equals(ford.getId())) continue;
            atributosVistos.add(c.getAtributo().getId());
            StatusCelula s = c.getStatusCelula();
            if (s == StatusCelula.VANTAGEM) vantagens++;
            else if (s == StatusCelula.RISCO) riscos++;
            else if (s == StatusCelula.PARIDADE) paridades++;
            else if (s == StatusCelula.SEM_DADO || s == StatusCelula.NAO_COMPARAVEL) semDado++;
        }
        return new ResumoComparacaoResposta(vantagens, riscos, paridades, semDado, atributosVistos.size());
    }
}
