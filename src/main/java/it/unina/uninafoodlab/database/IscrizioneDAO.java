package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Iscrizione;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO di base per la gestione delle Iscrizioni
 */
public class IscrizioneDAO {
    private static final Logger logger = LoggerFactory.getLogger(IscrizioneDAO.class);

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

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creazione iscrizione fallita, nessuna riga interessata.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    iscrizione.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creazione iscrizione fallita, nessun ID ottenuto.");
                }
            }

            logger.info("Iscrizione salvata con ID: {}", iscrizione.getId());
            return iscrizione;

        } catch (SQLException e) {
            logger.error("Errore nel salvataggio dell'iscrizione", e);
            throw new RuntimeException("Errore nel salvataggio dell'iscrizione", e);
        }
    }

    /**
     * Aggiorna un'iscrizione esistente
     */
    public Iscrizione update(Iscrizione iscrizione) {
        String sql = """
            UPDATE iscrizioni 
            SET utente_id = ?, corso_id = ?, data_iscrizione = ?, stato = ?, note = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, iscrizione.getUtenteId());
            stmt.setInt(2, iscrizione.getCorsoId());
            stmt.setTimestamp(3, Timestamp.valueOf(iscrizione.getDataIscrizione()));
            stmt.setString(4, iscrizione.getStato());
            stmt.setString(5, iscrizione.getNote());
            stmt.setInt(6, iscrizione.getId());

            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Aggiornamento iscrizione fallito, iscrizione non trovata con ID: " + iscrizione.getId());
            }

            logger.info("Iscrizione aggiornata con ID: {}", iscrizione.getId());
            return iscrizione;

        } catch (SQLException e) {
            logger.error("Errore nell'aggiornamento dell'iscrizione con ID: {}", iscrizione.getId(), e);
            throw new RuntimeException("Errore nell'aggiornamento dell'iscrizione", e);
        }
    }

    /**
     * Trova un'iscrizione per ID
     */
    public Optional<Iscrizione> findById(Integer id) {
        String sql = "SELECT * FROM iscrizioni WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Iscrizione iscrizione = mapResultSetToIscrizione(rs);
                    logger.debug("Iscrizione trovata con ID: {}", id);
                    return Optional.of(iscrizione);
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nella ricerca dell'iscrizione con ID: {}", id, e);
        }

        logger.debug("Iscrizione non trovata con ID: {}", id);
        return Optional.empty();
    }

    /**
     * Trova tutte le iscrizioni
     */
    public List<Iscrizione> findAll() {
        List<Iscrizione> iscrizioni = new ArrayList<>();
        String sql = "SELECT * FROM iscrizioni ORDER BY data_iscrizione DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                iscrizioni.add(mapResultSetToIscrizione(rs));
            }

            logger.debug("Trovate {} iscrizioni", iscrizioni.size());

        } catch (SQLException e) {
            logger.error("Errore nel recupero di tutte le iscrizioni", e);
        }

        return iscrizioni;
    }

    /**
     * Trova iscrizioni per utente ID
     */
    public List<Iscrizione> findByUtenteId(Integer utenteId) {
        List<Iscrizione> iscrizioni = new ArrayList<>();
        String sql = "SELECT * FROM iscrizioni WHERE utente_id = ? ORDER BY data_iscrizione DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, utenteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    iscrizioni.add(mapResultSetToIscrizione(rs));
                }
            }

            logger.debug("Trovate {} iscrizioni per utente ID: {}", iscrizioni.size(), utenteId);

        } catch (SQLException e) {
            logger.error("Errore nel recupero iscrizioni per utente ID: {}", utenteId, e);
        }

        return iscrizioni;
    }

    /**
     * Trova iscrizioni per corso ID
     */
    public List<Iscrizione> findByCorsoId(Integer corsoId) {
        List<Iscrizione> iscrizioni = new ArrayList<>();
        String sql = "SELECT * FROM iscrizioni WHERE corso_id = ? ORDER BY data_iscrizione DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, corsoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    iscrizioni.add(mapResultSetToIscrizione(rs));
                }
            }

            logger.debug("Trovate {} iscrizioni per corso ID: {}", iscrizioni.size(), corsoId);

        } catch (SQLException e) {
            logger.error("Errore nel recupero iscrizioni per corso ID: {}", corsoId, e);
        }

        return iscrizioni;
    }

    /**
     * Trova iscrizioni per stato
     */
    public List<Iscrizione> findByStato(String stato) {
        List<Iscrizione> iscrizioni = new ArrayList<>();
        String sql = "SELECT * FROM iscrizioni WHERE stato = ? ORDER BY data_iscrizione DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, stato);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    iscrizioni.add(mapResultSetToIscrizione(rs));
                }
            }

            logger.debug("Trovate {} iscrizioni con stato: {}", iscrizioni.size(), stato);

        } catch (SQLException e) {
            logger.error("Errore nel recupero iscrizioni per stato: {}", stato, e);
        }

        return iscrizioni;
    }

    /**
     * Elimina un'iscrizione per ID
     */
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM iscrizioni WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Iscrizione eliminata con ID: {}", id);
                return true;
            } else {
                logger.warn("Nessuna iscrizione trovata con ID: {}", id);
                return false;
            }

        } catch (SQLException e) {
            logger.error("Errore nell'eliminazione dell'iscrizione con ID: {}", id, e);
            return false;
        }
    }

    /**
     * Conta il numero di iscrizioni per un corso
     */
    public int countByCorsoId(Integer corsoId) {
        String sql = "SELECT COUNT(*) FROM iscrizioni WHERE corso_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, corsoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nel conteggio iscrizioni per corso ID: {}", corsoId, e);
        }

        return 0;
    }

    /**
     * Mappa un ResultSet a un oggetto Iscrizione
     */
    protected Iscrizione mapResultSetToIscrizione(ResultSet rs) throws SQLException {
        Iscrizione iscrizione = new Iscrizione();
        iscrizione.setId(rs.getInt("id"));
        iscrizione.setUtenteId(rs.getInt("utente_id"));
        iscrizione.setCorsoId(rs.getInt("corso_id"));
        
        Timestamp timestamp = rs.getTimestamp("data_iscrizione");
        if (timestamp != null) {
            iscrizione.setDataIscrizione(timestamp.toLocalDateTime());
        }
        
        iscrizione.setStato(rs.getString("stato"));
        iscrizione.setNote(rs.getString("note"));
        
        // I campi livello_esperienza e note_particolari non esistono nella tabella iscrizioni
        // Questi dati dovrebbero essere recuperati dalla tabella utenti se necessario
        // Per ora impostiamo valori di default
        iscrizione.setLivelloEsperienza(null);
        iscrizione.setNoteParticolari(null);

        return iscrizione;
    }
}
