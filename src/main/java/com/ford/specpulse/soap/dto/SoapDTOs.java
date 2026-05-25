package com.ford.specpulse.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.List;

/**
 * DTOs utilizados nas operacoes SOAP do SpecPulseSoapService.
 * Separados dos DTOs REST para independencia de protocolo.
 */
public class SoapDTOs {

    // ---- Ficha Tecnica ----

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ConsultaFichaTecnicaRequest", propOrder = {"marca","modelo","versao","atributos"})
    public static class ConsultaFichaTecnicaRequest {
        private String marca;
        private String modelo;
        private String versao;
        private List<String> atributos;

        public ConsultaFichaTecnicaRequest() {}
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        public String getVersao() { return versao; }
        public void setVersao(String versao) { this.versao = versao; }
        public List<String> getAtributos() { return atributos; }
        public void setAtributos(List<String> atributos) { this.atributos = atributos; }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ItemFichaTecnica", propOrder = {"atributo","valor","unidade","status"})
    public static class ItemFichaTecnica {
        private String atributo;
        private String valor;
        private String unidade;
        private String status;

        public ItemFichaTecnica() {}
        public ItemFichaTecnica(String atributo, String valor, String unidade, String status) {
            this.atributo = atributo;
            this.valor = valor;
            this.unidade = unidade;
            this.status = status;
        }
        public String getAtributo() { return atributo; }
        public void setAtributo(String atributo) { this.atributo = atributo; }
        public String getValor() { return valor; }
        public void setValor(String valor) { this.valor = valor; }
        public String getUnidade() { return unidade; }
        public void setUnidade(String unidade) { this.unidade = unidade; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "FichaTecnicaResponse", propOrder = {"marca","modelo","versao","itens"})
    public static class FichaTecnicaResponse {
        private String marca;
        private String modelo;
        private String versao;
        private List<ItemFichaTecnica> itens;

        public FichaTecnicaResponse() {}
        public String getMarca() { return marca; }
        public void setMarca(String marca) { this.marca = marca; }
        public String getModelo() { return modelo; }
        public void setModelo(String modelo) { this.modelo = modelo; }
        public String getVersao() { return versao; }
        public void setVersao(String versao) { this.versao = versao; }
        public List<ItemFichaTecnica> getItens() { return itens; }
        public void setItens(List<ItemFichaTecnica> itens) { this.itens = itens; }
    }

    // ---- Marca ----

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "MarcaResponse", propOrder = {"id","nome","slug","isFord"})
    public static class MarcaResponse {
        private String id;
        private String nome;
        private String slug;
        private boolean isFord;

        public MarcaResponse() {}
        public MarcaResponse(String id, String nome, String slug, boolean isFord) {
            this.id = id; this.nome = nome; this.slug = slug; this.isFord = isFord;
        }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getNome() { return nome; }
        public void setNome(String nome) { this.nome = nome; }
        public String getSlug() { return slug; }
        public void setSlug(String slug) { this.slug = slug; }
        public boolean isIsFord() { return isFord; }
        public void setIsFord(boolean isFord) { this.isFord = isFord; }
    }

    // ---- Comparacao ----

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "CriarComparacaoRequest",
            propOrder = {"referenciaVersaoId","concorrentesIds","perfilCliente","titulo"})
    public static class CriarComparacaoRequest {
        private String referenciaVersaoId;
        private List<String> concorrentesIds;
        private String perfilCliente;
        private String titulo;

        public CriarComparacaoRequest() {}
        public String getReferenciaVersaoId() { return referenciaVersaoId; }
        public void setReferenciaVersaoId(String referenciaVersaoId) { this.referenciaVersaoId = referenciaVersaoId; }
        public List<String> getConcorrentesIds() { return concorrentesIds; }
        public void setConcorrentesIds(List<String> concorrentesIds) { this.concorrentesIds = concorrentesIds; }
        public String getPerfilCliente() { return perfilCliente; }
        public void setPerfilCliente(String perfilCliente) { this.perfilCliente = perfilCliente; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ComparacaoResponse", propOrder = {"id","titulo","status","mensagem"})
    public static class ComparacaoResponse {
        private String id;
        private String titulo;
        private String status;
        private String mensagem;

        public ComparacaoResponse() {}
        public ComparacaoResponse(String id, String titulo, String status, String mensagem) {
            this.id = id; this.titulo = titulo; this.status = status; this.mensagem = mensagem;
        }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMensagem() { return mensagem; }
        public void setMensagem(String mensagem) { this.mensagem = mensagem; }
    }
}