package com.ford.specpulse.autenticacao.dominio;

import com.ford.specpulse.compartilhado.RegraNegocioException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;


/**
 * Emite e valida JWTs HS256. O access token leva claims minimos (sub, email,
 * perfil, type=access) e o refresh leva apenas type=refresh + jti para que o
 * servico de autenticacao consulte a tabela de refresh_tokens.
 */
@Component
public class TokenServico {

    public static final String CLAIM_TIPO = "type";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_PERFIL = "role";
    public static final String TIPO_ACESSO = "access";
    public static final String TIPO_REFRESH = "refresh";

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final PropriedadesJwt propriedades;

    public TokenServico(JwtEncoder encoder, JwtDecoder decoder, PropriedadesJwt propriedades) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.propriedades = propriedades;
    }


    public TokenEmitido emitirAcesso(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plusSeconds(propriedades.expiraAcessoSegundos());
        String jti = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.emissor())
                .issuedAt(agora)
                .expiresAt(expira)
                .subject(usuario.getId().toString())
                .id(jti)
                .claim(CLAIM_TIPO, TIPO_ACESSO)
                .claim(CLAIM_EMAIL, usuario.getEmail())
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .build();

        String token = encoder.encode(parametros(claims)).getTokenValue();
        return new TokenEmitido(token, UUID.fromString(jti),
                OffsetDateTime.ofInstant(expira, ZoneOffset.UTC));
    }


    public TokenEmitido emitirRefresh(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expira = agora.plusSeconds(propriedades.expiraRefreshSegundos());
        UUID jti = UUID.randomUUID();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(propriedades.emissor())
                .issuedAt(agora)
                .expiresAt(expira)
                .subject(usuario.getId().toString())
                .id(jti.toString())
                .claim(CLAIM_TIPO, TIPO_REFRESH)
                .build();

        String token = encoder.encode(parametros(claims)).getTokenValue();
        return new TokenEmitido(token, jti,
                OffsetDateTime.ofInstant(expira, ZoneOffset.UTC));
    }


    public Jwt validar(String token) {
        try {
            return decoder.decode(token);
        } catch (Exception ex) {
            throw new RegraNegocioException("Token invalido ou expirado.");
        }
    }


    private JwtEncoderParameters parametros(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return JwtEncoderParameters.from(header, claims);
    }


    public record TokenEmitido(String valor, UUID jti, OffsetDateTime expiraEm) {
    }
}
