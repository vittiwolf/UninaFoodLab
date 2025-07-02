# ⚙️ API e Servizi - UninaFoodLab

## 🎯 Architettura dei Servizi

UninaFoodLab implementa un'architettura a servizi strutturata su tre livelli principali:

1. **Service Layer** - Logica di business e orchestrazione
2. **DAO Layer** - Accesso ai dati e persistenza  
3. **Model Layer** - Rappresentazione del dominio

## 🔧 Service Layer

### UninaFoodLabService - Core Business Logic

Il service principale coordina tutte le operazioni di business del sistema.

```java
@Service
public class UninaFoodLabService {
    private static final Logger logger = LoggerFactory.getLogger(UninaFoodLabService.class);
    
    // Dependency injection dei DAO
    private final ChefDAO chefDAO;
    private final CorsoDAO corsoDAO;
    private final SessioneDAO sessioneDAO;
    private final RicettaDAO ricettaDAO;
    private final ReportDAO reportDAO;
    private final UtenteDAO utenteDAO;        // ✅ NUOVO
    private final IscrizioneDAO iscrizioneDAO; // ✅ NUOVO

    public UninaFoodLabService() {
        this.chefDAO = new ChefDAO();
        this.corsoDAO = new CorsoDAO();
        this.sessioneDAO = new SessioneDAO();
        this.ricettaDAO = new RicettaDAO();
        this.reportDAO = new ReportDAO();
        this.utenteDAO = new UtenteDAO();
        this.iscrizioneDAO = new IscrizioneDAO();
    }
}
```

### 🔐 Gestione Autenticazione

#### autenticaChef()

```java
/**
 * Autentica uno chef con username e password
 * @param username Username del chef
 * @param password Password in chiaro
 * @return Optional contenente il chef se autenticazione riuscita
 * @throws IllegalArgumentException se parametri invalidi
 */
public Optional<Chef> autenticaChef(String username, String password) {
    // Validazione input
    if (username == null || username.trim().isEmpty()) {
        throw new IllegalArgumentException("Username non può essere vuoto");
    }
    if (password == null || password.trim().isEmpty()) {
        throw new IllegalArgumentException("Password non può essere vuota");
    }

    logger.info("Tentativo di autenticazione per username: {}", username);
    
    try {
        Optional<Chef> chef = chefDAO.autenticaChef(username.trim(), password);
        
        if (chef.isPresent()) {
            logger.info("Autenticazione riuscita per chef: {} {}", 
                       chef.get().getNome(), chef.get().getCognome());
        } else {
            logger.warn("Autenticazione fallita per username: {}", username);
        }
        
        return chef;
    } catch (Exception e) {
        logger.error("Errore durante autenticazione per username: {}", username, e);
        return Optional.empty();
    }
}
```

### 📚 Gestione Corsi

#### creaCorso()

```java
/**
 * Crea un nuovo corso con validazione completa
 * @param corso Oggetto corso da creare
 * @return true se creazione riuscita, false altrimenti
 */
public boolean creaCorso(Corso corso) {
    logger.info("Creazione nuovo corso: {}", corso.getTitolo());
    
    try {
        // 1. Validazione business rules
        ValidationResult validation = validaCorso(
            corso.getTitolo(), 
            corso.getDataInizio(), 
            corso.getFrequenza(), 
            corso.getNumeroSessioni(), 
            corso.getPrezzo()
        );
        
        if (!validation.isValid()) {
            logger.warn("Validazione fallita per corso: {} - {}", 
                       corso.getTitolo(), validation.getErrorMessage());
            throw new IllegalArgumentException(validation.getErrorMessage());
        }
        
        // 2. Persistenza corso
        Corso nuovoCorso = corsoDAO.save(corso);
        
        // 3. Generazione automatica sessioni
        if (nuovoCorso != null && nuovoCorso.getId() != null) {
            int sessioniGenerate = generaSessioniCorso(nuovoCorso);
            logger.info("Corso creato con {} sessioni generate", sessioniGenerate);
            return true;
        }
        
        return false;
        
    } catch (ValidationException e) {
        logger.error("Errore di validazione durante creazione corso", e);
        throw e; // Re-throw per gestione UI
    } catch (Exception e) {
        logger.error("Errore imprevisto durante creazione corso", e);
        return false;
    }
}
```

