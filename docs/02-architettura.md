# 🏗️ Architettura del Sistema UninaFoodLab

## 📐 Panoramica Architetturale

UninaFoodLab segue un'architettura **a strati (Layered Architecture)** con separazione netta delle responsabilità, implementando diversi design patterns per garantire manutenibilità, scalabilità e testabilità.

## 🎯 Design Patterns Utilizzati

### 1. **Model-View-Controller (MVC)**

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│     VIEW        │    │   CONTROLLER    │    │     MODEL       │
│                 │    │                 │    │                 │
│ ┌─────────────┐ │    │ ┌─────────────┐ │    │ ┌─────────────┐ │
│ │ LoginView   │ │◄──►│ │LoginCtrl    │ │◄──►│ │ Chef        │ │
│ │ MainView    │ │    │ │MainCtrl     │ │    │ │ Corso       │ │
│ │ GraficiView │ │    │ │GraficiCtrl  │ │    │ │ Sessione    │ │
│ └─────────────┘ │    │ └─────────────┘ │    │ │ Ricetta     │ │
└─────────────────┘    └─────────────────┘    │ └─────────────┘ │
                                              └─────────────────┘
```

### 2. **Data Access Object (DAO)**

```java
// Interface DAO generica
public interface BaseDAO<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean delete(ID id);
}

// Implementazione specifica
public class ChefDAO implements BaseDAO<Chef, Integer> {
    @Override
    public Chef save(Chef chef) {
        // Implementazione persistenza chef
    }
    
    // Metodi specifici per Chef
    public Optional<Chef> autenticaChef(String username, String password) {
        // Logica autenticazione
    }
}
```

### 3. **Service Layer Pattern (AGGIORNATO)**

```java
@Service
public class UninaFoodLabService {
    private final ChefDAO chefDAO;
    private final CorsoDAO corsoDAO;
    private final SessioneDAO sessioneDAO;
    private final UtenteDAO utenteDAO;           // ✅ NUOVO
    private final IscrizioneDAO iscrizioneDAO;   // ✅ NUOVO
    
    // ✅ ELIMINATA DUPLICAZIONE - Un solo service unificato
    
    // Gestione Corsi
    public boolean creaCorso(Corso corso) { /* ... */ }
    
    // ✅ NUOVO: Gestione Utenti
    public boolean creaUtente(Utente utente) { /* ... */ }
    public boolean aggiornaUtente(Utente utente) { /* ... */ }
    public boolean disattivaUtente(Integer id) { /* ... */ }
    
    // ✅ NUOVO: Gestione Iscrizioni  
    public boolean iscriviUtenteACorso(Integer utente_id, Integer corso_id, String note) { /* ... */ }
    public boolean annullaIscrizione(Integer iscrizioneId, String motivo) { /* ... */ }
    
    // Incapsula tutta la business logic in un servizio unificato
}
```

### 4. **Helper Pattern per UI (NUOVO)**

```java
// Gestione dialog specializzati
public class DialogHelper {
    public void mostraDialogNuovoUtente(Runnable onSuccess) { /* ... */ }
    public void mostraDialogModificaUtente(Utente utente, Runnable onSuccess) { /* ... */ }
    public void mostraDialogNuovaIscrizione(Runnable onSuccess) { /* ... */ }
}

// Gestione tabelle specializzate  
public class TableManager {
    public void configuraTabellaUtenti(TableView<Utente> table) { /* ... */ }
    public void configuraTabellaIscrizioni(TableView<Iscrizione> table) { /* ... */ }
    public void caricaUtenti() { /* ... */ }
    public void caricaIscrizioni() { /* ... */ }
}
```

## 🏛️ Struttura a Livelli

### 📱 **Presentation Layer**
**Responsabilità:** Gestione dell'interfaccia utente e interazioni

```
src/main/java/it/unina/uninafoodlab/controller/
├── LoginController.java      # Gestione autenticazione
├── MainController.java       # Gestione principale corsi
└── GraficiController.java    # Gestione report e grafici

src/main/resources/fxml/
├── LoginView.fxml           # Interface login
├── MainView.fxml            # Interface principale
└── GraficiView.fxml         # Interface report
```

**Caratteristiche:**
- Controller JavaFX per gestione eventi UI
- Binding bidirezionale con i modelli
- Validazione input lato client
- Gestione feedback utente (alert, conferme)

### ⚙️ **Business Layer (AGGIORNATO)**
**Responsabilità:** Logica di business e coordinamento

```
src/main/java/it/unina/uninafoodlab/service/
├── UninaFoodLabService.java       # ✅ Service unificato (era duplicato)
├── ValidationResult.java         # Supporto validazioni
└── controller/helper/             # ✅ NUOVO: Helper per UI
    ├── DialogHelper.java          # Gestione dialog complessi
    ├── TableManager.java          # Gestione tabelle JavaFX
    ├── MessageHelper.java         # Gestione messaggi utente
    ├── FormManager.java           # Gestione form complessi
    └── ValidationUtils.java       # Validazioni centralizzate
