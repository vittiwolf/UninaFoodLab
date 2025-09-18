package it.unina.uninafoodlab.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modello per rappresentare un Corso di Cucina
 */
public class Corso {
    private Integer id;
    private Integer chefId;
    private String nomeChef;
    private Integer categoria_id;
    private String nomeCategoria;
    private String titolo;
    private String descrizione;
    private LocalDate data_inizio;
    private String frequenza; // 'settimanale', 'ogni_due_giorni', 'giornaliero'
    private Integer numero_sessioni;
    private BigDecimal prezzo;
    private LocalDateTime created_at;
    
    // Nuovi campi per supportare il controller
    private String stato = "BOZZA"; // BOZZA, ATTIVO, COMPLETATO, SOSPESO
    private Integer durata; // durata in ore
    private Integer maxPartecipanti;

    // Costruttore vuoto
    public Corso() {}

    // Costruttore completo
    public Corso(Integer id, Integer chefId, String nomeChef, Integer categoria_id, 
                 String nomeCategoria, String titolo, String descrizione, 
                 LocalDate data_inizio, String frequenza, Integer numero_sessioni, 
                 BigDecimal prezzo, Integer durata, Integer maxPartecipanti, LocalDateTime created_at) {
        this.id = id;
        this.chefId = chefId;
        this.nomeChef = nomeChef;
        this.categoria_id = categoria_id;
        this.nomeCategoria = nomeCategoria;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.data_inizio = data_inizio;
        this.frequenza = frequenza;
        this.numero_sessioni = numero_sessioni;
        this.prezzo = prezzo;
        this.durata = durata;
        this.maxPartecipanti = maxPartecipanti;
        this.created_at = created_at;
    }

    // Costruttore per nuovo corso (compatibilità)
    public Corso(Integer chefId, Integer categoria_id, String titolo, String descrizione,
                 LocalDate data_inizio, String frequenza, Integer numero_sessioni, BigDecimal prezzo) {
        this.chefId = chefId;
        this.categoria_id = categoria_id;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.data_inizio = data_inizio;
        this.frequenza = frequenza;
        this.numero_sessioni = numero_sessioni;
        this.prezzo = prezzo;
        // Imposta valori di default per i nuovi campi
        this.durata = 2;
        this.maxPartecipanti = 10;
    }

    // Costruttore per nuovo corso completo
    public Corso(Integer chefId, Integer categoria_id, String titolo, String descrizione,
                 LocalDate data_inizio, String frequenza, Integer numero_sessioni, 
                 BigDecimal prezzo, Integer durata, Integer maxPartecipanti) {
        this.chefId = chefId;
        this.categoria_id = categoria_id;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.data_inizio = data_inizio;
        this.frequenza = frequenza;
        this.numero_sessioni = numero_sessioni;
        this.prezzo = prezzo;
        this.durata = durata;
        this.maxPartecipanti = maxPartecipanti;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getChefId() { return chefId; }
    public void setChefId(Integer chefId) { this.chefId = chefId; }

    public String getNomeChef() { return nomeChef; }
    public void setNomeChef(String nomeChef) { this.nomeChef = nomeChef; }

    public Integer getCategoriaId() { return categoria_id; }
    public void setCategoriaId(Integer categoria_id) { this.categoria_id = categoria_id; }

    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }

    public String getTitolo() { return titolo; }
    public void setTitolo(String titolo) { this.titolo = titolo; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public LocalDate getDataInizio() { return data_inizio; }
    public void setDataInizio(LocalDate data_inizio) { this.data_inizio = data_inizio; }

    public String getFrequenza() { return frequenza; }
    public void setFrequenza(String frequenza) { this.frequenza = frequenza; }

    public Integer getNumeroSessioni() { return numero_sessioni; }
    public void setNumeroSessioni(Integer numero_sessioni) { this.numero_sessioni = numero_sessioni; }

    public BigDecimal getPrezzo() { return prezzo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }

    public LocalDateTime getCreatedAt() { return created_at; }
    public void setCreatedAt(LocalDateTime created_at) { this.created_at = created_at; }

    public Integer getDurata() { return durata; }
    public void setDurata(Integer durata) { this.durata = durata; }

    public Integer getMaxPartecipanti() { return maxPartecipanti; }
    public void setMaxPartecipanti(Integer maxPartecipanti) { this.maxPartecipanti = maxPartecipanti; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    // Metodi alias per compatibilità con il controller
    public String getCategoriaNome() { return nomeCategoria; }
    public void setCategoriaNome(String categoriaNome) { this.nomeCategoria = categoriaNome; }

    // Metodi di utilità
    public String getFrequenzaDescrizione() {
        switch (frequenza.toLowerCase()) {
            case "settimanale": return "Settimanale";
            case "ogni_due_giorni": return "Ogni 2 giorni";
            case "giornaliero": return "Giornaliero";
            default: return frequenza;
        }
    }

    @Override
    public String toString() {
        return titolo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Corso corso = (Corso) obj;
        return id != null && id.equals(corso.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
