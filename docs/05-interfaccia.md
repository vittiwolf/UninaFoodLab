# 05 - Interfaccia Utente e Controller

## Indice
- [Architettura MVC](#architettura-mvc)
- [Controller JavaFX](#controller-javafx)
- [File FXML](#file-fxml)
- [Gestione Eventi](#gestione-eventi)
- [Styling CSS](#styling-css)
- [Navigazione](#navigazione)
- [Validazione Input](#validazione-input)

## Architettura MVC

L'interfaccia utente segue il pattern **Model-View-Controller (MVC)**:

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│    View     │◄──►│ Controller  │◄──►│   Model     │
│   (FXML)    │    │  (JavaFX)   │    │ (Entities)  │
└─────────────┘    └─────────────┘    └─────────────┘
        │                   │                   │
        │                   ▼                   │
        │          ┌─────────────┐              │
        └─────────►│   Service   │◄─────────────┘
                   │   Layer     │
                   └─────────────┘
```

### Separazione delle Responsabilità

- **View (FXML)**: Definizione della struttura UI
- **Controller**: Logica di presentazione e gestione eventi
- **Model**: Entità di business e dati
- **Service**: Logica di business e accesso ai dati

## Controller JavaFX

### LoginController

Il controller per l'autenticazione degli chef:

```java
@FXML
public class LoginController implements Initializable {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblErrore;
    
    private UninaFoodLabService service;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service = new UninaFoodLabService();
        configuraBotoniValidazione();
    }
    
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();
        
        if (validaInput(username, password)) {
            try {
                Chef chef = service.autenticaChef(username, password);
                if (chef != null) {
                    apriFinestralePrincipale(chef);
                } else {
                    mostraErrore("Credenziali non valide");
                }
            } catch (Exception e) {
                logger.error("Errore durante login", e);
                mostraErrore("Errore di connessione");
            }
        }
    }
    
    private boolean validaInput(String username, String password) {
        if (username.isEmpty()) {
            mostraErrore("Inserire username");
            return false;
        }
        if (password.isEmpty()) {
            mostraErrore("Inserire password");
            return false;
        }
        return true;
    }
}
```

### MainController (AGGIORNATO)

Controller principale con funzionalità complete per la gestione di corsi, utenti e iscrizioni:

```java
@FXML
public class MainController implements Initializable {
    // ==================== COMPONENTI FXML ====================
    @FXML private TabPane mainTabPane;
    @FXML private Label lblBenvenuto;
    
    // Tab Corsi
    @FXML private TableView<Corso> tabellaCorsi;
    @FXML private TableColumn<Corso, Integer> colIdCorso;
    @FXML private TableColumn<Corso, String> colTitoloCorso;
    @FXML private TableColumn<Corso, String> colCategoriaCorso;
    
    // Tab Sessioni
    @FXML private TableView<Sessione> tabellaSessioni;
    @FXML private TableColumn<Sessione, Integer> colNumeroSessione;
    @FXML private TableColumn<Sessione, String> colTitoloSessione;
    @FXML private TableColumn<Sessione, String> colDataSessione;
    
    // Tab Ricette
    @FXML private TableView<Ricetta> tabellaRicette;
    @FXML private TableColumn<Ricetta, String> colNomeRicetta;
    @FXML private TableColumn<Ricetta, String> colCategoriaRicetta;
    
    // ✅ NUOVO: Tab Utenti
    @FXML private TableView<Utente> tabellaUtenti;
    @FXML private TableColumn<Utente, Integer> colIdUtente;
    @FXML private TableColumn<Utente, String> colNomeUtente;
    @FXML private TableColumn<Utente, String> colCognomeUtente;
    @FXML private TableColumn<Utente, String> colEmailUtente;
    @FXML private TableColumn<Utente, String> colLivelloEsperienza;
    @FXML private TableColumn<Utente, Boolean> colUtenteAttivo;
    
    // ✅ NUOVO: Tab Iscrizioni
    @FXML private TableView<Iscrizione> tabellaIscrizioni;
    @FXML private TableColumn<Iscrizione, Integer> colIdIscrizione;
    @FXML private TableColumn<Iscrizione, String> colUtenteIscrizione;
    @FXML private TableColumn<Iscrizione, String> colCorsoIscrizione;
    @FXML private TableColumn<Iscrizione, String> colDataIscrizione;
    @FXML private TableColumn<Iscrizione, String> colStatoIscrizione;
    
    // ==================== HELPER CLASSES ====================
    private UninaFoodLabService service;
    private Chef chefLoggato;
    private MessageHelper messageHelper;
    private DialogHelper dialogHelper;
    private TableManager tableManager;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        service = new UninaFoodLabService();
        initializeHelpers();
        configuraTabelleEColonne();
        configuraComboBox();
        caricaDatiIniziali();
    }
    
    private void initializeHelpers() {
        messageHelper = new MessageHelper();
        dialogHelper = new DialogHelper(service, messageHelper);
        tableManager = new TableManager(service, messageHelper);
    }
    
    // ==================== GESTIONE UTENTI ====================
    
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
                                                       utenteSelezionato.getNomeCompleto() + "'?");
        
        if (conferma) {
            try {
                service.disattivaUtente(utenteSelezionato.getId());
                aggiornaTabellaUtenti();
                messageHelper.mostraSuccesso("Successo", "Utente disattivato con successo");
            } catch (Exception e) {
                logger.error("Errore nella disattivazione dell'utente", e);
                messageHelper.mostraErrore("Errore nella disattivazione dell'utente: " + e.getMessage());
            }
        }
    }
    
    // ==================== GESTIONE ISCRIZIONI ====================
    
    @FXML
    private void nuovaIscrizione() {
        try {
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
        
        if (!"ATTIVA".equals(iscrizioneSelezionata.getStato())) {
            messageHelper.mostraAvviso("Attenzione", "Puoi annullare solo le iscrizioni attive");
            return;
        }
        
        try {
            dialogHelper.mostraDialogAnnullaIscrizione(iscrizioneSelezionata, this::aggiornaTabellaIscrizioni);
        } catch (Exception e) {
            logger.error("Errore nell'annullamento dell'iscrizione", e);
            messageHelper.mostraErrore("Errore nell'annullamento dell'iscrizione: " + e.getMessage());
        }
    }
    
    // ==================== METODI DI AGGIORNAMENTO ====================
    
    private void aggiornaTabellaUtenti() {
        tableManager.caricaUtenti();
    }
    
    private void aggiornaTabellaIscrizioni() {
        tableManager.caricaIscrizioni();
    }
}
```

### GraficiController

Controller per report e visualizzazioni grafiche:

```java
@FXML
public class GraficiController implements Initializable {
    @FXML private BorderPane graficoContainer;
    @FXML private ComboBox<String> cmbTipoGrafico;
    @FXML private ComboBox<String> cmbMeseAnno;
    @FXML private Label lblTotaleCorsi;
    @FXML private Label lblSessioniTotali;
    
    private UninaFoodLabService service;
    private Chef chefLoggato;
    
    @FXML
    private void aggiornaGrafico() {
        String tipoGrafico = cmbTipoGrafico.getValue();
        if (tipoGrafico == null) return;
        
        try {
            JFreeChart chart = null;
            
            switch (tipoGrafico) {
                case "Corsi per Categoria":
                    chart = creaGraficoCorsiPerCategoria();
                    break;
                case "Sessioni per Modalità":
                    chart = creaGraficoSessioniPerModalita();
                    break;
                case "Andamento Mensile Corsi":
                    chart = creaGraficoAndamentoMensile();
                    break;
                default:
                    chart = creaGraficoCorsiPerCategoria();
            }
            
            if (chart != null) {
                mostraGrafico(chart);
            }
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico", e);
            mostraErrore("Errore nella creazione del grafico");
        }
    }
    
    private void mostraGrafico(JFreeChart chart) {
        graficoContainer.setCenter(null);
        
        SwingNode swingNode = new SwingNode();
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        
        swingNode.setContent(chartPanel);
        graficoContainer.setCenter(swingNode);
    }
}
```

## File FXML

### LoginView.fxml

Interfaccia per l'autenticazione:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.scene.text.*?>

<BorderPane xmlns="http://javafx.com/javafx/11.0.1" 
           xmlns:fx="http://javafx.com/fxml/1" 
           fx:controller="it.unina.uninafoodlab.controller.LoginController"
           styleClass="login-container">

   <center>
      <VBox alignment="CENTER" spacing="20.0" maxWidth="400.0">
         <padding>
            <Insets top="50.0" right="50.0" bottom="50.0" left="50.0" />
         </padding>
         
         <Text text="UninaFoodLab" styleClass="title" />
         <Text text="Sistema Gestione Corsi di Cucina" styleClass="subtitle" />
         
         <VBox spacing="15.0">
            <Label text="Username:" styleClass="field-label" />
            <TextField fx:id="txtUsername" promptText="Inserisci username" 
                      styleClass="text-field" />
                      
            <Label text="Password:" styleClass="field-label" />
            <PasswordField fx:id="txtPassword" promptText="Inserisci password" 
                          styleClass="text-field" />
                          
            <Label fx:id="lblErrore" styleClass="error-label" visible="false" />
            
            <Button fx:id="btnLogin" text="Accedi" onAction="#handleLogin" 
                   styleClass="primary-button" prefWidth="200.0" />
         </VBox>
      </VBox>
   </center>
</BorderPane>
```

### MainView.fxml

Interfaccia principale per gestione corsi:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<?import javafx.geometry.Insets?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<BorderPane xmlns="http://javafx.com/javafx/11.0.1" 
           xmlns:fx="http://javafx.com/fxml/1" 
           fx:controller="it.unina.uninafoodlab.controller.MainController">

   <top>
      <MenuBar>
         <Menu text="File">
            <MenuItem text="Nuovo Corso" onAction="#nuovoCorso" />
            <SeparatorMenuItem />
            <MenuItem text="Esci" onAction="#esci" />
         </Menu>
         <Menu text="Visualizza">
            <MenuItem text="Report Mensili" onAction="#apriReport" />
            <MenuItem text="Statistiche" onAction="#apriStatistiche" />
         </Menu>
      </MenuBar>
   </top>

   <center>
      <SplitPane orientation="HORIZONTAL" dividerPositions="0.6">
         <!-- Tabella Corsi -->
         <VBox spacing="10.0">
            <padding><Insets top="10.0" right="10.0" bottom="10.0" left="10.0" /></padding>
            
            <Label text="I Miei Corsi" styleClass="section-title" />
            
            <TableView fx:id="tblCorsi" VBox.vgrow="ALWAYS">
               <columns>
                  <TableColumn fx:id="colNome" text="Nome Corso" prefWidth="200.0" />
                  <TableColumn fx:id="colCategoria" text="Categoria" prefWidth="150.0" />
                  <TableColumn fx:id="colSessioni" text="Sessioni" prefWidth="100.0" />
               </columns>
            </TableView>
            
            <HBox spacing="10.0">
               <Button text="Aggiungi Corso" onAction="#aggiungiCorso" 
                      styleClass="primary-button" />
               <Button text="Modifica" onAction="#modificaCorso" 
                      styleClass="secondary-button" />
               <Button text="Elimina" onAction="#eliminaCorso" 
                      styleClass="danger-button" />
            </HBox>
         </VBox>
         
         <!-- Form Dettagli -->
         <VBox spacing="15.0">
            <padding><Insets top="10.0" right="10.0" bottom="10.0" left="10.0" /></padding>
            
            <Label text="Dettagli Corso" styleClass="section-title" />
            
            <GridPane hgap="10.0" vgap="10.0">
               <columnConstraints>
                  <ColumnConstraints minWidth="80.0" />
                  <ColumnConstraints />
               </columnConstraints>
               
               <Label text="Nome:" GridPane.columnIndex="0" GridPane.rowIndex="0" />
               <TextField fx:id="txtNomeCorso" GridPane.columnIndex="1" GridPane.rowIndex="0" />
               
               <Label text="Categoria:" GridPane.columnIndex="0" GridPane.rowIndex="1" />
               <ComboBox fx:id="cmbCategoria" GridPane.columnIndex="1" GridPane.rowIndex="1" />
               
               <Label text="Descrizione:" GridPane.columnIndex="0" GridPane.rowIndex="2" />
               <TextArea fx:id="txtDescrizione" prefRowCount="4" 
                        GridPane.columnIndex="1" GridPane.rowIndex="2" />
            </GridPane>
         </VBox>
      </SplitPane>
   </center>

   <bottom>
      <HBox alignment="CENTER_RIGHT" spacing="10.0">
         <padding><Insets top="5.0" right="10.0" bottom="5.0" left="10.0" /></padding>
         <Label fx:id="lblStatusBar" text="Pronto" />
      </HBox>
   </bottom>
</BorderPane>
```

## Gestione Eventi

### Pattern Observer

L'applicazione utilizza il pattern Observer per notificare le modifiche:

```java
public class EventManager {
    private final Map<String, List<EventListener>> listeners = new HashMap<>();
    
    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }
    
    public void notify(String eventType, Object data) {
        List<EventListener> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            for (EventListener listener : eventListeners) {
                listener.onEvent(data);
            }
        }
    }
}

// Utilizzo nei controller
eventManager.subscribe("CORSO_CREATED", this::refreshCorsiTable);
eventManager.subscribe("SESSIONE_UPDATED", this::updateStatistiche);
```

### Validazione in Tempo Reale

```java
private void configuraBotoniValidazione() {
    // Validazione username
    txtUsername.textProperty().addListener((observable, oldValue, newValue) -> {
        boolean isValid = !newValue.trim().isEmpty() && newValue.length() >= 3;
        txtUsername.pseudoClassStateChanged(
            PseudoClass.getPseudoClass("error"), !isValid);
    });
    
    // Abilitazione button login
    BooleanBinding isFormValid = Bindings.createBooleanBinding(() -> 
        !txtUsername.getText().trim().isEmpty() && 
        !txtPassword.getText().isEmpty(),
        txtUsername.textProperty(), 
        txtPassword.textProperty()
    );
    
    btnLogin.disableProperty().bind(isFormValid.not());
}
```

## Styling CSS

### application.css

Stili principali dell'applicazione:

```css
/* Colori principali */
.root {
    -fx-primary-color: #2196F3;
    -fx-secondary-color: #FFC107;
    -fx-success-color: #4CAF50;
    -fx-danger-color: #F44336;
    -fx-background-color: #FAFAFA;
}

/* Container principale */
.login-container {
    -fx-background-color: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* Titoli */
.title {
    -fx-font-family: "Segoe UI", Arial, sans-serif;
    -fx-font-size: 28px;
    -fx-font-weight: bold;
    -fx-fill: white;
}

.subtitle {
    -fx-font-family: "Segoe UI", Arial, sans-serif;
    -fx-font-size: 14px;
    -fx-fill: #E3F2FD;
    -fx-opacity: 0.9;
}

.section-title {
    -fx-font-family: "Segoe UI", Arial, sans-serif;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-text-fill: #333;
}

/* Campi input */
.text-field {
    -fx-background-color: white;
    -fx-background-radius: 4px;
    -fx-border-color: #DDD;
    -fx-border-radius: 4px;
    -fx-padding: 8px 12px;
    -fx-font-size: 14px;
}

.text-field:focused {
    -fx-border-color: -fx-primary-color;
    -fx-effect: dropshadow(gaussian, rgba(33, 150, 243, 0.3), 5, 0, 0, 0);
}

.text-field:error {
    -fx-border-color: -fx-danger-color;
}

/* Bottoni */
.primary-button {
    -fx-background-color: -fx-primary-color;
    -fx-text-fill: white;
    -fx-background-radius: 4px;
    -fx-padding: 10px 20px;
    -fx-font-size: 14px;
    -fx-font-weight: bold;
    -fx-cursor: hand;
}

.primary-button:hover {
    -fx-background-color: derive(-fx-primary-color, -10%);
    -fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 4, 0, 0, 2);
}

.secondary-button {
    -fx-background-color: transparent;
    -fx-text-fill: -fx-primary-color;
    -fx-border-color: -fx-primary-color;
    -fx-border-radius: 4px;
    -fx-background-radius: 4px;
    -fx-padding: 10px 20px;
    -fx-font-size: 14px;
    -fx-cursor: hand;
}

.danger-button {
    -fx-background-color: -fx-danger-color;
    -fx-text-fill: white;
    -fx-background-radius: 4px;
    -fx-padding: 10px 20px;
    -fx-font-size: 14px;
    -fx-cursor: hand;
}

/* Tabelle */
.table-view {
    -fx-background-color: white;
    -fx-border-color: #DDD;
    -fx-border-radius: 4px;
}

.table-view .column-header {
    -fx-background-color: #F5F5F5;
    -fx-border-color: #DDD;
    -fx-font-weight: bold;
}

.table-row-cell:selected {
    -fx-background-color: rgba(33, 150, 243, 0.1);
}

/* Labels di errore */
.error-label {
    -fx-text-fill: -fx-danger-color;
    -fx-font-size: 12px;
}

.field-label {
    -fx-font-weight: bold;
    -fx-text-fill: #555;
}

/* Menu */
.menu-bar {
    -fx-background-color: white;
    -fx-border-color: #DDD;
    -fx-border-width: 0 0 1 0;
}

.menu:hover {
    -fx-background-color: #F5F5F5;
}
```

## Navigazione

### Sistema di Routing

```java
public class NavigationManager {
    private static final String VIEWS_PATH = "/fxml/";
    private Stage primaryStage;
    private Scene currentScene;
    
    public void navigateTo(String viewName, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource(VIEWS_PATH + viewName + ".fxml"));
            
            if (controller != null) {
                loader.setController(controller);
            }
            
            Parent root = loader.load();
            
            if (currentScene == null) {
                currentScene = new Scene(root);
                currentScene.getStylesheets().add(
                    getClass().getResource("/css/application.css").toExternalForm());
                primaryStage.setScene(currentScene);
            } else {
                currentScene.setRoot(root);
            }
            
            primaryStage.show();
            
        } catch (IOException e) {
            logger.error("Errore nel caricamento della vista: " + viewName, e);
        }
    }
    
    // Metodi di navigazione specifici
    public void showLogin() {
        navigateTo("LoginView", new LoginController());
    }
    
    public void showMain(Chef chef) {
        MainController controller = new MainController();
        controller.setChefLoggato(chef);
        navigateTo("MainView", controller);
    }
    
    public void showReports(Chef chef) {
        GraficiController controller = new GraficiController();
        controller.setChefLoggato(chef);
        navigateTo("GraficiView", controller);
    }
}
```

## Validazione Input

### Validatori Personalizzati

```java
public class InputValidators {
    
