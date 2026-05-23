package com.ford.specpulse.especificacao.api;

import com.ford.specpulse.especificacao.api.dto.FonteResposta;
import com.ford.specpulse.especificacao.dominio.FonteServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Fontes", description = "Fontes de evidencia para as especificacoes.")
@RestController
@RequestMapping("/api/v1/fontes")
public class FonteControlador {

    private final FonteServico servico;

    public FonteControlador(FonteServico servico) {
        this.servico = servico;
    }

    @Operation(summary = "Lista todas as fontes cadastradas.")
    @GetMapping
    public List<FonteResposta> listar() {
        return servico.listarTodas().stream().map(FonteResposta::de).toList();
    }

    @Operation(summary = "Busca uma fonte pelo identificador.")
    @GetMapping("/{id}")
    public FonteResposta buscarPorId(@PathVariable UUID id) {
        return FonteResposta.de(servico.buscarPorId(id));
    }
}
