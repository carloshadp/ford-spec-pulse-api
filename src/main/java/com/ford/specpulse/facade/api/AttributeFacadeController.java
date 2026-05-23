package com.ford.specpulse.facade.api;

import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.especificacao.dominio.AtributoDefinicaoServico;
import com.ford.specpulse.facade.api.dto.TechnicalAttributeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Tag(name = "Atributos", description = "Taxonomia de atributos técnicos de veículos.")
@RestController
@RequestMapping("/api/atributos")
@SecurityRequirement(name = "bearerAuth")
public class AttributeFacadeController {

    private final AtributoDefinicaoServico servico;

    public AttributeFacadeController(AtributoDefinicaoServico servico) {
        this.servico = servico;
    }

    @Operation(summary = "List the attribute taxonomy with optional filters.")
    @GetMapping("/taxonomia")
    public RespostaLista<TechnicalAttributeDto> taxonomia(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {

        Map<UUID, List<String>> sinonimos = servico.sinonimosPorAtributo();
        List<TechnicalAttributeDto> todos = servico.listarTodos().stream()
                .filter(a -> q == null ||
                        a.getNomeExibicao().toLowerCase().contains(q.toLowerCase()) ||
                        a.getCodigoCanonico().toLowerCase().contains(q.toLowerCase()))
                .filter(a -> category == null ||
                        TechnicalAttributeDto.categoriaIngles(a.getCategoria()).equals(category))
                .map(a -> TechnicalAttributeDto.de(a, sinonimos.getOrDefault(a.getId(), List.of())))
                .toList();
        return RespostaLista.paginada(todos, page, pageSize);
    }
}
