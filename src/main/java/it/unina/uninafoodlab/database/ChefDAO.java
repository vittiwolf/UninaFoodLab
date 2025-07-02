package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Chef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la gestione dei Chef nel database
 */
public class ChefDAO {
    private static final Logger logger = LoggerFactory.getLogger(ChefDAO.class);

    /**
     * Autentica un chef con username e password
     */
    public Optional<Chef> autenticaChef(String username, String password) {
        String sql = """
            SELECT id, username, password, nome, cognome, email, specializzazione, created_at
            FROM chef 
            WHERE username = ? AND password = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Chef chef = mapResultSetToChef(rs);
                    logger.info("Chef autenticato con successo: {}", chef.getUsername());
                    return Optional.of(chef);
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'autenticazione del chef", e);
        }

        logger.warn("Tentativo di autenticazione fallito per username: {}", username);
        return Optional.empty();
    }

    /**
     * Trova un chef per ID
     */
    public Optional<Chef> findById(Integer id) {
        String sql = """
            SELECT id, username, password, nome, cognome, email, specializzazione, created_at
            FROM chef 
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToChef(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca del chef con ID: " + id, e);
        }

        return Optional.empty();
    }

    /**
     * Trova un chef per username
     */
    public Optional<Chef> findByUsername(String username) {
        String sql = """
            SELECT id, username, password, nome, cognome, email, specializzazione, created_at
            FROM chef 
            WHERE username = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToChef(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca del chef con username: " + username, e);
        }

        return Optional.empty();
    }

    /**
     * Ottieni tutti i chef
     */
    public List<Chef> findAll() {
        List<Chef> chef = new ArrayList<>();
        String sql = """
            SELECT id, username, password, nome, cognome, email, specializzazione, created_at
            FROM chef 
            ORDER BY cognome, nome
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                chef.add(mapResultSetToChef(rs));
            }

            logger.debug("Trovati {} chef", chef.size());
        } catch (SQLException e) {
            logger.error("Errore durante il recupero di tutti i chef", e);
        }

        return chef;
    }

    /**
     * Salva un nuovo chef
     */
    public Chef save(Chef chef) {
        String sql = """
            INSERT INTO chef (username, password, nome, cognome, email, specializzazione)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, chef.getUsername());
            stmt.setString(2, chef.getPassword());
            stmt.setString(3, chef.getNome());
            stmt.setString(4, chef.getCognome());
            stmt.setString(5, chef.getEmail());
            stmt.setString(6, chef.getSpecializzazione());

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        chef.setId(rs.getInt(1));
                        logger.info("Chef salvato con successo con ID: {}", chef.getId());
                        return chef;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio del chef", e);
            throw new RuntimeException("Errore durante il salvataggio del chef", e);
        }

        throw new RuntimeException("Impossibile salvare il chef");
    }

    /**
     * Aggiorna un chef esistente
     */
    public Chef update(Chef chef) {
        String sql = """
            UPDATE chef 
            SET username = ?, password = ?, nome = ?, cognome = ?, email = ?, specializzazione = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, chef.getUsername());
            stmt.setString(2, chef.getPassword());
            stmt.setString(3, chef.getNome());
            stmt.setString(4, chef.getCognome());
            stmt.setString(5, chef.getEmail());
            stmt.setString(6, chef.getSpecializzazione());
            stmt.setInt(7, chef.getId());

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Chef aggiornato con successo: ID {}", chef.getId());
                return chef;
            } else {
                throw new RuntimeException("Nessuna riga aggiornata per il chef con ID: " + chef.getId());
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del chef", e);
            throw new RuntimeException("Errore durante l'aggiornamento del chef", e);
        }
    }

    /**
     * Elimina un chef
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM chef WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Chef eliminato con successo: ID {}", id);
                return true;
            } else {
                logger.warn("Nessun chef trovato con ID: {}", id);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione del chef con ID: " + id, e);
            return false;
        }
    }

    /**
     * Mappa un ResultSet a un oggetto Chef
     */
    private Chef mapResultSetToChef(ResultSet rs) throws SQLException {
        Chef chef = new Chef();
        chef.setId(rs.getInt("id"));
        chef.setUsername(rs.getString("username"));
        chef.setPassword(rs.getString("password"));
        chef.setNome(rs.getString("nome"));
        chef.setCognome(rs.getString("cognome"));
        chef.setEmail(rs.getString("email"));
        chef.setSpecializzazione(rs.getString("specializzazione"));
        
        Timestamp created_at = rs.getTimestamp("created_at");
        if (created_at != null) {
            chef.setCreatedAt(created_at.toLocalDateTime());
        }

        return chef;
    }
}
