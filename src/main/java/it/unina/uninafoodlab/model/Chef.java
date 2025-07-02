package it.unina.uninafoodlab.model;

import java.time.LocalDateTime;

/**
 * Modello per rappresentare un Chef del sistema UninaFoodLab
 */
public class Chef {
    private Integer id;
    private String username;
    private String password;
    private String nome;
    private String cognome;
    private String email;
    private String specializzazione;
    private LocalDateTime created_at;

    // Costruttore vuoto
    public Chef() {}

    // Costruttore completo
    public Chef(Integer id, String username, String password, String nome, String cognome, 
                String email, String specializzazione, LocalDateTime created_at) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.specializzazione = specializzazione;
        this.created_at = created_at;
    }

    // Costruttore per nuovo chef (senza ID)
    public Chef(String username, String password, String nome, String cognome, 
                String email, String specializzazione) {
        this.username = username;
        this.password = password;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.specializzazione = specializzazione;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }
    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSpecializzazione() { return specializzazione; }
    public void setSpecializzazione(String specializzazione) { this.specializzazione = specializzazione; }

    public LocalDateTime getCreatedAt() { return created_at; }
    public void setCreatedAt(LocalDateTime created_at) { this.created_at = created_at; }

    // Metodi di utilità
    public String getNomeCompleto() {
        return nome + " " + cognome;
    }

    @Override
    public String toString() {
        return "Chef{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", specializzazione='" + specializzazione + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Chef chef = (Chef) obj;
        return id != null && id.equals(chef.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
