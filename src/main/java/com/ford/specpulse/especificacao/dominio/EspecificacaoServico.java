package com.ford.specpulse.especificacao.dominio;

import com.ford.specpulse.compartilhado.RecursoNaoEncontradoException;
import com.ford.specpulse.especificacao.persistencia.EspecificacaoRepositorio;
import com.ford.specpulse.versao.dominio.VersaoServico;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EspecificacaoServico {

    private final EspecificacaoRepositorio repositorio;
    private final VersaoServico versaoServico;

    public EspecificacaoServico(EspecificacaoRepositorio repositorio, VersaoServico versaoServico) {
        this.repositorio = repositorio;
        this.versaoServico = versaoServico;
    }

    @Transactional(readOnly = true)
    public List<Especificacao> listarPorVersao(UUID versaoId) {
        versaoServico.buscarPorId(versaoId);
        return repositorio.findByVersaoId(versaoId);
    }

    @Transactional(readOnly = true)
    public Especificacao buscarPorId(UUID id) {
        return repositorio.findById(id)
                .orElseThrow(() -> RecursoNaoEncontradoException.porId("Especificacao", id));
    }

    @Transactional(readOnly = true)
    public Optional<Especificacao> buscarPorVersaoEAtributo(UUID versaoId, UUID atributoId) {
        return repositorio.findByVersaoIdAndAtributoId(versaoId, atributoId);
    }
}
