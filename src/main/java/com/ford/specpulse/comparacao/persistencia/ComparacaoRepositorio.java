package com.ford.specpulse.comparacao.persistencia;

import com.ford.specpulse.comparacao.dominio.Comparacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComparacaoRepositorio extends JpaRepository<Comparacao, UUID> {

    List<Comparacao> findByCriadoPor(String criadoPor);
}
