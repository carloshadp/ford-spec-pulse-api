package com.ford.specpulse.soap;

import com.ford.specpulse.soap.dto.SoapDTOs;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * WebService SOAP — Ford SpecPulse Competitive Intelligence API.
 *
 * Expoe as principais operacoes da API REST via protocolo SOAP/WSDL,
 * reutilizando a mesma camada de dados (JdbcTemplate / JPA).
 *
 * Endpoint: http://localhost:8080/soap/spec-pulse
 * WSDL:     http://localhost:8080/soap/spec-pulse?wsdl
 *
 * Operacoes:
 *   1. consultarFichaTecnica  — resolve atributos por texto livre
 *   2. listarMarcas           — lista todas as marcas cadastradas
 *   3. buscarVersaoPorSlug    — retorna versao e suas specs por slug
 *   4. criarComparacao        — cria snapshot comparativo imutavel
 */
@Slf4j
@Component
@WebService(
        serviceName  = "SpecPulseSoapService",
        portName     = "SpecPulseSoapPort",
        targetNamespace = "http://soap.specpulse.ford.com/"
)
@RequiredArgsConstructor
public class SpecPulseSoapService {

    private final JdbcTemplate jdbc;

    /**
     * Operacao 1 — Consulta ficha tecnica padronizada.
     *
     * Recebe marca + modelo + versao + lista livre de atributos (sinonimos aceitos)
     * e devolve uma ficha tecnica padronizada com os valores encontrados.
     * Atributos nao encontrados retornam status NAO_INFORMADO.
     * Atributos com sinonimo invalido retornam ATRIBUTO_DESCONHECIDO.
     */
    @WebMethod(operationName = "consultarFichaTecnica")
    public SoapDTOs.FichaTecnicaResponse consultarFichaTecnica(
            @WebParam(name = "request") SoapDTOs.ConsultaFichaTecnicaRequest request
    ) {
        log.info("[SOAP] consultarFichaTecnica: {} {} {}",
                request.getMarca(), request.getModelo(), request.getVersao());

        SoapDTOs.FichaTecnicaResponse response = new SoapDTOs.FichaTecnicaResponse();
        response.setMarca(request.getMarca());
        response.setModelo(request.getModelo());
        response.setVersao(request.getVersao());

        List<SoapDTOs.ItemFichaTecnica> itens = new ArrayList<>();

        for (String termo : request.getAtributos()) {
            String termoLower = termo.toLowerCase().trim();

            String sql = """
                SELECT ad.nome, e.valor, ad.unidade
                FROM especificacoes e
                JOIN atributos_definicao ad ON ad.id = e.atributo_id
                JOIN versoes v ON v.id = e.versao_id
                JOIN veiculos vc ON vc.id = v.veiculo_id
                JOIN marcas m ON m.id = vc.marca_id
                WHERE LOWER(m.nome) = LOWER(?)
                  AND LOWER(vc.nome) LIKE LOWER(?)
                  AND (
                      LOWER(ad.codigo) = ?
                      OR EXISTS (
                          SELECT 1 FROM atributo_sinonimos s
                          WHERE s.atributo_id = ad.id AND LOWER(s.sinonimo) = ?
                      )
                  )
                FETCH FIRST 1 ROWS ONLY
                """;

            try {
                List<Map<String, Object>> rows = jdbc.queryForList(
                        sql,
                        request.getMarca(),
                        "%" + request.getModelo() + "%",
                        termoLower, termoLower
                );

                if (!rows.isEmpty()) {
                    Map<String, Object> row = rows.get(0);
                    itens.add(new SoapDTOs.ItemFichaTecnica(
                            (String) row.get("NOME"),
                            row.get("VALOR") != null ? row.get("VALOR").toString() : null,
                            (String) row.get("UNIDADE"),
                            row.get("VALOR") != null ? "PUBLICADO" : "NAO_INFORMADO"
                    ));
                } else {
                    String checkAtributo = """
                        SELECT COUNT(*) FROM atributos_definicao ad
                        LEFT JOIN atributo_sinonimos s ON s.atributo_id = ad.id
                        WHERE LOWER(ad.codigo) = ? OR LOWER(s.sinonimo) = ?
                        """;
                    Integer count = jdbc.queryForObject(checkAtributo,
                            Integer.class, termoLower, termoLower);

                    String status = (count != null && count > 0) ? "NAO_INFORMADO" : "ATRIBUTO_DESCONHECIDO";
                    itens.add(new SoapDTOs.ItemFichaTecnica(termo, null, null, status));
                }
            } catch (Exception ex) {
                log.warn("[SOAP] Erro ao resolver atributo '{}': {}", termo, ex.getMessage());
                itens.add(new SoapDTOs.ItemFichaTecnica(termo, null, null, "ERRO"));
            }
        }

        response.setItens(itens);
        return response;
    }