#### Validazione Business Rules

```java
/**
 * Valida i dati di un corso secondo le business rules
 */
private ValidationResult validaCorso(String titolo, LocalDate data_inizio, 
                                    String frequenza, Integer numero_sessioni, 
                                    BigDecimal prezzo) {
    
    // Validazione titolo
    if (titolo == null || titolo.trim().isEmpty()) {
        return ValidationResult.invalid("Il titolo del corso è obbligatorio");
    }
    if (titolo.length() > 200) {
        return ValidationResult.invalid("Il titolo non può superare 200 caratteri");
    }
    
    // Validazione data inizio
    if (data_inizio == null) {
        return ValidationResult.invalid("La data di inizio è obbligatoria");
    }
    if (data_inizio.isBefore(LocalDate.now())) {
        return ValidationResult.invalid("La data di inizio non può essere nel passato");
    }
    if (data_inizio.isAfter(LocalDate.now().plusYears(2))) {
        return ValidationResult.invalid("La data di inizio non può essere oltre 2 anni nel futuro");
    }
    
    // Validazione frequenza
    if (frequenza == null || frequenza.trim().isEmpty()) {
        return ValidationResult.invalid("La frequenza è obbligatoria");
    }
    List<String> frequenzeValide = List.of("SETTIMANALE", "BISETTIMANALE", "MENSILE");
    if (!frequenzeValide.contains(frequenza.toUpperCase())) {
        return ValidationResult.invalid("Frequenza non valida. Valori ammessi: " + 
                                       String.join(", ", frequenzeValide));
    }
    
    // Validazione numero sessioni
    if (numero_sessioni == null || numero_sessioni <= 0) {
        return ValidationResult.invalid("Il numero di sessioni deve essere maggiore di zero");
    }
    if (numero_sessioni > 50) {
        return ValidationResult.invalid("Il numero massimo di sessioni è 50");
    }
    
    // Validazione prezzo
    if (prezzo != null && prezzo.compareTo(BigDecimal.ZERO) < 0) {
        return ValidationResult.invalid("Il prezzo non può essere negativo");
    }
    if (prezzo != null && prezzo.compareTo(new BigDecimal("10000")) > 0) {
        return ValidationResult.invalid("Il prezzo non può superare €10.000");
    }
    
    return ValidationResult.valid();
}
```

#### Generazione Automatica Sessioni

```java
/**
 * Genera automaticamente le sessioni per un corso
 * @param corso Corso per cui generare le sessioni
 * @return Numero di sessioni generate
 */
private int generaSessioniCorso(Corso corso) {
    logger.info("Generazione sessioni per corso: {}", corso.getTitolo());
    
    try {
        // Calcola intervallo tra sessioni basato su frequenza
        int intervalloGiorni = switch (corso.getFrequenza()) {
            case "SETTIMANALE" -> 7;
            case "BISETTIMANALE" -> 14;
            case "MENSILE" -> 30;
            default -> 7;
        };
        
        List<Sessione> sessioni = new ArrayList<>();
        LocalDate dataSessione = corso.getDataInizio();
        
        for (int i = 1; i <= corso.getNumeroSessioni(); i++) {
            Sessione sessione = new Sessione();
            sessione.setCorsoId(corso.getId());
            sessione.setNumeroSessione(i);
            sessione.setTitolo("Sessione " + i + " - " + corso.getTitolo());
            sessione.setDataSessione(dataSessione);
            
            // Alternanza sessioni teoriche/pratiche (ogni 3a sessione è pratica)
            if (i % 3 == 0) {
                sessione.setTipo("PRATICA");
                sessione.setModalita("PRESENZA"); // Sessioni pratiche sempre in presenza
            } else {
                sessione.setTipo("TEORICA");
                sessione.setModalita("ONLINE"); // Default teoriche online
            }
            
            sessione.setCompletata(false);
            sessioni.add(sessione);
            
            // Calcola data prossima sessione
            dataSessione = dataSessione.plusDays(intervalloGiorni);
        }
        
        // Salva tutte le sessioni
        return sessioneDAO.saveAll(sessioni).size();
        
    } catch (Exception e) {
        logger.error("Errore durante generazione sessioni per corso: {}", 
                    corso.getTitolo(), e);
        return 0;
    }
}
```

### 🗓️ Gestione Sessioni

