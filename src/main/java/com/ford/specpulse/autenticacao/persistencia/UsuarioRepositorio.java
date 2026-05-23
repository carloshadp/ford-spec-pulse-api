package com.ford.specpulse.autenticacao.persistencia;

import com.ford.specpulse.autenticacao.dominio.Usuario;
import com.ford.specpulse.seguranca.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;


public interface UsuarioRepositorio extends JpaRepository<Usuario, UUID> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByPerfil(Perfil perfil);
}
