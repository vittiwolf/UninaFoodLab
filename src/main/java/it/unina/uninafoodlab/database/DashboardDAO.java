package it.unina.uninafoodlab.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO per la gestione della Dashboard utilizzando le view avanzate del database
 */
public class DashboardDAO {
    private static final Logger logger = LoggerFactory.getLogger(DashboardDAO.class);

    /**
     * Ottieni metriche principali per la dashboard amministratore
     */
    public List<Map<String, String>> getDashboardMetrics() {
        List<Map<String, String>> metriche = new ArrayList<>();
        String sql = "SELECT metrica, valore, tipo FROM dashboard_admin ORDER BY metrica";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, String> metrica = new HashMap<>();
                metrica.put("metrica", rs.getString("metrica"));
                metrica.put("valore", rs.getString("valore"));
                metrica.put("tipo", rs.getString("tipo"));
                metriche.add(metrica);
            }
            
            logger.debug("Recuperate {} metriche dashboard", metriche.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero metriche dashboard", e);
        }
        
        return metriche;
    }

    /**
     * Ottieni dettagli completi dei corsi
     */
    public List<Map<String, Object>> getCorsiDettaglio() {
        List<Map<String, Object>> corsi = new ArrayList<>();
        String sql = """
            SELECT id, titolo, descrizione, data_inizio, frequenza, numero_sessioni, prezzo,
                   chef_nome, chef_specializzazione, categoria, iscritti_attivi, 
                   iscritti_completati, iscritti_annullati, numero_sessioni_programmate,
                   posti_disponibili, stato_corso, ricavo_corso
            FROM corsi_dettaglio 
            ORDER BY data_inizio DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> corso = new HashMap<>();
                corso.put("id", rs.getInt("id"));
                corso.put("titolo", rs.getString("titolo"));
                corso.put("descrizione", rs.getString("descrizione"));
                corso.put("dataInizio", rs.getDate("data_inizio"));
                corso.put("frequenza", rs.getString("frequenza"));
                corso.put("numeroSessioni", rs.getInt("numero_sessioni"));
                corso.put("prezzo", rs.getBigDecimal("prezzo"));
                corso.put("chefNome", rs.getString("chef_nome"));
                corso.put("chefSpecializzazione", rs.getString("chef_specializzazione"));
                corso.put("categoria", rs.getString("categoria"));
                corso.put("iscrittiAttivi", rs.getInt("iscritti_attivi"));
                corso.put("iscrittiCompletati", rs.getInt("iscritti_completati"));
                corso.put("iscrittiAnnullati", rs.getInt("iscritti_annullati"));
                corso.put("numeroSessioniProgrammate", rs.getInt("numero_sessioni_programmate"));
                corso.put("postiDisponibili", rs.getInt("posti_disponibili"));
                corso.put("statoCorso", rs.getString("stato_corso"));
                corso.put("ricavoCorso", rs.getBigDecimal("ricavo_corso"));
                
                corsi.add(corso);
            }
            
            logger.debug("Recuperati {} corsi dettaglio", corsi.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero corsi dettaglio", e);
        }
        
        return corsi;
    }

    /**
     * Ottieni report performance dei chef
     */
    public List<Map<String, Object>> getReportChef() {
        List<Map<String, Object>> report = new ArrayList<>();
        String sql = """
            SELECT chef_id, chef_nome, specializzazione, corsi_totali, corsi_futuri,
                   iscrizioni_totali, iscrizioni_attive, iscrizioni_completate,
                   tasso_completamento_percentuale, ricavo_totale, prezzo_medio_corsi
            FROM report_chef 
            ORDER BY ricavo_totale DESC NULLS LAST
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> chef = new HashMap<>();
                chef.put("chefId", rs.getInt("chef_id"));
                chef.put("chefNome", rs.getString("chef_nome"));
                chef.put("specializzazione", rs.getString("specializzazione"));
                chef.put("corsiTotali", rs.getInt("corsi_totali"));
                chef.put("corsiFuturi", rs.getInt("corsi_futuri"));
                chef.put("iscrizioniTotali", rs.getInt("iscrizioni_totali"));
                chef.put("iscrizioniAttive", rs.getInt("iscrizioni_attive"));
                chef.put("iscrizioniCompletate", rs.getInt("iscrizioni_completate"));
                chef.put("tassoCompletamento", rs.getBigDecimal("tasso_completamento_percentuale"));
                chef.put("ricavoTotale", rs.getBigDecimal("ricavo_totale"));
                chef.put("prezzoMedioCorsi", rs.getBigDecimal("prezzo_medio_corsi"));
                
                report.add(chef);
            }
            
            logger.debug("Recuperato report per {} chef", report.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero report chef", e);
        }
        
        return report;
    }

    /**
     * Ottieni analisi temporale delle iscrizioni
     */
    public List<Map<String, Object>> getAnalisiTemporale(int limiteRecord) {
        List<Map<String, Object>> analisi = new ArrayList<>();
        String sql = """
            SELECT anno, mese, periodo, totale_iscrizioni, iscrizioni_attive,
                   iscrizioni_completate, iscrizioni_annullate, ricavo_mensile
            FROM analisi_iscrizioni_mensili 
            ORDER BY anno DESC, mese DESC
            LIMIT ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limiteRecord);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> record = new HashMap<>();
                    record.put("anno", rs.getInt("anno"));
                    record.put("mese", rs.getInt("mese"));
                    record.put("periodo", rs.getString("periodo"));
                    record.put("totaleIscrizioni", rs.getInt("totale_iscrizioni"));
                    record.put("iscrizioniAttive", rs.getInt("iscrizioni_attive"));
                    record.put("iscrizioniCompletate", rs.getInt("iscritti_completati"));
                    record.put("iscrizioniAnnullate", rs.getInt("iscrizioni_annullate"));
                    record.put("ricavoMensile", rs.getBigDecimal("ricavo_mensile"));
                    
                    analisi.add(record);
                }
            }
            
            logger.debug("Recuperata analisi temporale: {} record", analisi.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero analisi temporale", e);
        }
        
        return analisi;
    }

    // Metodo getNotificheSistema rimosso: la sezione/feature 'notifiche' non è più prevista nell'interfaccia.

    /**
     * Ottieni statistiche generali del sistema
     */
    public Map<String, Object> getStatisticheGenerali() {
        Map<String, Object> statistiche = new HashMap<>();
        
        // Query per varie statistiche
        String[] queries = {
            "SELECT COUNT(*) as totale_utenti FROM utenti WHERE attivo = TRUE",
            "SELECT COUNT(*) as totale_chef FROM chef",
            "SELECT COUNT(*) as totale_corsi FROM corsi",
            "SELECT COUNT(*) as totale_iscrizioni FROM iscrizioni",
            "SELECT COALESCE(SUM(prezzo), 0) as ricavo_potenziale FROM corsi c JOIN iscrizioni i ON c.id = i.corso_id WHERE i.stato IN ('ATTIVA', 'COMPLETATA')",
            "SELECT AVG(calcola_eta(data_nascita)) as eta_media_utenti FROM utenti WHERE data_nascita IS NOT NULL"
        };
        
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String query : queries) {
                try (PreparedStatement stmt = conn.prepareStatement(query);
                     ResultSet rs = stmt.executeQuery()) {
                    
                    if (rs.next()) {
                        ResultSetMetaData metaData = rs.getMetaData();
                        String columnName = metaData.getColumnName(1);
                        Object value = rs.getObject(1);
                        statistiche.put(columnName, value);
                    }
                }
            }
            
            logger.debug("Calcolate statistiche generali: {}", statistiche);
            
        } catch (SQLException e) {
            logger.error("Errore nel calcolo statistiche generali", e);
        }
        
        return statistiche;
    }

    /**
     * Ottieni corsi che necessitano attenzione (pochi posti, inizio imminente, etc.)
     */
    public List<Map<String, Object>> getCorsiAttenzione() {
        List<Map<String, Object>> corsi = new ArrayList<>();
        String sql = """
            SELECT c.id, c.titolo, c.data_inizio,
                   verifica_posti_disponibili(c.id) as posti_disponibili,
                   EXTRACT(DAYS FROM (c.data_inizio - CURRENT_DATE)) as giorni_inizio,
                   ch.nome || ' ' || ch.cognome as chef_nome,
                   CASE 
                       WHEN verifica_posti_disponibili(c.id) = 0 THEN 'PIENO'
                       WHEN verifica_posti_disponibili(c.id) <= 3 THEN 'POCHI_POSTI'
                       WHEN c.data_inizio BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days' THEN 'INIZIO_IMMINENTE'
                       ELSE 'OK'
                   END as stato_attenzione
            FROM corsi c
            JOIN chef ch ON c.chef_id = ch.id
            WHERE c.data_inizio >= CURRENT_DATE
            AND (
                verifica_posti_disponibili(c.id) <= 3 OR
                c.data_inizio BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'
            )
            ORDER BY giorni_inizio, posti_disponibili
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> corso = new HashMap<>();
                corso.put("id", rs.getInt("id"));
                corso.put("titolo", rs.getString("titolo"));
                corso.put("dataInizio", rs.getDate("data_inizio"));
                corso.put("postiDisponibili", rs.getInt("posti_disponibili"));
                corso.put("giorniInizio", rs.getInt("giorni_inizio"));
                corso.put("chefNome", rs.getString("chef_nome"));
                corso.put("statoAttenzione", rs.getString("stato_attenzione"));
                
                corsi.add(corso);
            }
            
            logger.debug("Trovati {} corsi che necessitano attenzione", corsi.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero corsi che necessitano attenzione", e);
        }
        
        return corsi;
    }

    /**
     * Genera report mensile per chef utilizzando le view avanzate
     */
    public Map<String, Object> generaReportMensile(Integer chefId, int mese, int anno) {
        Map<String, Object> report = new HashMap<>();
        
        // Utilizza la view report_chef per dati base
        String sql = """
            SELECT chef_nome, specializzazione, corsi_totali, iscrizioni_totali,
                   tasso_completamento_percentuale, ricavo_totale
            FROM report_chef 
            WHERE chef_id = ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    report.put("chefNome", rs.getString("chef_nome"));
                    report.put("specializzazione", rs.getString("specializzazione"));
                    report.put("corsiTotali", rs.getInt("corsi_totali"));
                    report.put("iscrizioniTotali", rs.getInt("iscrizioni_totali"));
                    report.put("tassoCompletamento", rs.getBigDecimal("tasso_completamento_percentuale"));
                    report.put("ricavoTotale", rs.getBigDecimal("ricavo_totale"));
                }
            }
            
            // Aggiungi statistiche mensili specifiche
            report.putAll(getStatisticheMensili(chefId, mese, anno));
            
            logger.debug("Report mensile generato per chef {}: {}/{}", chefId, mese, anno);
            
        } catch (SQLException e) {
            logger.error("Errore nella generazione report mensile per chef {}", chefId, e);
        }
        
        return report;
    }

    /**
     * Ottieni mesi disponibili per un chef
     */
    public List<String> getMesiDisponibili(Integer chefId) {
        List<String> mesi = new ArrayList<>();
        String sql = """
            SELECT DISTINCT TO_CHAR(data_inizio, 'MM/YYYY') as mese_anno
            FROM corsi 
            WHERE chef_id = ?
            ORDER BY mese_anno DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mesi.add(rs.getString("mese_anno"));
                }
            }
            
            logger.debug("Trovati {} mesi disponibili per chef {}", mesi.size(), chefId);
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero mesi disponibili per chef {}", chefId, e);
        }
        
        return mesi;
    }

    /**
     * Ottieni statistiche corsi per chef in un periodo
     */
    public List<Map<String, Object>> getStatisticheCorsi(Integer chefId, int mese, int anno) {
        List<Map<String, Object>> statistiche = new ArrayList<>();
        String sql = """
            SELECT titolo, iscritti_attivi, iscritti_completati, 
                   posti_disponibili, ricavo_corso, stato_corso
            FROM corsi_dettaglio 
            WHERE id IN (
                SELECT id FROM corsi 
                WHERE chef_id = ? 
                AND EXTRACT(MONTH FROM data_inizio) = ? 
                AND EXTRACT(YEAR FROM data_inizio) = ?
            )
            ORDER BY ricavo_corso DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            stmt.setInt(2, mese);
            stmt.setInt(3, anno);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("titolo", rs.getString("titolo"));
                    stat.put("iscrittiAttivi", rs.getInt("iscritti_attivi"));
                    stat.put("iscrittiCompletati", rs.getInt("iscritti_completati"));
                    stat.put("postiDisponibili", rs.getInt("posti_disponibili"));
                    stat.put("ricavoCorso", rs.getBigDecimal("ricavo_corso"));
                    stat.put("statoCorso", rs.getString("stato_corso"));
                    statistiche.add(stat);
                }
            }
            
            logger.debug("Trovate statistiche per {} corsi", statistiche.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero statistiche corsi", e);
        }
        
        return statistiche;
    }

    /**
     * Ottieni statistiche sessioni per chef in un periodo
     */
    public List<Map<String, Object>> getStatisticheSessioni(Integer chefId, int mese, int anno) {
        List<Map<String, Object>> statistiche = new ArrayList<>();
        String sql = """
            SELECT s.titolo, s.tipo, s.data_sessione, s.durata_minuti,
                   c.titolo as corso_titolo,
                   COUNT(sr.ricetta_id) as numero_ricette
            FROM sessioni s
            JOIN corsi c ON s.corso_id = c.id
            LEFT JOIN sessioni_ricette sr ON s.id = sr.sessione_id
            WHERE c.chef_id = ?
            AND EXTRACT(MONTH FROM s.data_sessione) = ?
            AND EXTRACT(YEAR FROM s.data_sessione) = ?
            GROUP BY s.id, s.titolo, s.tipo, s.data_sessione, s.durata_minuti, c.titolo
            ORDER BY s.data_sessione
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            stmt.setInt(2, mese);
            stmt.setInt(3, anno);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> stat = new HashMap<>();
                    stat.put("titoloSessione", rs.getString("titolo"));
                    stat.put("tipo", rs.getString("tipo"));
                    stat.put("dataSessione", rs.getDate("data_sessione"));
                    stat.put("durataMinuti", rs.getInt("durata_minuti"));
                    stat.put("corsoTitolo", rs.getString("corso_titolo"));
                    stat.put("numeroRicette", rs.getInt("numero_ricette"));
                    statistiche.add(stat);
                }
            }
            
            logger.debug("Trovate statistiche per {} sessioni", statistiche.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero statistiche sessioni", e);
        }
        
        return statistiche;
    }

    /**
     * Ottieni distribuzione ricette per chef in un periodo
     */
    public List<Map<String, Object>> getDistribuzioneRicette(Integer chefId, int mese, int anno) {
        List<Map<String, Object>> distribuzione = new ArrayList<>();
        String sql = """
            SELECT r.nome as ricetta_nome, r.difficolta, r.tempo_preparazione,
                   COUNT(sr.sessione_id) as utilizzi,
                   STRING_AGG(DISTINCT c.titolo, ', ') as corsi_utilizzati
            FROM ricette r
            JOIN sessioni_ricette sr ON r.id = sr.ricetta_id
            JOIN sessioni s ON sr.sessione_id = s.id
            JOIN corsi c ON s.corso_id = c.id
            WHERE r.chef_id = ?
            AND EXTRACT(MONTH FROM s.data_sessione) = ?
            AND EXTRACT(YEAR FROM s.data_sessione) = ?
            GROUP BY r.id, r.nome, r.difficolta, r.tempo_preparazione
            ORDER BY utilizzi DESC, r.nome
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            stmt.setInt(2, mese);
            stmt.setInt(3, anno);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> dist = new HashMap<>();
                    dist.put("ricettaNome", rs.getString("ricetta_nome"));
                    dist.put("difficolta", rs.getInt("difficolta"));
                    dist.put("tempoPreparazione", rs.getInt("tempo_preparazione"));
                    dist.put("utilizzi", rs.getInt("utilizzi"));
                    dist.put("corsiUtilizzati", rs.getString("corsi_utilizzati"));
                    distribuzione.add(dist);
                }
            }
            
            logger.debug("Trovata distribuzione per {} ricette", distribuzione.size());
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero distribuzione ricette", e);
        }
        
        return distribuzione;
    }

    /**
     * Metodo helper per statistiche mensili specifiche
     */
    private Map<String, Object> getStatisticheMensili(Integer chefId, int mese, int anno) {
        Map<String, Object> stats = new HashMap<>();
        
        // Query semplificata per ottenere statistiche mensili per uno specifico chef
        String sql = """
            SELECT 
                COUNT(*) as totale_iscrizioni,
                COUNT(*) FILTER (WHERE i.stato = 'ATTIVA') as iscrizioni_attive,
                COUNT(*) FILTER (WHERE i.stato = 'COMPLETATA') as iscrizioni_completate,
                COALESCE(SUM(c.prezzo) FILTER (WHERE i.stato IN ('ATTIVA', 'COMPLETATA')), 0) as ricavo_mensile
            FROM iscrizioni i
            JOIN corsi c ON i.corso_id = c.id
            WHERE c.chef_id = ?
            AND EXTRACT(YEAR FROM i.data_iscrizione) = ?
            AND EXTRACT(MONTH FROM i.data_iscrizione) = ?
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            stmt.setInt(2, anno);
            stmt.setInt(3, mese);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    stats.put("iscrizioniMensili", rs.getInt("totale_iscrizioni"));
                    stats.put("iscrizioniAttiveMensili", rs.getInt("iscrizioni_attive"));
                    stats.put("iscrizioniCompletateMensili", rs.getInt("iscrizioni_completate"));
                    stats.put("ricavoMensile", rs.getBigDecimal("ricavo_mensile"));
                } else {
                    // Valori di default se non ci sono dati
                    stats.put("iscrizioniMensili", 0);
                    stats.put("iscrizioniAttiveMensili", 0);
                    stats.put("iscrizioniCompletateMensili", 0);
                    stats.put("ricavoMensile", BigDecimal.ZERO);
                }
            }
            
            // Aggiungi statistiche sui corsi per il mese specifico
            aggiungiStatisticheCorsiMensili(stats, chefId, mese, anno);
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero statistiche mensili", e);
            // Valori di default in caso di errore
            stats.put("iscrizioniMensili", 0);
            stats.put("iscrizioniAttiveMensili", 0);
            stats.put("iscrizioniCompletateMensili", 0);
            stats.put("ricavoMensile", BigDecimal.ZERO);
        }
        
        return stats;
    }
    
    /**
     * Aggiunge statistiche sui corsi, sessioni e ricette per il mese specifico
     */
    private void aggiungiStatisticheCorsiMensili(Map<String, Object> stats, Integer chefId, int mese, int anno) {
        try {
            // Query per corsi attivi nel mese
            String sqlCorsi = """
                SELECT COUNT(DISTINCT c.id) as numeroCorsiTotali
                FROM corsi c
                WHERE c.chef_id = ?
                AND (
                    (EXTRACT(YEAR FROM c.data_inizio) = ? AND EXTRACT(MONTH FROM c.data_inizio) = ?)
                    OR EXISTS (
                        SELECT 1 FROM sessioni s 
                        WHERE s.corso_id = c.id 
                        AND EXTRACT(YEAR FROM s.data_sessione) = ? 
                        AND EXTRACT(MONTH FROM s.data_sessione) = ?
                    )
                )
                """;
            
            // Query per sessioni del mese
            String sqlSessioni = """
                SELECT 
                    COUNT(*) as numeroSessioniTotali,
                    COUNT(*) FILTER (WHERE tipo = 'online') as numeroSessioniOnline,
                    COUNT(*) FILTER (WHERE tipo IN ('presenza', 'pratica')) as numeroSessioniPratiche
                FROM sessioni s
                JOIN corsi c ON s.corso_id = c.id
                WHERE c.chef_id = ?
                AND EXTRACT(YEAR FROM s.data_sessione) = ?
                AND EXTRACT(MONTH FROM s.data_sessione) = ?
                """;
            
            // Query per ricette associate
            String sqlRicette = """
                WITH ricette_sessione AS (
                    SELECT s.id as sessione_id,
                           COUNT(sr.ricetta_id) as num_ricette
                    FROM sessioni s
                    JOIN corsi c ON s.corso_id = c.id
                    LEFT JOIN sessioni_ricette sr ON s.id = sr.sessione_id
                    WHERE c.chef_id = ?
                      AND EXTRACT(YEAR FROM s.data_sessione) = ?
                      AND EXTRACT(MONTH FROM s.data_sessione) = ?
                    GROUP BY s.id
                )
                SELECT COALESCE(SUM(num_ricette),0) AS ricetteTotali
                FROM ricette_sessione
                """;
            
            try (Connection conn = DatabaseManager.getConnection()) {
                // Esegui query per corsi
                try (PreparedStatement stmt = conn.prepareStatement(sqlCorsi)) {
                    stmt.setInt(1, chefId);
                    stmt.setInt(2, anno);
                    stmt.setInt(3, mese);
                    stmt.setInt(4, anno);
                    stmt.setInt(5, mese);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            stats.put("numeroCorsiTotali", rs.getInt("numeroCorsiTotali"));
                        } else {
                            stats.put("numeroCorsiTotali", 0);
                        }
                    }
                }
                
                // Esegui query per sessioni
                try (PreparedStatement stmt = conn.prepareStatement(sqlSessioni)) {
                    stmt.setInt(1, chefId);
                    stmt.setInt(2, anno);
                    stmt.setInt(3, mese);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            stats.put("numeroSessioniOnline", rs.getInt("numeroSessioniOnline"));
                            stats.put("numeroSessioniPratiche", rs.getInt("numeroSessioniPratiche"));
                        } else {
                            stats.put("numeroSessioniOnline", 0);
                            stats.put("numeroSessioniPratiche", 0);
                        }
                    }
                }
                
                // Esegui query per ricette
                try (PreparedStatement stmt = conn.prepareStatement(sqlRicette)) {
                    stmt.setInt(1, chefId);
                    stmt.setInt(2, anno);
                    stmt.setInt(3, mese);
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            stats.put("ricetteTotali", rs.getInt("ricetteTotali"));
                        } else {
                            stats.put("ricetteTotali", 0);
                        }
                    }
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero statistiche corsi mensili", e);
            // Valori di default in caso di errore
            stats.put("numeroCorsiTotali", 0);
            stats.put("numeroSessioniOnline", 0);
            stats.put("numeroSessioniPratiche", 0);
            stats.put("ricetteTotali", 0);
        }
    }

    /**
     * Ottieni distribuzione corsi per categoria dello chef
     */
    public Map<String, Integer> getDistribuzioneCorsiPerCategoria(Integer chefId) {
        Map<String, Integer> distribuzione = new HashMap<>();
        
        if (chefId == null) {
            logger.warn("Chef ID è null");
            return distribuzione;
        }
        
        String sql = """
            SELECT cat.nome as categoria, COUNT(c.id) as numero_corsi
            FROM categorie_corsi cat 
            LEFT JOIN corsi c ON cat.id = c.categoria_id AND c.chef_id = ?
            GROUP BY cat.nome 
            HAVING COUNT(c.id) > 0
            ORDER BY numero_corsi DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String categoria = rs.getString("categoria");
                    int numeroCorsi = rs.getInt("numero_corsi");
                    distribuzione.put(categoria, numeroCorsi);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero distribuzione corsi per categoria", e);
        }
        
        return distribuzione;
    }

    /**
     * Ottieni distribuzione sessioni per modalità dello chef
     */
    public Map<String, Integer> getDistribuzioneSessioniPerModalita(Integer chefId) {
        Map<String, Integer> distribuzione = new HashMap<>();
        
        if (chefId == null) {
            logger.warn("Chef ID è null");
            return distribuzione;
        }
        
        String sql = """
            SELECT 
                CASE 
                    WHEN s.tipo = 'online' THEN 'Online'
                    WHEN s.tipo IN ('presenza', 'pratica') THEN 'Presenza'
                    ELSE 'Altro'
                END as modalita,
                COUNT(*) as numero_sessioni
            FROM sessioni s
            JOIN corsi c ON s.corso_id = c.id
            WHERE c.chef_id = ?
            GROUP BY modalita
            ORDER BY numero_sessioni DESC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String modalita = rs.getString("modalita");
                    int numeroSessioni = rs.getInt("numero_sessioni");
                    distribuzione.put(modalita, numeroSessioni);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero distribuzione sessioni per modalità", e);
        }
        
        return distribuzione;
    }

    /**
     * Ottieni distribuzione ricette per difficoltà dello chef
     */
    public Map<String, Integer> getDistribuzioneRicettePerDifficolta(Integer chefId) {
        // LinkedHashMap per preservare ordine FACILE -> MEDIO -> DIFFICILE
        Map<String, Integer> distribuzione = new LinkedHashMap<>();
        
        if (chefId == null) {
            logger.warn("Chef ID è null");
            return distribuzione;
        }
        
        // Nuova logica: bucket di difficoltà (1-2=FACILE, 3=MEDIO, 4-5=DIFFICILE)
        // Somma corretta evitando il problema di GROUP BY sul valore originale che causava overwrite nella mappa.
        String sql = """
            WITH ricette_bucket AS (
                SELECT CASE 
                        WHEN difficolta IN (1,2) THEN 'FACILE'
                        WHEN difficolta = 3 THEN 'MEDIO'
                        WHEN difficolta IN (4,5) THEN 'DIFFICILE'
                        ELSE 'SCONOSCIUTO'
                    END AS bucket
                FROM ricette
                WHERE chef_id = ?
            )
            SELECT bucket, COUNT(*) AS numero_ricette
            FROM ricette_bucket
            GROUP BY bucket
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String difficolta = rs.getString("bucket");
                    int numeroRicette = rs.getInt("numero_ricette");
                    distribuzione.put(difficolta, numeroRicette);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero distribuzione ricette per difficoltà", e);
        }
        
        // Garantisce presenza delle tre categorie principali anche se a 0
        distribuzione.putIfAbsent("FACILE", 0);
        distribuzione.putIfAbsent("MEDIO", 0);
        distribuzione.putIfAbsent("DIFFICILE", 0);
        
        return distribuzione;
    }

    /**
     * Distribuzione ricette per difficoltà limitata al mese/anno indicati (basata sulle ricette realmente utilizzate nelle sessioni del mese)
     */
    public Map<String, Integer> getDistribuzioneRicettePerDifficoltaMensile(Integer chefId, int anno, int mese) {
        Map<String, Integer> distribuzione = new LinkedHashMap<>();
        if (chefId == null) {
            logger.warn("Chef ID è null");
            return distribuzione;
        }
        String sql = """
            WITH ricette_utilizzate AS (
                SELECT r.id, r.difficolta
                FROM sessioni s
                JOIN corsi c ON s.corso_id = c.id
                JOIN sessioni_ricette sr ON s.id = sr.sessione_id
                JOIN ricette r ON sr.ricetta_id = r.id
                WHERE c.chef_id = ?
                  AND EXTRACT(YEAR FROM s.data_sessione) = ?
                  AND EXTRACT(MONTH FROM s.data_sessione) = ?
            )
            SELECT CASE 
                        WHEN difficolta = 1 THEN 'FACILE'
                        WHEN difficolta = 2 THEN 'MEDIO'
                        WHEN difficolta >= 3 THEN 'DIFFICILE'
                        ELSE 'SCONOSCIUTO'
                   END AS bucket,
                   COUNT(*) AS numero_ricette
            FROM ricette_utilizzate
            GROUP BY bucket
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, chefId);
            stmt.setInt(2, anno);
            stmt.setInt(3, mese);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    distribuzione.put(rs.getString("bucket"), rs.getInt("numero_ricette"));
                }
            }
        } catch (SQLException e) {
            logger.error("Errore distribuzione ricette mensile", e);
        }
        distribuzione.putIfAbsent("FACILE", 0);
        distribuzione.putIfAbsent("MEDIO", 0);
        distribuzione.putIfAbsent("DIFFICILE", 0);
        return distribuzione;
    }

    /**
     * Ottieni andamento mensile di corsi e sessioni dello chef
     */
    public Map<String, Map<String, Integer>> getAndamentoMensile(Integer chefId) {
        Map<String, Map<String, Integer>> andamento = new LinkedHashMap<>();
        
        if (chefId == null) {
            logger.warn("Chef ID è null");
            return andamento;
        }
        
        // Recupera sempre gli ultimi 12 mesi (includendo quelli senza dati) in ordine cronologico crescente
        // Usa generate_series per creare l'elenco dei mesi
        String sql = """
            WITH mesi AS (
                SELECT generate_series(
                    date_trunc('month', CURRENT_DATE) - INTERVAL '11 months',
                    date_trunc('month', CURRENT_DATE),
                    interval '1 month'
                ) AS primo_giorno
            ),
            corsi_mensili AS (
                SELECT date_trunc('month', data_inizio) AS mese, COUNT(*) AS numero_corsi
                FROM corsi
                WHERE chef_id = ?
                GROUP BY date_trunc('month', data_inizio)
            ),
            sessioni_mensili AS (
                SELECT date_trunc('month', s.data_sessione) AS mese, COUNT(*) AS numero_sessioni
                FROM sessioni s
                JOIN corsi c ON s.corso_id = c.id
                WHERE c.chef_id = ?
                GROUP BY date_trunc('month', s.data_sessione)
            )
            SELECT 
                EXTRACT(YEAR FROM m.primo_giorno) AS anno,
                EXTRACT(MONTH FROM m.primo_giorno) AS mese,
                COALESCE(c.numero_corsi, 0) AS corsi,
                COALESCE(s.numero_sessioni, 0) AS sessioni
            FROM mesi m
            LEFT JOIN corsi_mensili c ON date_trunc('month', c.mese) = m.primo_giorno
            LEFT JOIN sessioni_mensili s ON date_trunc('month', s.mese) = m.primo_giorno
            ORDER BY m.primo_giorno ASC
            """;
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, chefId);
            stmt.setInt(2, chefId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int anno = rs.getInt("anno");
                    int mese = rs.getInt("mese");
                    int corsi = rs.getInt("corsi");
                    int sessioni = rs.getInt("sessioni");
                    
                    // Formato mese-anno
                    String nomeMese = java.time.Month.of(mese).getDisplayName(
                            java.time.format.TextStyle.SHORT,
                            java.util.Locale.ITALIAN
                    );
                    // Forza prima lettera maiuscola (getDisplayName potrebbe restituire minuscolo in alcune JVM)
                    if (!nomeMese.isEmpty()) {
                        nomeMese = nomeMese.substring(0,1).toUpperCase() + nomeMese.substring(1);
                    }
                    nomeMese = nomeMese + " " + anno;
                    
                    Map<String, Integer> datiMese = new HashMap<>();
                    datiMese.put("corsi", corsi);
                    datiMese.put("sessioni", sessioni);
                    
                    andamento.put(nomeMese, datiMese);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Errore nel recupero andamento mensile", e);
        }
        
        return andamento;
    }
}
