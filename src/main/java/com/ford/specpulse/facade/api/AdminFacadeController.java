package com.ford.specpulse.facade.api;

import com.ford.specpulse.auditoria.persistencia.AuditoriaRepositorio;
import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.facade.api.dto.AuditLogEntryDto;
import com.ford.specpulse.facade.api.dto.BackupRespostaDto;
import com.ford.specpulse.service.BackupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@Tag(name = "Admin", description = "Admin-only operations: audit trail, diagnostics, backup.")
@RestController
@RequestMapping("/api/admin")
@SecurityRequirement(name = "bearerAuth")
public class AdminFacadeController {

    private final AuditoriaRepositorio auditoriaRepositorio;
    private final BackupService backupService;

    public AdminFacadeController(AuditoriaRepositorio auditoriaRepositorio, BackupService backupService) {
        this.auditoriaRepositorio = auditoriaRepositorio;
        this.backupService = backupService;
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

    @Operation(summary = "Executa um backup completo do banco agora (apenas admin). "
            + "Gera um .zip em specpulse.backup.diretorio; roda também agendado diariamente as 04:00.")
    @PostMapping("/backup")
    public ResponseEntity<BackupRespostaDto> backup() {
        String arquivo = backupService.executarBackup();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BackupRespostaDto(arquivo, OffsetDateTime.now()));
    }
}
