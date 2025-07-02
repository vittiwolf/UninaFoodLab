package it.unina.uninafoodlab.controller;

import it.unina.uninafoodlab.controller.helper.*;
import it.unina.uninafoodlab.model.*;
import it.unina.uninafoodlab.service.UninaFoodLabService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller principale per la gestione dei corsi di cucina
 * Refactorizzato per essere più modulare e mantenibile
 */
public class MainController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    // ==================== DIPENDENZE E HELPER ====================
    private UninaFoodLabService service;
    private Chef chefLoggato;
      // Helper classes per organizzare il codice
    private MessageHelper messageHelper;
    private DialogHelper dialogHelper;
    private TableManager tableManager;
    
    // ==================== COMPONENTI FXML ====================
    @FXML private TabPane mainTabPane;
    @FXML private Label lblBenvenuto;
    
    // Tab Corsi
    @FXML private TableView<Corso> tabellaCorsi;
    @FXML private TableColumn<Corso, Integer> colIdCorso;
    @FXML private TableColumn<Corso, String> colTitoloCorso;
    @FXML private TableColumn<Corso, String> colCategoriaCorso;
    @FXML private TableColumn<Corso, String> colFrequenza;
    @FXML private TableColumn<Corso, String> colDataInizio;
    @FXML private TableColumn<Corso, String> colStato;
    
    @FXML private ComboBox<CategoriaCorso> cmbFiltraCategoria;
    @FXML private Button btnNuovoCorso;
    @FXML private Button btnModificaCorso;
    @FXML private Button btnEliminaCorso;
      // Tab Sessioni
    @FXML private TableView<Sessione> tabellaSessioni;
    @FXML private TableColumn<Sessione, Integer> colNumeroSessione;
    @FXML private TableColumn<Sessione, String> colTitoloSessione;
    @FXML private TableColumn<Sessione, String> colDataSessione;
    @FXML private TableColumn<Sessione, String> colTipoSessione;
    @FXML private TableColumn<Sessione, String> colModalita;
    @FXML private TableColumn<Sessione, Boolean> colCompletata;
    
    @FXML private Button btnNuovaSessione;
    @FXML private Button btnModificaSessione;
    @FXML private Button btnAssociaRicetta;
    
    // Tab Ricette
    @FXML private TableView<Ricetta> tabellaRicette;
    @FXML private TableColumn<Ricetta, Integer> colIdRicetta;
    @FXML private TableColumn<Ricetta, String> colNomeRicetta;
    @FXML private TableColumn<Ricetta, String> colCategoriaRicetta;    @FXML private TableColumn<Ricetta, String> colDifficolta;
    @FXML private TableColumn<Ricetta, Integer> colTempoPreparazione;
    
    @FXML private Button btnNuovaRicetta;
    @FXML private Button btnModificaRicetta;
    @FXML private Button btnEliminaRicetta;
    
    // Tab Utenti
    @FXML private TableView<Utente> tabellaUtenti;
    @FXML private TableColumn<Utente, Integer> colIdUtente;
    @FXML private TableColumn<Utente, String> colNomeUtente;
    @FXML private TableColumn<Utente, String> colCognomeUtente;
    @FXML private TableColumn<Utente, String> colEmailUtente;
    @FXML private TableColumn<Utente, String> colLivelloEsperienza;
    @FXML private TableColumn<Utente, Boolean> colUtenteAttivo;
    
    @FXML private Button btnNuovoUtente;
    @FXML private Button btnModificaUtente;
    @FXML private Button btnDisattivaUtente;
    
    // Tab Iscrizioni
    @FXML private TableView<Iscrizione> tabellaIscrizioni;
    @FXML private TableColumn<Iscrizione, Integer> colIdIscrizione;
    @FXML private TableColumn<Iscrizione, String> colUtenteIscrizione;
    @FXML private TableColumn<Iscrizione, String> colCorsoIscrizione;
    @FXML private TableColumn<Iscrizione, String> colDataIscrizione;
    @FXML private TableColumn<Iscrizione, String> colStatoIscrizione;
      @FXML private Button btnNuovaIscrizione;
    @FXML private Button btnAnnullaIscrizione;
    
    // ==================== INIZIALIZZAZIONE ====================
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        service = new UninaFoodLabService();
        initializeHelpers();
        configuraTabelleEColonne();
        configuraComboBox();
        caricaDatiIniziali();
    }
    
    /**
     * Inizializza le classi helper
     */    private void initializeHelpers() {
        messageHelper = new MessageHelper();
        dialogHelper = new DialogHelper(service, messageHelper);
        tableManager = new TableManager(service, messageHelper);
    }
      public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
        lblBenvenuto.setText("Benvenuto, Chef " + chef.getNome() + " " + chef.getCognome());
        tableManager.caricaCorsiChef(chef.getId());
        
        // SOLUZIONE RIDIMENSIONAMENTO: Configura listener per gestire ridimensionamento finestra
        configuraTabellePerRidimensionamento();
    }
    
    /**
     * Configura le tabelle per gestire correttamente il ridimensionamento della finestra
     */
    private void configuraTabellePerRidimensionamento() {
        try {
            // Aspetta che le tabelle siano completamente renderizzate
            javafx.application.Platform.runLater(() -> {
                // Applica fix per ogni tabella quando la scena è disponibile
                if (mainTabPane.getScene() != null) {
                    applicaFixRidimensionamentoTabelle();
                } else {
                    // Se la scena non è ancora disponibile, aspetta
                    mainTabPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
                        if (newScene != null) {
                            applicaFixRidimensionamentoTabelle();
                        }
                    });
                }
            });
        } catch (Exception e) {
            logger.error("Errore nella configurazione delle tabelle per ridimensionamento", e);
        }
    }
    
    /**
     * Applica fix specifici per il ridimensionamento a tutte le tabelle
     */
    private void applicaFixRidimensionamentoTabelle() {
        javafx.scene.Scene scene = mainTabPane.getScene();
        if (scene == null) return;
        
        // Lista di tutte le tabelle nell'applicazione
        var tabelle = java.util.List.of(
            tabellaCorsi, tabellaSessioni, tabellaRicette, 
            tabellaUtenti, tabellaIscrizioni
        );
        
        // Per ogni tabella, configura listener per ridimensionamento
        tabelle.forEach(tabella -> {
            if (tabella != null) {
                // Listener per ridimensionamento della finestra
                scene.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                    javafx.application.Platform.runLater(() -> {
                        applicaFixSingolaTabella(tabella);
                    });
                });
                
                scene.heightProperty().addListener((obs, oldHeight, newHeight) -> {
                    javafx.application.Platform.runLater(() -> {
                        applicaFixSingolaTabella(tabella);
                    });
                });
                
                // Applica il fix immediatamente
                applicaFixSingolaTabella(tabella);
            }
        });
        
        logger.debug("Configurato fix ridimensionamento per tutte le tabelle");
    }
    
    /**
     * Applica fix per eliminare sezioni grigie su una singola tabella
     */
    private void applicaFixSingolaTabella(javafx.scene.control.TableView<?> tabella) {
        try {
            // Forza stile direttamente sulla tabella
            tabella.setStyle(
                "-fx-background-color: white !important; " +
                "-fx-control-inner-background: white !important; " +
                "-fx-background-insets: 0 !important; " +
                "-fx-padding: 0 !important;"
            );
            
            // Applica fix a tutti i componenti interni
            tabella.lookupAll(".virtual-flow").forEach(node -> {
                node.setStyle("-fx-background-color: white !important; -fx-background-insets: 0 !important;");
            });
            
            tabella.lookupAll(".clipped-container").forEach(node -> {
                node.setStyle("-fx-background-color: white !important; -fx-background-insets: 0 !important;");
            });
            
            tabella.lookupAll(".sheet").forEach(node -> {
                node.setStyle("-fx-background-color: white !important; -fx-background-insets: 0 !important;");
            });
            
            tabella.lookupAll(".table-row-cell:empty").forEach(node -> {
                node.setStyle("-fx-background-color: white !important; -fx-background-insets: 0 !important;");
            });
            
        } catch (Exception e) {
            logger.warn("Errore nell'applicazione del fix per tabella", e);
        }
    }
    
    // ==================== CONFIGURAZIONE INIZIALE ====================
      private void configuraTabelleEColonne() {
        // Configura tabelle usando il TableManager
        tableManager.configuraTabellaCorsi(tabellaCorsi, colIdCorso, colTitoloCorso, 
                                         colCategoriaCorso, colFrequenza, colDataInizio, colStato);
        
        tableManager.configuraTabellaSessioni(tabellaSessioni, colNumeroSessione, colTitoloSessione,
                                            colDataSessione, colTipoSessione, colModalita, colCompletata);
        
        tableManager.configuraTabellaRicette(tabellaRicette, colIdRicetta, colNomeRicetta,
                                           colCategoriaRicetta, colDifficolta, colTempoPreparazione);
        
        // Configura tabelle utenti e iscrizioni
        tableManager.configuraTabellaUtenti(tabellaUtenti, colIdUtente, colNomeUtente,
                                          colCognomeUtente, colEmailUtente, colLivelloEsperienza, colUtenteAttivo);
        
        tableManager.configuraTabellaIscrizioni(tabellaIscrizioni, colIdIscrizione, colUtenteIscrizione,
                                               colCorsoIscrizione, colDataIscrizione, colStatoIscrizione);
        
        // Listener per selezione corso
        tabellaCorsi.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    tableManager.caricaSessioniCorso(newSelection.getId());
                }
            });
    }    private void configuraComboBox() {
        // Solo il filtro categoria è ancora disponibile - non abbiamo più il form inline
        // Il FormManager potrebbe gestire questa configurazione, ma per ora la lasciamo vuota
        // dato che cmbFiltraCategoria sarà configurata quando caricheremo le categorie
    }
      private void caricaDatiIniziali() {
        // Carica categorie solo per il filtro, non per il form (che ora è un dialog)
        try {
            List<CategoriaCorso> categorie = service.getAllCategorie();
            cmbFiltraCategoria.getItems().addAll(categorie);
            
            // Aggiungi opzione "Tutte" per il filtro
            CategoriaCorso tutte = new CategoriaCorso();
            tutte.setNome("Tutte le categorie");
            cmbFiltraCategoria.getItems().add(0, tutte);
            cmbFiltraCategoria.setValue(tutte);
        } catch (Exception e) {
            logger.error("Errore nel caricamento delle categorie", e);
            messageHelper.mostraErrore("Errore nel caricamento delle categorie: " + e.getMessage());
        }
        
        tableManager.caricaRicette();
        tableManager.caricaUtenti();
        tableManager.caricaIscrizioni();
    }
    
    // ==================== ACTION HANDLERS - NAVIGAZIONE ====================
    
    @FXML
    private void logout() {
        logger.info("Logout utente: {}", chefLoggato.getUsername());
        try {
            // Chiudi la finestra corrente
            Stage currentStage = (Stage) mainTabPane.getScene().getWindow();
            currentStage.close();
            
            // Riapri la finestra di login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();
            
            Stage loginStage = new Stage();
            loginStage.setTitle("UninaFoodLab - Login");
            loginStage.setScene(new Scene(root));
            loginStage.show();
            
        } catch (IOException e) {
            logger.error("Errore durante logout", e);
            messageHelper.mostraErrore("Errore durante il logout: " + e.getMessage());
        }
    }
    
    @FXML
    private void mostraReport() {
        logger.info("Apertura report per chef: {}", chefLoggato.getId());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GraficiView.fxml"));
            Parent root = loader.load();
            
            GraficiController controller = loader.getController();
            controller.setChefLoggato(chefLoggato);
            
            Stage stage = new Stage();
            stage.setTitle("Report e Statistiche");
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainTabPane.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            logger.error("Errore nell'apertura dei report", e);
            messageHelper.mostraErrore("Errore nell'apertura dei report: " + e.getMessage());
        }
    }
    
    // ==================== ACTION HANDLERS - FILTRI ====================
    
    @FXML
    private void filtraPerCategoria() {
        CategoriaCorso categoria = cmbFiltraCategoria.getValue();
        if (categoria == null || "Tutte le categorie".equals(categoria.getNome())) {
            tableManager.caricaCorsiChef(chefLoggato.getId());
        } else {
            tableManager.caricaCorsiPerCategoria(chefLoggato.getId(), categoria.getId());
        }
    }
    
    // ==================== ACTION HANDLERS - CORSI ====================
      @FXML
    private void mostraNuovoCorso() {
        try {
            dialogHelper.mostraDialogNuovoCorso(chefLoggato, this::aggiornaTabellaCorsi);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog nuovo corso", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }    @FXML
    private void modificaCorso() {
        Corso corsoSelezionato = tabellaCorsi.getSelectionModel().getSelectedItem();
        if (corsoSelezionato == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona un corso da modificare");
            return;
        }
        
        try {
            dialogHelper.mostraDialogModificaCorso(corsoSelezionato, this::aggiornaTabellaCorsi);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog modifica corso", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void eliminaCorso() {
        Corso corsoSelezionato = tabellaCorsi.getSelectionModel().getSelectedItem();
        if (corsoSelezionato == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona un corso da eliminare");
            return;
        }
        
        boolean conferma = messageHelper.mostraConferma("Conferma eliminazione", 
                                                       "Eliminazione corso",
                                                       "Sei sicuro di voler eliminare il corso '" + corsoSelezionato.getTitolo() + "'?");
        
        if (conferma) {
            try {
                service.eliminaCorso(corsoSelezionato.getId());
                tableManager.caricaCorsiChef(chefLoggato.getId());
                messageHelper.mostraSuccesso("Successo", "Corso eliminato con successo");
                logger.info("Corso eliminato: {}", corsoSelezionato.getTitolo());
            } catch (Exception e) {
                logger.error("Errore nell'eliminazione del corso", e);
                messageHelper.mostraErrore("Errore nell'eliminazione del corso: " + e.getMessage());
            }
        }
    }
      
    // ==================== ACTION HANDLERS - SESSIONI ====================
    
    @FXML
    private void nuovaSessione() {
        Corso corsoSelezionato = tabellaCorsi.getSelectionModel().getSelectedItem();
        if (corsoSelezionato == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona prima un corso per aggiungere una sessione");
            return;
        }
        
        try {
            dialogHelper.mostraDialogNuovaSessione(corsoSelezionato, this::aggiornaTabellaSessioni);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog nuova sessione", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void modificaSessione() {
        Sessione sessioneSelezionata = tabellaSessioni.getSelectionModel().getSelectedItem();
        if (sessioneSelezionata == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona una sessione da modificare");
            return;
        }
        
        try {
            dialogHelper.mostraDialogModificaSessione(sessioneSelezionata, this::aggiornaTabellaSessioni);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog modifica sessione", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void associaRicetta() {
        Sessione sessioneSelezionata = tabellaSessioni.getSelectionModel().getSelectedItem();
        if (sessioneSelezionata == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona una sessione per associare una ricetta");
            return;
        }
        
        // Verifica che sia una sessione pratica
        if (!"presenza".equalsIgnoreCase(sessioneSelezionata.getTipo())) {
            messageHelper.mostraAvviso("Attenzione", "Le ricette possono essere associate solo alle sessioni pratiche (in presenza)");
            return;
        }
        
        try {
            dialogHelper.mostraDialogAssociazioneRicetta(sessioneSelezionata, tableManager.getListaRicette());
        } catch (Exception e) {
            logger.error("Errore nell'associazione ricetta", e);
            messageHelper.mostraErrore("Errore nell'associazione ricetta: " + e.getMessage());
        }
    }
    
    // ==================== ACTION HANDLERS - RICETTE ====================
    
    @FXML
    private void nuovaRicetta() {
        try {
            dialogHelper.mostraDialogNuovaRicetta(chefLoggato, this::aggiornaTabellaRicette);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog nuova ricetta", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void modificaRicetta() {
        Ricetta ricettaSelezionata = tabellaRicette.getSelectionModel().getSelectedItem();
        if (ricettaSelezionata == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona una ricetta da modificare");
            return;
        }
        
        try {
            dialogHelper.mostraDialogModificaRicetta(ricettaSelezionata, this::aggiornaTabellaRicette);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog modifica ricetta", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void eliminaRicetta() {
        Ricetta ricettaSelezionata = tabellaRicette.getSelectionModel().getSelectedItem();
        if (ricettaSelezionata == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona una ricetta da eliminare");
            return;
        }
        
        boolean conferma = messageHelper.mostraConferma("Conferma eliminazione",
                                                       "Eliminazione ricetta",
                                                       "Sei sicuro di voler eliminare la ricetta '" + ricettaSelezionata.getNome() + "'?");
        
        if (conferma) {
            try {
                service.eliminaRicetta(ricettaSelezionata.getId());
                tableManager.caricaRicette();
                messageHelper.mostraSuccesso("Successo", "Ricetta eliminata con successo");
                logger.info("Ricetta eliminata: {}", ricettaSelezionata.getNome());
            } catch (Exception e) {
                logger.error("Errore nell'eliminazione della ricetta", e);
                messageHelper.mostraErrore("Errore nell'eliminazione della ricetta: " + e.getMessage());
            }
        }
    }
    
    // ==================== METODI DI AGGIORNAMENTO TABELLE ====================
      private void aggiornaTabellaSessioni() {
        Corso corsoSelezionato = tabellaCorsi.getSelectionModel().getSelectedItem();
        if (corsoSelezionato != null) {
            tableManager.caricaSessioniCorso(corsoSelezionato.getId());
        }
    }
    
    private void aggiornaTabellaCorsi() {
        tableManager.caricaCorsiChef(chefLoggato.getId());
    }
      
    private void aggiornaTabellaRicette() {
        tableManager.caricaRicette();
    }
    
    private void aggiornaTabellaUtenti() {
        tableManager.caricaUtenti();
    }
    
    private void aggiornaTabellaIscrizioni() {
        tableManager.caricaIscrizioni();
    }
    
    // ==================== ACTION HANDLERS - GESTIONE UTENTI ====================
    
    @FXML
    private void nuovoUtente() {
        try {
            dialogHelper.mostraDialogNuovoUtente(this::aggiornaTabellaUtenti);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog nuovo utente", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void modificaUtente() {
        Utente utenteSelezionato = tabellaUtenti.getSelectionModel().getSelectedItem();
        if (utenteSelezionato == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona un utente da modificare");
            return;
        }
        
        try {
            dialogHelper.mostraDialogModificaUtente(utenteSelezionato, this::aggiornaTabellaUtenti);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog modifica utente", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void disattivaUtente() {
        Utente utenteSelezionato = tabellaUtenti.getSelectionModel().getSelectedItem();
        if (utenteSelezionato == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona un utente da disattivare");
            return;
        }
        
        boolean conferma = messageHelper.mostraConferma("Conferma disattivazione",
                                                       "Disattivazione utente",
                                                       "Sei sicuro di voler disattivare l'utente '" + 
                                                       utenteSelezionato.getNome() + " " + utenteSelezionato.getCognome() + "'?");
        
        if (conferma) {
            try {
                service.disattivaUtente(utenteSelezionato.getId());
                aggiornaTabellaUtenti();
                messageHelper.mostraSuccesso("Successo", "Utente disattivato con successo");
                logger.info("Utente disattivato: {} {}", utenteSelezionato.getNome(), utenteSelezionato.getCognome());
            } catch (Exception e) {
                logger.error("Errore nella disattivazione dell'utente", e);
                messageHelper.mostraErrore("Errore nella disattivazione dell'utente: " + e.getMessage());
            }
        }
    }
    
    // ==================== ACTION HANDLERS - GESTIONE ISCRIZIONI ====================
      @FXML
    private void nuovaIscrizione() {
        try {
            // Carica utenti e corsi per il dialog
            var utenti = service.getAllUtenti();
            var corsi = service.getCorsiChef(chefLoggato.getId(), null);
            
            if (utenti.isEmpty()) {
                messageHelper.mostraAvviso("Attenzione", "Non ci sono utenti registrati nel sistema");
                return;
            }
            
            if (corsi.isEmpty()) {
                messageHelper.mostraAvviso("Attenzione", "Non hai corsi disponibili per le iscrizioni");
                return;
            }
            
            dialogHelper.mostraDialogNuovaIscrizione(utenti, corsi, this::aggiornaTabellaIscrizioni);
        } catch (Exception e) {
            logger.error("Errore nell'apertura del dialog nuova iscrizione", e);
            messageHelper.mostraErrore("Errore nell'apertura del dialog: " + e.getMessage());
        }
    }
    
    @FXML
    private void annullaIscrizione() {
        Iscrizione iscrizioneSelezionata = tabellaIscrizioni.getSelectionModel().getSelectedItem();
        if (iscrizioneSelezionata == null) {
            messageHelper.mostraAvviso("Attenzione", "Seleziona un'iscrizione da annullare");
            return;
        }
        
        boolean conferma = messageHelper.mostraConferma("Conferma annullamento",
                                                       "Annullamento iscrizione",
                                                       "Sei sicuro di voler annullare questa iscrizione?");
          if (conferma) {
            try {
                service.annullaIscrizione(iscrizioneSelezionata.getId(), "Annullamento richiesto dall'amministratore");
                aggiornaTabellaIscrizioni();
                messageHelper.mostraSuccesso("Successo", "Iscrizione annullata con successo");
                logger.info("Iscrizione annullata: {}", iscrizioneSelezionata.getId());
            } catch (Exception e) {
                logger.error("Errore nell'annullamento dell'iscrizione", e);
                messageHelper.mostraErrore("Errore nell'annullamento dell'iscrizione: " + e.getMessage());
            }
        }
    }
}
