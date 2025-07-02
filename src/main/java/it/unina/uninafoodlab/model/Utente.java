package it.unina.uninafoodlab.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modello per rappresentare un Utente/Partecipante del sistema UninaFoodLab
 */
public class Utente {
    private Integer id;
    private String nome;
    private String cognome;
    private String email;
    private String telefono;
    private LocalDate dataNascita;
    private String livello_esperienza; // PRINCIPIANTE, INTERMEDIO, AVANZATO
    private boolean attivo;
    private LocalDateTime created_at;

    // Costruttore vuoto
    public Utente() {}

    // Costruttore completo
    public Utente(Integer id, String nome, String cognome, String email, String telefono,
                  LocalDate dataNascita, String livello_esperienza, boolean attivo, 
                  LocalDateTime created_at) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
        this.dataNascita = dataNascita;
        this.livello_esperienza = livello_esperienza;
        this.attivo = attivo;
        this.created_at = created_at;
    }

    // Costruttore per nuovo utente
    public Utente(String nome, String cognome, String email, String telefono,
                  LocalDate dataNascita, String livello_esperienza) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.telefono = telefono;
        this.dataNascita = dataNascita;
        this.livello_esperienza = livello_esperienza;
        this.attivo = true;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public LocalDate getDataNascita() { return dataNascita; }
    public void setDataNascita(LocalDate dataNascita) { this.dataNascita = dataNascita; }

    public String getLivelloEsperienza() { return livello_esperienza; }
    public void setLivelloEsperienza(String livello_esperienza) { this.livello_esperienza = livello_esperienza; }

    public boolean isAttivo() { return attivo; }
    public void setAttivo(boolean attivo) { this.attivo = attivo; }

    public LocalDateTime getCreatedAt() { return created_at; }
    public void setCreatedAt(LocalDateTime created_at) { this.created_at = created_at; }

    // Metodi di utilità
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    public int getEta() {
        if (dataNascita == null) return 0;
        return LocalDate.now().getYear() - dataNascita.getYear();
    }

    public String getLivelloEsperienzaDescrizione() {
        switch (livello_esperienza) {
            case "PRINCIPIANTE": return "Principiante";
            case "INTERMEDIO": return "Intermedio";
            case "AVANZATO": return "Avanzato";
            default: return livello_esperienza;
        }
    }

    @Override
    public String toString() {
        return "Utente{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", email='" + email + '\'' +
                ", livello_esperienza='" + livello_esperienza + '\'' +
                ", attivo=" + attivo +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Utente utente = (Utente) obj;
        return id != null && id.equals(utente.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
