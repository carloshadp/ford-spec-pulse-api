package com.ford.specpulse.especificacao.persistencia;

import com.ford.specpulse.especificacao.dominio.AtributoDefinicao;
import com.ford.specpulse.especificacao.dominio.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AtributoDefinicaoRepositorio extends JpaRepository<AtributoDefinicao, UUID> {

    Optional<AtributoDefinicao> findByCodigoCanonicoIgnoreCase(String codigoCanonico);

    Optional<AtributoDefinicao> findByNomeExibicaoIgnoreCase(String nomeExibicao);

    List<AtributoDefinicao> findByCategoria(Categoria categoria);
}
