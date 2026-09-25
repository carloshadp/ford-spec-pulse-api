package com.ford.specpulse.autenticacao;

import com.ford.specpulse.suporte.TesteIntegracaoBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Autorizacao: rotas publicas e permissoes por perfil")
class AutorizacaoPerfisTest extends TesteIntegracaoBase {

    @ParameterizedTest(name = "{0} le o catalogo")
    @ValueSource(strings = {"leitor", "analista", "gerente", "validador", "admin"})
    @DisplayName("todos os perfis autenticados leem o catalogo")
    void todosLeem(String usuario) throws Exception {
        mvc.perform(get("/api/veiculos").header("Authorization", bearer(usuario)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest(name = "{0} cria comparacao -> {1}")
    @CsvSource({"leitor,403", "validador,403", "analista,201", "gerente,201", "admin,201"})
    @DisplayName("somente analista, gerente e admin criam comparacoes")
    void criarComparacao(String usuario, int esperado) throws Exception {
        mvc.perform(post("/api/comparacoes")
                        .header("Authorization", bearer(usuario))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paraJson(Map.of(
                                "referenceVersionId", VERSAO_RAPTOR,
                                "competitorVersionIds", List.of(VERSAO_HILUX)))))
                .andExpect(status().is(esperado));
    }

    @ParameterizedTest(name = "{0} lista usuarios -> {1}")
    @CsvSource({"leitor,403", "analista,403", "gerente,403", "validador,403", "admin,200"})
    @DisplayName("somente admin lista usuarios")
    void listarUsuarios(String usuario, int esperado) throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", bearer(usuario)))
                .andExpect(status().is(esperado));
    }

    @ParameterizedTest(name = "{0} le auditoria -> {1}")
    @CsvSource({"gerente,403", "validador,403", "admin,200"})
    @DisplayName("somente admin le o registro de auditoria")
    void lerAuditoria(String usuario, int esperado) throws Exception {
        mvc.perform(get("/api/admin/registro-auditoria").header("Authorization", bearer(usuario)))
                .andExpect(status().is(esperado));
    }

    @ParameterizedTest(name = "{0} acessa qualidade de dados -> {1}")
    @CsvSource({"leitor,403", "analista,403", "validador,200", "admin,200"})
    @DisplayName("somente validador e admin acessam a fila de qualidade de dados")
    void qualidadeDados(String usuario, int esperado) throws Exception {
        mvc.perform(get("/api/qualidade-dados/itens").header("Authorization", bearer(usuario)))
                .andExpect(status().is(esperado));
    }

    @Test
    @DisplayName("acesso negado devolve 403 no formato de erro padrao")
    void corpoDoForbidden() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", bearer("leitor")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.path").value("/api/usuarios"));
    }

    @Test
    @DisplayName("documentacao OpenAPI e health check sao publicos")
    void rotasPublicas() throws Exception {
        mvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
