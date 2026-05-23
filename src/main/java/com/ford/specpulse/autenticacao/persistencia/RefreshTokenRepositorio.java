package com.ford.specpulse.autenticacao.persistencia;

import com.ford.specpulse.autenticacao.dominio.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;


public interface RefreshTokenRepositorio extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findById(UUID id);

    @Modifying
    @Query("update RefreshToken r set r.revogadoEm = :momento, r.motivoRevogacao = :motivo "
            + "where r.usuario.id = :usuarioId and r.revogadoEm is null")
    int revogarTodosDoUsuario(@Param("usuarioId") UUID usuarioId,
                               @Param("momento") OffsetDateTime momento,
                               @Param("motivo") String motivo);
}
