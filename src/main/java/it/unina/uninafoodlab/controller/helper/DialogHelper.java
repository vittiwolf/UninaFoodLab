package it.unina.uninafoodlab.controller.helper;

import it.unina.uninafoodlab.model.*;
import it.unina.uninafoodlab.service.UninaFoodLabService;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Helper class per la gestione dei dialog nel MainController
 */
public class DialogHelper {
    private static final Logger logger = LoggerFactory.getLogger(DialogHelper.class);
    
    private final UninaFoodLabService service;
    private final MessageHelper messageHelper;
    
    public DialogHelper(UninaFoodLabService service, MessageHelper messageHelper) {
        this.service = service;
        this.messageHelper = messageHelper;
    }
    
    // ==================== HELPER METHODS ====================
    
    /**
     * Crea mappa delle etichette per la frequenza
     */
    private Map<String, String> getFrequencyLabels() {
        Map<String, String> labels = new HashMap<>();
        labels.put("Settimanale", "settimanale");
        labels.put("Ogni 2 giorni", "ogni_due_giorni");
        labels.put("Giornaliero", "giornaliero");
        return labels;
    }
    
    /**
     * Trova la chiave (etichetta) dal valore della frequenza
     */
    private String getLabelFromValue(String value) {
        Map<String, String> labels = getFrequencyLabels();
        return labels.entrySet().stream()
                .filter(entry -> entry.getValue().equals(value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(value);
    }
    
    // ==================== DIALOGS PER SESSIONI ====================
    
    /**
     * Valida che la data della sessione non sia precedente alla data di inizio del corso
     */
    private boolean isValidDataSessione(LocalDate dataSessione, Corso corso) {
        if (dataSessione == null || corso == null || corso.getDataInizio() == null) {
            return false;
        }
        return !dataSessione.isBefore(corso.getDataInizio());
    }
    
    public void mostraDialogNuovaSessione(Corso corso, Runnable onSuccess) {
        Dialog<Sessione> dialog = new Dialog<>();
        dialog.setTitle("Nuova Sessione");
        dialog.setHeaderText("Crea una nuova sessione per il corso: " + corso.getTitolo());

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Crea", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = createSessionFormGrid();
        
        TextField txtTitolo = new TextField();
        txtTitolo.setPromptText("Titolo della sessione");
        TextArea txtDescrizione = new TextArea();
        txtDescrizione.setPromptText("Descrizione della sessione");
        txtDescrizione.setPrefRowCount(3);
        
        DatePicker dateSessione = new DatePicker(LocalDate.now().plusDays(1));
        
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("online", "presenza");
        cmbTipo.setValue("online");
        
        TextField txtDurata = new TextField("120");
        txtDurata.setPromptText("Durata in minuti");
        
        // Calcola il numero di sessione automaticamente
        List<Sessione> sessioni = service.getSessioniCorso(corso.getId());
        int numeroSessione = sessioni.size() + 1;
        Label lblNumeroSessione = new Label("Sessione #" + numeroSessione);

        addSessionFormFields(grid, lblNumeroSessione, txtTitolo, txtDescrizione, 
                           dateSessione, cmbTipo, txtDurata);

        dialog.getDialogPane().setContent(grid);

        // Abilita/disabilita il bottone Crea
        Node creaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        creaButton.setDisable(true);

        // Validazione in tempo reale
        txtTitolo.textProperty().addListener((observable, oldValue, newValue) -> {
            creaButton.setDisable(newValue.trim().isEmpty() || !isValidDataSessione(dateSessione.getValue(), corso));
        });
        
        // Validazione data sessione
        dateSessione.valueProperty().addListener((observable, oldValue, newValue) -> {
            creaButton.setDisable(txtTitolo.getText().trim().isEmpty() || !isValidDataSessione(newValue, corso));
        });

        // Converte il risultato in una Sessione quando Crea è premuto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                try {
                    LocalDate dataSessione = dateSessione.getValue();
                    
                    // Validazione della data
                    if (!isValidDataSessione(dataSessione, corso)) {
                        messageHelper.mostraErrore("La data della sessione non può essere precedente alla data di inizio del corso (" + 
                                                  corso.getDataInizio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
                        return null;
                    }
                    
                    Sessione sessione = new Sessione();
                    sessione.setCorsoId(corso.getId());
                    sessione.setNumeroSessione(numeroSessione);
                    sessione.setTitolo(txtTitolo.getText().trim());
                    sessione.setDescrizione(txtDescrizione.getText().trim());
                    sessione.setDataSessione(dataSessione);
                    sessione.setTipo(cmbTipo.getValue());
                    
                    int durata = Integer.parseInt(txtDurata.getText().trim());
                    sessione.setDurataMinuti(durata);
                    
                    return sessione;
                } catch (NumberFormatException e) {
                    messageHelper.mostraErrore("Inserisci un valore valido per la durata");
                    return null;
                }
            }
            return null;
        });