#### getSessioniByCorso()

```java
/**
 * Recupera tutte le sessioni di un corso ordinata per data
 * @param corso_id ID del corso
 * @return Lista sessioni del corso
 */
public List<Sessione> getSessioniByCorso(int corso_id) {
    logger.debug("Recupero sessioni per corso ID: {}", corso_id);
    
    try {
        List<Sessione> sessioni = sessioneDAO.findByCorsoId(corso_id);
        logger.debug("Trovate {} sessioni per corso ID: {}", sessioni.size(), corso_id);
        return sessioni;
    } catch (Exception e) {
        logger.error("Errore nel recupero sessioni per corso ID: {}", corso_id, e);
        return Collections.emptyList();
    }
}
```

#### associaRicettaASessione()

```java
/**
 * Associa una ricetta a una sessione pratica
 * @param sessione_id ID della sessione
 * @param ricetta_id ID della ricetta
 * @param ordineEsecuzione Ordine di esecuzione nella sessione
 * @return true se associazione riuscita
 */
public boolean associaRicettaASessione(Integer sessione_id, Integer ricetta_id, 
                                      Integer ordineEsecuzione) {
    logger.info("Associazione ricetta {} a sessione {} con ordine {}", 
               ricetta_id, sessione_id, ordineEsecuzione);
    
    try {
        // Verifica che la sessione esista ed è pratica
        Optional<Sessione> sessione = sessioneDAO.findById(sessione_id);
        if (sessione.isEmpty()) {
            throw new IllegalArgumentException("Sessione non trovata con ID: " + sessione_id);
        }
        
        if (!sessione.get().isPratica()) {
            throw new IllegalArgumentException(
                "Le ricette possono essere associate solo alle sessioni pratiche");
        }
        
        // Verifica che la ricetta esista
        Optional<Ricetta> ricetta = ricettaDAO.findById(ricetta_id);
        if (ricetta.isEmpty()) {
            throw new IllegalArgumentException("Ricetta non trovata con ID: " + ricetta_id);
        }
        
        // Esegui associazione
        boolean result = sessioneDAO.associaRicetta(sessione_id, ricetta_id, ordineEsecuzione);
        
        if (result) {
            logger.info("Ricetta '{}' associata con successo alla sessione '{}'", 
                       ricetta.get().getNome(), sessione.get().getTitolo());
        }
        
        return result;
        
    } catch (Exception e) {
        logger.error("Errore durante associazione ricetta {} a sessione {}", 
                    ricetta_id, sessione_id, e);
        return false;
    }
}
```

### 🍝 Gestione Ricette

#### getAllRicette()

```java
/**
 * Recupera tutte le ricette disponibili nel sistema
 * @return Lista completa ricette
 */
public List<Ricetta> getAllRicette() {
    logger.debug("Recupero di tutte le ricette");
    
    try {
        List<Ricetta> ricette = ricettaDAO.findAll();
        logger.debug("Recuperate {} ricette totali", ricette.size());
        return ricette;
    } catch (Exception e) {
        logger.error("Errore nel recupero di tutte le ricette", e);
        return Collections.emptyList();
    }
}
```

#### cercaRicette()

```java
/**
 * Cerca ricette per nome con ricerca full-text
 * @param nome Termine di ricerca
 * @return Lista ricette corrispondenti
 */
public List<Ricetta> cercaRicette(String nome) {
    if (nome == null || nome.trim().isEmpty()) {
        return getAllRicette();
    }
    
    logger.debug("Ricerca ricette con termine: '{}'", nome);
    
    try {
        List<Ricetta> ricette = ricettaDAO.searchByNome(nome.trim());
        logger.debug("Trovate {} ricette per termine: '{}'", ricette.size(), nome);
        return ricette;
    } catch (Exception e) {
        logger.error("Errore nella ricerca ricette per termine: '{}'", nome, e);
        return Collections.emptyList();
    }
}
```

### 📊 Gestione Report

#### generaReportMensile()

