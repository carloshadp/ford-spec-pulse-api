package com.ford.specpulse.autenticacao.dominio;

import com.ford.specpulse.autenticacao.persistencia.RefreshTokenRepositorio;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.security.BruteForceProtectionService;
import com.ford.specpulse.seguranca.Perfil;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;


@Service
public class AutenticacaoServico {

    private final UsuarioServico usuarioServico;
    private final TokenServico tokenServico;
    private final RefreshTokenRepositorio refreshRepositorio;
    private final BruteForceProtectionService bruteForceService;

    public AutenticacaoServico(UsuarioServico usuarioServico,
                                TokenServico tokenServico,
                                RefreshTokenRepositorio refreshRepositorio,
                                BruteForceProtectionService bruteForceService) {
        this.usuarioServico = usuarioServico;
        this.tokenServico = tokenServico;
        this.refreshRepositorio = refreshRepositorio;
        this.bruteForceService = bruteForceService;
    }


    @Transactional
    public ResultadoAutenticacao registrar(String nome, String email, String senha, Perfil perfil) {
        Usuario novo = usuarioServico.registrar(nome, email, senha, perfil);
        return emitirPar(novo);
    }


    @Transactional
    public ResultadoAutenticacao autenticar(String email, String senha, String ip) {
        if (bruteForceService.estaBloqueado(ip)) {
            throw new RegraNegocioException("Acesso temporariamente bloqueado. Tente novamente mais tarde.");
        }

        Usuario usuario;
        try {
            usuario = usuarioServico.buscarPorEmail(email);
        } catch (Exception ex) {
            bruteForceService.registrarFalha(ip, email);
            throw new RegraNegocioException("Credenciais invalidas.");
        }

        if (!usuario.isAtivo()) {
            bruteForceService.registrarFalha(ip, email);
            throw new RegraNegocioException("Credenciais invalidas.");
        }

        if (!usuarioServico.senhaCorresponde(senha, usuario)) {
            bruteForceService.registrarFalha(ip, email);
            throw new RegraNegocioException("Credenciais invalidas.");
        }

        bruteForceService.registrarSucesso(ip);
        return emitirPar(usuario);
    }


    @Transactional
    public ResultadoAutenticacao renovar(String refreshTokenSerializado) {
        Jwt jwt = tokenServico.validar(refreshTokenSerializado);
        String tipo = jwt.getClaim(TokenServico.CLAIM_TIPO);
        if (!TokenServico.TIPO_REFRESH.equals(tipo)) {
            throw new RegraNegocioException("Token nao e do tipo refresh.");
        }

        UUID jti = UUID.fromString(jwt.getId());
        RefreshToken armazenado = refreshRepositorio.findById(jti)
                .orElseThrow(() -> new RegraNegocioException("Refresh token nao reconhecido."));

        OffsetDateTime agora = OffsetDateTime.now();
        if (!armazenado.isAtivo(agora)) {
            throw new RegraNegocioException("Refresh token expirado ou revogado.");
        }

        armazenado.revogar("ROTACAO");
        refreshRepositorio.save(armazenado);

        Usuario usuario = armazenado.getUsuario();
        if (!usuario.isAtivo()) {
            throw new RegraNegocioException("Usuario desativado.");
        }
        return emitirPar(usuario);
    }


    @Transactional
    public void revogarSessoesDoUsuario(UUID usuarioId) {
        refreshRepositorio.revogarTodosDoUsuario(usuarioId, OffsetDateTime.now(), "LOGOUT");
    }


    private ResultadoAutenticacao emitirPar(Usuario usuario) {
        TokenServico.TokenEmitido acesso = tokenServico.emitirAcesso(usuario);
        TokenServico.TokenEmitido refresh = tokenServico.emitirRefresh(usuario);

        RefreshToken registro = new RefreshToken(refresh.jti(), usuario, refresh.expiraEm());
        refreshRepositorio.save(registro);

        return new ResultadoAutenticacao(usuario, acesso, refresh);
    }


    public record ResultadoAutenticacao(
            Usuario usuario,
            TokenServico.TokenEmitido acesso,
            TokenServico.TokenEmitido refresh
    ) {
    }
}
