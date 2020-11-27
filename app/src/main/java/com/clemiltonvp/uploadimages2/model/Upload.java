package com.clemiltonvp.uploadimages2.model;

public class Upload {
   private String nomeImagem;
   private String uri;

    public Upload(String nomeImagem, String uri) {
        this.nomeImagem = nomeImagem;
        this.uri = uri;
    }

    public String getNomeImagem() {
        return nomeImagem;
    }

    public void setNomeImagem(String nomeImagem) {
        this.nomeImagem = nomeImagem;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }
}
