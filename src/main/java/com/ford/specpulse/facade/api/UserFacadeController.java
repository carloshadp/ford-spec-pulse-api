package com.ford.specpulse.facade.api;

import com.ford.specpulse.auditoria.dominio.AuditoriaServico;
import com.ford.specpulse.autenticacao.dominio.UsuarioServico;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.compartilhado.RespostaLista;
import com.ford.specpulse.facade.api.dto.UserMeDto;
import com.ford.specpulse.seguranca.Perfil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Usuarios", description = "Perfil e permissões do usuário autenticado.")
@RestController
@RequestMapping({"/api/usuarios", "/api/users"})
@SecurityRequirement(name = "bearerAuth")
public class UserFacadeController {

    private final UsuarioServico usuarioServico;
    private final AuditoriaServico auditoriaServico;

    public UserFacadeController(UsuarioServico usuarioServico,
                                AuditoriaServico auditoriaServico) {
        this.usuarioServico = usuarioServico;
        this.auditoriaServico = auditoriaServico;
    }

    @Operation(summary = "Load the authenticated user's profile and roles.")
    @GetMapping("/me")
    public UserMeDto me(@AuthenticationPrincipal Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        return UserMeDto.de(usuarioServico.buscarPorId(id));
    }

    @Operation(summary = "List users (admin only).")
    @GetMapping
    public RespostaLista<UserMeDto> listar(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "25") int pageSize) {
        List<UserMeDto> todos = usuarioServico.listarTodos().stream()
                .map(UserMeDto::de)
                .toList();
        return RespostaLista.paginada(todos, page, pageSize);
    }

    @Operation(summary = "Update user role or active status (admin only).")
    @PatchMapping("/{id}")
    public UserMeDto atualizar(@PathVariable UUID id,
                               @Valid @RequestBody PatchUserRequest req,
                               @AuthenticationPrincipal Jwt jwt,
                               HttpServletRequest httpReq) {
        Perfil novoPerfil = parsePerfil(req.role());
        var atualizado = usuarioServico.atualizarUsuario(id, novoPerfil, req.active());
        UUID adminId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        String mudancas = (req.role() != null ? "role=" + req.role() : "")
                + (req.active() != null ? (req.role() != null ? "," : "") + "active=" + req.active() : "");
        auditoriaServico.registrarAtualizacaoUsuario(adminId, id.toString(), mudancas, httpReq.getRemoteAddr());
        return UserMeDto.de(atualizado);
    }

    private record PatchUserRequest(String role, Boolean active) {}

    private static Perfil parsePerfil(String role) {
        if (role == null) return null;
        return switch (role.toLowerCase()) {
            case "read_only" -> Perfil.SOMENTE_LEITURA;
            case "analyst" -> Perfil.ANALISTA;
            case "manager" -> Perfil.GERENTE;
            case "data_validator" -> Perfil.VALIDADOR_DADOS;
            case "admin" -> Perfil.ADMINISTRADOR;
            default -> throw new RegraNegocioException("Perfil invalido: " + role
                    + ". Valores aceitos: read_only, analyst, manager, data_validator, admin.");
        };
    }
}
