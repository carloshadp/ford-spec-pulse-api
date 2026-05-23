package com.ford.specpulse.facade.api;

import com.ford.specpulse.auditoria.persistencia.AuditoriaRepositorio;
import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.facade.api.dto.AuditLogEntryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin", description = "Admin-only operations: audit trail, diagnostics.")
@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminFacadeController {

    private final AuditoriaRepositorio auditoriaRepositorio;

    public AdminFacadeController(AuditoriaRepositorio auditoriaRepositorio) {
        this.auditoriaRepositorio = auditoriaRepositorio;
    }

    @Operation(summary = "Trilha de auditoria completa (apenas admin). Ordenado do mais recente.")
    @GetMapping("/registro-auditoria")
    public RespostaLista<AuditLogEntryDto> auditLog(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        PageRequest pr = PageRequest.of(page - 1, pageSize, Sort.by("ocorridoEm").descending());
        var resultado = auditoriaRepositorio.findAll(pr);
        List<AuditLogEntryDto> conteudo = resultado.getContent().stream()
                .map(AuditLogEntryDto::de)
                .toList();
        return new RespostaLista<>(conteudo, page, pageSize, (int) resultado.getTotalElements());
    }
}
