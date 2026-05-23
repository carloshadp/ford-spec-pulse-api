package com.ford.specpulse.veiculo.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.compartilhado.RegraNegocioException;
import com.ford.specpulse.veiculo.persistencia.MarcaRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;


@Service
public class MarcaServico {

    private final MarcaRepositorio repositorio;

    public MarcaServico(MarcaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Marca> listarTodas() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Marca buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Marca", id));
    }

    @Transactional
    public Marca criar(String nome, String paisOrigem) {
        if (repositorio.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException("Ja existe uma marca cadastrada com o nome '" + nome + "'.");
        }
        return repositorio.save(new Marca(nome, paisOrigem));
    }
}
