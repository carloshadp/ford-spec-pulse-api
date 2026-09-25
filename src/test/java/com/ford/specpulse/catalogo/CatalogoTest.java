package com.ford.specpulse.catalogo;

import com.ford.specpulse.suporte.TesteIntegracaoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Catalogo: marcas, veiculos, versoes, especificacoes e taxonomia")
class CatalogoTest extends TesteIntegracaoBase {

    @Test
    @DisplayName("listagem de marcas usa o envelope paginado do contrato")
    void envelopePaginado() throws Exception {
        mvc.perform(get("/api/marcas").param("pageSize", "3").header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.pageSize").value(3))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(3)));
    }

    @Test
    @DisplayName("marca pode ser buscada pelo ID estavel (slug)")
    void marcaPorSlug() throws Exception {
        mvc.perform(get("/api/marcas/brand-ford").header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("brand-ford"))
                .andExpect(jsonPath("$.name").value("Ford"))
                .andExpect(jsonPath("$.isFord").value(true));
    }

    @Test
    @DisplayName("filtro isFord=true devolve apenas veiculos Ford, incluindo a Ranger Raptor")
    void filtroVeiculosFord() throws Exception {
        mvc.perform(get("/api/veiculos").param("isFord", "true").header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].brandId", everyItem(is("brand-ford"))))
                .andExpect(jsonPath("$.data[*].id", hasItem("vehicle-ford-ranger-raptor-2024")));
    }

    @Test
    @DisplayName("veiculo inexistente devolve 404 no formato de erro padrao")
    void veiculoInexistente() throws Exception {
        mvc.perform(get("/api/veiculos/vehicle-nao-existe").header("Authorization", bearer("leitor")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/veiculos/vehicle-nao-existe"))
                .andExpect(jsonPath("$.requestId", startsWith("req_")));
    }

    @Test
    @DisplayName("versoes de um veiculo sao listadas pelo slug do veiculo")
    void versoesDoVeiculo() throws Exception {
        mvc.perform(get("/api/veiculos/vehicle-ford-ranger-raptor-2024/versoes")
                        .header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(VERSAO_RAPTOR))
                .andExpect(jsonPath("$.data[0].vehicleId").value("vehicle-ford-ranger-raptor-2024"));
    }

    @Test
    @DisplayName("especificacoes trazem status, confianca de 0 a 1, nivel de confianca e evidencias")
    void especificacoesRastreaveis() throws Exception {
        mvc.perform(get("/api/versoes/" + VERSAO_RAPTOR + "/especificacoes")
                        .header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThan(0)))
                .andExpect(jsonPath("$.data[*].versionId", everyItem(is(VERSAO_RAPTOR))))
                .andExpect(jsonPath("$.data[*].status").exists())
                .andExpect(jsonPath("$.data[*].confidence", everyItem(greaterThanOrEqualTo(0.0))))
                .andExpect(jsonPath("$.data[*].confidence", everyItem(lessThanOrEqualTo(1.0))))
                .andExpect(jsonPath("$.data[*].confidenceLevel").exists())
                .andExpect(jsonPath("$.data[0].evidenceIds").isArray());
    }

    @Test
    @DisplayName("taxonomia encontra atributo canonico pela busca textual")
    void buscaNaTaxonomia() throws Exception {
        mvc.perform(get("/api/atributos/taxonomia").param("q", "potencia")
                        .header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].id", hasItem("attr_potencia_cv")));
    }
}
