package it.unina.uninafoodlab.model;

/**
 * Modello per rappresentare le statistiche mensili di un Chef
 */
public class ReportMensile {
    private int mese;
    private int anno;
    private String nomeChef;
    private int numeroCorsiTotali;
    private int numeroSessioniOnline;
    private int numeroSessioniPratiche;
    private int ricetteTotali; // nuovo campo: numero totale di ricette associate alle sessioni del mese

    // Costruttore vuoto
    public ReportMensile() {}

    // Costruttore completo
    public ReportMensile(int mese, int anno, String nomeChef, int numeroCorsiTotali,
                         int numeroSessioniOnline, int numeroSessioniPratiche,
                         int ricetteTotali) {
        this.mese = mese;
        this.anno = anno;
        this.nomeChef = nomeChef;
        this.numeroCorsiTotali = numeroCorsiTotali;
        this.numeroSessioniOnline = numeroSessioniOnline;
        this.numeroSessioniPratiche = numeroSessioniPratiche;
        this.ricetteTotali = ricetteTotali;
    }

    // Getters e Setters
    public int getMese() { return mese; }
    public void setMese(int mese) { this.mese = mese; }

    public int getAnno() { return anno; }
    public void setAnno(int anno) { this.anno = anno; }

    public String getNomeChef() { return nomeChef; }
    public void setNomeChef(String nomeChef) { this.nomeChef = nomeChef; }

    public int getNumeroCorsiTotali() { return numeroCorsiTotali; }
    public void setNumeroCorsiTotali(int numeroCorsiTotali) { this.numeroCorsiTotali = numeroCorsiTotali; }

    public int getNumeroSessioniOnline() { return numeroSessioniOnline; }
    public void setNumeroSessioniOnline(int numeroSessioniOnline) { this.numeroSessioniOnline = numeroSessioniOnline; }

    public int getNumeroSessioniPratiche() { return numeroSessioniPratiche; }
    public void setNumeroSessioniPratiche(int numeroSessioniPratiche) { this.numeroSessioniPratiche = numeroSessioniPratiche; }


    public int getRicetteTotali() { return ricetteTotali; }
    public void setRicetteTotali(int ricetteTotali) { this.ricetteTotali = ricetteTotali; }

    // Metodi di utilità
    public String getPeriodo() {
        String[] nomiMesi = {
            "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
            "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
        };
        return nomiMesi[mese - 1] + " " + anno;
    }

    public int getTotaleSessioni() {
        return numeroSessioniOnline + numeroSessioniPratiche;
    }

    public double getPercentualeSessioniPratiche() {
        int totale = getTotaleSessioni();
        return totale > 0 ? (double) numeroSessioniPratiche / totale * 100 : 0;
    }

    public double getPercentualeSessioniOnline() {
        int totale = getTotaleSessioni();
        return totale > 0 ? (double) numeroSessioniOnline / totale * 100 : 0;
    }

    @Override
    public String toString() {
        return "ReportMensile{" +
                "periodo='" + getPeriodo() + '\'' +
                ", chef='" + nomeChef + '\'' +
                ", corsiTotali=" + numeroCorsiTotali +
                ", sessioniTotali=" + getTotaleSessioni() +
                '}';
    }
}
