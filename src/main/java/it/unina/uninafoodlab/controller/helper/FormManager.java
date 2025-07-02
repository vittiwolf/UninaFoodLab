package it.unina.uninafoodlab.controller.helper;

import it.unina.uninafoodlab.model.*;
import it.unina.uninafoodlab.service.UninaFoodLabService;
import it.unina.uninafoodlab.controller.helper.ValidationUtils.ValidationResult;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Manager per la gestione dei form di creazione/modifica corsi
 */
public class FormManager {
    private static final Logger logger = LoggerFactory.getLogger(FormManager.class);
    
    private final UninaFoodLabService service;
    private final MessageHelper messageHelper;
    
    public FormManager(UninaFoodLabService service, MessageHelper messageHelper) {
        this.service = service;
        this.messageHelper = messageHelper;
    }
    
    // ==================== CONFIGURAZIONE FORM ====================
    
    /**
     * Configura le ComboBox del form
     */
    public void configuraComboBox(ComboBox<String> cmbFrequenza, 
                                ComboBox<CategoriaCorso> cmbCategoriaCorso,
                                ComboBox<CategoriaCorso> cmbFiltraCategoria) {
        // Configurazione ComboBox frequenza
        cmbFrequenza.setItems(FXCollections.observableArrayList(
            "SETTIMANALE", "BISETTIMANALE", "MENSILE"));
        
        // Carica categorie corsi
        caricaCategorie(cmbCategoriaCorso, cmbFiltraCategoria);
    }
    
    /**
     * Carica le categorie nelle ComboBox
     */
    public void caricaCategorie(ComboBox<CategoriaCorso> cmbCategoriaCorso,
                              ComboBox<CategoriaCorso> cmbFiltraCategoria) {
        try {
            List<CategoriaCorso> categorie = service.getAllCategorie();
            cmbCategoriaCorso.setItems(FXCollections.observableArrayList(categorie));
            cmbFiltraCategoria.setItems(FXCollections.observableArrayList(categorie));
            
            // Aggiungi opzione "Tutte" per il filtro
            CategoriaCorso tutte = new CategoriaCorso();
            tutte.setNome("Tutte le categorie");
            cmbFiltraCategoria.getItems().add(0, tutte);
            cmbFiltraCategoria.setValue(tutte);
            
        } catch (Exception e) {
            logger.error("Errore nel caricamento delle categorie", e);
            messageHelper.mostraErrore("Errore nel caricamento delle categorie: " + e.getMessage());
        }
    }
    
    // ==================== GESTIONE FORM CORSI ====================
    
    /**
     * Valida il form del corso
     */
    public boolean validaFormCorso(TextField txtTitolo, ComboBox<CategoriaCorso> cmbCategoria,
                                 DatePicker dateInizio, TextField txtDurata,
                                 TextField txtMaxPartecipanti, TextField txtNumeroSessioni,
                                 TextField txtPrezzo) {
        
        ValidationResult result = ValidationUtils.validaFormCorso(
            txtTitolo, cmbCategoria, dateInizio, txtDurata,
            txtMaxPartecipanti, txtNumeroSessioni, txtPrezzo);
        
        if (!result.isValid()) {
            messageHelper.mostraErrore(result.getErrorMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Popola il form con i dati di un corso esistente
     */
    public void popolaFormCorso(Corso corso, TextField txtTitolo, TextArea txtDescrizione,
                              TextField txtDurata, TextField txtMaxPartecipanti,
                              TextField txtNumeroSessioni, TextField txtPrezzo,
                              ComboBox<String> cmbFrequenza, DatePicker dateInizio,
                              ComboBox<CategoriaCorso> cmbCategoria) {
        
        txtTitolo.setText(corso.getTitolo() != null ? corso.getTitolo() : "");
        txtDescrizione.setText(corso.getDescrizione() != null ? corso.getDescrizione() : "");
        txtDurata.setText(corso.getDurata() != null ? String.valueOf(corso.getDurata()) : "");
        txtMaxPartecipanti.setText(corso.getMaxPartecipanti() != null ? String.valueOf(corso.getMaxPartecipanti()) : "");
        txtNumeroSessioni.setText(corso.getNumeroSessioni() != null ? String.valueOf(corso.getNumeroSessioni()) : "");
        
        if (corso.getPrezzo() != null) {
            txtPrezzo.setText(corso.getPrezzo().toString());
        } else {
            txtPrezzo.clear();
        }
        
        cmbFrequenza.setValue(corso.getFrequenza());
        dateInizio.setValue(corso.getDataInizio());
        
        // Imposta la categoria nel ComboBox
        if (corso.getCategoriaId() != null) {
            for (CategoriaCorso cat : cmbCategoria.getItems()) {
                if (cat.getId().equals(corso.getCategoriaId())) {
                    cmbCategoria.setValue(cat);
                    break;
                }
            }
        }
    }
    
    /**
     * Pulisce tutti i campi del form
     */
    public void pulisciFormCorso(TextField txtTitolo, TextArea txtDescrizione,
                               TextField txtDurata, TextField txtMaxPartecipanti,
                               TextField txtNumeroSessioni, TextField txtPrezzo,
                               ComboBox<CategoriaCorso> cmbCategoria,
                               ComboBox<String> cmbFrequenza, DatePicker dateInizio) {
        
        txtTitolo.clear();
        txtDescrizione.clear();
        txtDurata.clear();
        txtMaxPartecipanti.clear();
        txtNumeroSessioni.clear();
        txtPrezzo.clear();
        cmbCategoria.setValue(null);
        cmbFrequenza.setValue(null);
        dateInizio.setValue(null);
    }
    
    /**
     * Crea un corso dai dati del form
     */
    public Corso creaCorsoFromForm(Chef chefLoggato, TextField txtTitolo, TextArea txtDescrizione,
                                 TextField txtDurata, TextField txtMaxPartecipanti,
                                 TextField txtNumeroSessioni, TextField txtPrezzo,
                                 ComboBox<String> cmbFrequenza, DatePicker dateInizio,
                                 ComboBox<CategoriaCorso> cmbCategoria) {
        
        try {
            Corso corso = new Corso();
            corso.setTitolo(txtTitolo.getText().trim());
            corso.setDescrizione(txtDescrizione.getText().trim());
            corso.setCategoriaId(cmbCategoria.getValue().getId());
            corso.setChefId(chefLoggato.getId());
            
            // Parsing campi numerici
            corso.setDurata(Integer.parseInt(txtDurata.getText().trim()));
            corso.setMaxPartecipanti(Integer.parseInt(txtMaxPartecipanti.getText().trim()));
            corso.setNumeroSessioni(Integer.parseInt(txtNumeroSessioni.getText().trim()));
            
            if (!txtPrezzo.getText().trim().isEmpty()) {
                corso.setPrezzo(new BigDecimal(txtPrezzo.getText().trim()));
            }
            
            if (cmbFrequenza.getValue() != null) {
                corso.setFrequenza(cmbFrequenza.getValue());
            }
            
            corso.setDataInizio(dateInizio.getValue());
            corso.setStato("ATTIVO");
            
            return corso;
            
        } catch (NumberFormatException e) {
            messageHelper.mostraErrore("Verifica che i campi numerici contengano valori validi");
            return null;
        }
    }
}
