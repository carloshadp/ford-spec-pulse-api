package com.ford.specpulse.veiculo.persistencia;

import com.ford.specpulse.veiculo.dominio.Marca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarcaRepositorio extends JpaRepository<Marca, UUID> {

    Optional<Marca> findByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCase(String nome);
}