        Optional<Sessione> result = dialog.showAndWait();
        result.ifPresent(sessione -> {
            boolean successo = service.creaSessione(sessione);
            if (successo) {
                messageHelper.mostraSuccesso("Successo", "Sessione creata con successo!");
                onSuccess.run();
                logger.info("Nuova sessione creata: {}", sessione.getTitolo());
            } else {
                messageHelper.mostraErrore("Errore nella creazione della sessione");
            }
        });
    }

    public void mostraDialogModificaSessione(Sessione sessione, Runnable onSuccess) {
        Dialog<Sessione> dialog = new Dialog<>();
        dialog.setTitle("Modifica Sessione");
        dialog.setHeaderText("Modifica sessione: " + sessione.getTitolo());

        // Ottieni il corso per la validazione della data
        Corso corso = service.getCorsoById(sessione.getCorsoId());

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = createSessionFormGrid();

        TextField txtTitolo = new TextField(sessione.getTitolo());
        TextArea txtDescrizione = new TextArea(sessione.getDescrizione() != null ? sessione.getDescrizione() : "");
        txtDescrizione.setPrefRowCount(3);
        
        DatePicker dateSessione = new DatePicker(sessione.getDataSessione());
        
        ComboBox<String> cmbTipo = new ComboBox<>();
        cmbTipo.getItems().addAll("online", "presenza");
        cmbTipo.setValue(sessione.getTipo());
        
        TextField txtDurata = new TextField(String.valueOf(sessione.getDurataMinuti() != null ? sessione.getDurataMinuti() : 120));
        
        Label lblNumeroSessione = new Label("Sessione #" + sessione.getNumeroSessione());

        addSessionFormFields(grid, lblNumeroSessione, txtTitolo, txtDescrizione, 
                           dateSessione, cmbTipo, txtDurata);

        dialog.getDialogPane().setContent(grid);

        // Abilita/disabilita il bottone Salva
        Node salvaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        salvaButton.setDisable(txtTitolo.getText().trim().isEmpty());

        // Validazione in tempo reale
        txtTitolo.textProperty().addListener((observable, oldValue, newValue) -> {
            salvaButton.setDisable(newValue.trim().isEmpty() || 
                (corso != null && !isValidDataSessione(dateSessione.getValue(), corso)));
        });
        
        // Validazione data sessione
        dateSessione.valueProperty().addListener((observable, oldValue, newValue) -> {
            salvaButton.setDisable(txtTitolo.getText().trim().isEmpty() || 
                (corso != null && !isValidDataSessione(newValue, corso)));
        });

        // Converte il risultato in una Sessione quando Salva è premuto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                try {
                    LocalDate dataSessione = dateSessione.getValue();
                    
                    // Validazione della data
                    if (corso != null && !isValidDataSessione(dataSessione, corso)) {
                        messageHelper.mostraErrore("La data della sessione non può essere precedente alla data di inizio del corso (" + 
                                                  corso.getDataInizio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")");
                        return null;
                    }
                    
                    sessione.setTitolo(txtTitolo.getText().trim());
                    sessione.setDescrizione(txtDescrizione.getText().trim());
                    sessione.setDataSessione(dataSessione);
                    sessione.setTipo(cmbTipo.getValue());
                    
                    int durata = Integer.parseInt(txtDurata.getText().trim());
                    sessione.setDurataMinuti(durata);
                    
                    return sessione;
                } catch (NumberFormatException e) {
                    messageHelper.mostraErrore("Inserisci un valore valido per la durata");
                    return null;
                }
            }
            return null;
        });

        Optional<Sessione> result = dialog.showAndWait();
        result.ifPresent(sessioneModificata -> {
            boolean successo = service.aggiornaSessione(sessioneModificata);
            if (successo) {
                messageHelper.mostraSuccesso("Successo", "Sessione aggiornata con successo!");
                onSuccess.run();
                logger.info("Sessione aggiornata: {}", sessioneModificata.getTitolo());
            } else {
                messageHelper.mostraErrore("Errore nell'aggiornamento della sessione");
            }
        });
    }
    
    // ==================== DIALOGS PER RICETTE ====================
    
    public void mostraDialogNuovaRicetta(Chef chefLoggato, Runnable onSuccess) {
        Dialog<Ricetta> dialog = new Dialog<>();
        dialog.setTitle("Nuova Ricetta");
        dialog.setHeaderText("Crea una nuova ricetta");

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Crea", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = createRecipeFormGrid();

        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome della ricetta");
        
        TextArea txtDescrizione = new TextArea();
        txtDescrizione.setPromptText("Descrizione della ricetta");
        txtDescrizione.setPrefRowCount(3);
        
        ComboBox<Integer> cmbDifficolta = new ComboBox<>();
        cmbDifficolta.getItems().addAll(1, 2, 3, 4, 5);
        cmbDifficolta.setValue(1);
        
        TextField txtTempoPreparazione = new TextField("30");
        txtTempoPreparazione.setPromptText("Tempo in minuti");
        
        TextField txtNumeroPortions = new TextField("4");
        txtNumeroPortions.setPromptText("Numero di porzioni");
        
        TextArea txtIstruzioni = new TextArea();
        txtIstruzioni.setPromptText("Istruzioni di preparazione");
        txtIstruzioni.setPrefRowCount(4);

        addRecipeFormFields(grid, txtNome, txtDescrizione, cmbDifficolta, 
                          txtTempoPreparazione, txtNumeroPortions, txtIstruzioni);

        dialog.getDialogPane().setContent(grid);

        // Abilita/disabilita il bottone Crea
        Node creaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        creaButton.setDisable(true);

        // Validazione in tempo reale
        setupRecipeValidation(txtNome, txtDescrizione, creaButton);

        // Converte il risultato in una Ricetta quando Crea è premuto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                return createRecipeFromForm(chefLoggato, txtNome, txtDescrizione, cmbDifficolta,
                                          txtTempoPreparazione, txtNumeroPortions, txtIstruzioni);
            }
            return null;
        });

        Optional<Ricetta> result = dialog.showAndWait();
        result.ifPresent(ricetta -> {
            boolean successo = service.creaRicetta(ricetta);
            if (successo) {
                messageHelper.mostraSuccesso("Successo", "Ricetta creata con successo!");
                onSuccess.run();
                logger.info("Nuova ricetta creata: {}", ricetta.getNome());
            } else {
                messageHelper.mostraErrore("Errore nella creazione della ricetta");
            }
        });
    }

    public void mostraDialogModificaRicetta(Ricetta ricetta, Runnable onSuccess) {
        Dialog<Ricetta> dialog = new Dialog<>();
        dialog.setTitle("Modifica Ricetta");
        dialog.setHeaderText("Modifica ricetta: " + ricetta.getNome());

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = createRecipeFormGrid();

        TextField txtNome = new TextField(ricetta.getNome());
        
        TextArea txtDescrizione = new TextArea(ricetta.getDescrizione() != null ? ricetta.getDescrizione() : "");
        txtDescrizione.setPrefRowCount(3);
        
        ComboBox<Integer> cmbDifficolta = new ComboBox<>();
        cmbDifficolta.getItems().addAll(1, 2, 3, 4, 5);
        cmbDifficolta.setValue(ricetta.getDifficolta() != null ? ricetta.getDifficolta() : 1);
        
        TextField txtTempoPreparazione = new TextField(String.valueOf(ricetta.getTempoPreparazione() != null ? ricetta.getTempoPreparazione() : 30));
        
        TextField txtNumeroPortions = new TextField(String.valueOf(ricetta.getNumeroPortions() != null ? ricetta.getNumeroPortions() : 4));
        
        TextArea txtIstruzioni = new TextArea(ricetta.getIstruzioni() != null ? ricetta.getIstruzioni() : "");
        txtIstruzioni.setPrefRowCount(4);

        addRecipeFormFields(grid, txtNome, txtDescrizione, cmbDifficolta, 
                          txtTempoPreparazione, txtNumeroPortions, txtIstruzioni);

        dialog.getDialogPane().setContent(grid);

        // Abilita/disabilita il bottone Salva
        Node salvaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        salvaButton.setDisable(txtNome.getText().trim().isEmpty() || txtDescrizione.getText().trim().isEmpty());

        // Validazione in tempo reale
        setupRecipeValidation(txtNome, txtDescrizione, salvaButton);

        // Converte il risultato in una Ricetta quando Salva è premuto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                return updateRecipeFromForm(ricetta, txtNome, txtDescrizione, cmbDifficolta,
                                          txtTempoPreparazione, txtNumeroPortions, txtIstruzioni);
            }
            return null;
        });

        Optional<Ricetta> result = dialog.showAndWait();
        result.ifPresent(ricettaModificata -> {
            boolean successo = service.aggiornaRicetta(ricettaModificata);
            if (successo) {
                messageHelper.mostraSuccesso("Successo", "Ricetta aggiornata con successo!");
                onSuccess.run();
                logger.info("Ricetta aggiornata: {}", ricettaModificata.getNome());
            } else {
                messageHelper.mostraErrore("Errore nell'aggiornamento della ricetta");
            }
        });
    }
    
    // ==================== DIALOG PER ASSOCIAZIONE RICETTE ====================
    
    public void mostraDialogAssociazioneRicetta(Sessione sessione, ObservableList<Ricetta> listaRicette, Runnable onSuccessRefresh) {
        // Crea un dialog personalizzato per selezionare le ricette
        Dialog<List<Integer>> dialog = new Dialog<>();
        dialog.setTitle("Associa Ricette alla Sessione");
        dialog.setHeaderText("Seleziona le ricette da associare alla sessione pratica: " + sessione.getTitolo());
        
        // Pulsanti
        ButtonType associaButtonType = new ButtonType("Associa", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(associaButtonType, ButtonType.CANCEL);
        
        // Contenuto del dialog
        VBox content = new VBox(10);
        content.setPrefWidth(500);
        
        Label istruzioni = new Label("Seleziona una o più ricette da associare alla sessione:");
        istruzioni.setStyle("-fx-font-weight: bold;");
        
        // ListView per selezione multipla delle ricette
        ListView<Ricetta> listViewRicette = new ListView<>();
        listViewRicette.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewRicette.setItems(listaRicette);
        listViewRicette.setPrefHeight(300);
        
        // Custom cell factory per mostrare nome e difficoltà
        listViewRicette.setCellFactory(listView -> new ListCell<Ricetta>() {
            @Override
            protected void updateItem(Ricetta ricetta, boolean empty) {
                super.updateItem(ricetta, empty);
                if (empty || ricetta == null) {
                    setText(null);
                } else {
                    setText(ricetta.getNome() + " (Difficoltà: " + ricetta.getDifficolta() + "/5)");
                }
            }
        });
        
        content.getChildren().addAll(istruzioni, listViewRicette);
        dialog.getDialogPane().setContent(content);
        
        // Converter per il risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == associaButtonType) {
                return listViewRicette.getSelectionModel().getSelectedItems()
                    .stream()
                    .map(Ricetta::getId)
                    .toList();
            }
            return null;
        });
        
        // Mostra il dialog e processa il risultato
        Optional<List<Integer>> result = dialog.showAndWait();
        result.ifPresent(ricetteIds -> {
            if (!ricetteIds.isEmpty()) {
                int create = associaRicetteASessione(sessione.getId(), ricetteIds);
                if (create > 0 && onSuccessRefresh != null) {
                    // Aggiorna la tabella sessioni dopo l'associazione
                    javafx.application.Platform.runLater(onSuccessRefresh);
                }
            } else {
                messageHelper.mostraAvviso("Attenzione", "Nessuna ricetta selezionata");
            }
        });
    }
    
    // ==================== DIALOGS PER UTENTI ====================
    
    /**
     * Mostra dialog per creare un nuovo utente
     */
    public void mostraDialogNuovoUtente(Runnable onSuccess) {
        Dialog<Utente> dialog = new Dialog<>();
        dialog.setTitle("Nuovo Utente");
        dialog.setHeaderText("Registra un nuovo partecipante ai corsi");

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Crea", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome");
        TextField txtCognome = new TextField();
        txtCognome.setPromptText("Cognome");
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Email");
        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Telefono");
        DatePicker dpDataNascita = new DatePicker();
        
        ComboBox<String> cmbLivelloEsperienza = new ComboBox<>();
        cmbLivelloEsperienza.getItems().addAll("PRINCIPIANTE", "INTERMEDIO", "AVANZATO");
        cmbLivelloEsperienza.setValue("PRINCIPIANTE");

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Cognome:"), 0, 1);
        grid.add(txtCognome, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Telefono:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(new Label("Data di Nascita:"), 0, 4);
        grid.add(dpDataNascita, 1, 4);
        grid.add(new Label("Livello Esperienza:"), 0, 5);
        grid.add(cmbLivelloEsperienza, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Validazione input
        Node confermaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        confermaButton.setDisable(true);

        txtNome.textProperty().addListener((observable, oldValue, newValue) -> 
            confermaButton.setDisable(newValue.trim().isEmpty() || txtCognome.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()));
        txtCognome.textProperty().addListener((observable, oldValue, newValue) -> 
            confermaButton.setDisable(newValue.trim().isEmpty() || txtNome.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()));
        txtEmail.textProperty().addListener((observable, oldValue, newValue) -> 
            confermaButton.setDisable(newValue.trim().isEmpty() || txtNome.getText().trim().isEmpty() || txtCognome.getText().trim().isEmpty()));

        // Converte il risultato quando viene premuto il bottone OK
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                Utente utente = new Utente();
                utente.setNome(txtNome.getText().trim());
                utente.setCognome(txtCognome.getText().trim());
                utente.setEmail(txtEmail.getText().trim());
                utente.setTelefono(txtTelefono.getText().trim());
                utente.setDataNascita(dpDataNascita.getValue());
                utente.setLivelloEsperienza(cmbLivelloEsperienza.getValue());
                return utente;
            }
            return null;        });

        // Mostra il dialog e gestisce il risultato
        Optional<Utente> result = dialog.showAndWait();
        result.ifPresent(utente -> {
            try {
                // Usa il service per la gestione utenti
                boolean success = service.creaUtente(utente);
                if (success) {
                    messageHelper.mostraSuccesso("Successo", "Utente creato con successo!");
                    onSuccess.run();
                } else {
                    messageHelper.mostraErrore("Errore nella creazione dell'utente");
                }
            } catch (Exception e) {
                messageHelper.mostraErrore("Errore nella creazione dell'utente: " + e.getMessage());
            }
        });
    }
    
    /**
     * Mostra dialog per modificare un utente esistente
     */
    public void mostraDialogModificaUtente(Utente utente, Runnable onSuccess) {
        Dialog<Utente> dialog = new Dialog<>();
        dialog.setTitle("Modifica Utente");
        dialog.setHeaderText("Modifica i dati dell'utente: " + utente.getNomeCompleto());

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField txtNome = new TextField(utente.getNome());
        TextField txtCognome = new TextField(utente.getCognome());
        TextField txtEmail = new TextField(utente.getEmail());
        TextField txtTelefono = new TextField(utente.getTelefono());
        DatePicker dpDataNascita = new DatePicker(utente.getDataNascita());
        
        ComboBox<String> cmbLivelloEsperienza = new ComboBox<>();
        cmbLivelloEsperienza.getItems().addAll("PRINCIPIANTE", "INTERMEDIO", "AVANZATO");
        cmbLivelloEsperienza.setValue(utente.getLivelloEsperienza());

        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Cognome:"), 0, 1);
        grid.add(txtCognome, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Telefono:"), 0, 3);
        grid.add(txtTelefono, 1, 3);
        grid.add(new Label("Data di Nascita:"), 0, 4);
        grid.add(dpDataNascita, 1, 4);
        grid.add(new Label("Livello Esperienza:"), 0, 5);
        grid.add(cmbLivelloEsperienza, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Converte il risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                utente.setNome(txtNome.getText().trim());
                utente.setCognome(txtCognome.getText().trim());
                utente.setEmail(txtEmail.getText().trim());
                utente.setTelefono(txtTelefono.getText().trim());
                utente.setDataNascita(dpDataNascita.getValue());
                utente.setLivelloEsperienza(cmbLivelloEsperienza.getValue());
                return utente;
            }
            return null;
        });

        // Mostra il dialog e gestisce il risultato
        Optional<Utente> result = dialog.showAndWait();        result.ifPresent(utenteModificato -> {
            try {
                boolean success = service.aggiornaUtente(utenteModificato);
                if (success) {
                    messageHelper.mostraSuccesso("Successo", "Utente aggiornato con successo!");
                    onSuccess.run();
                } else {
                    messageHelper.mostraErrore("Errore nell'aggiornamento dell'utente");
                }
            } catch (Exception e) {
                messageHelper.mostraErrore("Errore nell'aggiornamento dell'utente: " + e.getMessage());
            }
        });
    }
    
    // ==================== DIALOGS PER ISCRIZIONI ====================
    
    /**
     * Mostra dialog per iscrivere un utente a un corso
     */
    public void mostraDialogNuovaIscrizione(List<Utente> utenti, List<Corso> corsi, Runnable onSuccess) {
        Dialog<Iscrizione> dialog = new Dialog<>();
        dialog.setTitle("Nuova Iscrizione");
        dialog.setHeaderText("Iscrive un utente a un corso");

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Iscrive", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        ComboBox<Utente> cmbUtente = new ComboBox<>();
        cmbUtente.getItems().addAll(utenti);
        cmbUtente.setConverter(new javafx.util.StringConverter<Utente>() {
            @Override
            public String toString(Utente utente) {
                return utente != null ? utente.getNomeCompleto() + " (" + utente.getEmail() + ")" : "";
            }
            @Override
            public Utente fromString(String string) {
                return null;
            }
        });
        
        ComboBox<Corso> cmbCorso = new ComboBox<>();
        cmbCorso.getItems().addAll(corsi);
        cmbCorso.setConverter(new javafx.util.StringConverter<Corso>() {
            @Override
            public String toString(Corso corso) {
                return corso != null ? corso.getTitolo() : "";
            }
            @Override
            public Corso fromString(String string) {
                return null;
            }
        });
        
        TextArea txtNote = new TextArea();
        txtNote.setPromptText("Note aggiuntive (opzionale)");
        txtNote.setPrefRowCount(3);

        grid.add(new Label("Utente:"), 0, 0);
        grid.add(cmbUtente, 1, 0);
        grid.add(new Label("Corso:"), 0, 1);
        grid.add(cmbCorso, 1, 1);
        grid.add(new Label("Note:"), 0, 2);
        grid.add(txtNote, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // Validazione input
        Node confermaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        confermaButton.setDisable(true);

        cmbUtente.valueProperty().addListener((observable, oldValue, newValue) -> 
            confermaButton.setDisable(newValue == null || cmbCorso.getValue() == null));
        cmbCorso.valueProperty().addListener((observable, oldValue, newValue) -> 
            confermaButton.setDisable(newValue == null || cmbUtente.getValue() == null));

        // Converte il risultato
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                Iscrizione iscrizione = new Iscrizione();
                iscrizione.setUtenteId(cmbUtente.getValue().getId());
                iscrizione.setCorsoId(cmbCorso.getValue().getId());
                iscrizione.setNote(txtNote.getText().trim());
                return iscrizione;
            }
            return null;
        });

        // Mostra il dialog e gestisce il risultato
        Optional<Iscrizione> result = dialog.showAndWait();        result.ifPresent(iscrizione -> {
            try {
                boolean success = service.iscriviUtenteACorso(iscrizione.getUtenteId(), iscrizione.getCorsoId(), iscrizione.getNote());
                if (success) {
                    messageHelper.mostraSuccesso("Successo", "Iscrizione creata con successo!");
                    onSuccess.run();
                } else {
                    messageHelper.mostraErrore("Errore nella creazione dell'iscrizione");
                }
            } catch (Exception e) {
                messageHelper.mostraErrore("Errore nella creazione dell'iscrizione: " + e.getMessage());
            }        });
    }
    
    // ==================== DIALOGS PER CORSI ====================
    
    /**
     * Mostra dialog per creare un nuovo corso
     */
    public void mostraDialogNuovoCorso(Chef chefLoggato, Runnable onSuccess) {
        Dialog<Corso> dialog = new Dialog<>();
        dialog.setTitle("Nuovo Corso");
        dialog.setHeaderText("Crea un nuovo corso di cucina");

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Crea", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField txtTitolo = new TextField();
        txtTitolo.setPromptText("Titolo del corso");
        
        TextArea txtDescrizione = new TextArea();
        txtDescrizione.setPromptText("Descrizione del corso");
        txtDescrizione.setPrefRowCount(3);
          ComboBox<CategoriaCorso> cmbCategoria = new ComboBox<>();
        try {
            List<CategoriaCorso> categorie = service.getCategorieCorsi();
            cmbCategoria.getItems().addAll(categorie);
            if (!categorie.isEmpty()) {
                cmbCategoria.setValue(categorie.get(0));
            }
        } catch (Exception e) {
            logger.error("Errore nel caricamento categorie", e);
            messageHelper.mostraErrore("Errore nel caricamento delle categorie: " + e.getMessage());
        }
        
        TextField txtDurataOre = new TextField();
        txtDurataOre.setPromptText("Durata in ore (1-8)");
        
        TextField txtMaxPartecipanti = new TextField();
        txtMaxPartecipanti.setPromptText("Max partecipanti (1-50)");
        
        TextField txtPrezzo = new TextField();
        txtPrezzo.setPromptText("Prezzo del corso");
        
        ComboBox<String> cmbFrequenza = new ComboBox<>();
        Map<String, String> frequencyLabels = getFrequencyLabels();
        cmbFrequenza.getItems().addAll(frequencyLabels.keySet());
        cmbFrequenza.setValue("Settimanale");
        
        TextField txtNumeroSessioni = new TextField();
        txtNumeroSessioni.setPromptText("Numero di sessioni");
        
        DatePicker dateInizio = new DatePicker(LocalDate.now().plusDays(7));
        
        // Aggiungi i controlli al grid
        grid.add(new Label("Titolo:"), 0, 0);
        grid.add(txtTitolo, 1, 0);
        grid.add(new Label("Descrizione:"), 0, 1);
        grid.add(txtDescrizione, 1, 1);
        grid.add(new Label("Categoria:"), 0, 2);
        grid.add(cmbCategoria, 1, 2);
        grid.add(new Label("Durata (ore):"), 0, 3);
        grid.add(txtDurataOre, 1, 3);
        grid.add(new Label("Max Partecipanti:"), 0, 4);
        grid.add(txtMaxPartecipanti, 1, 4);
        grid.add(new Label("Prezzo (€):"), 0, 5);
        grid.add(txtPrezzo, 1, 5);
        grid.add(new Label("Frequenza:"), 0, 6);
        grid.add(cmbFrequenza, 1, 6);
        grid.add(new Label("Numero Sessioni:"), 0, 7);
        grid.add(txtNumeroSessioni, 1, 7);
        grid.add(new Label("Data Inizio:"), 0, 8);
        grid.add(dateInizio, 1, 8);

        dialog.getDialogPane().setContent(grid);

        // Abilita/disabilita il bottone Crea
        Node creaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        creaButton.setDisable(true);

        // Validazione in tempo reale
        txtTitolo.textProperty().addListener((observable, oldValue, newValue) -> {
            creaButton.setDisable(newValue.trim().isEmpty());
        });

        // Converte il risultato in un Corso quando Crea è premuto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                try {
                    Corso corso = new Corso();
                    corso.setChefId(chefLoggato.getId());
                    corso.setTitolo(txtTitolo.getText().trim());
                    corso.setDescrizione(txtDescrizione.getText().trim());
                    corso.setCategoriaId(cmbCategoria.getValue().getId());
                    int durataOre = Integer.parseInt(txtDurataOre.getText().trim());
                    if (durataOre < 1 || durataOre > 8) {
                        messageHelper.mostraErrore("La durata del corso deve essere compresa tra 1 e 8 ore");
                        return null;
                    }
                    corso.setDurata(durataOre);
                    
                    int maxPartecipanti = Integer.parseInt(txtMaxPartecipanti.getText().trim());
                    if (maxPartecipanti < 1 || maxPartecipanti > 50) {
                        messageHelper.mostraErrore("Il numero massimo di partecipanti deve essere compreso tra 1 e 50");
                        return null;
                    }
                    corso.setMaxPartecipanti(maxPartecipanti);
                    
                    BigDecimal prezzo = new BigDecimal(txtPrezzo.getText().trim());
                    corso.setPrezzo(prezzo);
                    
                    corso.setFrequenza(frequencyLabels.get(cmbFrequenza.getValue()));
                    
                    int numero_sessioni = Integer.parseInt(txtNumeroSessioni.getText().trim());
                    corso.setNumeroSessioni(numero_sessioni);
                    
                    corso.setDataInizio(dateInizio.getValue());
                    corso.setStato("programmato");
                    
                    return corso;
                } catch (NumberFormatException e) {
                    messageHelper.mostraErrore("Inserisci valori numerici validi per durata, partecipanti, prezzo e numero sessioni");
                    return null;
                }
            }
            return null;
        });

        // Mostra il dialog e gestisce il risultato
        Optional<Corso> result = dialog.showAndWait();
        result.ifPresent(corso -> {
            boolean successo = service.creaCorso(corso);
            if (successo) {
                messageHelper.mostraSuccesso("Successo", "Corso creato con successo!");
                onSuccess.run();
                logger.info("Nuovo corso creato: {}", corso.getTitolo());
            } else {
                messageHelper.mostraErrore("Errore nella creazione del corso");
            }
        });
    }
    
    /**
     * Mostra dialog per modificare un corso esistente
     */
    public void mostraDialogModificaCorso(Corso corso, Runnable onSuccess) {
        Dialog<Corso> dialog = new Dialog<>();
        dialog.setTitle("Modifica Corso");
        dialog.setHeaderText("Modifica corso: " + corso.getTitolo());

        // Configura i bottoni
        ButtonType confermaButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confermaButtonType, ButtonType.CANCEL);

        // Crea i controlli del form pre-popolati con i dati esistenti
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField txtTitolo = new TextField(corso.getTitolo());
        
        TextArea txtDescrizione = new TextArea(corso.getDescrizione() != null ? corso.getDescrizione() : "");
        txtDescrizione.setPrefRowCount(3);
        
        ComboBox<CategoriaCorso> cmbCategoria = new ComboBox<>();
        try {
            List<CategoriaCorso> categorie = service.getCategorieCorsi();
            cmbCategoria.getItems().addAll(categorie);
            // Seleziona la categoria attuale del corso
            cmbCategoria.setValue(categorie.stream()
                .filter(cat -> cat.getId().equals(corso.getCategoriaId()))
                .findFirst().orElse(null));
        } catch (Exception e) {
            logger.error("Errore nel caricamento categorie", e);
            messageHelper.mostraErrore("Errore nel caricamento delle categorie: " + e.getMessage());
        }
        
        TextField txtDurataOre = new TextField(String.valueOf(corso.getDurata() != null ? corso.getDurata() : 0));
        txtDurataOre.setPromptText("Durata in ore (1-8)");
        
        TextField txtMaxPartecipanti = new TextField(String.valueOf(corso.getMaxPartecipanti() != null ? corso.getMaxPartecipanti() : 0));
        txtMaxPartecipanti.setPromptText("Max partecipanti (1-50)");
        
        TextField txtPrezzo = new TextField(corso.getPrezzo() != null ? corso.getPrezzo().toString() : "0.00");
        
        ComboBox<String> cmbFrequenza = new ComboBox<>();
        Map<String, String> frequencyLabelsEdit = getFrequencyLabels();
        cmbFrequenza.getItems().addAll(frequencyLabelsEdit.keySet());
        cmbFrequenza.setValue(getLabelFromValue(corso.getFrequenza() != null ? corso.getFrequenza() : "settimanale"));
        
        TextField txtNumeroSessioni = new TextField(String.valueOf(corso.getNumeroSessioni() != null ? corso.getNumeroSessioni() : 0));
        
        DatePicker dateInizio = new DatePicker(corso.getDataInizio() != null ? corso.getDataInizio() : LocalDate.now().plusDays(7));
        
        // Aggiungi i controlli al grid
        grid.add(new Label("Titolo:"), 0, 0);
        grid.add(txtTitolo, 1, 0);
        grid.add(new Label("Descrizione:"), 0, 1);
        grid.add(txtDescrizione, 1, 1);
        grid.add(new Label("Categoria:"), 0, 2);
        grid.add(cmbCategoria, 1, 2);
        grid.add(new Label("Durata (ore):"), 0, 3);
        grid.add(txtDurataOre, 1, 3);
        grid.add(new Label("Max Partecipanti:"), 0, 4);
        grid.add(txtMaxPartecipanti, 1, 4);
        grid.add(new Label("Prezzo (€):"), 0, 5);
        grid.add(txtPrezzo, 1, 5);
        grid.add(new Label("Frequenza:"), 0, 6);
        grid.add(cmbFrequenza, 1, 6);
        grid.add(new Label("Numero Sessioni:"), 0, 7);
        grid.add(txtNumeroSessioni, 1, 7);
        grid.add(new Label("Data Inizio:"), 0, 8);
        grid.add(dateInizio, 1, 8);

        dialog.getDialogPane().setContent(grid);

        // Abilita/disabilita il bottone Salva
        Node salvaButton = dialog.getDialogPane().lookupButton(confermaButtonType);
        salvaButton.setDisable(txtTitolo.getText().trim().isEmpty());

        // Validazione in tempo reale
        txtTitolo.textProperty().addListener((observable, oldValue, newValue) -> {
            salvaButton.setDisable(newValue.trim().isEmpty());
        });

        // Converte il risultato in un Corso quando Salva è premuto
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confermaButtonType) {
                try {
                    corso.setTitolo(txtTitolo.getText().trim());
                    corso.setDescrizione(txtDescrizione.getText().trim());
                    corso.setCategoriaId(cmbCategoria.getValue().getId());
                    
                    int durataOre = Integer.parseInt(txtDurataOre.getText().trim());
                    if (durataOre < 1 || durataOre > 8) {
                        messageHelper.mostraErrore("La durata del corso deve essere compresa tra 1 e 8 ore");
                        return null;
                    }
                    corso.setDurata(durataOre);
                    
                    int maxPartecipanti = Integer.parseInt(txtMaxPartecipanti.getText().trim());
                    if (maxPartecipanti < 1 || maxPartecipanti > 50) {
                        messageHelper.mostraErrore("Il numero massimo di partecipanti deve essere compreso tra 1 e 50");
                        return null;
                    }
                    corso.setMaxPartecipanti(maxPartecipanti);
                    
                    BigDecimal prezzo = new BigDecimal(txtPrezzo.getText().trim());
                    corso.setPrezzo(prezzo);
                    
                    corso.setFrequenza(frequencyLabelsEdit.get(cmbFrequenza.getValue()));
                    
                    int numero_sessioni = Integer.parseInt(txtNumeroSessioni.getText().trim());
                    corso.setNumeroSessioni(numero_sessioni);
                    
                    corso.setDataInizio(dateInizio.getValue());
                    
                    return corso;
                } catch (NumberFormatException e) {
                    messageHelper.mostraErrore("Inserisci valori numerici validi per durata, partecipanti, prezzo e numero sessioni");
                    return null;
                }
            }
            return null;
        });        // Mostra il dialog e gestisce il risultato
        Optional<Corso> result = dialog.showAndWait();
        result.ifPresent(corsoModificato -> {
            try {
                boolean successo = service.aggiornaCorso(corsoModificato);
                if (successo) {
                    messageHelper.mostraSuccesso("Successo", "Corso aggiornato con successo!");
                    onSuccess.run();
                    logger.info("Corso aggiornato: {}", corsoModificato.getTitolo());
                } else {
                    messageHelper.mostraErrore("Errore nell'aggiornamento del corso");
                }
            } catch (Exception e) {
                logger.error("Errore nell'aggiornamento del corso", e);
                messageHelper.mostraErrore("Errore nell'aggiornamento del corso: " + e.getMessage());
            }
        });
    }

    // ==================== METODI HELPER PRIVATI ====================
    
    private GridPane createSessionFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }
    
    private GridPane createRecipeFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        return grid;
    }
    
    private void addSessionFormFields(GridPane grid, Label lblNumeroSessione, TextField txtTitolo,
                                    TextArea txtDescrizione, DatePicker dateSessione, 
                                    ComboBox<String> cmbTipo, TextField txtDurata) {
        grid.add(new Label("Numero Sessione:"), 0, 0);
        grid.add(lblNumeroSessione, 1, 0);
        grid.add(new Label("Titolo:"), 0, 1);
        grid.add(txtTitolo, 1, 1);
        grid.add(new Label("Descrizione:"), 0, 2);
        grid.add(txtDescrizione, 1, 2);
        grid.add(new Label("Data:"), 0, 3);
        grid.add(dateSessione, 1, 3);
        grid.add(new Label("Tipo:"), 0, 4);
        grid.add(cmbTipo, 1, 4);
        grid.add(new Label("Durata (min):"), 0, 5);
        grid.add(txtDurata, 1, 5);
    }
    
    private void addRecipeFormFields(GridPane grid, TextField txtNome, TextArea txtDescrizione,
                                   ComboBox<Integer> cmbDifficolta, TextField txtTempoPreparazione,
                                   TextField txtNumeroPortions, TextArea txtIstruzioni) {
        grid.add(new Label("Nome:"), 0, 0);
        grid.add(txtNome, 1, 0);
        grid.add(new Label("Descrizione:"), 0, 1);
        grid.add(txtDescrizione, 1, 1);
        grid.add(new Label("Difficoltà (1-5):"), 0, 2);
        grid.add(cmbDifficolta, 1, 2);
        grid.add(new Label("Tempo (min):"), 0, 3);
        grid.add(txtTempoPreparazione, 1, 3);
        grid.add(new Label("Porzioni:"), 0, 4);
        grid.add(txtNumeroPortions, 1, 4);
        grid.add(new Label("Istruzioni:"), 0, 5);
        grid.add(txtIstruzioni, 1, 5);
    }
    
    private void setupRecipeValidation(TextField txtNome, TextArea txtDescrizione, Node button) {
        txtNome.textProperty().addListener((observable, oldValue, newValue) -> {
            button.setDisable(newValue.trim().isEmpty() || txtDescrizione.getText().trim().isEmpty());
        });
        
        txtDescrizione.textProperty().addListener((observable, oldValue, newValue) -> {
            button.setDisable(txtNome.getText().trim().isEmpty() || newValue.trim().isEmpty());
        });
    }
    
    private Ricetta createRecipeFromForm(Chef chefLoggato, TextField txtNome, TextArea txtDescrizione,
                                       ComboBox<Integer> cmbDifficolta, TextField txtTempoPreparazione,
                                       TextField txtNumeroPortions, TextArea txtIstruzioni) {
        try {
            Ricetta ricetta = new Ricetta();
            ricetta.setChefId(chefLoggato.getId());
            ricetta.setNome(txtNome.getText().trim());
            ricetta.setDescrizione(txtDescrizione.getText().trim());
            ricetta.setDifficolta(cmbDifficolta.getValue());
            
            int tempo = Integer.parseInt(txtTempoPreparazione.getText().trim());
            ricetta.setTempoPreparazione(tempo);
            
            int porzioni = Integer.parseInt(txtNumeroPortions.getText().trim());
            ricetta.setNumeroPortions(porzioni);
            
            ricetta.setIstruzioni(txtIstruzioni.getText().trim());
            
            return ricetta;
        } catch (NumberFormatException e) {
            messageHelper.mostraErrore("Inserisci valori numerici validi per tempo e porzioni");
            return null;
        }
    }
    
    private Ricetta updateRecipeFromForm(Ricetta ricetta, TextField txtNome, TextArea txtDescrizione,
                                       ComboBox<Integer> cmbDifficolta, TextField txtTempoPreparazione,
                                       TextField txtNumeroPortions, TextArea txtIstruzioni) {
        try {
            ricetta.setNome(txtNome.getText().trim());
            ricetta.setDescrizione(txtDescrizione.getText().trim());
            ricetta.setDifficolta(cmbDifficolta.getValue());
            
            int tempo = Integer.parseInt(txtTempoPreparazione.getText().trim());
            ricetta.setTempoPreparazione(tempo);
            
            int porzioni = Integer.parseInt(txtNumeroPortions.getText().trim());
            ricetta.setNumeroPortions(porzioni);
            
            ricetta.setIstruzioni(txtIstruzioni.getText().trim());
            
            return ricetta;
        } catch (NumberFormatException e) {
            messageHelper.mostraErrore("Inserisci valori numerici validi per tempo e porzioni");
            return null;
        }
    }
    
    private int associaRicetteASessione(Integer sessione_id, List<Integer> ricetteIds) {
        int associazioniCreate = 0;
        try {
            for (int i = 0; i < ricetteIds.size(); i++) {
                Integer ricetta_id = ricetteIds.get(i);
                boolean successo = service.associaRicettaASessione(sessione_id, ricetta_id, i + 1);
                if (successo) {
                    associazioniCreate++;
                }
            }
            if (associazioniCreate > 0) {
                messageHelper.mostraSuccesso("Successo", "Associate " + associazioniCreate + " ricette alla sessione con successo!");
                logger.info("Associate {} ricette alla sessione ID: {}", associazioniCreate, sessione_id);
            } else {
                messageHelper.mostraErrore("Nessuna ricetta è stata associata correttamente");
            }
        } catch (Exception e) {
            logger.error("Errore nell'associazione delle ricette", e);
            messageHelper.mostraErrore("Errore nell'associazione delle ricette: " + e.getMessage());
        }
        return associazioniCreate;
    }
}