```

**Funzionalità Chiave:**
- ✅ **Service Unificato**: Eliminata duplicazione `UninaFoodLabServiceExtended`
- ✅ **Gestione Utenti**: CRUD completo per utenti e partecipanti
- ✅ **Gestione Iscrizioni**: Sistema completo di iscrizioni ai corsi
- Orchestrazione delle operazioni complesse
- Validazione delle business rules
- Gestione transazioni
- Coordinamento tra diversi DAO

```java
// Esempio di business logic complessa
public boolean creaCorso(Corso corso) {
    // 1. Validazione business rules
    ValidationResult validation = validaCorso(corso);
    if (!validation.isValid()) {
        throw new IllegalArgumentException(validation.getErrorMessage());
    }
    
    // 2. Persistenza corso
    Corso corsoPersistito = corsoDAO.save(corso);
    
    // 3. Generazione automatica sessioni
    if (corsoPersistito != null) {
        generaSessioniCorso(corsoPersistito);
        return true;
    }
    
    return false;
}
```

### 🗃️ **Data Access Layer**
**Responsabilità:** Accesso e persistenza dati

```
src/main/java/it/unina/uninafoodlab/database/
├── DatabaseManager.java     # Gestione connessioni
├── ChefDAO.java            # Accesso dati chef
├── CorsoDAO.java           # Accesso dati corsi
├── SessioneDAO.java        # Accesso dati sessioni
├── RicettaDAO.java         # Accesso dati ricette
├── UtenteDAO.java          # ✅ NUOVO: Accesso dati utenti
├── IscrizioneDAO.java      # ✅ NUOVO: Accesso dati iscrizioni
└── ReportDAO.java          # Generazione report
```

**Design Pattern:** Repository Pattern
```java
public class CorsoDAO {
    // CRUD operations
    public Corso save(Corso corso) { /* */ }
    public Optional<Corso> findById(Integer id) { /* */ }
    public List<Corso> findAll() { /* */ }
    public boolean delete(Integer id) { /* */ }
    
    // Query specifiche del dominio
    public List<Corso> findByChefId(Integer chefId) { /* */ }
    public List<Corso> findByCategoria(Integer categoria_id) { /* */ }
    public List<Corso> findByChefIdAndCategoria(Integer chefId, Integer categoria_id) { /* */ }
}
```

### 📊 **Model Layer**
**Responsabilità:** Rappresentazione del dominio

```
src/main/java/it/unina/uninafoodlab/model/
├── Chef.java              # Entità chef/istruttore
├── Corso.java             # Entità corso di cucina
├── CategoriaCorso.java    # Categoria tematica
├── Sessione.java          # Sessione teorica/pratica
├── Ricetta.java           # Ricetta culinaria
├── Utente.java            # ✅ NUOVO: Entità utente/partecipante
├── Iscrizione.java        # ✅ NUOVO: Entità iscrizione utente-corso
└── ReportMensile.java     # Report statistiche
```

## 🔌 Gestione delle Connessioni

### Connection Pooling con HikariCP

```java
public class DatabaseManager {
    private static HikariDataSource dataSource;
    
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/uninafoodlab");
        config.setUsername("postgres");
        config.setPassword("password");
        
