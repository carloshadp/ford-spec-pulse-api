package com.ford.specpulse.ficha;

import com.fasterxml.jackson.databind.JsonNode;
import com.ford.specpulse.suporte.TesteIntegracaoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Ficha tecnica: consulta por texto livre com sinonimos")
class FichaTecnicaTest extends TesteIntegracaoBase {

    private static final Map<String, Object> RAPTOR = Map.of(
            "marca", "Ford",
            "modelo", "Ranger Raptor",
            "versao", "Raptor 3.0 V6 Biturbo Gasolina 4x4 Cabine Dupla");

    @Test
    @DisplayName("sinonimos resolvem para o mesmo atributo canonico e termo desconhecido e sinalizado")
    void sinonimosETermoDesconhecido() throws Exception {
        String corpo = mvc.perform(post("/api/fichas-tecnicas/consultar")
                        .header("Authorization", bearer("leitor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(consulta(List.of("potencia", "cavalos", "termo-que-nao-existe")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelo").value("Ranger Raptor"))
                .andExpect(jsonPath("$.itens[0].status").value("PRESENTE"))
                .andExpect(jsonPath("$.itens[2].status").value("ATRIBUTO_DESCONHECIDO"))
                .andExpect(jsonPath("$.resumo.total").value(3))
                .andExpect(jsonPath("$.resumo.desconhecidos").value(1))
                .andReturn().getResponse().getContentAsString();

        JsonNode itens = json.readTree(corpo).get("itens");
        assertThat(itens.get(0).get("codigoCanonico").asText())
                .isNotBlank()
                .isEqualTo(itens.get(1).get("codigoCanonico").asText());
    }

    @Test
    @DisplayName("versao inexistente devolve 404")
    void versaoInexistente() throws Exception {
        Map<String, Object> corpo = Map.of(
                "marca", "Ford", "modelo", "Ranger Raptor", "versao", "Versao Que Nao Existe",
                "atributos", List.of("potencia"));

        mvc.perform(post("/api/fichas-tecnicas/consultar")
                        .header("Authorization", bearer("leitor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(corpo)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("consulta sem atributos devolve 400")
    void semAtributos() throws Exception {
        mvc.perform(post("/api/fichas-tecnicas/consultar")
                        .header("Authorization", bearer("leitor"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(consulta(List.of()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("atributos"));
    }

    private static Map<String, Object> consulta(List<String> atributos) {
        Map<String, Object> corpo = new java.util.HashMap<>(RAPTOR);
        corpo.put("atributos", atributos);
        return corpo;
    }
}
