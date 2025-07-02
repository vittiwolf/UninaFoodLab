package it.unina.uninafoodlab.model;

import java.time.LocalDateTime;

/**
 * Modello per rappresentare una Ricetta
 */
public class Ricetta {
    private Integer id;
    private Integer chefId;
    private String nomeChef;
    private String nome;
    private String descrizione;
    private Integer difficolta; // 1-5
    private Integer tempoPreparazione; // in minuti
    private Integer numeroPortions;
    private String istruzioni;
    private LocalDateTime created_at;

    // Costruttore vuoto
    public Ricetta() {}

    // Costruttore completo
    public Ricetta(Integer id, Integer chefId, String nomeChef, String nome, String descrizione,
                   Integer difficolta, Integer tempoPreparazione, Integer numeroPortions,
                   String istruzioni, LocalDateTime created_at) {
        this.id = id;
        this.chefId = chefId;
        this.nomeChef = nomeChef;
        this.nome = nome;
        this.descrizione = descrizione;
        this.difficolta = difficolta;
        this.tempoPreparazione = tempoPreparazione;
        this.numeroPortions = numeroPortions;
        this.istruzioni = istruzioni;
        this.created_at = created_at;
    }

    // Costruttore per nuova ricetta
    public Ricetta(Integer chefId, String nome, String descrizione, Integer difficolta,
                   Integer tempoPreparazione, Integer numeroPortions, String istruzioni) {
        this.chefId = chefId;
        this.nome = nome;
        this.descrizione = descrizione;
        this.difficolta = difficolta;
        this.tempoPreparazione = tempoPreparazione;
        this.numeroPortions = numeroPortions;
        this.istruzioni = istruzioni;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getChefId() { return chefId; }
    public void setChefId(Integer chefId) { this.chefId = chefId; }

    public String getNomeChef() { return nomeChef; }
    public void setNomeChef(String nomeChef) { this.nomeChef = nomeChef; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Integer getDifficolta() { return difficolta; }
    public void setDifficolta(Integer difficolta) { this.difficolta = difficolta; }

    public Integer getTempoPreparazione() { return tempoPreparazione; }
    public void setTempoPreparazione(Integer tempoPreparazione) { this.tempoPreparazione = tempoPreparazione; }

    public Integer getNumeroPortions() { return numeroPortions; }
    public void setNumeroPortions(Integer numeroPortions) { this.numeroPortions = numeroPortions; }

    public String getIstruzioni() { return istruzioni; }
    public void setIstruzioni(String istruzioni) { this.istruzioni = istruzioni; }

    public LocalDateTime getCreatedAt() { return created_at; }
    public void setCreatedAt(LocalDateTime created_at) { this.created_at = created_at; }

    // Metodi di utilità
    public String getDifficoltaStelle() {
        if (difficolta == null) return "";
        StringBuilder stelle = new StringBuilder();
        for (int i = 0; i < difficolta; i++) {
            stelle.append("★");
        }
        for (int i = difficolta; i < 5; i++) {
            stelle.append("☆");
        }
        return stelle.toString();
    }

    public String getTempoPreparazioneFormattato() {
        if (tempoPreparazione == null) return "";
        if (tempoPreparazione >= 60) {
            int ore = tempoPreparazione / 60;
            int minuti = tempoPreparazione % 60;
            if (minuti == 0) {
                return ore + "h";
            } else {
                return ore + "h " + minuti + "m";
            }
        } else {
            return tempoPreparazione + "m";
        }
    }

    @Override
    public String toString() {
        return nome;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ricetta ricetta = (Ricetta) obj;
        return id != null && id.equals(ricetta.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
