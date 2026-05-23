package com.ford.specpulse.autenticacao.api;

import com.ford.specpulse.autenticacao.api.dto.LoginRequisicao;
import com.ford.specpulse.autenticacao.api.dto.RefreshRequisicao;
import com.ford.specpulse.autenticacao.api.dto.RegistroRequisicao;
import com.ford.specpulse.autenticacao.api.dto.TokenResposta;
import com.ford.specpulse.autenticacao.dominio.AutenticacaoServico;
import com.ford.specpulse.autenticacao.dominio.AutenticacaoServico.ResultadoAutenticacao;
import com.ford.specpulse.auditoria.dominio.AuditoriaServico;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.seguranca.Perfil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


@Tag(name = "Autenticacao",
        description = "Endpoints de registro, login, refresh e logout.")
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoControlador {

    private final AutenticacaoServico autenticacaoServico;
    private final AuditoriaServico auditoriaServico;

    public AutenticacaoControlador(AutenticacaoServico autenticacaoServico,
                                    AuditoriaServico auditoriaServico) {
        this.autenticacaoServico = autenticacaoServico;
        this.auditoriaServico = auditoriaServico;
    }


    @Operation(summary = "Registra um novo usuario.",
            description = "Endpoint publico. Cria um usuario com perfil SOMENTE_LEITURA por padrao. "
                    + "Se um perfil diferente for solicitado, retorna 422 quando o requisitante nao "
                    + "for ADMINISTRADOR autenticado.")
    @PostMapping("/register")
    public ResponseEntity<TokenResposta> registrar(@Valid @RequestBody RegistroRequisicao req,
                                                     Authentication autenticacao,
                                                     HttpServletRequest httpReq) {
        Perfil perfilSolicitado = req.perfil();
        if (perfilSolicitado != null && perfilSolicitado != Perfil.SOMENTE_LEITURA) {
            boolean requisitanteEhAdmin = autenticacao != null
                    && autenticacao.isAuthenticated()
                    && autenticacao.getAuthorities().stream().anyMatch(
                            a -> a.getAuthority().equals("ROLE_" + Perfil.ADMINISTRADOR.name()));
            if (!requisitanteEhAdmin) {
                throw new RegraNegocioException(
                        "Somente ADMINISTRADOR pode registrar usuarios com perfil diferente de SOMENTE_LEITURA.");
            }
        }

        ResultadoAutenticacao resultado = autenticacaoServico.registrar(
                req.nome(), req.email(), req.senha(), perfilSolicitado);
        auditoriaServico.registrarRegistro(req.email(), httpReq.getRemoteAddr());
        return ResponseEntity.status(HttpStatus.CREATED).body(TokenResposta.de(resultado));
    }


    @Operation(summary = "Autentica um usuario por email + senha.",
            description = "Endpoint publico. Retorna access token (15 min) e "
                    + "refresh token (7 dias). Use o access token como Authorization: Bearer <token>.")
    @PostMapping("/login")
    public TokenResposta autenticar(@Valid @RequestBody LoginRequisicao req,
                                    HttpServletRequest httpReq) {
        ResultadoAutenticacao resultado;
        try {
            resultado = autenticacaoServico.autenticar(req.email(), req.senha());
            auditoriaServico.registrarLogin(req.email(), httpReq.getRemoteAddr(), true);
        } catch (Exception ex) {
            auditoriaServico.registrarLogin(req.email(), httpReq.getRemoteAddr(), false);
            throw ex;
        }
        return TokenResposta.de(resultado);
    }


    @Operation(summary = "Renova o par de tokens usando um refresh valido.",
            description = "Rotaciona refresh tokens: o token enviado e revogado e um novo par "
                    + "(access + refresh) e emitido.")
    @PostMapping("/refresh")
    public TokenResposta renovar(@Valid @RequestBody RefreshRequisicao req) {
        ResultadoAutenticacao resultado = autenticacaoServico.renovar(req.refreshToken());
        return TokenResposta.de(resultado);
    }


    @Operation(summary = "Revoga todos os refresh tokens do usuario autenticado.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<Void> sair(
            @org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        UUID usuarioId = UUID.fromString(jwt.getSubject());
        autenticacaoServico.revogarSessoesDoUsuario(usuarioId);
        return ResponseEntity.noContent().build();
    }
}
