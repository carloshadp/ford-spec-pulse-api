package com.ford.specpulse.especificacao.persistencia;

import com.ford.specpulse.especificacao.dominio.Fonte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FonteRepositorio extends JpaRepository<Fonte, UUID> {
}
