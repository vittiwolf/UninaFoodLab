package it.unina.uninafoodlab.controller.helper;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Helper class per la gestione dei messaggi (errori, successi, avvisi)
 */
public class MessageHelper {
    
    /**
     * Mostra un messaggio di errore
     */
    public void mostraErrore(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    
    /**
     * Mostra un messaggio di successo
     */
    public void mostraSuccesso(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    
    /**
     * Mostra un avviso
     */
    public void mostraAvviso(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    
    /**
     * Mostra un dialog di conferma e ritorna true se l'utente conferma
     */
    public boolean mostraConferma(String titolo, String header, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(header);
        alert.setContentText(messaggio);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}
