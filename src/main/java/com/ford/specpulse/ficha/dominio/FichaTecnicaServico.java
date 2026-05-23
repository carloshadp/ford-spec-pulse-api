package com.ford.specpulse.ficha.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.especificacao.dominio.AtributoDefinicao;
import com.ford.specpulse.especificacao.dominio.AtributoDefinicaoServico;
import com.ford.specpulse.especificacao.dominio.Especificacao;
import com.ford.specpulse.especificacao.dominio.StatusEspecificacao;
import com.ford.specpulse.especificacao.dominio.TipoDado;
import com.ford.specpulse.especificacao.persistencia.EspecificacaoRepositorio;
import com.ford.specpulse.ficha.api.dto.ConsultaFichaTecnicaRequisicao;
import com.ford.specpulse.ficha.api.dto.FichaTecnicaResposta;
import com.ford.specpulse.ficha.api.dto.ItemFichaResposta;
import com.ford.specpulse.ficha.api.dto.ResumoFichaResposta;
import com.ford.specpulse.veiculo.dominio.Veiculo;
import com.ford.specpulse.veiculo.persistencia.VeiculoRepositorio;
import com.ford.specpulse.versao.dominio.Versao;
import com.ford.specpulse.versao.persistencia.VersaoRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
public class FichaTecnicaServico {

    private final VeiculoRepositorio veiculoRepositorio;
    private final VersaoRepositorio versaoRepositorio;
    private final EspecificacaoRepositorio especificacaoRepositorio;
    private final AtributoDefinicaoServico atributoServico;

    public FichaTecnicaServico(VeiculoRepositorio veiculoRepositorio,
                               VersaoRepositorio versaoRepositorio,
                               EspecificacaoRepositorio especificacaoRepositorio,
                               AtributoDefinicaoServico atributoServico) {
        this.veiculoRepositorio = veiculoRepositorio;
        this.versaoRepositorio = versaoRepositorio;
        this.especificacaoRepositorio = especificacaoRepositorio;
        this.atributoServico = atributoServico;
    }

    @Transactional(readOnly = true)
    public FichaTecnicaResposta consultar(ConsultaFichaTecnicaRequisicao requisicao) {
        Veiculo veiculo = resolverVeiculo(requisicao.marca(), requisicao.modelo());
        Versao versao = resolverVersao(veiculo, requisicao.versao());

        List<Especificacao> specs = especificacaoRepositorio.findByVersaoId(versao.getId());

        List<ItemFichaResposta> itens = new ArrayList<>(requisicao.atributos().size());
        for (String termo : requisicao.atributos()) {
            itens.add(montarItem(termo, specs));
        }

        return new FichaTecnicaResposta(
                versao.getId(),
                veiculo.getMarca().getNome(),
                veiculo.getModelo(),
                versao.getNome(),
                veiculo.getAnoModelo(),
                veiculo.getMercado().name(),
                OffsetDateTime.now(),
                itens,
                montarResumo(itens)
        );
    }


    private Veiculo resolverVeiculo(String marca, String modelo) {
        List<Veiculo> achados = veiculoRepositorio
                .findByMarcaNomeIgnoreCaseAndModeloIgnoreCase(marca.trim(), modelo.trim());
        if (achados.isEmpty()) {
            throw new RecursoNaoEncontradoException(
                    "Nenhum veiculo encontrado para marca '" + marca + "' e modelo '" + modelo + "'.");
        }
        // Quando ha mais de um ano-modelo cadastrado, retorna o mais recente.
        return achados.stream()
                .max(Comparator.comparing(Veiculo::getAnoModelo))
                .orElseThrow();
    }

