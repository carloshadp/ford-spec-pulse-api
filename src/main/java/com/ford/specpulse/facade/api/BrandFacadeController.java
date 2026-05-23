package com.ford.specpulse.facade.api;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.facade.api.dto.BrandDto;
import com.ford.specpulse.veiculo.dominio.Marca;
import com.ford.specpulse.veiculo.dominio.MarcaServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Marcas", description = "Consulta e filtro de marcas de veículos.")
@RestController
@RequestMapping("/api/marcas")
@SecurityRequirement(name = "bearerAuth")
public class BrandFacadeController {

    private final MarcaServico servico;

    public BrandFacadeController(MarcaServico servico) {
        this.servico = servico;
    }

    @Operation(summary = "List all brands with optional filters.")
    @GetMapping
    public RespostaLista<BrandDto> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean isFord,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {

        List<BrandDto> todos = servico.listarTodas().stream()
                .filter(m -> q == null || m.getNome().toLowerCase().contains(q.toLowerCase()))
                .filter(m -> isFord == null || "Ford".equalsIgnoreCase(m.getNome()) == isFord)
                .map(BrandDto::de)
                .toList();
        return RespostaLista.paginada(todos, page, pageSize);
    }

    @Operation(summary = "Get a brand by ID or slug.")
    @GetMapping("/{id}")
    public BrandDto buscar(@PathVariable String id) {
        return BrandDto.de(resolver(id));
    }

    private Marca resolver(String id) {
        try {
            return servico.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return servico.listarTodas().stream()
                    .filter(m -> SlugUtil.slugMarca(m.getNome()).equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Marca não encontrada: " + id));
        }
    }
}