```java
/**
 * Genera report mensile per uno chef con tutte le statistiche
 * @param chefId ID dello chef (null per report globale)
 * @param anno Anno del report
 * @param mese Mese del report (1-12)
 * @return ReportMensile con statistiche complete
 */
public ReportMensile generaReportMensileWrapper(Integer chefId, int anno, int mese) {
    logger.info("Generazione report mensile per chef: {}, {}/{}", chefId, mese, anno);
    
    try {
        Optional<ReportMensile> reportOpt = reportDAO.generaReportMensile(chefId, mese, anno);
        
        if (reportOpt.isPresent()) {
            ReportMensile report = reportOpt.get();
            logger.info("Report generato con {} corsi totali", report.getNumeroCorsiTotali());
            return report;
        } else {
            logger.warn("Nessun dato trovato per report {}/{} chef: {}", mese, anno, chefId);
            return creaReportVuoto(anno, mese);
        }
        
    } catch (Exception e) {
        logger.error("Errore durante generazione report mensile per chef: {}, {}/{}", 
                    chefId, mese, anno, e);
        return creaReportVuoto(anno, mese);
    }
}

/**
 * Crea un report vuoto per gestire casi senza dati
 */
private ReportMensile creaReportVuoto(int anno, int mese) {
    ReportMensile report = new ReportMensile();
    report.setAnno(anno);
    report.setMese(mese);
    report.setNumeroCorsiTotali(0);
    report.setNumeroSessioniOnline(0);
    report.setNumeroSessioniPratiche(0);
    report.setMediaRicettePerSessione(0.0);
    return report;
}
```

## 👥 Gestione Utenti

### creaUtente()

```java
/**
 * Crea un nuovo utente con validazione completa
 * @param utente Oggetto utente da creare
 * @return true se creazione riuscita, false altrimenti
 */
public boolean creaUtente(Utente utente) {
    logger.info("Creazione nuovo utente: {} {}", utente.getNome(), utente.getCognome());
    
    try {
        // 1. Validazione dati
        if (utente.getNome() == null || utente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome utente richiesto");
        }
        if (utente.getCognome() == null || utente.getCognome().trim().isEmpty()) {
            throw new IllegalArgumentException("Cognome utente richiesto");
        }
        if (!isValidEmail(utente.getEmail())) {
            throw new IllegalArgumentException("Email non valida");
        }
        
        // 2. Verifica unicità email
        List<Utente> utentiEsistenti = utenteDAO.findByEmail(utente.getEmail());
        if (!utentiEsistenti.isEmpty()) {
            throw new IllegalArgumentException("Email già utilizzata da un altro utente");
        }
        
        // 3. Salvataggio
        Utente nuovoUtente = utenteDAO.save(utente);
        boolean successo = nuovoUtente != null && nuovoUtente.getId() != null;
        
        if (successo) {
            logger.info("Utente creato con successo: ID {}", nuovoUtente.getId());
        }
        
        return successo;
    } catch (Exception e) {
        logger.error("Errore nella creazione utente", e);
        return false;
    }
}
```

### aggiornaUtente()

```java
/**
 * Aggiorna un utente esistente
 * @param utente Oggetto utente con dati aggiornati
 * @return true se aggiornamento riuscito
 */
public boolean aggiornaUtente(Utente utente) {
    logger.info("Aggiornamento utente: ID {}", utente.getId());
    
    try {
        // Validazioni come per creaUtente
        Utente utenteAggiornato = utenteDAO.update(utente);
        boolean successo = utenteAggiornato != null;
        
        if (successo) {
            logger.info("Utente aggiornato con successo: ID {}", utente.getId());
        }
        
        return successo;
    } catch (Exception e) {
        logger.error("Errore nell'aggiornamento utente ID: {}", utente.getId(), e);
        return false;
    }
}
```

### disattivaUtente()

```java
/**
 * Disattiva un utente (soft delete)
 * @param id ID dell'utente da disattivare
 * @return true se disattivazione riuscita
 */
public boolean disattivaUtente(Integer id) {
    logger.info("Disattivazione utente: ID {}", id);
    
    try {
        Optional<Utente> utente = utenteDAO.findById(id);
        if (utente.isEmpty()) {
            throw new IllegalArgumentException("Utente non trovato");
        }
        
        boolean successo = utenteDAO.disattiva(id);
        
        if (successo) {
            logger.info("Utente disattivato con successo: ID {}", id);
        }
        
        return successo;
    } catch (Exception e) {
        logger.error("Errore nella disattivazione utente ID: {}", id, e);
        return false;
    }
}
```

## 📝 Gestione Iscrizioni

