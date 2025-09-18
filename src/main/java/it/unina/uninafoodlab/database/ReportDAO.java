package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.ReportMensile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la generazione dei Report Mensili
 */
public class ReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);

    /**
     * Genera il report mensile per uno chef specifico
     */
    public Optional<ReportMensile> generaReportMensile(Integer chefId, int mese, int anno) {
        String sql = """
            WITH corsi_del_mese AS (
                SELECT c.id, c.chef_id, c.titolo
                FROM corsi c
                WHERE c.chef_id = ? 
                AND EXTRACT(MONTH FROM c.data_inizio) = ? 
                AND EXTRACT(YEAR FROM c.data_inizio) = ?
            ),
            sessioni_del_mese AS (
                SELECT s.id, s.corso_id, s.tipo,
                       (SELECT COUNT(*) FROM sessioni_ricette sr WHERE sr.sessione_id = s.id) as num_ricette
                FROM sessioni s
                JOIN corsi_del_mese c ON s.corso_id = c.id
                WHERE EXTRACT(MONTH FROM s.data_sessione) = ? 
                AND EXTRACT(YEAR FROM s.data_sessione) = ?
            ),
            stats AS (
                SELECT 
                    COUNT(DISTINCT cdm.id) as numero_corsi_totali,
                    COUNT(CASE WHEN sdm.tipo = 'online' THEN 1 END) as numero_sessioni_online,
                    COUNT(CASE WHEN sdm.tipo = 'presenza' THEN 1 END) as numero_sessioni_pratiche,
                    COALESCE(AVG(CASE WHEN sdm.tipo = 'presenza' AND sdm.num_ricette > 0 THEN sdm.num_ricette END), 0) as media_ricette_per_sessione,
                    COALESCE(MAX(CASE WHEN sdm.tipo = 'presenza' THEN sdm.num_ricette END), 0) as massimo_ricette_per_sessione,
                    COALESCE(MIN(CASE WHEN sdm.tipo = 'presenza' AND sdm.num_ricette > 0 THEN sdm.num_ricette END), 0) as minimo_ricette_per_sessione
                FROM corsi_del_mese cdm
                LEFT JOIN sessioni_del_mese sdm ON cdm.id = sdm.corso_id
            )
            SELECT 
                stats.*,
                ch.nome || ' ' || ch.cognome as nome_chef
            FROM stats
            CROSS JOIN chef ch
            WHERE ch.id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);
            stmt.setInt(2, mese);
            stmt.setInt(3, anno);
            stmt.setInt(4, mese);
            stmt.setInt(5, anno);
            stmt.setInt(6, chefId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ReportMensile report = new ReportMensile();
                    report.setMese(mese);
                    report.setAnno(anno);
                    report.setNomeChef(rs.getString("nome_chef"));
                    report.setNumeroCorsiTotali(rs.getInt("numero_corsi_totali"));
                    report.setNumeroSessioniOnline(rs.getInt("numero_sessioni_online"));
                    report.setNumeroSessioniPratiche(rs.getInt("numero_sessioni_pratiche"));
                    // Campi media/max/min rimossi dal modello: ignorati

                    logger.info("Report mensile generato per chef ID: {} - {}/{}", chefId, mese, anno);
                    return Optional.of(report);
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante la generazione del report mensile", e);
        }

        return Optional.empty();
    }

    /**
     * Ottieni lista dei mesi/anni disponibili per un chef
     */
    public List<String> getMesiDisponibili(Integer chefId) {
        List<String> mesi = new ArrayList<>();
        String sql = """
            SELECT DISTINCT 
                EXTRACT(YEAR FROM c.data_inizio) as anno,
                EXTRACT(MONTH FROM c.data_inizio) as mese
            FROM corsi c
            WHERE c.chef_id = ?
            ORDER BY anno DESC, mese DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int anno = rs.getInt("anno");
                    int mese = rs.getInt("mese");
                    
                    String[] nomiMesi = {
                        "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
                        "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"
                    };
                    
                    String periodo = nomiMesi[mese - 1] + " " + anno;
                    mesi.add(periodo);
                }
            }

            logger.debug("Trovati {} periodi disponibili per chef ID: {}", mesi.size(), chefId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei mesi disponibili", e);
        }

        return mesi;
    }

    /**
     * Ottieni statistiche dettagliate per i grafici
     */
    public List<Object[]> getStatisticheCorsi(Integer chefId, int mese, int anno) {
        List<Object[]> statistiche = new ArrayList<>();
        String sql = """
            SELECT 
                cat.nome as categoria,
                COUNT(c.id) as numero_corsi
            FROM corsi c
            JOIN categorie_corsi cat ON c.categoria_id = cat.id
            WHERE c.chef_id = ? 
            AND EXTRACT(MONTH FROM c.data_inizio) = ? 
            AND EXTRACT(YEAR FROM c.data_inizio) = ?
            GROUP BY cat.nome
            ORDER BY numero_corsi DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);
            stmt.setInt(2, mese);
            stmt.setInt(3, anno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getString("categoria"),
                        rs.getInt("numero_corsi")
                    };
                    statistiche.add(row);
                }
            }

            logger.debug("Trovate {} categorie con corsi per chef ID: {}", statistiche.size(), chefId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle statistiche corsi", e);
        }

        return statistiche;
    }

    /**
     * Ottieni statistiche sessioni per tipo
     */
    public List<Object[]> getStatisticheSessioni(Integer chefId, int mese, int anno) {
        List<Object[]> statistiche = new ArrayList<>();
        String sql = """
            SELECT 
                s.tipo,
                COUNT(s.id) as numero_sessioni
            FROM sessioni s
            JOIN corsi c ON s.corso_id = c.id
            WHERE c.chef_id = ? 
            AND EXTRACT(MONTH FROM s.data_sessione) = ? 
            AND EXTRACT(YEAR FROM s.data_sessione) = ?
            GROUP BY s.tipo
            ORDER BY numero_sessioni DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);
            stmt.setInt(2, mese);
            stmt.setInt(3, anno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo");
                    String tipoDescrizione = "presenza".equals(tipo) ? "In Presenza" : "Online";
                    
                    Object[] row = {
                        tipoDescrizione,
                        rs.getInt("numero_sessioni")
                    };
                    statistiche.add(row);
                }
            }

            logger.debug("Trovate {} tipologie di sessioni per chef ID: {}", statistiche.size(), chefId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle statistiche sessioni", e);
        }

        return statistiche;
    }

    /**
     * Ottieni distribuzione ricette per sessione pratica
     */
    public List<Object[]> getDistribuzioneRicette(Integer chefId, int mese, int anno) {
        List<Object[]> distribuzione = new ArrayList<>();
        String sql = """
            WITH sessioni_ricette_count AS (
                SELECT 
                    s.id as sessione_id,
                    COUNT(sr.ricetta_id) as num_ricette
                FROM sessioni s
                JOIN corsi c ON s.corso_id = c.id
                LEFT JOIN sessioni_ricette sr ON s.id = sr.sessione_id
                WHERE c.chef_id = ? 
                AND s.tipo = 'presenza'
                AND EXTRACT(MONTH FROM s.data_sessione) = ? 
                AND EXTRACT(YEAR FROM s.data_sessione) = ?
                GROUP BY s.id
            )
            SELECT 
                src.num_ricette,
                COUNT(*) as numero_sessioni
            FROM sessioni_ricette_count src
            WHERE src.num_ricette > 0
            GROUP BY src.num_ricette
            ORDER BY src.num_ricette
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);
            stmt.setInt(2, mese);
            stmt.setInt(3, anno);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Object[] row = {
                        rs.getInt("num_ricette") + " ricette",
                        rs.getInt("numero_sessioni")
                    };
                    distribuzione.add(row);
                }
            }

            logger.debug("Trovate {} distribuzioni ricette per chef ID: {}", distribuzione.size(), chefId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero della distribuzione ricette", e);
        }

        return distribuzione;
    }
}
