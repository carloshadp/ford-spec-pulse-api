package com.ford.specpulse.facade.api;

import com.ford.specpulse.comparacao.dominio.Comparacao;
import com.ford.specpulse.comparacao.dominio.ComparacaoServico;
import com.ford.specpulse.comparacao.dominio.PerfilCliente;
import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.compartilhado.SlugUtil;
import com.ford.specpulse.facade.api.dto.ComparisonCreateRequestDto;
import com.ford.specpulse.facade.api.dto.ComparisonResultDto;
import com.ford.specpulse.versao.dominio.Versao;
import com.ford.specpulse.versao.dominio.VersaoServico;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Comparacoes", description = "Análises comparativas Ford vs concorrentes.")
@RestController
@RequestMapping("/api/comparacoes")
@SecurityRequirement(name = "bearerAuth")
@Transactional(readOnly = true)
public class ComparisonFacadeController {

    private final ComparacaoServico comparacaoServico;
    private final VersaoServico versaoServico;

    public ComparisonFacadeController(ComparacaoServico comparacaoServico,
                                      VersaoServico versaoServico) {
        this.comparacaoServico = comparacaoServico;
        this.versaoServico = versaoServico;
    }

    @Operation(summary = "Create a Ford vs competitor comparison.")
    @PostMapping
    @Transactional
    public ResponseEntity<ComparisonResultDto> criar(
            @Valid @RequestBody ComparisonCreateRequestDto req,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpReq) {

        Versao versaoFord = resolverVersao(req.referenceVersionId());
        List<UUID> concorrentesIds = req.competitorVersionIds().stream()
                .map(this::resolverVersao)
                .map(Versao::getId)
                .toList();

        String titulo = req.title() != null ? req.title()
                : "Comparação " + versaoFord.getNome();
        String descricao = req.description();
        String criadoPor = jwt != null ? jwt.getSubject() : "anonimo";
        PerfilCliente perfil = mapearPerfilCliente(req.customerProfileId());

        Comparacao criada = comparacaoServico.criar(
                titulo, descricao, versaoFord.getId(), concorrentesIds, perfil, criadoPor);

        return ResponseEntity.status(HttpStatus.CREATED).body(ComparisonResultDto.de(criada));
    }

    @Operation(summary = "Get a comparison result by ID.")
    @GetMapping("/{id}")
    public ComparisonResultDto buscar(@PathVariable String id) {
        return ComparisonResultDto.de(comparacaoServico.buscarPorId(UUID.fromString(id)));
    }

    @Operation(summary = "Obtém a matriz comparativa com filtros opcionais.")
    @GetMapping("/{id}/matriz")
    public RespostaLista<ComparisonResultDto.MatrixRowDto> matriz(
            @PathVariable String id,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String confidenceLevel,
            @RequestParam(required = false) String difference,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {

        Comparacao comp = comparacaoServico.buscarPorId(UUID.fromString(id));
        ComparisonResultDto dto = ComparisonResultDto.de(comp);

        List<ComparisonResultDto.MatrixRowDto> linhas = dto.rows().stream()
                .filter(r -> category == null || r.category().equals(category))
                .filter(r -> q == null || r.canonicalName().toLowerCase().contains(q.toLowerCase()))
                .filter(r -> difference == null || r.cells().stream()
                        .anyMatch(c -> c.difference().equals(difference)))
                .toList();
        return RespostaLista.paginada(linhas, page, pageSize);
    }

    @Operation(summary = "List all saved comparisons.")
    @GetMapping
    public RespostaLista<ComparisonResultDto> listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        List<ComparisonResultDto> todos = comparacaoServico.listarTodas().stream()
                .map(ComparisonResultDto::de)
                .toList();
        return RespostaLista.paginada(todos, page, pageSize);
    }

    private Versao resolverVersao(String id) {
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

    private PerfilCliente mapearPerfilCliente(String profileId) {
        if (profileId == null) return PerfilCliente.PREMIUM_URBANO;
        return switch (profileId.toLowerCase()) {
            case "offroad", "off_road" -> PerfilCliente.OFFROAD;
            case "fleet", "frotista" -> PerfilCliente.FROTISTA;
            case "heavy_work", "trabalho_pesado" -> PerfilCliente.TRABALHO_PESADO;
            case "tech_safety", "tecnologia_seguranca" -> PerfilCliente.TECNOLOGIA_SEGURANCA;
            default -> PerfilCliente.PREMIUM_URBANO;
        };
    }
}