### iscriviUtenteACorso()

```java
/**
 * Iscrive un utente a un corso con validazioni complete
 * @param utente_id ID dell'utente
 * @param corso_id ID del corso
 * @param note Note aggiuntive per l'iscrizione
 * @return true se iscrizione riuscita
 */
public boolean iscriviUtenteACorso(Integer utente_id, Integer corso_id, String note) {
    logger.info("Iscrizione utente {} al corso {}", utente_id, corso_id);
    
    try {
        // 1. Validazione parametri
        if (utente_id == null || corso_id == null) {
            throw new IllegalArgumentException("ID utente e corso richiesti");
        }
        
        // 2. Verifica che l'utente esista ed sia attivo
        Optional<Utente> utente = utenteDAO.findById(utente_id);
        if (utente.isEmpty() || !utente.get().isAttivo()) {
            throw new IllegalArgumentException("Utente non trovato o non attivo");
        }
        
        // 3. Verifica che il corso esista
        Optional<Corso> corso = corsoDAO.findById(corso_id);
        if (corso.isEmpty()) {
            throw new IllegalArgumentException("Corso non trovato");
        }
        
        // 4. Verifica che l'utente non sia già iscritto
        if (iscrizioneDAO.isUtenteIscritto(utente_id, corso_id)) {
            throw new IllegalArgumentException("Utente già iscritto a questo corso");
        }
        
        // 5. Crea l'iscrizione
        Iscrizione iscrizione = new Iscrizione(utente_id, corso_id, note);
        Iscrizione nuovaIscrizione = iscrizioneDAO.save(iscrizione);
        
        boolean successo = nuovaIscrizione != null;
        if (successo) {
            logger.info("Iscrizione creata con successo: ID {}", nuovaIscrizione.getId());
        }
        
        return successo;
    } catch (Exception e) {
        logger.error("Errore nell'iscrizione utente {} al corso {}", utente_id, corso_id, e);
        return false;
    }
}
```

### annullaIscrizione()

```java
/**
 * Annulla un'iscrizione esistente
 * @param iscrizioneId ID dell'iscrizione
 * @param motivo Motivo dell'annullamento
 * @return true se annullamento riuscito
 */
public boolean annullaIscrizione(Integer iscrizioneId, String motivo) {
    logger.info("Annullamento iscrizione: ID {}", iscrizioneId);
    
    try {
        boolean successo = iscrizioneDAO.annullaIscrizione(iscrizioneId, motivo);
        
        if (successo) {
            logger.info("Iscrizione annullata con successo: ID {}", iscrizioneId);
        }
        
        return successo;
    } catch (Exception e) {
        logger.error("Errore nell'annullamento iscrizione ID: {}", iscrizioneId, e);
        return false;
    }
}
```

## 🗃️ DAO Layer - Accesso ai Dati

### ChefDAO - Gestione Chef

#### autenticaChef()

```java
/**
 * Autentica un chef verificando username e password
 * @param username Username del chef
 * @param password Password in chiaro
 * @return Optional contenente il chef se autenticazione riuscita
 */
public Optional<Chef> autenticaChef(String username, String password) {
    String sql = """
        SELECT id, nome, cognome, username, email, telefono, specializzazione, created_at
        FROM chef 
        WHERE username = ? AND password_hash = crypt(?, password_hash)
        """;
    
    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username);
        stmt.setString(2, password);
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                Chef chef = mapResultSetToChef(rs);
                logger.debug("Chef autenticato: {} {}", chef.getNome(), chef.getCognome());
                return Optional.of(chef);
            }
        }
        
        logger.debug("Autenticazione fallita per username: {}", username);
        return Optional.empty();
        
    } catch (SQLException e) {
        logger.error("Errore durante autenticazione chef: {}", username, e);
        return Optional.empty();
    }
}
```

### CorsoDAO - Gestione Corsi

#### findByChefId()

