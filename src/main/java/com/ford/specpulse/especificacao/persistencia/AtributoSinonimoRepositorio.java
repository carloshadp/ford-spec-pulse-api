package com.ford.specpulse.especificacao.persistencia;

import com.ford.specpulse.especificacao.dominio.AtributoSinonimo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtributoSinonimoRepositorio extends JpaRepository<AtributoSinonimo, UUID> {

    Optional<AtributoSinonimo> findByTermoIgnoreCase(String termo);
}
