package com.ford.specpulse.especificacao.api;

import com.ford.specpulse.especificacao.api.dto.EspecificacaoResposta;
import com.ford.specpulse.especificacao.dominio.EspecificacaoServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Especificacoes", description = "Valores tecnicos por versao com evidencia e confianca.")
@RestController
@RequestMapping("/api/v1")
public class EspecificacaoControlador {

    private final EspecificacaoServico servico;

    public EspecificacaoControlador(EspecificacaoServico servico) {
        this.servico = servico;
    }

    @Operation(summary = "Lista as especificacoes de uma versao.")
    @GetMapping("/versoes/{versaoId}/especificacoes")
    public List<EspecificacaoResposta> listarPorVersao(@PathVariable UUID versaoId) {
        return servico.listarPorVersao(versaoId).stream().map(EspecificacaoResposta::de).toList();
    }

    @Operation(summary = "Busca uma especificacao pelo identificador.")
    @GetMapping("/especificacoes/{id}")
    public EspecificacaoResposta buscarPorId(@PathVariable UUID id) {
        return EspecificacaoResposta.de(servico.buscarPorId(id));
    }
}