```java
/**
 * Trova tutti i corsi di uno chef specifico
 * @param chefId ID del chef
 * @return Lista corsi del chef
 */
public List<Corso> findByChefId(Integer chefId) {
    List<Corso> corsi = new ArrayList<>();
    String sql = """
        SELECT c.id, c.chef_id, c.categoria_id, c.titolo, c.descrizione, 
               c.data_inizio, c.frequenza, c.numero_sessioni, c.prezzo, c.created_at,
               ch.nome || ' ' || ch.cognome as nome_chef,
               cat.nome as nome_categoria
        FROM corsi c
        JOIN chef ch ON c.chef_id = ch.id
        JOIN categorie_corsi cat ON c.categoria_id = cat.id
        WHERE c.chef_id = ?
        ORDER BY c.data_inizio DESC
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, chefId);

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                corsi.add(mapResultSetToCorso(rs));
            }
        }

        logger.debug("Trovati {} corsi per chef ID: {}", corsi.size(), chefId);
        
    } catch (SQLException e) {
        logger.error("Errore durante il recupero dei corsi per chef ID: " + chefId, e);
    }

    return corsi;
}
```

#### save()

```java
/**
 * Salva un nuovo corso o aggiorna esistente
 * @param corso Corso da salvare
 * @return Corso salvato con ID assegnato
 */
public Corso save(Corso corso) {
    if (corso.getId() == null) {
        return insert(corso);
    } else {
        return update(corso);
    }
}

private Corso insert(Corso corso) {
    String sql = """
        INSERT INTO corsi (chef_id, categoria_id, titolo, descrizione, data_inizio, 
                          frequenza, numero_sessioni, prezzo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        RETURNING id, created_at
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, corso.getChefId());
        stmt.setInt(2, corso.getCategoriaId());
        stmt.setString(3, corso.getTitolo());
        stmt.setString(4, corso.getDescrizione());
        stmt.setDate(5, Date.valueOf(corso.getDataInizio()));
        stmt.setString(6, corso.getFrequenza());
        stmt.setInt(7, corso.getNumeroSessioni());
        stmt.setBigDecimal(8, corso.getPrezzo());

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                corso.setId(rs.getInt("id"));
                corso.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                logger.info("Corso salvato con ID: {}", corso.getId());
                return corso;
            }
        }

    } catch (SQLException e) {
        logger.error("Errore durante il salvataggio del corso: {}", corso.getTitolo(), e);
        throw new PersistenceException("Impossibile salvare il corso", e);
    }

    throw new PersistenceException("Salvataggio corso fallito: nessun ID ritornato");
}
```

### SessioneDAO - Gestione Sessioni

#### associaRicetta()

```java
/**
 * Associa una ricetta a una sessione
 * @param sessione_id ID sessione
 * @param ricetta_id ID ricetta  
 * @param ordineEsecuzione Ordine di esecuzione
 * @return true se associazione riuscita
 */
public boolean associaRicetta(Integer sessione_id, Integer ricetta_id, Integer ordineEsecuzione) {
    String sql = """
        INSERT INTO sessioni_ricette (sessione_id, ricetta_id, ordine_esecuzione)
        VALUES (?, ?, ?)
        ON CONFLICT (sessione_id, ricetta_id) 
        DO UPDATE SET ordine_esecuzione = EXCLUDED.ordine_esecuzione
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, sessione_id);
        stmt.setInt(2, ricetta_id);
        stmt.setInt(3, ordineEsecuzione);

        int rowsAffected = stmt.executeUpdate();
        boolean success = rowsAffected > 0;
        
        if (success) {
            logger.debug("Ricetta {} associata a sessione {} con ordine {}", 
                        ricetta_id, sessione_id, ordineEsecuzione);
        }
        
        return success;

    } catch (SQLException e) {
        logger.error("Errore durante associazione ricetta {} a sessione {}", 
                    ricetta_id, sessione_id, e);
        return false;
    }
}
```

### ReportDAO - Generazione Report

#### generaReportMensile()

