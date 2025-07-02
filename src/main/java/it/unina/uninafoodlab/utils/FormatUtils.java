package it.unina.uninafoodlab.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Classe di utilità per la formattazione di date e valori
 */
public class FormatUtils {
    
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Formatta una data per la visualizzazione
     * @param data Data da formattare
     * @return Stringa formattata o stringa vuota se la data è null
     */
    public static String formattaData(LocalDate data) {
        return data != null ? data.format(DATE_FORMATTER) : "";
    }
    
    /**
     * Formatta una data e ora per la visualizzazione
     * @param dataOra Data e ora da formattare
     * @return Stringa formattata o stringa vuota se la data è null
     */
    public static String formattaDataOra(LocalDateTime dataOra) {
        return dataOra != null ? dataOra.format(DATETIME_FORMATTER) : "";
    }
    
    /**
     * Formatta un valore decimale con un numero specifico di cifre decimali
     * @param valore Valore da formattare
     * @param decimali Numero di cifre decimali
     * @return Stringa formattata
     */
    public static String formattaDecimale(Double valore, int decimali) {
        if (valore == null) {
            return "";
        }
        String formato = "%." + decimali + "f";
        return String.format(formato, valore);
    }
    
    /**
     * Formatta un valore decimale con 2 cifre decimali di default
     * @param valore Valore da formattare
     * @return Stringa formattata
     */
    public static String formattaDecimale(Double valore) {
        return formattaDecimale(valore, 2);
    }
    
    /**
     * Capitalizza la prima lettera di una stringa
     * @param str Stringa da capitalizzare
     * @return Stringa con la prima lettera maiuscola
     */
    public static String capitalizza(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    
    /**
     * Formatta lo stato di un campione per la visualizzazione
     * @param stato Stato del campione
     * @return Stato formattato
     */
    public static String formattaStato(String stato) {
        if (stato == null) {
            return "Non Definito";
        }
        
        return switch (stato) {
            case "IN_ATTESA" -> "In Attesa";
            case "IN_ANALISI" -> "In Analisi";
            case "COMPLETATO" -> "Completato";
            case "SCARTATO" -> "Scartato";
            default -> capitalizza(stato.replace("_", " "));
        };
    }
    
    /**
     * Formatta l'esito di un'analisi per la visualizzazione
     * @param esito Esito dell'analisi
     * @return Esito formattato
     */
    public static String formattaEsito(String esito) {
        if (esito == null) {
            return "Non Definito";
        }
        
        return switch (esito) {
            case "CONFORME" -> "Conforme";
            case "NON_CONFORME" -> "Non Conforme";
            case "INCERTO" -> "Incerto";
            default -> capitalizza(esito.replace("_", " "));
        };
    }
    
    /**
     * Verifica se una stringa è vuota o null
     * @param str Stringa da verificare
     * @return true se la stringa è vuota o null
     */
    public static boolean isVuota(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Tronca una stringa alla lunghezza specificata aggiungendo "..."
     * @param str Stringa da troncare
     * @param lunghezzaMassima Lunghezza massima
     * @return Stringa troncata
     */
    public static String tronca(String str, int lunghezzaMassima) {
        if (str == null || str.length() <= lunghezzaMassima) {
            return str;
        }
        return str.substring(0, lunghezzaMassima - 3) + "...";
    }
}
