package com.ford.specpulse.facade.api;

import com.ford.specpulse.compartilhado.RespostaLista;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Stubs para endpoints esperados pelo frontend que ainda não têm implementação completa.
 * Retornam listas vazias com envelope correto para não quebrar a integração.
 */
@Tag(name = "Stubs", description = "Placeholders retornando listas vazias — implementação futura.")
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
public class StubFacadeController {

    private static final RespostaLista<Map<String, Object>> VAZIO =
            new RespostaLista<>(List.of(), 1, 25, 0);

    @Operation(summary = "Lacunas competitivas (stub).")
    @GetMapping("/lacunas")
    public RespostaLista<Map<String, Object>> lacunas(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "25") int pageSize) {
        return VAZIO;
    }

    @Operation(summary = "Lacunas de uma comparação específica (stub).")
    @GetMapping("/comparacoes/{id}/lacunas")
    public RespostaLista<Map<String, Object>> comparacaoLacunas(@PathVariable String id) {
        return VAZIO;
    }

    @Operation(summary = "Recomendações (stub).")
    @GetMapping("/recomendacoes")
    public RespostaLista<Map<String, Object>> recomendacoes() {
        return VAZIO;
    }

    @Operation(summary = "Alertas de mercado (stub).")
    @GetMapping("/alertas-mercado")
    public RespostaLista<Map<String, Object>> alertasMercado() {
        return VAZIO;
    }

    @Operation(summary = "Fila de qualidade de dados (stub).")
    @GetMapping("/qualidade-dados/itens")
    public RespostaLista<Map<String, Object>> qualidadeDados() {
        return VAZIO;
    }

    @Operation(summary = "Documentos enviados (stub).")
    @GetMapping("/uploads")
    public RespostaLista<Map<String, Object>> uploads() {
        return VAZIO;
    }

    @Operation(summary = "Perfis de clientes (stub).")
    @GetMapping("/perfis-clientes")
    public RespostaLista<Map<String, Object>> perfisClientes() {
        return VAZIO;
    }

    @Operation(summary = "Histórico de análises (stub).")
    @GetMapping("/historico-analises")
    public RespostaLista<Map<String, Object>> historicoAnalises() {
        return VAZIO;
    }

    @Operation(summary = "Fontes de evidência de uma versão (stub).")
    @GetMapping("/versoes/{id}/fontes")
    public RespostaLista<Map<String, Object>> versaoFontes(@PathVariable String id) {
        return VAZIO;
    }

    @Operation(summary = "Exportar relatório (stub).")
    @PostMapping("/relatorios/exportar")
    public Map<String, Object> exportarRelatorio() {
        return Map.of("status", "enfileirado", "id", "relatorio-pendente");
    }

    @Operation(summary = "Status de um relatório (stub).")
    @GetMapping("/relatorios/{id}")
    public Map<String, Object> statusRelatorio(@PathVariable String id) {
        return Map.of("id", id, "status", "processando");
    }
}