```java
/**
 * Genera report mensile completo per uno chef
 * @param chefId ID chef (null per report globale)
 * @param mese Mese (1-12)
 * @param anno Anno
 * @return Optional con report se dati presenti
 */
public Optional<ReportMensile> generaReportMensile(Integer chefId, int mese, int anno) {
    String sql = """
        SELECT 
            COUNT(DISTINCT c.id) as corsi_totali,
            COUNT(DISTINCT CASE WHEN s.modalita = 'ONLINE' THEN s.id END) as sessioni_online,
            COUNT(DISTINCT CASE WHEN s.tipo = 'PRATICA' THEN s.id END) as sessioni_pratiche,
            COALESCE(AVG(ricette_count.cnt), 0) as media_ricette_sessione
        FROM corsi c
            LEFT JOIN sessioni s ON c.id = s.corso_id
            LEFT JOIN (
                SELECT sr.sessione_id, COUNT(*) as cnt
                FROM sessioni_ricette sr
                GROUP BY sr.sessione_id
            ) ricette_count ON s.id = ricette_count.sessione_id
        WHERE ($1 IS NULL OR c.chef_id = $1)
            AND EXTRACT(YEAR FROM c.created_at) = $2
            AND EXTRACT(MONTH FROM c.created_at) = $3
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        if (chefId != null) {
            stmt.setInt(1, chefId);
        } else {
            stmt.setNull(1, Types.INTEGER);
        }
        stmt.setInt(2, anno);
        stmt.setInt(3, mese);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next() && rs.getInt("corsi_totali") > 0) {
                ReportMensile report = new ReportMensile();
                report.setAnno(anno);
                report.setMese(mese);
                report.setNumeroCorsiTotali(rs.getInt("corsi_totali"));
                report.setNumeroSessioniOnline(rs.getInt("sessioni_online"));
                report.setNumeroSessioniPratiche(rs.getInt("sessioni_pratiche"));
                report.setMediaRicettePerSessione(rs.getDouble("media_ricette_sessione"));
                
                logger.debug("Report generato per chef {} - {}/{}: {} corsi", 
                           chefId, mese, anno, report.getNumeroCorsiTotali());
                
                return Optional.of(report);
            }
        }

    } catch (SQLException e) {
        logger.error("Errore durante generazione report mensile per chef {} - {}/{}", 
                    chefId, mese, anno, e);
    }

    return Optional.empty();
}
```

### UtenteDAO - Gestione Utenti

#### findAll()

```java
/**
 * Recupera tutti gli utenti attivi del sistema
 * @return Lista di tutti gli utenti attivi
 */
public List<Utente> findAll() {
    List<Utente> utenti = new ArrayList<>();
    String sql = """
        SELECT id, nome, cognome, email, telefono, data_nascita, 
               livello_esperienza, attivo, created_at
        FROM utenti 
        WHERE attivo = true
        ORDER BY cognome, nome
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            utenti.add(mapResultSetToUtente(rs));
        }

        logger.debug("Trovati {} utenti attivi", utenti.size());
    } catch (SQLException e) {
        logger.error("Errore durante il recupero degli utenti", e);
    }

    return utenti;
}
```

#### save()

```java
/**
 * Salva un nuovo utente nel database
 * @param utente Utente da salvare
 * @return Utente salvato con ID assegnato
 */
public Utente save(Utente utente) {
    String sql = """
        INSERT INTO utenti (nome, cognome, email, telefono, data_nascita, livello_esperienza)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        stmt.setString(1, utente.getNome());
        stmt.setString(2, utente.getCognome());
        stmt.setString(3, utente.getEmail());
        stmt.setString(4, utente.getTelefono());
        stmt.setDate(5, utente.getDataNascita() != null ? Date.valueOf(utente.getDataNascita()) : null);
        stmt.setString(6, utente.getLivelloEsperienza());

        int rowsAffected = stmt.executeUpdate();
        
        if (rowsAffected > 0) {
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    utente.setId(rs.getInt(1));
                    utente.setAttivo(true);
                    utente.setCreatedAt(LocalDateTime.now());
                    logger.info("Utente salvato con ID: {}", utente.getId());
                    return utente;
                }
            }
        }
    } catch (SQLException e) {
        logger.error("Errore durante il salvataggio dell'utente", e);
        throw new RuntimeException("Errore durante il salvataggio dell'utente", e);
    }

    throw new RuntimeException("Impossibile salvare l'utente");
}
```

#### disattiva()

```java
/**
 * Disattiva un utente (soft delete)
 * @param id ID dell'utente da disattivare
 * @return true se disattivazione riuscita
 */
public boolean disattiva(Integer id) {
    String sql = "UPDATE utenti SET attivo = false WHERE id = ?";

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, id);
        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            logger.info("Utente disattivato: ID {}", id);
            return true;
        } else {
            logger.warn("Nessun utente trovato con ID: {}", id);
            return false;
        }
    } catch (SQLException e) {
        logger.error("Errore durante la disattivazione dell'utente ID: " + id, e);
        return false;
    }
}
```

