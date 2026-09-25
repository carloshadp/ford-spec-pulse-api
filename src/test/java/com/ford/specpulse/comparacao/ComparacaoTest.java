package com.ford.specpulse.comparacao;

import com.fasterxml.jackson.databind.JsonNode;
import com.ford.specpulse.suporte.TesteIntegracaoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Comparacoes: criacao, consulta, matriz e validacoes")
class ComparacaoTest extends TesteIntegracaoBase {

    @Test
    @DisplayName("cria comparacao Ford vs 2 concorrentes e devolve 201 com versoes e linhas")
    void criarComparacao() throws Exception {
        mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer("analista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of(
                                "referenceVersionId", VERSAO_RAPTOR,
                                "competitorVersionIds", List.of(VERSAO_HILUX, VERSAO_S10),
                                "customerProfileId", "fleet"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("ready"))
                .andExpect(jsonPath("$.request.referenceVersionId").value(VERSAO_RAPTOR))
                .andExpect(jsonPath("$.comparedVersions", hasSize(3)))
                .andExpect(jsonPath("$.rows", not(empty())))
                .andExpect(jsonPath("$.summary").exists());
    }

    @Test
    @DisplayName("comparacao criada pode ser consultada pelo ID, inclusive a matriz filtrada")
    void consultarComparacaoEMatriz() throws Exception {
        String id = criar(VERSAO_HILUX).get("id").asText();

        mvc.perform(get("/api/comparacoes/" + id).header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        mvc.perform(get("/api/comparacoes/" + id + "/matriz")
                        .param("category", "engine_transmission")
                        .header("Authorization", bearer("leitor")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", not(empty())))
                .andExpect(jsonPath("$.data[*].category", everyItem(is("engine_transmission"))));
    }

    @Test
    @DisplayName("sem concorrentes devolve 400 apontando competitorVersionIds")
    void semConcorrentes() throws Exception {
        mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer("analista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of(
                                "referenceVersionId", VERSAO_RAPTOR,
                                "competitorVersionIds", List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("competitorVersionIds"));
    }

    @Test
    @DisplayName("mais de 3 concorrentes devolve 400 (limite do MVP)")
    void maisDeTresConcorrentes() throws Exception {
        mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer("analista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of(
                                "referenceVersionId", VERSAO_RAPTOR,
                                "competitorVersionIds", List.of(VERSAO_HILUX, VERSAO_S10, VERSAO_HILUX, VERSAO_S10)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("versao concorrente inexistente devolve 404")
    void versaoInexistente() throws Exception {
        mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer("analista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of(
                                "referenceVersionId", VERSAO_RAPTOR,
                                "competitorVersionIds", List.of("version-nao-existe")))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("comparacao inexistente devolve 404")
    void comparacaoInexistente() throws Exception {
        mvc.perform(get("/api/comparacoes/" + UUID.randomUUID()).header("Authorization", bearer("leitor")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    private JsonNode criar(String concorrente) throws Exception {
        String corpo = mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer("gerente"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of(
                                "referenceVersionId", VERSAO_RAPTOR,
                                "competitorVersionIds", List.of(concorrente)))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo);
    }
}
