package com.ford.specpulse.autenticacao.dominio;

import com.ford.specpulse.autenticacao.persistencia.UsuarioRepositorio;
import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.seguranca.Perfil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class UsuarioServico implements UserDetailsService {

    private final UsuarioRepositorio repositorio;
    private final PasswordEncoder codificador;

    public UsuarioServico(UsuarioRepositorio repositorio, PasswordEncoder codificador) {
        this.repositorio = repositorio;
        this.codificador = codificador;
    }


    @Transactional
    public Usuario registrar(String nome, String email, String senha, Perfil perfil) {
        String emailNormalizado = email.trim().toLowerCase();
        if (repositorio.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new RegraNegocioException("Email ja esta em uso.");
        }
        Perfil perfilFinal = perfil != null ? perfil : Perfil.SOMENTE_LEITURA;
        String hash = codificador.encode(senha);
        Usuario novo = new Usuario(nome.trim(), emailNormalizado, hash, perfilFinal);
        return repositorio.save(novo);
    }


    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return repositorio.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuario nao encontrado para o email informado."));
    }


    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuario nao encontrado para o id informado."));
    }


    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return repositorio.findAll();
    }

    @Transactional
    public Usuario atualizarUsuario(UUID id, Perfil novoPerfil, Boolean ativo) {
        Usuario u = buscarPorId(id);
        if (novoPerfil != null) u.alterarPerfil(novoPerfil);
        if (ativo != null) {
            if (ativo) u.ativar(); else u.desativar();
        }
        return repositorio.save(u);
    }

    @Transactional(readOnly = true)
    public boolean existeAdmin() {
        return repositorio.existsByPerfil(Perfil.ADMINISTRADOR);
    }

    public boolean senhaCorresponde(String senhaTextoPuro, Usuario usuario) {
        return codificador.matches(senhaTextoPuro, usuario.getSenhaHash());
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repositorio.findByEmailIgnoreCase(username.trim())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario nao encontrado: " + username));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getSenhaHash())
                .authorities(List.of(new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getPerfil().name())))
                .disabled(!usuario.isAtivo())
                .build();
    }
}
