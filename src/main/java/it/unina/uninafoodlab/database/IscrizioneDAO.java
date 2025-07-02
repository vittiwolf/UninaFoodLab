package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Iscrizione;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la gestione delle Iscrizioni nel database
 */
public class IscrizioneDAO {
    private static final Logger logger = LoggerFactory.getLogger(IscrizioneDAO.class);

    /**
     * Ottieni tutte le iscrizioni di un corso
     */
    public List<Iscrizione> findByCorsoId(Integer corso_id) {
        List<Iscrizione> iscrizioni = new ArrayList<>();
        String sql = """
            SELECT i.id, i.utente_id, i.corso_id, i.data_iscrizione, i.stato, i.note,
                   u.nome, u.cognome, u.email,
                   c.titolo as titolo_corso
            FROM iscrizioni i
            JOIN utenti u ON i.utente_id = u.id
            JOIN corsi c ON i.corso_id = c.id
            WHERE i.corso_id = ?
            ORDER BY i.data_iscrizione DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, corso_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    iscrizioni.add(mapResultSetToIscrizione(rs));
                }
            }

            logger.debug("Trovate {} iscrizioni per corso ID: {}", iscrizioni.size(), corso_id);

        } catch (SQLException e) {
            logger.error("Errore nel recupero delle iscrizioni per corso ID: {}", corso_id, e);
        }

        return iscrizioni;
    }

    /**
     * Ottieni tutte le iscrizioni di un utente
     */
    public List<Iscrizione> findByUtenteId(Integer utente_id) {
        List<Iscrizione> iscrizioni = new ArrayList<>();
        String sql = """
            SELECT i.id, i.utente_id, i.corso_id, i.data_iscrizione, i.stato, i.note,
                   u.nome, u.cognome, u.email,
                   c.titolo as titolo_corso
            FROM iscrizioni i
            JOIN utenti u ON i.utente_id = u.id
            JOIN corsi c ON i.corso_id = c.id
            WHERE i.utente_id = ?
            ORDER BY i.data_iscrizione DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utente_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    iscrizioni.add(mapResultSetToIscrizione(rs));
                }
            }

            logger.debug("Trovate {} iscrizioni per utente ID: {}", iscrizioni.size(), utente_id);

        } catch (SQLException e) {
            logger.error("Errore nel recupero delle iscrizioni per utente ID: {}", utente_id, e);
        }

        return iscrizioni;
    }

    /**
     * Trova un'iscrizione per ID
     */
    public Optional<Iscrizione> findById(Integer id) {
        String sql = """
            SELECT i.id, i.utente_id, i.corso_id, i.data_iscrizione, i.stato, i.note,
                   u.nome, u.cognome, u.email,
                   c.titolo as titolo_corso
            FROM iscrizioni i
            JOIN utenti u ON i.utente_id = u.id
            JOIN corsi c ON i.corso_id = c.id
            WHERE i.id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToIscrizione(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nel recupero dell'iscrizione con ID: {}", id, e);
        }

        return Optional.empty();
    }

    /**
     * Verifica se un utente è già iscritto a un corso
     */
    public boolean isUtenteIscritto(Integer utente_id, Integer corso_id) {
        String sql = """
            SELECT COUNT(*) 
            FROM iscrizioni 
            WHERE utente_id = ? AND corso_id = ? AND stato = 'ATTIVA'
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utente_id);
            stmt.setInt(2, corso_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nel controllo iscrizione utente {} al corso {}", utente_id, corso_id, e);
        }

        return false;
    }

    /**
     * Salva una nuova iscrizione
     */
    public Iscrizione save(Iscrizione iscrizione) {
        String sql = """
            INSERT INTO iscrizioni (utente_id, corso_id, data_iscrizione, stato, note) 
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, iscrizione.getUtenteId());
            stmt.setInt(2, iscrizione.getCorsoId());
            stmt.setTimestamp(3, Timestamp.valueOf(iscrizione.getDataIscrizione()));
            stmt.setString(4, iscrizione.getStato());
            stmt.setString(5, iscrizione.getNote());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        iscrizione.setId(keys.getInt(1));
                        logger.info("Nuova iscrizione salvata con ID: {}", iscrizione.getId());
                        return iscrizione;
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dell'iscrizione: utente {} corso {}", 
                        iscrizione.getUtenteId(), iscrizione.getCorsoId(), e);
        }

        return null;
    }

    /**
     * Aggiorna un'iscrizione esistente
     */
    public Iscrizione update(Iscrizione iscrizione) {
        String sql = """
            UPDATE iscrizioni 
            SET stato = ?, note = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, iscrizione.getStato());
            stmt.setString(2, iscrizione.getNote());
            stmt.setInt(3, iscrizione.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                logger.info("Iscrizione aggiornata: ID {}", iscrizione.getId());
                return iscrizione;
            }

        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento dell'iscrizione: ID {}", iscrizione.getId(), e);
        }

        return null;
    }

    /**
     * Annulla un'iscrizione
     */
    public boolean annullaIscrizione(Integer id, String motivo) {
        String sql = """
            UPDATE iscrizioni 
            SET stato = 'ANNULLATA', note = COALESCE(note || '; ', '') || ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "Annullata: " + motivo);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                logger.info("Iscrizione annullata: ID {}", id);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Errore durante l'annullamento dell'iscrizione: ID {}", id, e);
        }

        return false;
    }

    /**
     * Completa un'iscrizione
     */
    public boolean completaIscrizione(Integer id) {
        String sql = "UPDATE iscrizioni SET stato = 'COMPLETATA' WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                logger.info("Iscrizione completata: ID {}", id);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Errore durante il completamento dell'iscrizione: ID {}", id, e);
        }

        return false;
    }

    /**
     * Ottieni il numero di iscritti attivi per un corso
     */
    public int countIscrittiAttivi(Integer corso_id) {
        String sql = "SELECT COUNT(*) FROM iscrizioni WHERE corso_id = ? AND stato = 'ATTIVA'";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, corso_id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nel conteggio iscritti per corso ID: {}", corso_id, e);
        }

        return 0;
    }

    /**
     * Ottieni tutte le iscrizioni attive per report
     */
    public List<Iscrizione> findAllAttive() {
        List<Iscrizione> iscrizioni = new ArrayList<>();
        String sql = """
            SELECT i.id, i.utente_id, i.corso_id, i.data_iscrizione, i.stato, i.note,
                   u.nome, u.cognome, u.email,
                   c.titolo as titolo_corso
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
                iscrizioni.add(mapResultSetToIscrizione(rs));
            }

            logger.debug("Trovate {} iscrizioni attive", iscrizioni.size());

        } catch (SQLException e) {
            logger.error("Errore nel recupero delle iscrizioni attive", e);
        }

        return iscrizioni;
    }

    /**
     * Mappa un ResultSet a un oggetto Iscrizione
     */
    private Iscrizione mapResultSetToIscrizione(ResultSet rs) throws SQLException {
        Iscrizione iscrizione = new Iscrizione();
        iscrizione.setId(rs.getInt("id"));
        iscrizione.setUtenteId(rs.getInt("utente_id"));
        iscrizione.setCorsoId(rs.getInt("corso_id"));
        iscrizione.setNomeUtente(rs.getString("nome"));
        iscrizione.setCognomeUtente(rs.getString("cognome"));
        iscrizione.setEmailUtente(rs.getString("email"));
        iscrizione.setTitoloCorso(rs.getString("titolo_corso"));
        
        Timestamp data_iscrizione = rs.getTimestamp("data_iscrizione");
        if (data_iscrizione != null) {
            iscrizione.setDataIscrizione(data_iscrizione.toLocalDateTime());
        }
        
        iscrizione.setStato(rs.getString("stato"));
        iscrizione.setNote(rs.getString("note"));
        
        return iscrizione;
    }
}
