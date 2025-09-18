package it.unina.uninafoodlab.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modello per rappresentare una Sessione di un Corso
 */
public class Sessione {
    private Integer id;
    private Integer corso_id;
    private String titoloCorso;
    private Integer numeroSessione;
    private LocalDate dataSessione;
    private String tipo; // 'online' o 'presenza'
    private String titolo;
    private String descrizione;
    private Integer durataMinuti;
    private Boolean completata;
    private String ricetteAssociate; // Campo per visualizzazione nella tabella
    private LocalDateTime created_at;

    // Costruttore vuoto
    public Sessione() {}

    // Costruttore completo
    public Sessione(Integer id, Integer corso_id, String titoloCorso, Integer numeroSessione,
                    LocalDate dataSessione, String tipo, String titolo, String descrizione,
                    Integer durataMinuti, Boolean completata, LocalDateTime created_at) {
        this.id = id;
        this.corso_id = corso_id;
        this.titoloCorso = titoloCorso;
        this.numeroSessione = numeroSessione;
        this.dataSessione = dataSessione;
        this.tipo = tipo;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.durataMinuti = durataMinuti;
        this.completata = completata;
        this.created_at = created_at;
    }

    // Costruttore per nuova sessione
    public Sessione(Integer corso_id, Integer numeroSessione, LocalDate dataSessione,
                    String tipo, String titolo, String descrizione, Integer durataMinuti) {
        this.corso_id = corso_id;
        this.numeroSessione = numeroSessione;
        this.dataSessione = dataSessione;
        this.tipo = tipo;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.durataMinuti = durataMinuti;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getCorsoId() { return corso_id; }
    public void setCorsoId(Integer corso_id) { this.corso_id = corso_id; }

    public String getTitoloCorso() { return titoloCorso; }
    public void setTitoloCorso(String titoloCorso) { this.titoloCorso = titoloCorso; }

    public Integer getNumeroSessione() { return numeroSessione; }
    public void setNumeroSessione(Integer numeroSessione) { this.numeroSessione = numeroSessione; }

    public LocalDate getDataSessione() { return dataSessione; }
    public void setDataSessione(LocalDate dataSessione) { this.dataSessione = dataSessione; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Integer getDurataMinuti() { return durataMinuti; }
    public void setDurataMinuti(Integer durataMinuti) { this.durataMinuti = durataMinuti; }

    public Boolean getCompletata() { return completata; }
    public void setCompletata(Boolean completata) { this.completata = completata; }

    public LocalDateTime getCreatedAt() { return created_at; }
    public void setCreatedAt(LocalDateTime created_at) { this.created_at = created_at; }

    // Metodi di utilità
    public boolean isPratica() {
        return "presenza".equalsIgnoreCase(tipo);
    }

    public boolean isOnline() {
        return "online".equalsIgnoreCase(tipo);
    }

    public String getTipoDescrizione() {
        return isPratica() ? "In Presenza" : "Online";
    }

    public String getDurataOre() {
        if (durataMinuti == null) return "";
        int ore = durataMinuti / 60;
        int minuti = durataMinuti % 60;
        if (ore > 0 && minuti > 0) {
            return ore + "h " + minuti + "m";
        } else if (ore > 0) {
            return ore + "h";
        } else {
            return minuti + "m";
        }
    }

    public String getCompletataDescrizione() {
        return completata != null && completata ? "Sì" : "No";
    }

    public String getRicetteAssociate() {
        // Questo campo sarà utilizzato dalla TableView per mostrare le ricette
        // Il valore sarà impostato dal controller
        return ricetteAssociate != null ? ricetteAssociate : "";
    }

    public void setRicetteAssociate(String ricetteAssociate) {
        this.ricetteAssociate = ricetteAssociate;
    }

    @Override
    public String toString() {
        return "Sessione " + numeroSessione + ": " + titolo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sessione sessione = (Sessione) obj;
        return id != null && id.equals(sessione.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