    public static class CorsoValidator {
        public static ValidationResult validateNome(String nome) {
            if (nome == null || nome.trim().isEmpty()) {
                return new ValidationResult(false, "Il nome del corso è obbligatorio");
            }
            if (nome.length() < 3) {
                return new ValidationResult(false, "Il nome deve essere di almeno 3 caratteri");
            }
            if (nome.length() > 100) {
                return new ValidationResult(false, "Il nome non può superare 100 caratteri");
            }
            return new ValidationResult(true, null);
        }
        
        public static ValidationResult validateDescrizione(String descrizione) {
            if (descrizione != null && descrizione.length() > 500) {
                return new ValidationResult(false, "La descrizione non può superare 500 caratteri");
            }
            return new ValidationResult(true, null);
        }
    }
    
    public static class ChefValidator {
        public static ValidationResult validateUsername(String username) {
            if (username == null || username.trim().isEmpty()) {
                return new ValidationResult(false, "Username obbligatorio");
            }
            if (!username.matches("^[a-zA-Z0-9_]{3,20}$")) {
                return new ValidationResult(false, "Username non valido (3-20 caratteri, solo lettere, numeri e _)");
            }
            return new ValidationResult(true, null);
        }
    }
}
```

### Feedback Visivo

```java
private void setupValidationFeedback() {
    // Listener per validazione in tempo reale
    txtNomeCorso.textProperty().addListener((obs, oldText, newText) -> {
        ValidationResult result = InputValidators.CorsoValidator.validateNome(newText);
        updateFieldValidation(txtNomeCorso, result);
    });
    
    txtDescrizione.textProperty().addListener((obs, oldText, newText) -> {
        ValidationResult result = InputValidators.CorsoValidator.validateDescrizione(newText);
        updateFieldValidation(txtDescrizione, result);
    });
}

private void updateFieldValidation(Control field, ValidationResult result) {
    if (result.isValid()) {
        field.pseudoClassStateChanged(PseudoClass.getPseudoClass("error"), false);
        field.setTooltip(null);
    } else {
        field.pseudoClassStateChanged(PseudoClass.getPseudoClass("error"), true);
        field.setTooltip(new Tooltip(result.getErrorMessage()));
    }
}
```

---

*Questo documento descrive l'architettura dell'interfaccia utente di UninaFoodLab, implementata con JavaFX seguendo il pattern MVC per una chiara separazione delle responsabilità.*
