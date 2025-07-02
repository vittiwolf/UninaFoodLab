package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Utente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la gestione degli Utenti nel database
 */
public class UtenteDAO {
    private static final Logger logger = LoggerFactory.getLogger(UtenteDAO.class);

    /**
     * Ottieni tutti gli utenti attivi
     */
    public List<Utente> findAllAttivi() {
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
            logger.error("Errore nel recupero degli utenti attivi", e);
        }

        return utenti;
    }

    /**
     * Trova un utente per ID
     */
    public Optional<Utente> findById(Integer id) {
        String sql = """
            SELECT id, nome, cognome, email, telefono, data_nascita, 
                   livello_esperienza, attivo, created_at
            FROM utenti 
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUtente(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nel recupero dell'utente con ID: {}", id, e);
        }

        return Optional.empty();
    }

    /**
     * Trova utenti per email
     */
    public Optional<Utente> findByEmail(String email) {
        String sql = """
            SELECT id, nome, cognome, email, telefono, data_nascita, 
                   livello_esperienza, attivo, created_at
            FROM utenti 
            WHERE email = ? AND attivo = true
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUtente(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nel recupero dell'utente con email: {}", email, e);
        }

        return Optional.empty();
    }

    /**
     * Salva un nuovo utente
     */
    public Utente save(Utente utente) {
        String sql = """
            INSERT INTO utenti (nome, cognome, email, telefono, data_nascita, 
                               livello_esperienza, attivo) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getEmail());
            stmt.setString(4, utente.getTelefono());
            
            if (utente.getDataNascita() != null) {
                stmt.setDate(5, Date.valueOf(utente.getDataNascita()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            stmt.setString(6, utente.getLivelloEsperienza());
            stmt.setBoolean(7, utente.isAttivo());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        utente.setId(keys.getInt(1));
                        utente.setCreatedAt(LocalDateTime.now());
                        logger.info("Nuovo utente salvato con ID: {}", utente.getId());
                        return utente;
                    }
                }
            }

        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio dell'utente: {} {}", 
                        utente.getNome(), utente.getCognome(), e);
        }

        return null;
    }

    /**
     * Aggiorna un utente esistente
     */
    public Utente update(Utente utente) {
        String sql = """
            UPDATE utenti 
            SET nome = ?, cognome = ?, email = ?, telefono = ?, 
                data_nascita = ?, livello_esperienza = ?, attivo = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utente.getNome());
            stmt.setString(2, utente.getCognome());
            stmt.setString(3, utente.getEmail());
            stmt.setString(4, utente.getTelefono());
            
            if (utente.getDataNascita() != null) {
                stmt.setDate(5, Date.valueOf(utente.getDataNascita()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            
            stmt.setString(6, utente.getLivelloEsperienza());
            stmt.setBoolean(7, utente.isAttivo());
            stmt.setInt(8, utente.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                logger.info("Utente aggiornato: ID {}", utente.getId());
                return utente;
            }

        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento dell'utente: ID {}", utente.getId(), e);
        }

        return null;
    }

    /**
     * Disattiva un utente (soft delete)
     */
    public boolean disattiva(Integer id) {
        String sql = "UPDATE utenti SET attivo = false WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                logger.info("Utente disattivato: ID {}", id);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Errore durante la disattivazione dell'utente: ID {}", id, e);
        }

        return false;
    }

    /**
     * Cerca utenti per nome o cognome
     */
    public List<Utente> searchByName(String searchTerm) {
        List<Utente> utenti = new ArrayList<>();
        String sql = """
            SELECT id, nome, cognome, email, telefono, data_nascita, 
                   livello_esperienza, attivo, created_at
            FROM utenti 
            WHERE attivo = true 
              AND (LOWER(nome) LIKE LOWER(?) OR LOWER(cognome) LIKE LOWER(?))
            ORDER BY cognome, nome
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    utenti.add(mapResultSetToUtente(rs));
                }
            }

            logger.debug("Trovati {} utenti per ricerca: '{}'", utenti.size(), searchTerm);

        } catch (SQLException e) {
            logger.error("Errore nella ricerca utenti con termine: {}", searchTerm, e);
        }

        return utenti;
    }

    /**
     * Verifica se un'email è già utilizzata
     */
    public boolean isEmailUsed(String email, Integer excludeId) {
        String sql = "SELECT COUNT(*) FROM utenti WHERE email = ? AND attivo = true";
        
        if (excludeId != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            if (excludeId != null) {
                stmt.setInt(2, excludeId);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Errore nel controllo email utilizzata: {}", email, e);
        }

        return false;
    }

    /**
     * Mappa un ResultSet a un oggetto Utente
     */
    private Utente mapResultSetToUtente(ResultSet rs) throws SQLException {
        Utente utente = new Utente();
        utente.setId(rs.getInt("id"));
        utente.setNome(rs.getString("nome"));
        utente.setCognome(rs.getString("cognome"));
        utente.setEmail(rs.getString("email"));
        utente.setTelefono(rs.getString("telefono"));
        
        Date dataNascita = rs.getDate("data_nascita");
        if (dataNascita != null) {
            utente.setDataNascita(dataNascita.toLocalDate());
        }
        
        utente.setLivelloEsperienza(rs.getString("livello_esperienza"));
        utente.setAttivo(rs.getBoolean("attivo"));
        
        Timestamp created_at = rs.getTimestamp("created_at");
        if (created_at != null) {
            utente.setCreatedAt(created_at.toLocalDateTime());
        }
        
        return utente;
    }
}
