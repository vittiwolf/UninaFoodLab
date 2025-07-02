package it.unina.uninafoodlab.service;

import it.unina.uninafoodlab.database.*;
import it.unina.uninafoodlab.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service per la gestione della logica di business di UninaFoodLab
 * Sistema per la gestione di corsi di cucina tematici
 */
public class UninaFoodLabService {
    private static final Logger logger = LoggerFactory.getLogger(UninaFoodLabService.class);
      private final ChefDAO chefDAO;
    private final CorsoDAO corsoDAO;
    private final SessioneDAO sessioneDAO;
    private final RicettaDAO ricettaDAO;
    private final ReportDAO reportDAO;
    private final UtenteDAO utenteDAO;
    private final IscrizioneDAO iscrizioneDAO;

    public UninaFoodLabService() {
        this.chefDAO = new ChefDAO();
        this.corsoDAO = new CorsoDAO();
        this.sessioneDAO = new SessioneDAO();
        this.ricettaDAO = new RicettaDAO();
        this.reportDAO = new ReportDAO();
        this.utenteDAO = new UtenteDAO();
        this.iscrizioneDAO = new IscrizioneDAO();
    }

    // === AUTENTICAZIONE ===
    
    /**
     * Autentica uno chef con username e password
     */
    public Optional<Chef> autenticaChef(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username non può essere vuoto");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password non può essere vuota");
        }

        return chefDAO.autenticaChef(username.trim(), password);
    }

    // === GESTIONE CORSI ===

    /**
     * Crea un nuovo corso
     */
    public Corso creaCorso(Integer chefId, Integer categoria_id, String titolo, String descrizione,
                          LocalDate data_inizio, String frequenza, Integer numero_sessioni, BigDecimal prezzo) {
        
        // Validazioni
        ValidationResult validation = validaCorso(titolo, data_inizio, frequenza, numero_sessioni, prezzo);
        if (!validation.isValid()) {
            throw new IllegalArgumentException(validation.getErrorMessage());
        }

        Corso corso = new Corso(chefId, categoria_id, titolo, descrizione, data_inizio, frequenza, numero_sessioni, prezzo);
        corso = corsoDAO.save(corso);
        
        // Genera automaticamente le sessioni del corso
        generaSessioniCorso(corso);
        
        logger.info("Corso creato con successo: {}", corso.getTitolo());
        return corso;
    }

    /**
     * Ottieni i corsi di uno chef con filtri opzionali
     */
    public List<Corso> getCorsiChef(Integer chefId, Integer categoria_id) {
        if (categoria_id != null) {
            return corsoDAO.findByChefIdAndCategoria(chefId, categoria_id);
        } else {
            return corsoDAO.findByChefId(chefId);
        }
    }

    /**
     * Ottieni tutte le categorie corsi disponibili
     */
    public List<CategoriaCorso> getCategorieCorsi() {
        return corsoDAO.findAllCategorie();
    }

    /**
     * Aggiorna un corso esistente
     */
    public boolean aggiornaCorso(Corso corso) {
        try {
            // Validazioni
            if (corso.getId() == null) {
                throw new IllegalArgumentException("ID corso è obbligatorio per l'aggiornamento");
            }
            if (corso.getTitolo() == null || corso.getTitolo().trim().isEmpty()) {
                throw new IllegalArgumentException("Il titolo del corso è obbligatorio");
            }
            if (corso.getDataInizio() == null) {
                throw new IllegalArgumentException("La data di inizio è obbligatoria");
            }
            if (corso.getDataInizio().isBefore(LocalDate.now().minusDays(1))) {
                throw new IllegalArgumentException("La data di inizio non può essere nel passato");
            }
            if (corso.getFrequenza() == null || corso.getFrequenza().trim().isEmpty()) {
                throw new IllegalArgumentException("La frequenza è obbligatoria");
            }
            if (corso.getNumeroSessioni() == null || corso.getNumeroSessioni() <= 0) {
                throw new IllegalArgumentException("Il numero di sessioni deve essere maggiore di zero");
            }
            if (corso.getPrezzo() != null && corso.getPrezzo().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Il prezzo non può essere negativo");
            }

            Corso corsoAggiornato = corsoDAO.update(corso);
            boolean successo = corsoAggiornato != null;
            
            if (successo) {
                logger.info("Corso aggiornato con successo: {}", corso.getTitolo());
            }
            
            return successo;
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento del corso", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    // === GESTIONE SESSIONI ===

    /**
     * Ottieni le sessioni di un corso
     */
    public List<Sessione> getSessioniCorso(Integer corso_id) {
        return sessioneDAO.findByCorsoId(corso_id);
    }

    /**
     * Ottieni solo le sessioni pratiche di un corso
     */
    public List<Sessione> getSessioniPratiche(Integer corso_id) {
        return sessioneDAO.findSessioniPraticheByCorsoId(corso_id);
    }

    /**
     * Crea una nuova sessione
     */
    public boolean creaSessione(Sessione sessione) {
        try {
            // Validazioni
            if (sessione.getCorsoId() == null) {
                throw new IllegalArgumentException("ID corso è obbligatorio");
            }
            if (sessione.getDataSessione() == null) {
                throw new IllegalArgumentException("Data sessione è obbligatoria");
            }
            if (sessione.getDataSessione().isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La data della sessione non può essere nel passato");
            }
            if (sessione.getTitolo() == null || sessione.getTitolo().trim().isEmpty()) {
                throw new IllegalArgumentException("Il titolo della sessione è obbligatorio");
            }
            if (sessione.getTipo() == null || sessione.getTipo().trim().isEmpty()) {
                throw new IllegalArgumentException("Il tipo di sessione è obbligatorio");
            }

            Sessione nuovaSessione = sessioneDAO.save(sessione);
            return nuovaSessione != null;
        } catch (Exception e) {
            logger.error("Errore nella creazione della sessione", e);
            return false;
        }
    }

    /**
     * Aggiorna una sessione esistente
     */
    public boolean aggiornaSessione(Sessione sessione) {
        try {
            // Validazioni
            if (sessione.getId() == null) {
                throw new IllegalArgumentException("ID sessione è obbligatorio per l'aggiornamento");
            }
            if (sessione.getTitolo() == null || sessione.getTitolo().trim().isEmpty()) {
                throw new IllegalArgumentException("Il titolo della sessione è obbligatorio");
            }
            if (sessione.getDataSessione() == null) {
                throw new IllegalArgumentException("Data sessione è obbligatoria");
            }
            if (sessione.getTipo() == null || sessione.getTipo().trim().isEmpty()) {
                throw new IllegalArgumentException("Il tipo di sessione è obbligatorio");
            }

            Sessione sessioneAggiornata = sessioneDAO.update(sessione);
            return sessioneAggiornata != null;
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento della sessione", e);
            return false;
        }
    }

    /**
     * Associa una ricetta a una sessione pratica
     * @param sessione_id ID della sessione
     * @param ricetta_id ID della ricetta
     * @param ordineEsecuzione Ordine di esecuzione nella sessione
     * @return true se associazione riuscita
     */
    public boolean associaRicettaASessione(Integer sessione_id, Integer ricetta_id, Integer ordineEsecuzione) {
        logger.info("Associazione ricetta {} a sessione {} con ordine {}", 
                   ricetta_id, sessione_id, ordineEsecuzione);
        
        try {
            // Verifica che la sessione esista ed è pratica
            Optional<Sessione> sessione = sessioneDAO.findById(sessione_id);
            if (sessione.isEmpty()) {
                throw new IllegalArgumentException("Sessione non trovata con ID: " + sessione_id);
            }
            
            if (!"presenza".equalsIgnoreCase(sessione.get().getTipo())) {
                throw new IllegalArgumentException(
                    "Le ricette possono essere associate solo alle sessioni pratiche (in presenza)");
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

    // === GESTIONE RICETTE ===

    /**
     * Ottieni tutte le ricette di uno chef
     */
    public List<Ricetta> getRicetteChef(Integer chefId) {
        return ricettaDAO.findByChefId(chefId);
    }

    /**
     * Ottieni ricette disponibili per una sessione (non ancora associate)
     */
    public List<Ricetta> getRicetteDisponibili(Integer sessione_id, Integer chefId) {
        return ricettaDAO.findRicetteDisponibiliPerSessione(sessione_id, chefId);
    }

    /**
     * Crea una nuova ricetta
     */
    public boolean creaRicetta(Ricetta ricetta) {
        try {
            // Validazioni
            if (ricetta.getChefId() == null) {
                throw new IllegalArgumentException("ID chef è obbligatorio");
            }
            if (ricetta.getNome() == null || ricetta.getNome().trim().isEmpty()) {
                throw new IllegalArgumentException("Il nome della ricetta è obbligatorio");
            }
            if (ricetta.getDescrizione() == null || ricetta.getDescrizione().trim().isEmpty()) {
                throw new IllegalArgumentException("La descrizione della ricetta è obbligatoria");
            }
            if (ricetta.getDifficolta() == null || ricetta.getDifficolta() < 1 || ricetta.getDifficolta() > 5) {
                ricetta.setDifficolta(1); // Default facile
            }
            if (ricetta.getTempoPreparazione() == null || ricetta.getTempoPreparazione() <= 0) {
                ricetta.setTempoPreparazione(30); // Default 30 minuti
            }
            if (ricetta.getNumeroPortions() == null || ricetta.getNumeroPortions() <= 0) {
                ricetta.setNumeroPortions(4); // Default 4 porzioni
            }

            Ricetta nuovaRicetta = ricettaDAO.save(ricetta);
            return nuovaRicetta != null;
        } catch (Exception e) {
            logger.error("Errore nella creazione della ricetta", e);
            return false;
        }
    }

    /**
     * Aggiorna una ricetta esistente
     */
    public boolean aggiornaRicetta(Ricetta ricetta) {
        try {
            // Validazioni
            if (ricetta.getId() == null) {
                throw new IllegalArgumentException("ID ricetta è obbligatorio per l'aggiornamento");
            }
            if (ricetta.getNome() == null || ricetta.getNome().trim().isEmpty()) {
                throw new IllegalArgumentException("Il nome della ricetta è obbligatorio");
            }
            if (ricetta.getDescrizione() == null || ricetta.getDescrizione().trim().isEmpty()) {
                throw new IllegalArgumentException("La descrizione della ricetta è obbligatoria");
            }
            if (ricetta.getDifficolta() == null || ricetta.getDifficolta() < 1 || ricetta.getDifficolta() > 5) {
                ricetta.setDifficolta(1); // Default facile
            }
            if (ricetta.getTempoPreparazione() == null || ricetta.getTempoPreparazione() <= 0) {
                ricetta.setTempoPreparazione(30); // Default 30 minuti
            }
            if (ricetta.getNumeroPortions() == null || ricetta.getNumeroPortions() <= 0) {
                ricetta.setNumeroPortions(4); // Default 4 porzioni
            }

            Ricetta ricettaAggiornata = ricettaDAO.update(ricetta);
            return ricettaAggiornata != null;
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento della ricetta", e);
            return false;
        }
    }

    // === REPORT MENSILI ===

    /**
     * Genera report mensile per uno chef
     */
    public Optional<ReportMensile> generaReportMensile(Integer chefId, int mese, int anno) {
        return reportDAO.generaReportMensile(chefId, mese, anno);
    }

    /**
     * Ottieni i periodi disponibili per i report di uno chef
     */
    public List<String> getPeriodiDisponibili(Integer chefId) {
        return reportDAO.getMesiDisponibili(chefId);
    }

    /**
     * Ottieni dati per grafici - distribuzione corsi per categoria
     */
    public List<Object[]> getStatisticheCorsi(Integer chefId, int mese, int anno) {
        return reportDAO.getStatisticheCorsi(chefId, mese, anno);
    }

    /**
     * Ottieni dati per grafici - distribuzione sessioni per tipo
     */
    public List<Object[]> getStatisticheSessioni(Integer chefId, int mese, int anno) {
        return reportDAO.getStatisticheSessioni(chefId, mese, anno);
    }

    /**
     * Ottieni dati per grafici - distribuzione ricette per sessione
     */
    public List<Object[]> getDistribuzioneRicette(Integer chefId, int mese, int anno) {
        return reportDAO.getDistribuzioneRicette(chefId, mese, anno);
    }

    // === METODI PRIVATI DI UTILITÀ ===

    /**
     * Genera automaticamente le sessioni per un corso
     */
    private void generaSessioniCorso(Corso corso) {
        LocalDate dataCorrente = corso.getDataInizio();
        
        for (int i = 1; i <= corso.getNumeroSessioni(); i++) {
            Sessione sessione = new Sessione();
            sessione.setCorsoId(corso.getId());
            sessione.setNumeroSessione(i);
            sessione.setDataSessione(dataCorrente);
            
            // Alterna tra sessioni online e pratiche
            if (i % 2 == 1) {
                sessione.setTipo("presenza");
                sessione.setTitolo("Sessione Pratica " + i);
                sessione.setDescrizione("Sessione pratica con preparazione di ricette");
            } else {
                sessione.setTipo("online");
                sessione.setTitolo("Sessione Teorica " + i);
                sessione.setDescrizione("Sessione teorica online");
            }
            
            sessione.setDurataMinuti(120); // 2 ore di default
            
            sessioneDAO.save(sessione);
            
            // Calcola la prossima data in base alla frequenza
            dataCorrente = calcolaProximaDataSessione(dataCorrente, corso.getFrequenza());
        }
        
        logger.info("Generate {} sessioni per il corso: {}", corso.getNumeroSessioni(), corso.getTitolo());
    }

    /**
     * Calcola la prossima data della sessione in base alla frequenza
     */
    private LocalDate calcolaProximaDataSessione(LocalDate dataAttuale, String frequenza) {
        switch (frequenza.toLowerCase()) {
            case "settimanale":
                return dataAttuale.plusWeeks(1);
            case "ogni_due_giorni":
                return dataAttuale.plusDays(2);
            case "giornaliero":
                return dataAttuale.plusDays(1);
            default:
                return dataAttuale.plusWeeks(1); // Default settimanale
        }
    }

    /**
     * Valida i dati di un corso
     */
    private ValidationResult validaCorso(String titolo, LocalDate data_inizio, String frequenza, 
                                        Integer numero_sessioni, BigDecimal prezzo) {
        if (titolo == null || titolo.trim().isEmpty()) {
            return ValidationResult.invalid("Il titolo del corso è obbligatorio");
        }
        
        if (data_inizio == null) {
            return ValidationResult.invalid("La data di inizio è obbligatoria");
        }
        
        if (data_inizio.isBefore(LocalDate.now())) {
            return ValidationResult.invalid("La data di inizio non può essere nel passato");
        }
        
        if (frequenza == null || frequenza.trim().isEmpty()) {
            return ValidationResult.invalid("La frequenza è obbligatoria");
        }
        
        if (!List.of("settimanale", "ogni_due_giorni", "giornaliero").contains(frequenza.toLowerCase())) {
            return ValidationResult.invalid("Frequenza non valida. Valori ammessi: settimanale, ogni_due_giorni, giornaliero");
        }
        
        if (numero_sessioni == null || numero_sessioni <= 0) {
            return ValidationResult.invalid("Il numero di sessioni deve essere maggiore di zero");
        }
        
        if (numero_sessioni > 50) {
            return ValidationResult.invalid("Il numero massimo di sessioni è 50");
        }
        
        if (prezzo != null && prezzo.compareTo(BigDecimal.ZERO) < 0) {
            return ValidationResult.invalid("Il prezzo non può essere negativo");
        }
        
        return ValidationResult.valid();
    }

    // === METODI DI SUPPORTO PER IL CONTROLLER ===
    
    /**
     * Ottieni tutte le categorie disponibili
     */
    public List<CategoriaCorso> getAllCategorie() {
        return getCategorieCorsi();
    }
    
    /**
     * Ottieni corsi di uno chef
     */
    public List<Corso> getCorsiByChef(Integer chefId) {
        return getCorsiChef(chefId, null);
    }
    
    /**
     * Ottieni sessioni di un corso
     */
    public List<Sessione> getSessioniByCorso(int corso_id) {
        return getSessioniCorso(corso_id);
    }
    
    /**
     * Ottieni tutte le ricette
     */
    public List<Ricetta> getAllRicette() {
        return ricettaDAO.findAll();
    }
      /**
     * Ottieni corsi di uno chef filtrati per categoria
     */
    public List<Corso> getCorsiByChefECategoria(Integer chefId, Integer categoria_id) {
        return getCorsiChef(chefId, categoria_id);
    }
    
    /**
     * Crea corso da oggetto Corso
     */
    public boolean creaCorso(Corso corso) {
        try {
            ValidationResult validation = validaCorso(corso.getTitolo(), corso.getDataInizio(), 
                                                    corso.getFrequenza(), corso.getNumeroSessioni(), corso.getPrezzo());
            if (!validation.isValid()) {
                throw new IllegalArgumentException(validation.getErrorMessage());
            }
            
            Corso nuovoCorso = corsoDAO.save(corso);
            
            // Genera automaticamente le sessioni del corso
            if (nuovoCorso != null && nuovoCorso.getId() != null) {
                generaSessioniCorso(nuovoCorso);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Errore nella creazione del corso", e);
            return false;
        }
    }
    
    /**
     * Elimina un corso
     */
    public boolean eliminaCorso(Integer corso_id) {
        try {
            return corsoDAO.delete(corso_id);
        } catch (Exception e) {
            logger.error("Errore nell'eliminazione del corso", e);
            return false;
        }
    }
    
    /**
     * Elimina una ricetta
     */
    public boolean eliminaRicetta(Integer ricetta_id) {
        try {
            return ricettaDAO.delete(ricetta_id);
        } catch (Exception e) {
            logger.error("Errore nell'eliminazione della ricetta", e);
            return false;
        }
    }    /**
     * Genera report mensile - versione overloaded con parametri diversi
     */
    public ReportMensile generaReportMensileWrapper(Integer chefId, int anno, int mese) {
        Optional<ReportMensile> report = generaReportMensile(chefId, mese, anno);
        return report.orElse(new ReportMensile()); // Ritorna un report vuoto se non trovato
    }
    
    // === GESTIONE UTENTI ===
    
    /**
     * Ottieni tutti gli utenti attivi
     */
    public List<Utente> getAllUtenti() {
        return utenteDAO.findAllAttivi();
    }
    
    /**
     * Trova un utente per ID
     */
    public Optional<Utente> getUtenteById(Integer id) {
        return utenteDAO.findById(id);
    }
    
    /**
     * Trova un utente per email
     */
    public Optional<Utente> getUtenteByEmail(String email) {
        return utenteDAO.findByEmail(email);
    }
    
    /**
     * Crea un nuovo utente
     */
    public boolean creaUtente(Utente utente) {
        try {
            // Validazioni
            if (utente.getNome() == null || utente.getNome().trim().isEmpty()) {
                throw new IllegalArgumentException("Il nome è obbligatorio");
            }
            if (utente.getCognome() == null || utente.getCognome().trim().isEmpty()) {
                throw new IllegalArgumentException("Il cognome è obbligatorio");
            }
            if (utente.getEmail() == null || utente.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("L'email è obbligatoria");
            }
            if (!isValidEmail(utente.getEmail())) {
                throw new IllegalArgumentException("Formato email non valido");
            }
            if (utenteDAO.isEmailUsed(utente.getEmail(), null)) {
                throw new IllegalArgumentException("Email già utilizzata da un altro utente");
            }
            if (utente.getLivelloEsperienza() == null) {
                utente.setLivelloEsperienza("PRINCIPIANTE"); // Default
            }

            Utente nuovoUtente = utenteDAO.save(utente);
            return nuovoUtente != null;
        } catch (Exception e) {
            logger.error("Errore nella creazione dell'utente", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Aggiorna un utente esistente
     */
    public boolean aggiornaUtente(Utente utente) {
        try {
            // Validazioni
            if (utente.getId() == null) {
                throw new IllegalArgumentException("ID utente è obbligatorio per l'aggiornamento");
            }
            if (utente.getNome() == null || utente.getNome().trim().isEmpty()) {
                throw new IllegalArgumentException("Il nome è obbligatorio");
            }
            if (utente.getCognome() == null || utente.getCognome().trim().isEmpty()) {
                throw new IllegalArgumentException("Il cognome è obbligatorio");
            }
            if (utente.getEmail() == null || utente.getEmail().trim().isEmpty()) {
                throw new IllegalArgumentException("L'email è obbligatoria");
            }
            if (!isValidEmail(utente.getEmail())) {
                throw new IllegalArgumentException("Formato email non valido");
            }
            if (utenteDAO.isEmailUsed(utente.getEmail(), utente.getId())) {
                throw new IllegalArgumentException("Email già utilizzata da un altro utente");
            }

            Utente utenteAggiornato = utenteDAO.update(utente);
            return utenteAggiornato != null;
        } catch (Exception e) {
            logger.error("Errore nell'aggiornamento dell'utente", e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Disattiva un utente
     */
    public boolean disattivaUtente(Integer id) {
        try {
            return utenteDAO.disattiva(id);
        } catch (Exception e) {
            logger.error("Errore nella disattivazione dell'utente", e);
            return false;
        }
    }
    
    /**
     * Cerca utenti per nome o cognome
     */
    public List<Utente> cercaUtenti(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllUtenti();
        }
        return utenteDAO.searchByName(searchTerm.trim());
    }
    
    // === GESTIONE ISCRIZIONI ===
    
    /**
     * Ottieni tutte le iscrizioni di un corso
     */
    public List<Iscrizione> getIscrizioniCorso(Integer corso_id) {
        return iscrizioneDAO.findByCorsoId(corso_id);
    }
    
    /**
     * Ottieni tutte le iscrizioni di un utente
     */
    public List<Iscrizione> getIscrizioniUtente(Integer utente_id) {
        return iscrizioneDAO.findByUtenteId(utente_id);
    }
    
    /**
     * Iscrive un utente a un corso
     */
    public boolean iscriviUtenteACorso(Integer utente_id, Integer corso_id, String note) {
        try {
            // Validazioni
            if (utente_id == null) {
                throw new IllegalArgumentException("ID utente è obbligatorio");
            }
            if (corso_id == null) {
                throw new IllegalArgumentException("ID corso è obbligatorio");
            }
            
            // Verifica che l'utente esista e sia attivo
            Optional<Utente> utente = utenteDAO.findById(utente_id);
            if (utente.isEmpty() || !utente.get().isAttivo()) {
                throw new IllegalArgumentException("Utente non trovato o non attivo");
            }
            
            // Verifica che il corso esista
            List<Corso> corsi = corsoDAO.findByChefId(null); // Trova tutti i corsi
            boolean corsoEsiste = corsi.stream().anyMatch(c -> c.getId().equals(corso_id));
            if (!corsoEsiste) {
                throw new IllegalArgumentException("Corso non trovato");
            }
            
            // Verifica che l'utente non sia già iscritto
            if (iscrizioneDAO.isUtenteIscritto(utente_id, corso_id)) {
                throw new IllegalArgumentException("Utente già iscritto a questo corso");
            }
            
            // Crea l'iscrizione
            Iscrizione iscrizione = new Iscrizione(utente_id, corso_id, note);
            Iscrizione nuovaIscrizione = iscrizioneDAO.save(iscrizione);
            
            return nuovaIscrizione != null;
            
        } catch (Exception e) {
            logger.error("Errore nell'iscrizione utente {} al corso {}", utente_id, corso_id, e);
            throw new RuntimeException(e.getMessage());
        }
    }
    
    /**
     * Annulla un'iscrizione
     */
    public boolean annullaIscrizione(Integer iscrizioneId, String motivo) {
        try {
            return iscrizioneDAO.annullaIscrizione(iscrizioneId, motivo);
        } catch (Exception e) {
            logger.error("Errore nell'annullamento dell'iscrizione {}", iscrizioneId, e);
            return false;
        }
    }
    
    /**
     * Completa un'iscrizione
     */
    public boolean completaIscrizione(Integer iscrizioneId) {
        try {
            return iscrizioneDAO.completaIscrizione(iscrizioneId);
        } catch (Exception e) {
            logger.error("Errore nel completamento dell'iscrizione {}", iscrizioneId, e);
            return false;
        }
    }
    
    /**
     * Ottieni il numero di iscritti attivi per un corso
     */
    public int getNumeroIscrittiCorso(Integer corso_id) {
        return iscrizioneDAO.countIscrittiAttivi(corso_id);
    }
    
    /**
     * Verifica se un utente è iscritto a un corso
     */
    public boolean isUtenteIscritto(Integer utente_id, Integer corso_id) {
        return iscrizioneDAO.isUtenteIscritto(utente_id, corso_id);
    }    /**
     * Ottieni tutte le iscrizioni attive
     */
    public List<Iscrizione> getAllIscrizioniAttive() {
        return iscrizioneDAO.findAllAttive();
    }
    
    /**
     * Ottieni tutte le iscrizioni (solo attive per ora)
     */
    public List<Iscrizione> getAllIscrizioni() {
        return iscrizioneDAO.findAllAttive();
    }
    
    // === METODI DI UTILITÀ ===
    
    /**
     * Valida formato email
     */    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Regex semplificata per validazione email
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
}