    /**
     * Operacao 2 — Lista todas as marcas cadastradas.
     */
    @WebMethod(operationName = "listarMarcas")
    public List<SoapDTOs.MarcaResponse> listarMarcas(
            @WebParam(name = "apenasFord") Boolean apenasford
    ) {
        log.info("[SOAP] listarMarcas apenasford={}", apenasford);

        String sql = Boolean.TRUE.equals(apenasford)
                ? "SELECT id, nome, slug, is_ford FROM marcas WHERE is_ford = 1 ORDER BY nome"
                : "SELECT id, nome, slug, is_ford FROM marcas ORDER BY nome";

        return jdbc.query(sql, (rs, i) -> new SoapDTOs.MarcaResponse(
                rs.getString("ID"),
                rs.getString("NOME"),
                rs.getString("SLUG"),
                rs.getInt("IS_FORD") == 1
        ));
    }

    /**
     * Operacao 3 — Busca versao e suas especificacoes pelo slug.
     */
    @WebMethod(operationName = "buscarVersaoPorSlug")
    public SoapDTOs.FichaTecnicaResponse buscarVersaoPorSlug(
            @WebParam(name = "slug") String slug
    ) {
        log.info("[SOAP] buscarVersaoPorSlug: {}", slug);

        String sqlVersao = """
            SELECT v.nome as versao_nome, vc.nome as veiculo_nome, m.nome as marca_nome
            FROM versoes v
            JOIN veiculos vc ON vc.id = v.veiculo_id
            JOIN marcas m ON m.id = vc.marca_id
            WHERE v.slug = ?
            """;

        List<Map<String, Object>> versaoRows = jdbc.queryForList(sqlVersao, slug);
        if (versaoRows.isEmpty()) {
            SoapDTOs.FichaTecnicaResponse notFound = new SoapDTOs.FichaTecnicaResponse();
            notFound.setVersao("Versao nao encontrada: " + slug);
            notFound.setItens(new ArrayList<>());
            return notFound;
        }

        Map<String, Object> v = versaoRows.get(0);
        SoapDTOs.FichaTecnicaResponse response = new SoapDTOs.FichaTecnicaResponse();
        response.setMarca((String) v.get("MARCA_NOME"));
        response.setModelo((String) v.get("VEICULO_NOME"));
        response.setVersao((String) v.get("VERSAO_NOME"));

        String sqlSpecs = """
            SELECT ad.nome, e.valor, ad.unidade, ad.categoria
            FROM especificacoes e
            JOIN atributos_definicao ad ON ad.id = e.atributo_id
            JOIN versoes ver ON ver.id = e.versao_id
            WHERE ver.slug = ?
            ORDER BY ad.categoria, ad.nome
            """;

        List<SoapDTOs.ItemFichaTecnica> itens = jdbc.query(sqlSpecs, (rs, i) ->
                new SoapDTOs.ItemFichaTecnica(
                        rs.getString("NOME"),
                        rs.getString("VALOR"),
                        rs.getString("UNIDADE"),
                        "PUBLICADO"
                ), slug);

        response.setItens(itens);
        return response;
    }

    /**
     * Operacao 4 — Cria comparacao imutavel entre versoes.
     */
    @WebMethod(operationName = "criarComparacao")
    public SoapDTOs.ComparacaoResponse criarComparacao(
            @WebParam(name = "request") SoapDTOs.CriarComparacaoRequest request
    ) {
        log.info("[SOAP] criarComparacao: {} vs {} concorrentes",
                request.getReferenciaVersaoId(),
                request.getConcorrentesIds() != null ? request.getConcorrentesIds().size() : 0);

        try {
            String idComparacao = java.util.UUID.randomUUID().toString();

            String sql = """
                INSERT INTO comparacoes (id, titulo, referencia_versao_id, perfil_cliente)
                VALUES (?, ?, ?, ?)
                """;

            jdbc.update(sql,
                    idComparacao,
                    request.getTitulo(),
                    request.getReferenciaVersaoId(),
                    request.getPerfilCliente()
            );

            if (request.getConcorrentesIds() != null) {
                for (String concorrenteId : request.getConcorrentesIds()) {
                    jdbc.update(
                            "INSERT INTO comparacao_versoes (comparacao_id, versao_id) VALUES (?, ?)",
                            idComparacao, concorrenteId
                    );
                }
            }

            return new SoapDTOs.ComparacaoResponse(
                    idComparacao,
                    request.getTitulo(),
                    "CRIADA",
                    "Comparacao criada com sucesso. Acesse GET /api/comparacoes/" + idComparacao
            );

        } catch (Exception ex) {
            log.error("[SOAP] Erro ao criar comparacao: {}", ex.getMessage());
            return new SoapDTOs.ComparacaoResponse(
                    null, request.getTitulo(), "ERRO", ex.getMessage()
            );
        }
    }
}