package com.ford.specpulse.comparacao.dominio;

import com.ford.specpulse.compartilhado.EntidadeBase;
import com.ford.specpulse.versao.dominio.Versao;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "comparacoes")
public class Comparacao extends EntidadeBase {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "versao_ford_id", nullable = false)
    private Versao versaoFord;

    @Enumerated(EnumType.STRING)
    @Column(name = "perfil_cliente", length = 40)
    private PerfilCliente perfilCliente;

    @Column(name = "criado_por", nullable = false, length = 80)
    private String criadoPor;

    @OneToMany(mappedBy = "comparacao", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordem ASC")
    private List<ComparacaoVersao> concorrentes = new ArrayList<>();

    @OneToMany(mappedBy = "comparacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ComparacaoCelula> celulas = new ArrayList<>();

    protected Comparacao() {
    }

    public Comparacao(String titulo, String descricao, Versao versaoFord,
                      PerfilCliente perfilCliente, String criadoPor) {
        this.titulo = Objects.requireNonNull(titulo, "titulo e obrigatorio");
        this.versaoFord = Objects.requireNonNull(versaoFord, "versao Ford de referencia e obrigatoria");
        this.criadoPor = Objects.requireNonNull(criadoPor, "criadoPor e obrigatorio");
        this.descricao = descricao;
        this.perfilCliente = perfilCliente;
    }

    public void adicionarConcorrente(Versao versao, int ordem) {
        concorrentes.add(new ComparacaoVersao(this, versao, ordem));
    }

    public void adicionarCelula(ComparacaoCelula celula) {
        celulas.add(celula);
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public Versao getVersaoFord() { return versaoFord; }
    public PerfilCliente getPerfilCliente() { return perfilCliente; }
    public String getCriadoPor() { return criadoPor; }
    public List<ComparacaoVersao> getConcorrentes() { return concorrentes; }
    public List<ComparacaoCelula> getCelulas() { return celulas; }
}
