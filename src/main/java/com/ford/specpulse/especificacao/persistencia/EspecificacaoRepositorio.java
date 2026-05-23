package com.ford.specpulse.especificacao.persistencia;

import com.ford.specpulse.especificacao.dominio.Especificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EspecificacaoRepositorio extends JpaRepository<Especificacao, UUID> {

    @Query("SELECT e FROM Especificacao e JOIN FETCH e.versao v JOIN FETCH v.veiculo vei JOIN FETCH vei.marca JOIN FETCH e.atributo LEFT JOIN FETCH e.fonte WHERE e.versao.id = :versaoId")
    List<Especificacao> findByVersaoId(UUID versaoId);

    Optional<Especificacao> findByVersaoIdAndAtributoId(UUID versaoId, UUID atributoId);
}