        // Configurazione pool
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

**Vantaggi:**
- **Performance**: Pool di connessioni riutilizzabili
- **Scalabilità**: Gestione efficiente delle risorse
- **Affidabilità**: Connection testing automatico
- **Monitoring**: Metriche di utilizzo integrate

## 🎨 Architettura dell'Interfaccia

### FXML + Controller Pattern

```xml
<!-- MainView.fxml -->
<BorderPane xmlns:fx="http://javafx.com/fxml/1" 
            fx:controller="it.unina.uninafoodlab.controller.MainController">
    <top>
        <Label fx:id="lblBenvenuto" text="Benvenuto, Chef" />
    </top>
    <center>
        <TabPane fx:id="mainTabPane">
            <Tab text="Corsi">
                <TableView fx:id="tabellaCorsi">
                    <!-- Configurazione colonne -->
                </TableView>
            </Tab>
        </TabPane>
    </center>
</BorderPane>
```

```java
// MainController.java
public class MainController implements Initializable {
    @FXML private Label lblBenvenuto;
    @FXML private TabPane mainTabPane;
    @FXML private TableView<Corso> tabellaCorsi;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configuraTabelleEColonne();
        configuraPulsanti();
        caricaDatiIniziali();
    }
}
```

## 📈 Gestione degli Eventi

### Observer Pattern per Aggiornamenti UI

```java
// Listener per selezione corso
tabellaCorsi.getSelectionModel().selectedItemProperty().addListener(
    (obs, oldSelection, newSelection) -> {
        if (newSelection != null) {
            caricaSessioniCorso(newSelection.getId());
        }
    }
);
```

### Command Pattern per Azioni Utente

```java
// Configurazione pulsanti con lambda expressions
btnNuovoCorso.setOnAction(e -> mostraFormNuovoCorso());
btnModificaCorso.setOnAction(e -> modificaCorsoSelezionato());
btnEliminaCorso.setOnAction(e -> eliminaCorsoSelezionato());
```

## 🔒 Gestione degli Errori

### Exception Handling Strutturato

```java
public class UninaFoodLabService {
    private static final Logger logger = LoggerFactory.getLogger(UninaFoodLabService.class);
    
    public boolean creaCorso(Corso corso) {
        try {
            // Business logic
            return true;
        } catch (ValidationException e) {
            logger.warn("Validazione fallita per corso: {}", corso.getTitolo(), e);
            throw e; // Re-throw per gestione UI
        } catch (SQLException e) {
            logger.error("Errore database durante creazione corso", e);
            return false; // Gestione graceful
        } catch (Exception e) {
            logger.error("Errore imprevisto durante creazione corso", e);
            return false;
        }
    }
}
```

### Validazione Multi-Livello

```java
// 1. Validazione lato client (UI)
private boolean validaFormCorso() {
    if (txtTitoloCorso.getText().trim().isEmpty()) {
        mostraErrore("Il titolo del corso è obbligatorio");
        return false;
    }
    return true;
}

// 2. Validazione business logic
private ValidationResult validaCorso(Corso corso) {
    if (corso.getDataInizio().isBefore(LocalDate.now())) {
        return ValidationResult.invalid("La data di inizio non può essere nel passato");
    }
    return ValidationResult.valid();
}

// 3. Validazione database (constraints)
-- Definiti nello schema SQL
ALTER TABLE corsi ADD CONSTRAINT chk_prezzo_positivo CHECK (prezzo >= 0);
```

## 🔄 Dependency Injection

### Constructor Injection Pattern

```java
public class UninaFoodLabService {
    private final ChefDAO chefDAO;
    private final CorsoDAO corsoDAO;
    private final SessioneDAO sessioneDAO;
    
    public UninaFoodLabService() {
        // Dependency injection manuale
        this.chefDAO = new ChefDAO();
        this.corsoDAO = new CorsoDAO();
        this.sessioneDAO = new SessioneDAO();
    }
    
    // Metodi business che utilizzano le dipendenze
}
```

## 📊 Monitoring e Logging

### Structured Logging con SLF4J

```java
// logback.xml configuration
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>logs/uninafoodlab.log</file>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="FILE" />
    </root>
</configuration>
```

```java
// Utilizzo nei componenti
private static final Logger logger = LoggerFactory.getLogger(CorsoDAO.class);

public Corso save(Corso corso) {
    logger.info("Salvando nuovo corso: {}", corso.getTitolo());
    try {
        // Operazione database
        logger.debug("Corso salvato con ID: {}", corso.getId());
        return corso;
    } catch (SQLException e) {
        logger.error("Errore durante salvataggio corso: {}", corso.getTitolo(), e);
        throw new PersistenceException("Impossibile salvare il corso", e);
    }
}
```

## 🎯 Principi SOLID Applicati

### 1. **Single Responsibility Principle (SRP)**
- `ChefDAO`: Solo gestione persistenza chef
- `LoginController`: Solo gestione autenticazione UI
- `ValidationResult`: Solo risultati validazione

### 2. **Open/Closed Principle (OCP)**
- Interfacce DAO estendibili senza modificare esistente
- Strategy pattern per diversi tipi di report

### 3. **Liskov Substitution Principle (LSP)**
- Implementazioni DAO intercambiabili
- Polimorfismo nei controller

### 4. **Interface Segregation Principle (ISP)**
- Interfacce specifiche per ogni responsabilità
- Evitate interfacce "fat"

### 5. **Dependency Inversion Principle (DIP)**
- Service dipende da astrazioni DAO
- Controller dipende da abstrazioni Service

---

**Prossimo:** [Database Design](./03-database.md)
