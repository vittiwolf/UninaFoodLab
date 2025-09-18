package it.unina.uninafoodlab.controller.helper;

import it.unina.uninafoodlab.model.CategoriaCorso;
import javafx.scene.control.*;

/**
 * Utility class per le validazioni dei form
 */
public class ValidationUtils {

    /**
     * Valida il form del corso
     */
    public static ValidationResult validaFormCorso(TextField txtTitolo, ComboBox<CategoriaCorso> cmbCategoria,
            DatePicker dateInizio, TextField txtDurata,
            TextField txtMaxPartecipanti, TextField txtNumeroSessioni,
            TextField txtPrezzo) {

        if (txtTitolo.getText().trim().isEmpty()) {
            return ValidationResult.error("Il titolo del corso è obbligatorio");
        }

        if (cmbCategoria.getValue() == null) {
            return ValidationResult.error("Seleziona una categoria per il corso");
        }

        if (dateInizio.getValue() == null) {
            return ValidationResult.error("Seleziona la data di inizio del corso");
        }

        try {
            int durata = Integer.parseInt(txtDurata.getText().trim());
            if (durata <= 0) {
                return ValidationResult.error("La durata deve essere maggiore di 0");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Inserisci un valore valido per la durata");
        }

        try {
            int partecipanti = Integer.parseInt(txtMaxPartecipanti.getText().trim());
            if (partecipanti <= 0 || partecipanti > 20) {
                return ValidationResult.error("Il numero di partecipanti deve essere tra 1 e 20");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Inserisci un valore valido per i partecipanti");
        }

        try {
            int sessioni = Integer.parseInt(txtNumeroSessioni.getText().trim());
            if (sessioni <= 0 || sessioni > 20) {
                return ValidationResult.error("Il numero di sessioni deve essere tra 1 e 20");
            }
        } catch (NumberFormatException e) {
            return ValidationResult.error("Inserisci un valore valido per il numero di sessioni");
        }

        if (!txtPrezzo.getText().trim().isEmpty()) {
            try {
                double prezzo = Double.parseDouble(txtPrezzo.getText().trim());
                if (prezzo < 0) {
                    return ValidationResult.error("Il prezzo non può essere negativo");
                }
            } catch (NumberFormatException e) {
                return ValidationResult.error("Inserisci un valore valido per il prezzo");
            }
        }

        return ValidationResult.success();
    }

    /**
     * Risultato di una validazione
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult success() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult error(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