    private Versao resolverVersao(Veiculo veiculo, String termoVersao) {
        String termo = termoVersao.trim();

        List<Versao> exato = versaoRepositorio.findByVeiculoIdAndNomeIgnoreCase(veiculo.getId(), termo);
        if (exato.size() == 1) {
            return exato.get(0);
        }
        if (exato.size() > 1) {
            throw new RegraNegocioException(
                    "Versao '" + termoVersao + "' e ambigua para " + veiculo.getModelo()
                            + " (encontradas " + exato.size() + " correspondencias exatas).");
        }

        List<Versao> parcial = versaoRepositorio.findByVeiculoIdAndNomeContainingIgnoreCase(veiculo.getId(), termo);
        if (parcial.isEmpty()) {
            throw new RecursoNaoEncontradoException(
                    "Versao '" + termoVersao + "' nao encontrada para " + veiculo.getModelo() + ".");
        }
        if (parcial.size() > 1) {
            String candidatos = parcial.stream().map(Versao::getNome).reduce((a, b) -> a + " | " + b).orElse("");
            throw new RegraNegocioException(
                    "Versao '" + termoVersao + "' e ambigua. Candidatas: " + candidatos);
        }
        return parcial.get(0);
    }


    private ItemFichaResposta montarItem(String termo, List<Especificacao> specsDaVersao) {
        Optional<AtributoDefinicao> atributoOpt = atributoServico.buscarPorTermo(termo);
        if (atributoOpt.isEmpty()) {
            return ItemFichaResposta.atributoDesconhecido(termo);
        }
        AtributoDefinicao atributo = atributoOpt.get();

        Optional<Especificacao> specOpt = specsDaVersao.stream()
                .filter(s -> s.getAtributo().getId().equals(atributo.getId()))
                .findFirst();

        if (specOpt.isEmpty()) {
            return new ItemFichaResposta(
                    termo, atributo.getCodigoCanonico(), atributo.getNomeExibicao(),
                    atributo.getCategoria().name(),
                    null, atributo.getUnidade(),
                    StatusItemFicha.NAO_DISPONIVEL,
                    null, null, null);
        }

        Especificacao spec = specOpt.get();
        if (spec.getStatus() == StatusEspecificacao.NAO_INFORMADO) {
            return new ItemFichaResposta(
                    termo, atributo.getCodigoCanonico(), atributo.getNomeExibicao(),
                    atributo.getCategoria().name(),
                    null,
                    spec.getUnidade() != null ? spec.getUnidade() : atributo.getUnidade(),
                    StatusItemFicha.NAO_INFORMADO,
                    spec.getConfianca(),
                    spec.getFonte() != null ? spec.getFonte().getNome() : null,
                    spec.getDataCaptura());
        }

        String unidade = spec.getUnidade() != null ? spec.getUnidade() : atributo.getUnidade();
        return new ItemFichaResposta(
                termo,
                atributo.getCodigoCanonico(),
                atributo.getNomeExibicao(),
                atributo.getCategoria().name(),
                formatarValor(spec, atributo.getTipoDado(), unidade),
                unidade,
                StatusItemFicha.PRESENTE,
                spec.getConfianca(),
                spec.getFonte() != null ? spec.getFonte().getNome() : null,
                spec.getDataCaptura());
    }

    private String formatarValor(Especificacao spec, TipoDado tipo, String unidade) {
        return switch (tipo) {
            case NUMERICO -> formatarNumero(spec.getValorNumero(), unidade);
            case TEXTO    -> spec.getValorTexto();
            case BOOLEANO -> {
                if (spec.getValorBooleano() == null) yield null;
                yield spec.getValorBooleano() ? "Sim" : "Nao";
            }
        };
    }

    private String formatarNumero(BigDecimal valor, String unidade) {
        if (valor == null) return null;
        String numero = valor.stripTrailingZeros().toPlainString();
        return (unidade != null && !unidade.isBlank()) ? numero + " " + unidade : numero;
    }

    private ResumoFichaResposta montarResumo(List<ItemFichaResposta> itens) {
        int p = 0, ni = 0, nd = 0, dk = 0;
        for (ItemFichaResposta item : itens) {
            switch (item.status()) {
                case PRESENTE -> p++;
                case NAO_INFORMADO -> ni++;
                case NAO_DISPONIVEL -> nd++;
                case ATRIBUTO_DESCONHECIDO -> dk++;
            }
        }
        return new ResumoFichaResposta(p, ni, nd, dk, itens.size());
    }
}
