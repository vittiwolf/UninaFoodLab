package it.unina.uninafoodlab.model;

import java.time.LocalDateTime;

/**
 * Modello per rappresentare un'Iscrizione di un Utente a un Corso
 */
public class Iscrizione {
    private Integer id;
    private Integer utente_id;
    private Integer corso_id;
    private String nomeUtente;
    private String cognomeUtente;
    private String emailUtente;
    private String titoloCorso;
    private LocalDateTime data_iscrizione;
    private String stato; // ATTIVA, COMPLETATA, ANNULLATA
    private String note;
    private String livelloEsperienza; // PRINCIPIANTE, INTERMEDIO, AVANZATO
    private String noteParticolari;

    // Costruttore vuoto
    public Iscrizione() {}

    // Costruttore completo
    public Iscrizione(Integer id, Integer utente_id, Integer corso_id, String nomeUtente,
                      String cognomeUtente, String emailUtente, String titoloCorso,
                      LocalDateTime data_iscrizione, String stato, String note,
                      String livelloEsperienza, String noteParticolari) {
        this.id = id;
        this.utente_id = utente_id;
        this.corso_id = corso_id;
        this.nomeUtente = nomeUtente;
        this.cognomeUtente = cognomeUtente;
        this.emailUtente = emailUtente;
        this.titoloCorso = titoloCorso;
        this.data_iscrizione = data_iscrizione;
        this.stato = stato;
        this.note = note;
        this.livelloEsperienza = livelloEsperienza;
        this.noteParticolari = noteParticolari;
    }

    // Costruttore per nuova iscrizione
    public Iscrizione(Integer utente_id, Integer corso_id, String note) {
        this.utente_id = utente_id;
        this.corso_id = corso_id;
        this.note = note;
        this.stato = "ATTIVA";
        this.data_iscrizione = LocalDateTime.now();
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getUtenteId() { return utente_id; }
    public void setUtenteId(Integer utente_id) { this.utente_id = utente_id; }

    public Integer getCorsoId() { return corso_id; }
    public void setCorsoId(Integer corso_id) { this.corso_id = corso_id; }

    public String getNomeUtente() { return nomeUtente; }
    public void setNomeUtente(String nomeUtente) { this.nomeUtente = nomeUtente; }

    public String getCognomeUtente() { return cognomeUtente; }
    public void setCognomeUtente(String cognomeUtente) { this.cognomeUtente = cognomeUtente; }

    public String getEmailUtente() { return emailUtente; }
    public void setEmailUtente(String emailUtente) { this.emailUtente = emailUtente; }

    public String getTitoloCorso() { return titoloCorso; }
    public void setTitoloCorso(String titoloCorso) { this.titoloCorso = titoloCorso; }

    public LocalDateTime getDataIscrizione() { return data_iscrizione; }
    public void setDataIscrizione(LocalDateTime data_iscrizione) { this.data_iscrizione = data_iscrizione; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getLivelloEsperienza() { return livelloEsperienza; }
    public void setLivelloEsperienza(String livelloEsperienza) { this.livelloEsperienza = livelloEsperienza; }

    public String getNoteParticolari() { return noteParticolari; }
    public void setNoteParticolari(String noteParticolari) { this.noteParticolari = noteParticolari; }

    // Metodi di utilità
    public String getNomeCompletoUtente() {
        return nomeUtente + " " + cognomeUtente;
    }
    
    /**
     * Getter per la colonna statoIscrizione della tabella JavaFX
     * @return stato formattato per visualizzazione
     */
    public String getStatoIscrizione() {
        return getStatoDescrizione();
    }

    public String getStatoDescrizione() {
        switch (stato) {
            case "ATTIVA": return "Attiva";
            case "COMPLETATA": return "Completata";
            case "ANNULLATA": return "Annullata";
            default: return stato;
        }
    }

    public boolean isAttiva() {
        return "ATTIVA".equals(stato);
    }

    public boolean isCompletata() {
        return "COMPLETATA".equals(stato);
    }

    public boolean isAnnullata() {
        return "ANNULLATA".equals(stato);
    }

    @Override
    public String toString() {
        return "Iscrizione{" +
                "id=" + id +
                ", utente_id=" + utente_id +
                ", corso_id=" + corso_id +
                ", nomeUtente='" + nomeUtente + '\'' +
                ", titoloCorso='" + titoloCorso + '\'' +
                ", stato='" + stato + '\'' +
                ", data_iscrizione=" + data_iscrizione +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Iscrizione iscrizione = (Iscrizione) obj;
        return id != null && id.equals(iscrizione.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