### IscrizioneDAO - Gestione Iscrizioni

#### findAllAttive()

```java
/**
 * Recupera tutte le iscrizioni attive con informazioni denormalizzate
 * @return Lista di iscrizioni attive
 */
public List<Iscrizione> findAllAttive() {
    List<Iscrizione> iscrizioni = new ArrayList<>();
    String sql = """
        SELECT i.id, i.utente_id, i.corso_id, i.data_iscrizione, i.stato, i.note,
               u.nome as nome_utente, u.cognome as cognome_utente, u.email as email_utente,
               c.titolo as titolo_corso
        FROM iscrizioni i
        JOIN utenti u ON i.utente_id = u.id
        JOIN corsi c ON i.corso_id = c.id
        WHERE i.stato IN ('ATTIVA', 'COMPLETATA')
        ORDER BY i.data_iscrizione DESC
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            iscrizioni.add(mapResultSetToIscrizione(rs));
        }

        logger.debug("Trovate {} iscrizioni attive", iscrizioni.size());
    } catch (SQLException e) {
        logger.error("Errore durante il recupero delle iscrizioni attive", e);
    }

    return iscrizioni;
}
```

#### isUtenteIscritto()

```java
/**
 * Verifica se un utente è già iscritto a un corso
 * @param utente_id ID dell'utente
 * @param corso_id ID del corso
 * @return true se l'utente è già iscritto
 */
public boolean isUtenteIscritto(Integer utente_id, Integer corso_id) {
    String sql = """
        SELECT COUNT(*) as count
        FROM iscrizioni 
        WHERE utente_id = ? AND corso_id = ? AND stato = 'ATTIVA'
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, utente_id);
        stmt.setInt(2, corso_id);

        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int count = rs.getInt("count");
                logger.debug("Utente {} iscritto a corso {}: {}", utente_id, corso_id, count > 0);
                return count > 0;
            }
        }
    } catch (SQLException e) {
        logger.error("Errore nella verifica iscrizione utente {} corso {}", utente_id, corso_id, e);
    }

    return false;
}
```

#### annullaIscrizione()

```java
/**
 * Annulla un'iscrizione cambiando lo stato
 * @param iscrizioneId ID dell'iscrizione
 * @param motivo Motivo dell'annullamento
 * @return true se annullamento riuscito
 */
public boolean annullaIscrizione(Integer iscrizioneId, String motivo) {
    String sql = """
        UPDATE iscrizioni 
        SET stato = 'ANNULLATA', note = COALESCE(note, '') || ? 
        WHERE id = ? AND stato = 'ATTIVA'
        """;

    try (Connection conn = DatabaseManager.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        String motivoCompleto = " [ANNULLATA: " + (motivo != null ? motivo : "Nessun motivo") + "]";
        stmt.setString(1, motivoCompleto);
        stmt.setInt(2, iscrizioneId);

        int rowsAffected = stmt.executeUpdate();

        if (rowsAffected > 0) {
            logger.info("Iscrizione annullata: ID {}", iscrizioneId);
            return true;
        } else {
            logger.warn("Nessuna iscrizione attiva trovata con ID: {}", iscrizioneId);
            return false;
        }
    } catch (SQLException e) {
        logger.error("Errore nell'annullamento iscrizione ID: " + iscrizioneId, e);
        return false;
    }
}
```

### Custom Exceptions

```java
// Eccezione per errori di validazione business
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Eccezione per errori di persistenza
public class PersistenceException extends RuntimeException {
    public PersistenceException(String message) {
        super(message);
    }
    
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Eccezione per accesso non autorizzato
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

### Exception Handling Pattern

```java
public class ServiceExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceExceptionHandler.class);
    
    public static <T> T handleDatabaseOperation(Supplier<T> operation, String operationName) {
        try {
            return operation.get();
        } catch (SQLException e) {
            logger.error("Errore database durante {}: {}", operationName, e.getMessage(), e);
            throw new PersistenceException("Errore durante " + operationName, e);
        } catch (Exception e) {
            logger.error("Errore imprevisto durante {}: {}", operationName, e.getMessage(), e);
            throw new RuntimeException("Errore imprevisto durante " + operationName, e);
        }
    }
}
```

---

**Prossimo:** [Interfaccia Utente](./05-interfaccia.md)
