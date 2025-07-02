package it.unina.uninafoodlab.model;

/**
 * Modello per rappresentare una Categoria di Corso
 */
public class CategoriaCorso {
    private Integer id;
    private String nome;
    private String descrizione;

    // Costruttore vuoto
    public CategoriaCorso() {}

    // Costruttore completo
    public CategoriaCorso(Integer id, String nome, String descrizione) {
        this.id = id;
        this.nome = nome;
        this.descrizione = descrizione;
    }

    // Costruttore senza ID
    public CategoriaCorso(String nome, String descrizione) {
        this.nome = nome;
        this.descrizione = descrizione;
    }

    // Getters e Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    @Override
    public String toString() {
        return nome; // Per le ComboBox
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CategoriaCorso that = (CategoriaCorso) obj;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
