package com.ford.specpulse.facade.api;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.especificacao.dominio.Especificacao;
import com.ford.specpulse.especificacao.dominio.EspecificacaoServico;
import com.ford.specpulse.facade.api.dto.SpecValueDto;
import com.ford.specpulse.facade.api.dto.TechnicalAttributeDto;
import com.ford.specpulse.facade.api.dto.VehicleVersionDto;
import com.ford.specpulse.versao.dominio.Versao;
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

@Tag(name = "Versoes", description = "Detalhes e especificações de versões (trims) de veículos.")
@RestController
@RequestMapping("/api/versoes")
@SecurityRequirement(name = "bearerAuth")
@Transactional(readOnly = true)
public class VersionFacadeController {

    private final VersaoServico versaoServico;
    private final EspecificacaoServico especificacaoServico;

    public VersionFacadeController(VersaoServico versaoServico, EspecificacaoServico especificacaoServico) {
        this.versaoServico = versaoServico;
        this.especificacaoServico = especificacaoServico;
    }

    @Operation(summary = "Get a version by ID or slug.")
    @GetMapping("/{id}")
    public VehicleVersionDto buscar(@PathVariable String id) {
        return VehicleVersionDto.de(resolver(id));
    }

    @Operation(summary = "Lista as especificações técnicas de uma versão.")
    @GetMapping("/{id}/especificacoes")
    public RespostaLista<SpecValueDto> especificacoes(
            @PathVariable String id,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String confidenceLevel,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {

        Versao versao = resolver(id);
        List<Especificacao> especificacoes = especificacaoServico.listarPorVersao(versao.getId());

        List<SpecValueDto> specs = especificacoes.stream()
                .filter(e -> category == null ||
                        TechnicalAttributeDto.categoriaIngles(e.getAtributo().getCategoria()).equals(category))
                .map(SpecValueDto::de)
                .filter(s -> status == null || s.status().equals(status))
                .filter(s -> confidenceLevel == null || s.confidenceLevel().equals(confidenceLevel))
                .toList();
        return RespostaLista.paginada(specs, page, pageSize);
    }

    private Versao resolver(String id) {
        try {
            return versaoServico.buscarPorId(UUID.fromString(id));
        } catch (IllegalArgumentException e) {
            return versaoServico.listarTodas().stream()
                    .filter(v -> SlugUtil.slugVersao(
                            v.getVeiculo().getMarca().getNome(),
                            v.getVeiculo().getModelo(),
                            v.getVeiculo().getAnoModelo(),
                            v.getNome()).equals(id))
                    .findFirst()
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Versão não encontrada: " + id));
        }
    }
}
