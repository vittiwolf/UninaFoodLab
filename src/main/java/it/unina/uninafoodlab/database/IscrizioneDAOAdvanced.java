package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Iscrizione;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO Avanzato per la gestione delle Iscrizioni con funzionalità del database avanzate
 * Utilizza funzioni, trigger e view implementate nel database PostgreSQL
 */
public class IscrizioneDAOAdvanced extends IscrizioneDAO {
    private static final Logger logger = LoggerFactory.getLogger(IscrizioneDAOAdvanced.class);

    /**
     * Verifica posti disponibili utilizzando la funzione del database
     */
    public int getPostiDisponibili(Integer corsoId) {
        String sql = "SELECT verifica_posti_disponibili(?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, corsoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int postiDisponibili = rs.getInt(1);
                    logger.debug("Posti disponibili per corso {}: {}", corsoId, postiDisponibili);
                    return postiDisponibili;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel controllo posti disponibili per corso {}", corsoId, e);
        }
        
        return 0;
    }

    /**
     * Ottieni statistiche complete di un corso utilizzando la funzione del database
     */
    public Map<String, Object> getStatisticheCorso(Integer corsoId) {
        String sql = "SELECT * FROM calcola_statistiche_corso(?)";
        Map<String, Object> statistiche = new HashMap<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, corsoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    statistiche.put("totaleIscritti", rs.getInt("totale_iscritti"));
                    statistiche.put("iscrittiAttivi", rs.getInt("iscritti_attivi"));
                    statistiche.put("iscrittiCompletati", rs.getInt("iscritti_completati"));
                    statistiche.put("iscrittiAnnullati", rs.getInt("iscritti_annullati"));
                    statistiche.put("tassoCompletamento", rs.getBigDecimal("tasso_completamento"));
                    statistiche.put("ricavoTotale", rs.getBigDecimal("ricavo_totale"));
                    
                    logger.debug("Statistiche calcolate per corso {}: {}", corsoId, statistiche);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel calcolo statistiche per corso {}", corsoId, e);
        }
        
        return statistiche;
    }

    /**
     * Calcola prezzo scontato utilizzando la funzione del database
     */
    public Double calcolaPrezzoScontato(Integer corsoId, Double scontoPercentuale) {
        String sql = "SELECT calcola_prezzo_scontato(?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, corsoId);
            stmt.setBigDecimal(2, java.math.BigDecimal.valueOf(scontoPercentuale));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double prezzoScontato = rs.getDouble(1);
                    logger.debug("Prezzo scontato per corso {}: {}", corsoId, prezzoScontato);
                    return prezzoScontato;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel calcolo prezzo scontato per corso {}", corsoId, e);
        }
        
        return null;
    }

    /**
     * Ottieni iscrizioni complete utilizzando JOIN con le tabelle correlate
     */
    public List<Map<String, Object>> getIscrizioniComplete() {
        List<Map<String, Object>> iscrizioni = new ArrayList<>();
        String sql = """
            SELECT i.id, i.utente_id, i.corso_id, i.data_iscrizione, i.stato, i.note,
                   u.nome || ' ' || u.cognome as utente_nome_completo,
                   u.email as utente_email,
                   EXTRACT(YEAR FROM AGE(u.data_nascita)) as utente_eta,
                   u.livello_esperienza,
                   c.titolo as corso_titolo,
                   c.data_inizio as corso_data_inizio,
                   c.prezzo as corso_prezzo,
                   ch.nome || ' ' || ch.cognome as chef_nome,
                   cc.nome as categoria_corso,
                   i.data_iscrizione - c.data_inizio as giorni_anticipo_iscrizione
            FROM iscrizioni i
            JOIN utenti u ON i.utente_id = u.id
            JOIN corsi c ON i.corso_id = c.id
            JOIN chefs ch ON c.chef_id = ch.id
            LEFT JOIN categorie_corsi cc ON c.categoria_id = cc.id
            ORDER BY i.data_iscrizione DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> iscrizione = new HashMap<>();
                iscrizione.put("id", rs.getInt("id"));
                iscrizione.put("utenteId", rs.getInt("utente_id"));
                iscrizione.put("corsoId", rs.getInt("corso_id"));
                iscrizione.put("dataIscrizione", rs.getTimestamp("data_iscrizione"));
                iscrizione.put("stato", rs.getString("stato"));
                iscrizione.put("note", rs.getString("note"));
                iscrizione.put("utenteNomeCompleto", rs.getString("utente_nome_completo"));
                iscrizione.put("utenteEmail", rs.getString("utente_email"));
                iscrizione.put("utenteEta", rs.getInt("utente_eta"));
                iscrizione.put("livelloEsperienza", rs.getString("livello_esperienza"));
                iscrizione.put("corsoTitolo", rs.getString("corso_titolo"));
                iscrizione.put("corsoDataInizio", rs.getDate("corso_data_inizio"));
                iscrizione.put("corsoPrezzo", rs.getBigDecimal("corso_prezzo"));
                iscrizione.put("chefNome", rs.getString("chef_nome"));
                iscrizione.put("categoriaCorso", rs.getString("categoria_corso"));
                iscrizione.put("giorniAnticipoIscrizione", rs.getInt("giorni_anticipo_iscrizione"));
                
                iscrizioni.add(iscrizione);
            }
            
            logger.debug("Recuperate {} iscrizioni complete", iscrizioni.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero iscrizioni complete", e);
        }
        
        return iscrizioni;
    }

    /**
     * Ottieni log delle modifiche per un'iscrizione
     */
    public List<Map<String, Object>> getLogIscrizione(Integer iscrizioneId) {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = """
            SELECT id, azione, stato_precedente, stato_nuovo, 
                   utente_modifica, timestamp_modifica, note
            FROM log_iscrizioni 
            WHERE iscrizione_id = ? 
            ORDER BY timestamp_modifica DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, iscrizioneId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> log = new HashMap<>();
                    log.put("id", rs.getInt("id"));
                    log.put("azione", rs.getString("azione"));
                    log.put("statoPrecedente", rs.getString("stato_precedente"));
                    log.put("statoNuovo", rs.getString("stato_nuovo"));
                    log.put("utenteModifica", rs.getString("utente_modifica"));
                    log.put("timestampModifica", rs.getTimestamp("timestamp_modifica"));
                    log.put("note", rs.getString("note"));
                    
                    logs.add(log);
                }
            }
            
            logger.debug("Recuperati {} log per iscrizione {}", logs.size(), iscrizioneId);
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero log per iscrizione {}", iscrizioneId, e);
        }
        
        return logs;
    }

    /**
     * Valida email utilizzando la funzione del database
     */
    public boolean validaEmail(String email) {
        String sql = "SELECT valida_email(?)";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean valida = rs.getBoolean(1);
                    logger.debug("Validazione email '{}': {}", email, valida ? "valida" : "non valida");
                    return valida;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nella validazione email '{}'", email, e);
        }
        
        return false;
    }

    /**
     * Iscrizione con controllo automatico posti (utilizza trigger del database)
     * Il trigger controlla automaticamente i posti disponibili
     */
    @Override
    public Iscrizione save(Iscrizione iscrizione) {
        // Il trigger genera automaticamente il codice_iscrizione
        // Il trigger controlla automaticamente i posti disponibili
        // Il trigger valida automaticamente l'email se presente
        return super.save(iscrizione);
    }

    /**
     * Aggiornamento con log automatico (utilizza trigger del database)
     */
    @Override
    public Iscrizione update(Iscrizione iscrizione) {
        // Il trigger aggiorna automaticamente modified_at
        // Il trigger registra automaticamente il log delle modifiche
        return super.update(iscrizione);
    }

    /**
     * Ottieni analisi mensile iscrizioni utilizzando la view
     */
    public List<Map<String, Object>> getAnalisiMensile(int anno) {
        List<Map<String, Object>> analisi = new ArrayList<>();
        String sql = """
            SELECT anno, mese, periodo, totale_iscrizioni, 
                   iscrizioni_attive, iscrizioni_completate, 
                   iscrizioni_annullate, ricavo_mensile
            FROM analisi_iscrizioni_mensili 
            WHERE anno = ?
            ORDER BY mese DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, anno);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("anno", rs.getInt("anno"));
                    record.put("mese", rs.getInt("mese"));
                    record.put("periodo", rs.getString("periodo"));
                    record.put("totaleIscrizioni", rs.getInt("totale_iscrizioni"));
                    record.put("iscrizioniAttive", rs.getInt("iscrizioni_attive"));
                    record.put("iscrizioniCompletate", rs.getInt("iscrizioni_completate"));
                    record.put("iscrizioniAnnullate", rs.getInt("iscrizioni_annullate"));
                    record.put("ricavoMensile", rs.getBigDecimal("ricavo_mensile"));
                    
                    analisi.add(record);
                }
            }
            
            logger.debug("Recuperata analisi mensile per anno {}: {} record", anno, analisi.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero analisi mensile per anno {}", anno, e);
        }
        
        return analisi;
    }

    // === METODI AGGIUNTIVI RICHIESTI DAL SERVICE ===
    
    /**
     * Verifica se un utente è già iscritto a un corso
     */
    public boolean isUtenteIscritto(Integer utenteId, Integer corsoId) {
        String sql = "SELECT COUNT(*) FROM iscrizioni WHERE utente_id = ? AND corso_id = ? AND stato IN ('ATTIVA', 'COMPLETATA')";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, utenteId);
            stmt.setInt(2, corsoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean iscritto = rs.getInt(1) > 0;
                    logger.debug("Utente {} iscritto al corso {}: {}", utenteId, corsoId, iscritto);
                    return iscritto;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel controllo iscrizione utente {} a corso {}", utenteId, corsoId, e);
        }
        
        return false;
    }
    
    /**
     * Annulla un'iscrizione
     */
    public boolean annullaIscrizione(Integer iscrizioneId, String motivo) {
        String sql = "UPDATE iscrizioni SET stato = 'ANNULLATA', note = COALESCE(note, '') || ? || ?, modified_at = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String motivoCompleto = "\nMotivo annullamento: " + (motivo != null ? motivo : "Non specificato");
            stmt.setString(1, motivoCompleto);
            stmt.setString(2, " - Annullata il " + java.time.LocalDateTime.now());
            stmt.setInt(3, iscrizioneId);
            
            int affectedRows = stmt.executeUpdate();
            boolean successo = affectedRows > 0;
            
            if (successo) {
                logger.info("Iscrizione {} annullata con successo. Motivo: {}", iscrizioneId, motivo);
            } else {
                logger.warn("Nessuna iscrizione trovata con ID: {}", iscrizioneId);
            }
            
            return successo;
            
        } catch (SQLException e) {
            logger.error("Errore nell'annullamento dell'iscrizione {}", iscrizioneId, e);
            return false;
        }
    }
    
    /**
     * Completa un'iscrizione
     */
    public boolean completaIscrizione(Integer iscrizioneId) {
        String sql = "UPDATE iscrizioni SET stato = 'COMPLETATA', modified_at = CURRENT_TIMESTAMP WHERE id = ? AND stato = 'ATTIVA'";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, iscrizioneId);
            
            int affectedRows = stmt.executeUpdate();
            boolean successo = affectedRows > 0;
            
            if (successo) {
                logger.info("Iscrizione {} completata con successo", iscrizioneId);
            } else {
                logger.warn("Nessuna iscrizione attiva trovata con ID: {}", iscrizioneId);
            }
            
            return successo;
            
        } catch (SQLException e) {
            logger.error("Errore nel completamento dell'iscrizione {}", iscrizioneId, e);
            return false;
        }
    }
    
    /**
     * Conta il numero di iscritti attivi per un corso
     */
    public int countIscrittiAttivi(Integer corsoId) {
        String sql = "SELECT COUNT(*) FROM iscrizioni WHERE corso_id = ? AND stato = 'ATTIVA'";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, corsoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.debug("Iscritti attivi per corso {}: {}", corsoId, count);
                    return count;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel conteggio iscritti attivi per corso {}", corsoId, e);
        }
        
        return 0;
    }
    
    /**
     * Trova tutte le iscrizioni attive
     */
    public List<Iscrizione> findAllAttive() {
        return findByStato("ATTIVA");
    }

    /**
     * Trova tutte le iscrizioni attive includendo i dettagli (nome utente, cognome, email, titolo corso)
     * popolando direttamente l'oggetto Iscrizione per l'uso nella TableView.
     */
    public List<Iscrizione> findAllAttiveDettagliato() {
        List<Iscrizione> result = new ArrayList<>();
        String sql = """
            SELECT i.id, i.utente_id, i.corso_id, i.data_iscrizione, i.stato, i.note,
                   u.nome, u.cognome, u.email, u.livello_esperienza,
                   c.titolo
            FROM iscrizioni i
            JOIN utenti u ON i.utente_id = u.id
            JOIN corsi c ON i.corso_id = c.id
            WHERE i.stato = 'ATTIVA'
            ORDER BY i.data_iscrizione DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Iscrizione iscrizione = new Iscrizione();
                iscrizione.setId(rs.getInt("id"));
                iscrizione.setUtenteId(rs.getInt("utente_id"));
                iscrizione.setCorsoId(rs.getInt("corso_id"));
                Timestamp ts = rs.getTimestamp("data_iscrizione");
                if (ts != null) {
                    iscrizione.setDataIscrizione(ts.toLocalDateTime());
                }
                iscrizione.setStato(rs.getString("stato"));
                iscrizione.setNote(rs.getString("note"));
                iscrizione.setNomeUtente(rs.getString("nome"));
                iscrizione.setCognomeUtente(rs.getString("cognome"));
                iscrizione.setEmailUtente(rs.getString("email"));
                iscrizione.setLivelloEsperienza(rs.getString("livello_esperienza"));
                iscrizione.setTitoloCorso(rs.getString("titolo"));
                result.add(iscrizione);
            }
            logger.debug("Recuperate {} iscrizioni attive dettagliate", result.size());
        } catch (SQLException e) {
            logger.error("Errore nel recupero iscrizioni attive dettagliate", e);
        }
        return result;
    }
}
