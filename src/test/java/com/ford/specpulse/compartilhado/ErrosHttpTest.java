package com.ford.specpulse.compartilhado;

import com.ford.specpulse.suporte.TesteIntegracaoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Erros HTTP: status coerentes e corpo padronizado")
class ErrosHttpTest extends TesteIntegracaoBase {

    @Test
    @DisplayName("rota inexistente devolve 404 padronizado")
    void rotaInexistente() throws Exception {
        mvc.perform(get("/api/rota-que-nao-existe").header("Authorization", bearer("leitor")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.path").value("/api/rota-que-nao-existe"))
                .andExpect(jsonPath("$.requestId", startsWith("req_")));
    }

    @Test
    @DisplayName("metodo nao suportado devolve 405 com header Allow")
    void metodoNaoSuportado() throws Exception {
        mvc.perform(delete("/api/marcas").header("Authorization", bearer("admin")))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string("Allow", containsString("GET")))
                .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
    }

    @Test
    @DisplayName("parametro com tipo invalido devolve 400 apontando o parametro")
    void parametroComTipoInvalido() throws Exception {
        mvc.perform(get("/api/marcas").param("page", "abc").header("Authorization", bearer("leitor")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details[0].field").value("page"));
    }

    @Test
    @DisplayName("UUID invalido no path devolve 400")
    void uuidInvalidoNoPath() throws Exception {
        mvc.perform(patch("/api/usuarios/nao-e-uuid")
                        .header("Authorization", bearer("admin"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\": true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details[0].field").value("id"));
    }

    @Test
    @DisplayName("JSON malformado devolve 400")
    void jsonMalformado() throws Exception {
        mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer("analista"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ isto nao e json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("corpo ausente devolve 400")
    void corpoAusente() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Content-Type nao suportado devolve 415")
    void contentTypeNaoSuportado() throws Exception {
        mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer("analista"))
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("texto"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }

    @Test
    @DisplayName("ID de comparacao que nao e UUID devolve 404, tambem na matriz")
    void idDeComparacaoInvalido() throws Exception {
        mvc.perform(get("/api/comparacoes/abc").header("Authorization", bearer("leitor")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));

        mvc.perform(get("/api/comparacoes/abc/matriz").header("Authorization", bearer("leitor")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("requestId do corpo de erro e o mesmo do header X-Request-Id, inclusive no 401")
    void requestIdNoHeaderENoCorpo() throws Exception {
        MvcResult resultado = mvc.perform(get("/api/marcas"))
                .andExpect(status().isUnauthorized())
                .andReturn();

        String doHeader = resultado.getResponse().getHeader("X-Request-Id");
        String doCorpo = json.readTree(resultado.getResponse().getContentAsString()).get("requestId").asText();
        assertThat(doHeader).startsWith("req_").isEqualTo(doCorpo);
    }
}
