package it.unina.uninafoodlab.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Classe di utilità per mostrare dialog e messaggi all'utente
 */
public class DialogUtils {
    private static final Logger logger = LoggerFactory.getLogger(DialogUtils.class);
    
    /**
     * Mostra un dialog di conferma
     * @param titolo Titolo del dialog
     * @param messaggio Messaggio da mostrare
     * @return true se l'utente conferma, false altrimenti
     */
    public static boolean mostraConferma(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titolo);
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Mostra un messaggio di successo
     * @param messaggio Messaggio da mostrare
     */
    public static void mostraSuccesso(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Operazione Completata");
        alert.setHeaderText("Successo");
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    
    /**
     * Mostra un messaggio di errore
     * @param titolo Titolo del dialog
     * @param messaggio Messaggio di errore
     */
    public static void mostraErrore(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titolo);
        alert.setHeaderText("Si è verificato un errore");
        alert.setContentText(messaggio);
        alert.showAndWait();
        
        logger.error("Errore mostrato all'utente: {} - {}", titolo, messaggio);
    }
    
    /**
     * Mostra un messaggio di avviso
     * @param titolo Titolo del dialog
     * @param messaggio Messaggio di avviso
     */
    public static void mostraAvviso(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titolo);
        alert.setHeaderText("Attenzione");
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    
    /**
     * Mostra un messaggio informativo
     * @param titolo Titolo del dialog
     * @param messaggio Messaggio informativo
     */
    public static void mostraInformazione(String titolo, String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titolo);
        alert.setHeaderText("Informazione");
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
