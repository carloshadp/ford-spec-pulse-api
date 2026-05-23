package com.ford.specpulse.comparacao.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.comparacao.persistencia.ComparacaoRepositorio;
import com.ford.specpulse.especificacao.dominio.AtributoDefinicao;
import com.ford.specpulse.especificacao.dominio.DirecaoMelhor;
import com.ford.specpulse.especificacao.dominio.Especificacao;
import com.ford.specpulse.especificacao.dominio.NivelConfianca;
import com.ford.specpulse.especificacao.dominio.StatusEspecificacao;
import com.ford.specpulse.especificacao.dominio.TipoDado;
import com.ford.specpulse.especificacao.persistencia.EspecificacaoRepositorio;
import com.ford.specpulse.versao.dominio.Versao;
import com.ford.specpulse.versao.dominio.VersaoServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ComparacaoServico {

    private static final String NOME_MARCA_FORD = "Ford";
    private static final int MIN_CONCORRENTES = 1;
    private static final int MAX_CONCORRENTES = 3;

    private final ComparacaoRepositorio repositorio;
    private final VersaoServico versaoServico;
    private final EspecificacaoRepositorio especificacaoRepositorio;

    public ComparacaoServico(ComparacaoRepositorio repositorio,
                             VersaoServico versaoServico,
                             EspecificacaoRepositorio especificacaoRepositorio) {
        this.repositorio = repositorio;
        this.versaoServico = versaoServico;
        this.especificacaoRepositorio = especificacaoRepositorio;
    }

    @Transactional(readOnly = true)
    public List<Comparacao> listarTodas() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Comparacao buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Comparacao", id));
    }

    @Transactional
    public Comparacao criar(String titulo, String descricao, UUID versaoFordId,
                            List<UUID> versoesConcorrentesIds, PerfilCliente perfilCliente,
                            String criadoPor) {

        validarConcorrentes(versoesConcorrentesIds, versaoFordId);

        Versao versaoFord = versaoServico.buscarPorId(versaoFordId);
        validarVersaoFord(versaoFord);

        List<Versao> concorrentes = versoesConcorrentesIds.stream()
                .map(versaoServico::buscarPorId)
                .toList();

        Comparacao comparacao = new Comparacao(titulo, descricao, versaoFord, perfilCliente, criadoPor);
        for (int i = 0; i < concorrentes.size(); i++) {
            comparacao.adicionarConcorrente(concorrentes.get(i), i);
        }

        Map<UUID, List<Especificacao>> specsPorVersao = carregarSpecsPorVersao(versaoFord, concorrentes);
        Set<AtributoDefinicao> atributos = coletarAtributosComuns(specsPorVersao);

        gerarCelulas(comparacao, versaoFord, concorrentes, specsPorVersao, atributos);

        return repositorio.save(comparacao);
    }


    private void validarConcorrentes(List<UUID> versoesConcorrentesIds, UUID versaoFordId) {
        if (versoesConcorrentesIds == null || versoesConcorrentesIds.isEmpty()) {
            throw new RegraNegocioException("Informe ao menos um concorrente.");
        }
        if (versoesConcorrentesIds.size() < MIN_CONCORRENTES || versoesConcorrentesIds.size() > MAX_CONCORRENTES) {
            throw new RegraNegocioException(
                    "A comparacao aceita entre " + MIN_CONCORRENTES + " e " + MAX_CONCORRENTES + " concorrentes.");
        }
        Set<UUID> sem = new HashSet<>(versoesConcorrentesIds);
        if (sem.size() != versoesConcorrentesIds.size()) {
            throw new RegraNegocioException("Concorrentes duplicados na lista.");
        }
        if (sem.contains(versaoFordId)) {
            throw new RegraNegocioException("A versao Ford de referencia nao pode constar entre os concorrentes.");
        }
    }

    private void validarVersaoFord(Versao versaoFord) {
        String marca = versaoFord.getVeiculo().getMarca().getNome();
        if (!NOME_MARCA_FORD.equalsIgnoreCase(marca)) {
            throw new RegraNegocioException(
                    "A versao de referencia deve ser de uma marca Ford, recebida: " + marca);
        }
    }


    private Map<UUID, List<Especificacao>> carregarSpecsPorVersao(Versao versaoFord, List<Versao> concorrentes) {
        Map<UUID, List<Especificacao>> mapa = new java.util.HashMap<>();
        mapa.put(versaoFord.getId(), especificacaoRepositorio.findByVersaoId(versaoFord.getId()));
        for (Versao c : concorrentes) {
            mapa.put(c.getId(), especificacaoRepositorio.findByVersaoId(c.getId()));
        }
        return mapa;
    }

    private Set<AtributoDefinicao> coletarAtributosComuns(Map<UUID, List<Especificacao>> specsPorVersao) {
        return specsPorVersao.values().stream()
                .flatMap(List::stream)
                .map(Especificacao::getAtributo)
                .collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }


    private void gerarCelulas(Comparacao comparacao, Versao versaoFord, List<Versao> concorrentes,
                              Map<UUID, List<Especificacao>> specsPorVersao,
                              Set<AtributoDefinicao> atributos) {

        for (AtributoDefinicao atributo : atributos) {
            Especificacao specFord = localizar(specsPorVersao.get(versaoFord.getId()), atributo);

            comparacao.adicionarCelula(montarCelula(comparacao, versaoFord, atributo, specFord,
                    StatusCelula.REFERENCIA));

            for (Versao c : concorrentes) {
                Especificacao specConc = localizar(specsPorVersao.get(c.getId()), atributo);
                StatusCelula status = calcularStatus(atributo, specFord, specConc);
                comparacao.adicionarCelula(montarCelula(comparacao, c, atributo, specConc, status));
            }
        }
    }

    private Especificacao localizar(List<Especificacao> specs, AtributoDefinicao atributo) {
        if (specs == null) return null;
        return specs.stream()
                .filter(s -> s.getAtributo().getId().equals(atributo.getId()))
                .findFirst()
                .orElse(null);
    }

    private ComparacaoCelula montarCelula(Comparacao comparacao, Versao versao,
                                          AtributoDefinicao atributo, Especificacao spec,
                                          StatusCelula statusCelula) {
        if (spec == null) {
            return new ComparacaoCelula(comparacao, versao, atributo,
                    null, null, null, atributo.getUnidade(),
                    NivelConfianca.BAIXA,
                    statusCelula == StatusCelula.REFERENCIA ? StatusCelula.REFERENCIA : StatusCelula.SEM_DADO,
                    StatusEspecificacao.NAO_INFORMADO,
                    null);
        }
        StatusCelula statusFinal = statusCelula;
        if (spec.getStatus() == StatusEspecificacao.NAO_INFORMADO && statusCelula != StatusCelula.REFERENCIA) {
            statusFinal = StatusCelula.SEM_DADO;
        }
        return new ComparacaoCelula(
                comparacao,
                versao,
                atributo,
                spec.getValorTexto(),
                spec.getValorNumero(),
                spec.getValorBooleano(),
                spec.getUnidade() != null ? spec.getUnidade() : atributo.getUnidade(),
                spec.getConfianca(),
                statusFinal,
                spec.getStatus(),
                spec.getFonte() != null ? spec.getFonte().getNome() : null
        );
    }

    private StatusCelula calcularStatus(AtributoDefinicao atributo,
                                        Especificacao specFord, Especificacao specConc) {
        if (specFord == null || specConc == null) {
            return StatusCelula.SEM_DADO;
        }
        if (specFord.getStatus() != StatusEspecificacao.CONFIRMADO
                || specConc.getStatus() != StatusEspecificacao.CONFIRMADO) {
            return StatusCelula.SEM_DADO;
        }
        return switch (atributo.getTipoDado()) {
            case NUMERICO -> compararNumerico(specFord.getValorNumero(),
                    specConc.getValorNumero(), atributo.getDirecaoMelhor());
            case BOOLEANO -> compararBooleano(specFord.getValorBooleano(), specConc.getValorBooleano());
            case TEXTO   -> StatusCelula.NAO_COMPARAVEL;
        };
    }

    private StatusCelula compararNumerico(BigDecimal ford, BigDecimal conc, DirecaoMelhor direcao) {
        if (ford == null || conc == null) return StatusCelula.SEM_DADO;
        int cmp = ford.compareTo(conc);
        if (cmp == 0) return StatusCelula.PARIDADE;
        return switch (direcao) {
            case MAIOR_MELHOR -> cmp > 0 ? StatusCelula.VANTAGEM : StatusCelula.RISCO;
            case MENOR_MELHOR -> cmp < 0 ? StatusCelula.VANTAGEM : StatusCelula.RISCO;
            case NAO_APLICA   -> StatusCelula.NAO_COMPARAVEL;
        };
    }

    private StatusCelula compararBooleano(Boolean ford, Boolean conc) {
        if (ford == null || conc == null) return StatusCelula.SEM_DADO;
        if (ford.equals(conc)) return StatusCelula.PARIDADE;
        return ford ? StatusCelula.VANTAGEM : StatusCelula.RISCO;
    }
}
