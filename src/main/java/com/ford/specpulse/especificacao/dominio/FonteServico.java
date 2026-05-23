package com.ford.specpulse.especificacao.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.especificacao.persistencia.FonteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FonteServico {

    private final FonteRepositorio repositorio;

    public FonteServico(FonteRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional(readOnly = true)
    public List<Fonte> listarTodas() {
        return repositorio.findAll();
    }

    @Transactional(readOnly = true)
    public Fonte buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Fonte", id));
    }
}
