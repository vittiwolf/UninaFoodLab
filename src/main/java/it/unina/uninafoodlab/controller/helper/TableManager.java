package it.unina.uninafoodlab.controller.helper;

import it.unina.uninafoodlab.model.*;
import it.unina.uninafoodlab.service.UninaFoodLabService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Manager per la configurazione e gestione delle tabelle
 */
public class TableManager {
    private static final Logger logger = LoggerFactory.getLogger(TableManager.class);
    
    private final UninaFoodLabService service;
    private final MessageHelper messageHelper;
      // ObservableList per le tabelle
    private final ObservableList<Corso> listaCorsi = FXCollections.observableArrayList();
    private final ObservableList<Sessione> listaSessioni = FXCollections.observableArrayList();
    private final ObservableList<Ricetta> listaRicette = FXCollections.observableArrayList();
    private final ObservableList<Utente> listaUtenti = FXCollections.observableArrayList();
    private final ObservableList<Iscrizione> listaIscrizioni = FXCollections.observableArrayList();
    
    public TableManager(UninaFoodLabService service, MessageHelper messageHelper) {
        this.service = service;
        this.messageHelper = messageHelper;
    }
    
    // ==================== CONFIGURAZIONE TABELLE ====================
    
    /**
     * Configura la tabella dei corsi
     */    public void configuraTabellaCorsi(TableView<Corso> tabellaCorsi,
                                    TableColumn<Corso, Integer> colIdCorso,
                                    TableColumn<Corso, String> colTitoloCorso,
                                    TableColumn<Corso, String> colCategoriaCorso,
                                    TableColumn<Corso, String> colFrequenza,
                                    TableColumn<Corso, String> colDataInizio,
                                    TableColumn<Corso, Integer> colDurataCorso,
                                    TableColumn<Corso, Integer> colMaxPartecipanti,
                                    TableColumn<Corso, String> colStato) {
        
        // Configurazione colonne
        colIdCorso.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitoloCorso.setCellValueFactory(new PropertyValueFactory<>("titolo"));
        colCategoriaCorso.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategoriaNome() != null ? 
                cellData.getValue().getCategoriaNome() : ""));        
        colFrequenza.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFrequenzaDescrizione() != null ? 
                cellData.getValue().getFrequenzaDescrizione() : "N/A"));
        colDataInizio.setCellValueFactory(cellData -> {
            LocalDate data_inizio = cellData.getValue().getDataInizio();
            if (data_inizio != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(data_inizio.format(formatter));
            } else {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        colDurataCorso.setCellValueFactory(new PropertyValueFactory<>("durata"));
        colMaxPartecipanti.setCellValueFactory(new PropertyValueFactory<>("maxPartecipanti"));
        colStato.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStato() != null ? cellData.getValue().getStato() : "BOZZA"));
        
        // SOLUZIONE PER LA SEZIONE GRIGIA: Placeholder personalizzato
        configurePlaceholder(tabellaCorsi, "📚 Nessun corso disponibile", "Clicca su 'Nuovo Corso' per creare il tuo primo corso");
        
        // ABILITA LE LINEE DI GRIGLIA DELLA TABELLA
        tabellaCorsi.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Corso> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
            return row;
        });
        
        tabellaCorsi.setItems(listaCorsi);
    }
      /**
     * Configura la tabella delle sessioni
     */
    public void configuraTabellaSessioni(TableView<Sessione> tabellaSessioni,
                                       TableColumn<Sessione, Integer> colNumeroSessione,
                                       TableColumn<Sessione, String> colTitoloSessione,
                                       TableColumn<Sessione, String> colDataSessione,
                                       TableColumn<Sessione, String> colTipoSessione,
                                       TableColumn<Sessione, String> colModalita,
                                       TableColumn<Sessione, String> colCompletata,
                                       TableColumn<Sessione, String> colRicetteAssociate) {
          colNumeroSessione.setCellValueFactory(new PropertyValueFactory<>("numeroSessione"));
        colTitoloSessione.setCellValueFactory(new PropertyValueFactory<>("titolo"));
        colDataSessione.setCellValueFactory(cellData -> {
            LocalDate dataSessione = cellData.getValue().getDataSessione();
            if (dataSessione != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(dataSessione.format(formatter));
            } else {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        colTipoSessione.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colModalita.setCellValueFactory(cellData -> {
            String modalita = cellData.getValue().getTipoDescrizione();
            return new javafx.beans.property.SimpleStringProperty(modalita != null ? modalita : "N/A");
        });
        colCompletata.setCellValueFactory(cellData -> {
            String completataDesc = cellData.getValue().getCompletataDescrizione();
            return new javafx.beans.property.SimpleStringProperty(completataDesc);
        });
        
        colRicetteAssociate.setCellValueFactory(cellData -> {
            String ricetteDesc = cellData.getValue().getRicetteAssociate();
            return new javafx.beans.property.SimpleStringProperty(ricetteDesc);
        });
        
        // SOLUZIONE PER LA SEZIONE GRIGIA: Placeholder personalizzato
        configurePlaceholder(tabellaSessioni, "📅 Nessuna sessione programmata", "Seleziona un corso per visualizzare le sessioni");
        
        // ABILITA LE LINEE DI GRIGLIA DELLA TABELLA
        tabellaSessioni.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Sessione> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
            return row;
        });
        
        tabellaSessioni.setItems(listaSessioni);
    }
    
    /**
     * Configura la tabella delle ricette
     */
    public void configuraTabellaRicette(TableView<Ricetta> tabellaRicette,
                                      TableColumn<Ricetta, Integer> colIdRicetta,
                                      TableColumn<Ricetta, String> colNomeRicetta,
                                      TableColumn<Ricetta, String> colCategoriaRicetta,
                                      TableColumn<Ricetta, String> colDifficolta,
                                      TableColumn<Ricetta, Integer> colTempoPreparazione) {
        
        colIdRicetta.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomeRicetta.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCategoriaRicetta.setCellValueFactory(cellData -> {
            Integer difficolta = cellData.getValue().getDifficolta();
            String categoria = switch (difficolta != null ? difficolta : 1) {
                case 1 -> "Facile";
                case 2 -> "Facile";
                case 3 -> "Medio";
                case 4 -> "Difficile";
                case 5 -> "Difficile";
                default -> "Non definito";
            };
            return new javafx.beans.property.SimpleStringProperty(categoria);
        });
        colDifficolta.setCellValueFactory(new PropertyValueFactory<>("difficolta"));
        colTempoPreparazione.setCellValueFactory(new PropertyValueFactory<>("tempoPreparazione"));
        
        // SOLUZIONE PER LA SEZIONE GRIGIA: Placeholder personalizzato
        configurePlaceholder(tabellaRicette, "👨‍🍳 Nessuna ricetta disponibile", "Clicca su 'Nuova Ricetta' per aggiungere le tue ricette");
        
        // ABILITA LE LINEE DI GRIGLIA DELLA TABELLA
        tabellaRicette.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Ricetta> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
            return row;
        });
        
        tabellaRicette.setItems(listaRicette);
    }
      // ==================== METODO HELPER PER PLACEHOLDER ====================
    
    /**
     * Configura un placeholder personalizzato per una tabella
     * RISOLVE IL PROBLEMA DELLA SEZIONE GRIGIA CON APPROCCIO SUPER AGGRESSIVO
     */
    private <T> void configurePlaceholder(TableView<T> tableView, String titolo, String sottotitolo) {
        // Crea un VBox per contenere il placeholder
        javafx.scene.layout.VBox placeholder = new javafx.scene.layout.VBox(10);
        placeholder.setAlignment(javafx.geometry.Pos.CENTER);
        placeholder.setStyle("-fx-background-color: white;");
        
        // Etichetta principale
        javafx.scene.control.Label lblTitolo = new javafx.scene.control.Label(titolo);
        lblTitolo.setStyle(
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #6c757d;"
        );
        
        // Etichetta secondaria
        javafx.scene.control.Label lblSottotitolo = new javafx.scene.control.Label(sottotitolo);
        lblSottotitolo.setStyle(
            "-fx-font-size: 12px; " +
            "-fx-text-fill: #6c757d;" +
            "-fx-font-style: italic;"
        );
          placeholder.getChildren().addAll(lblTitolo, lblSottotitolo);
        
        // Imposta il placeholder personalizzato
        tableView.setPlaceholder(placeholder);
        
        // Stile base semplice per la tabella (finestra non ridimensionabile)
        tableView.setStyle("-fx-background-color: white; -fx-control-inner-background: white;");
        
        logger.debug("Configurato placeholder per tabella: {}", titolo);
    }
  
    
    // ==================== CARICAMENTO DATI ====================
    
    /**
     * Carica i corsi di uno chef
     */
    public void caricaCorsiChef(Integer chefId) {
        if (chefId == null) return;
        
        try {
            List<Corso> corsi = service.getCorsiByChef(chefId);
            listaCorsi.clear();
            listaCorsi.addAll(corsi);
        } catch (Exception e) {
            logger.error("Errore nel caricamento dei corsi", e);
            messageHelper.mostraErrore("Errore nel caricamento dei corsi: " + e.getMessage());
        }
    }
    
    /**
     * Carica i corsi filtrati per categoria
     */
    public void caricaCorsiPerCategoria(Integer chefId, Integer categoria_id) {
        try {
            List<Corso> corsiFiltrati = service.getCorsiByChefECategoria(chefId, categoria_id);
            listaCorsi.clear();
            listaCorsi.addAll(corsiFiltrati);
        } catch (Exception e) {
            logger.error("Errore nel filtro per categoria", e);
            messageHelper.mostraErrore("Errore nel filtro per categoria: " + e.getMessage());
        }
    }
    
    /**
     * Carica le sessioni di un corso
     */
    public void caricaSessioniCorso(int corso_id) {
        try {
            List<Sessione> sessioni = service.getSessioniByCorso(corso_id);
            
            // Carica le ricette associate per ogni sessione
            for (Sessione sessione : sessioni) {
                List<Ricetta> ricette = service.getRicetteSessione(sessione.getId());
                if (ricette != null && !ricette.isEmpty()) {
                    String nomiRicette = ricette.stream()
                        .map(Ricetta::getNome)
                        .reduce((r1, r2) -> r1 + ", " + r2)
                        .orElse("");
                    sessione.setRicetteAssociate(nomiRicette);
                } else {
                    sessione.setRicetteAssociate("Nessuna ricetta associata");
                }
            }
            
            listaSessioni.clear();
            listaSessioni.addAll(sessioni);
        } catch (Exception e) {
            logger.error("Errore nel caricamento delle sessioni", e);
            messageHelper.mostraErrore("Errore nel caricamento delle sessioni: " + e.getMessage());
        }
    }
    
    /**
     * Carica tutte le ricette
     */
    public void caricaRicette() {
        try {
            List<Ricetta> ricette = service.getAllRicette();
            listaRicette.clear();
            listaRicette.addAll(ricette);
        } catch (Exception e) {
            logger.error("Errore nel caricamento delle ricette", e);
            messageHelper.mostraErrore("Errore nel caricamento delle ricette: " + e.getMessage());
        }
    }
      // ==================== GETTERS PER LE LISTE ====================
    
    public ObservableList<Corso> getListaCorsi() {
        return listaCorsi;
    }
    
    public ObservableList<Sessione> getListaSessioni() {
        return listaSessioni;
    }
    
    public ObservableList<Ricetta> getListaRicette() {
        return listaRicette;
    }
    
    public ObservableList<Utente> getListaUtenti() {
        return listaUtenti;
    }
    
    public ObservableList<Iscrizione> getListaIscrizioni() {
        return listaIscrizioni;
    }
    
    // ==================== CONFIGURAZIONE TABELLE UTENTI E ISCRIZIONI ====================
    
    /**
     * Configura la tabella degli utenti
     */
    public void configuraTabellaUtenti(TableView<Utente> tabellaUtenti,
                                     TableColumn<Utente, Integer> colIdUtente,
                                     TableColumn<Utente, String> colNomeUtente,
                                     TableColumn<Utente, String> colCognomeUtente,
                                     TableColumn<Utente, String> colEmailUtente,
                                     TableColumn<Utente, String> colLivelloEsperienza,
                                     TableColumn<Utente, String> colUtenteAttivo) {
        
        colIdUtente.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNomeUtente.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCognomeUtente.setCellValueFactory(new PropertyValueFactory<>("cognome"));
        colEmailUtente.setCellValueFactory(new PropertyValueFactory<>("email"));
        colLivelloEsperienza.setCellValueFactory(new PropertyValueFactory<>("livelloEsperienza"));
        colUtenteAttivo.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getAttivoLabel())
        );
        
        // SOLUZIONE PER LA SEZIONE GRIGIA: Placeholder personalizzato
        configurePlaceholder(tabellaUtenti, "👥 Nessun utente registrato", "Clicca su 'Nuovo Utente' per registrare i partecipanti");
        
        // ABILITA LE LINEE DI GRIGLIA DELLA TABELLA
        tabellaUtenti.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Utente> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
            return row;
        });
        
        tabellaUtenti.setItems(listaUtenti);
        
        logger.debug("Tabella utenti configurata");
    }
    
    /**
     * Configura la tabella delle iscrizioni
     */    public void configuraTabellaIscrizioni(TableView<Iscrizione> tabellaIscrizioni,
                                         TableColumn<Iscrizione, Integer> colIdIscrizione,
                                         TableColumn<Iscrizione, String> colUtenteIscrizione,
                                         TableColumn<Iscrizione, String> colCorsoIscrizione,
                                         TableColumn<Iscrizione, String> colDataIscrizione,
                                         TableColumn<Iscrizione, String> colStatoIscrizione) {
        
        colIdIscrizione.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUtenteIscrizione.setCellValueFactory(cellData -> {
            Iscrizione iscrizione = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                iscrizione.getNomeUtente() + " " + iscrizione.getCognomeUtente()
            );
        });        colCorsoIscrizione.setCellValueFactory(new PropertyValueFactory<>("titoloCorso"));
        colDataIscrizione.setCellValueFactory(cellData -> {
            java.time.LocalDateTime data_iscrizione = cellData.getValue().getDataIscrizione();
            if (data_iscrizione != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new javafx.beans.property.SimpleStringProperty(data_iscrizione.format(formatter));
            } else {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        colStatoIscrizione.setCellValueFactory(new PropertyValueFactory<>("statoIscrizione"));
        
        // SOLUZIONE PER LA SEZIONE GRIGIA: Placeholder personalizzato
        configurePlaceholder(tabellaIscrizioni, "📝 Nessuna iscrizione presente", "Clicca su 'Nuova Iscrizione' per iscrivere utenti ai corsi");
        
        // ABILITA LE LINEE DI GRIGLIA DELLA TABELLA
        tabellaIscrizioni.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Iscrizione> row = new javafx.scene.control.TableRow<>();
            row.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1px 0;");
            return row;
        });
        
        tabellaIscrizioni.setItems(listaIscrizioni);
        
        logger.debug("Tabella iscrizioni configurata");
    }
    
    // ==================== CARICAMENTO DATI UTENTI E ISCRIZIONI ====================
    
    /**
     * Carica tutti gli utenti nella tabella
     */
    public void caricaUtenti() {
        try {
            List<Utente> utenti = service.getAllUtenti();
            listaUtenti.clear();
            listaUtenti.addAll(utenti);
            logger.debug("Caricati {} utenti", utenti.size());
        } catch (Exception e) {
            logger.error("Errore nel caricamento degli utenti", e);
            messageHelper.mostraErrore("Errore nel caricamento degli utenti: " + e.getMessage());
        }
    }
    
    /**
     * Carica tutte le iscrizioni nella tabella
     */
    public void caricaIscrizioni() {
        try {
            List<Iscrizione> iscrizioni = service.getAllIscrizioni();
            listaIscrizioni.clear();
            listaIscrizioni.addAll(iscrizioni);
            logger.debug("Caricate {} iscrizioni", iscrizioni.size());
        } catch (Exception e) {
            logger.error("Errore nel caricamento delle iscrizioni", e);
            messageHelper.mostraErrore("Errore nel caricamento delle iscrizioni: " + e.getMessage());
        }
    }
    
    /**
     * Carica le iscrizioni per un corso specifico
     */
    public void caricaIscrizioniCorso(int idCorso) {
        try {
            List<Iscrizione> iscrizioni = service.getIscrizioniCorso(idCorso);
            listaIscrizioni.clear();
            listaIscrizioni.addAll(iscrizioni);
            logger.debug("Caricate {} iscrizioni per il corso {}", iscrizioni.size(), idCorso);
        } catch (Exception e) {
            logger.error("Errore nel caricamento delle iscrizioni del corso {}", idCorso, e);
            messageHelper.mostraErrore("Errore nel caricamento delle iscrizioni: " + e.getMessage());
        }
    }
}
