package com.ford.specpulse.facade.api;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.facade.api.dto.VehicleDto;
import com.ford.specpulse.facade.api.dto.VehicleVersionDto;
import com.ford.specpulse.veiculo.dominio.Veiculo;
import com.ford.specpulse.veiculo.dominio.VeiculoServico;
import com.ford.specpulse.versao.dominio.VersaoServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Veiculos", description = "Catálogo de modelos e versões de veículos.")
@RestController
@RequestMapping("/api/veiculos")
@SecurityRequirement(name = "bearerAuth")
@Transactional(readOnly = true)
public class VehicleFacadeController {

    private final VeiculoServico veiculoServico;
    private final VersaoServico versaoServico;

    public VehicleFacadeController(VeiculoServico veiculoServico, VersaoServico versaoServico) {
        this.veiculoServico = veiculoServico;
        this.versaoServico = versaoServico;
    }

    @Operation(summary = "List vehicles with optional filters.")
    @GetMapping
    public RespostaLista<VehicleDto> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) Boolean isFord,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String market,
            @RequestParam(required = false) Integer year,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {

        List<VehicleDto> todos = veiculoServico.listarTodos().stream()
                .filter(v -> q == null || v.getModelo().toLowerCase().contains(q.toLowerCase()))
                .filter(v -> brandId == null || SlugUtil.slugMarca(v.getMarca().getNome()).equals(brandId)
                        || v.getMarca().getId().toString().equals(brandId))
                .filter(v -> isFord == null || "Ford".equalsIgnoreCase(v.getMarca().getNome()) == isFord)
                .filter(v -> segment == null || VehicleDto.de(v).segment().equals(segment))
                .filter(v -> market == null || v.getMercado().name().equalsIgnoreCase(market))
                .filter(v -> year == null || v.getAnoModelo().equals(year))
                .map(VehicleDto::de)
                .toList();
        return RespostaLista.paginada(todos, page, pageSize);
    }

    @Operation(summary = "Get a vehicle by ID or slug.")
    @GetMapping("/{id}")
    public VehicleDto buscar(@PathVariable String id) {
        return VehicleDto.de(resolver(id));
    }

    @Operation(summary = "Lista as versões (trims) de um veículo.")
    @GetMapping("/{id}/versoes")
    public RespostaLista<VehicleVersionDto> listarVersoes(
            @PathVariable String id,
            @RequestParam(required = false) String powertrain,
            @RequestParam(required = false) String drivetrain,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {

        Veiculo veiculo = resolver(id);
        List<VehicleVersionDto> versoes = versaoServico.listarPorVeiculo(veiculo.getId()).stream()
                .filter(v -> powertrain == null || VehicleVersionDto.de(v).powertrain().equals(powertrain))
                .filter(v -> drivetrain == null || VehicleVersionDto.de(v).drivetrain().equals(drivetrain))
                .map(VehicleVersionDto::de)
                .toList();
        return RespostaLista.paginada(versoes, page, pageSize);
    }

    private Veiculo resolver(String id) {
        try {
            return veiculoServico.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return veiculoServico.listarTodos().stream()
                    .filter(v -> SlugUtil.slugVeiculo(
                            v.getMarca().getNome(), v.getModelo(), v.getAnoModelo()).equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Veículo não encontrado: " + id));
        }
    }
}
