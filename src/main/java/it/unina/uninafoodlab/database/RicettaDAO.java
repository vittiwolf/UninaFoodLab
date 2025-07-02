package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Ricetta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la gestione delle Ricette nel database
 */
public class RicettaDAO {
    private static final Logger logger = LoggerFactory.getLogger(RicettaDAO.class);

    /**
     * Ottieni tutte le ricette di uno chef
     */
    public List<Ricetta> findByChefId(Integer chefId) {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = """
            SELECT r.id, r.chef_id, r.nome, r.descrizione, r.difficolta, 
                   r.tempo_preparazione, r.numero_porzioni, r.istruzioni, r.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef
            FROM ricette r
            JOIN chef ch ON r.chef_id = ch.id
            WHERE r.chef_id = ?
            ORDER BY r.nome
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ricette.add(mapResultSetToRicetta(rs));
                }
            }

            logger.debug("Trovate {} ricette per chef ID: {}", ricette.size(), chefId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle ricette per chef ID: " + chefId, e);
        }

        return ricette;
    }

    /**
     * Ottieni tutte le ricette disponibili nel sistema
     */
    public List<Ricetta> findAll() {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = """
            SELECT r.id, r.chef_id, r.nome, r.descrizione, r.difficolta, 
                   r.tempo_preparazione, r.numero_porzioni, r.istruzioni, r.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef
            FROM ricette r
            JOIN chef ch ON r.chef_id = ch.id
            ORDER BY r.nome
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ricette.add(mapResultSetToRicetta(rs));
            }

            logger.debug("Trovate {} ricette totali", ricette.size());
        } catch (SQLException e) {
            logger.error("Errore durante il recupero di tutte le ricette", e);
        }

        return ricette;
    }

    /**
     * Cerca ricette per nome (ricerca parziale)
     */
    public List<Ricetta> searchByNome(String nome) {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = """
            SELECT r.id, r.chef_id, r.nome, r.descrizione, r.difficolta, 
                   r.tempo_preparazione, r.numero_porzioni, r.istruzioni, r.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef
            FROM ricette r
            JOIN chef ch ON r.chef_id = ch.id
            WHERE LOWER(r.nome) LIKE LOWER(?)
            ORDER BY r.nome
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + nome + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ricette.add(mapResultSetToRicetta(rs));
                }
            }

            logger.debug("Trovate {} ricette con nome contenente: {}", ricette.size(), nome);
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca ricette per nome: " + nome, e);
        }

        return ricette;
    }

    /**
     * Trova una ricetta per ID
     */
    public Optional<Ricetta> findById(Integer id) {
        String sql = """
            SELECT r.id, r.chef_id, r.nome, r.descrizione, r.difficolta, 
                   r.tempo_preparazione, r.numero_porzioni, r.istruzioni, r.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef
            FROM ricette r
            JOIN chef ch ON r.chef_id = ch.id
            WHERE r.id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRicetta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca della ricetta con ID: " + id, e);
        }

        return Optional.empty();
    }

    /**
     * Salva una nuova ricetta
     */
    public Ricetta save(Ricetta ricetta) {
        String sql = """
            INSERT INTO ricette (chef_id, nome, descrizione, difficolta, 
                               tempo_preparazione, numero_porzioni, istruzioni)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, ricetta.getChefId());
            stmt.setString(2, ricetta.getNome());
            stmt.setString(3, ricetta.getDescrizione());
            stmt.setInt(4, ricetta.getDifficolta() != null ? ricetta.getDifficolta() : 1);
            stmt.setInt(5, ricetta.getTempoPreparazione() != null ? ricetta.getTempoPreparazione() : 30);
            stmt.setInt(6, ricetta.getNumeroPortions() != null ? ricetta.getNumeroPortions() : 4);
            stmt.setString(7, ricetta.getIstruzioni());

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        ricetta.setId(rs.getInt(1));
                        logger.info("Ricetta salvata con successo con ID: {}", ricetta.getId());
                        return ricetta;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio della ricetta", e);
            throw new RuntimeException("Errore durante il salvataggio della ricetta", e);
        }

        throw new RuntimeException("Impossibile salvare la ricetta");
    }

    /**
     * Aggiorna una ricetta esistente
     */
    public Ricetta update(Ricetta ricetta) {
        String sql = """
            UPDATE ricette 
            SET nome = ?, descrizione = ?, difficolta = ?, 
                tempo_preparazione = ?, numero_porzioni = ?, istruzioni = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ricetta.getNome());
            stmt.setString(2, ricetta.getDescrizione());
            stmt.setInt(3, ricetta.getDifficolta() != null ? ricetta.getDifficolta() : 1);
            stmt.setInt(4, ricetta.getTempoPreparazione() != null ? ricetta.getTempoPreparazione() : 30);
            stmt.setInt(5, ricetta.getNumeroPortions() != null ? ricetta.getNumeroPortions() : 4);
            stmt.setString(6, ricetta.getIstruzioni());
            stmt.setInt(7, ricetta.getId());

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Ricetta aggiornata con successo: ID {}", ricetta.getId());
                return ricetta;
            } else {
                throw new RuntimeException("Nessuna riga aggiornata per la ricetta con ID: " + ricetta.getId());
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento della ricetta", e);
            throw new RuntimeException("Errore durante l'aggiornamento della ricetta", e);
        }
    }

    /**
     * Elimina una ricetta
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM ricette WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Ricetta eliminata con successo: ID {}", id);
                return true;
            } else {
                logger.warn("Nessuna ricetta trovata con ID: {}", id);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione della ricetta con ID: " + id, e);
            return false;
        }
    }

    /**
     * Ottieni le ricette non ancora associate a una sessione specifica
     */
    public List<Ricetta> findRicetteDisponibiliPerSessione(Integer sessione_id, Integer chefId) {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = """
            SELECT r.id, r.chef_id, r.nome, r.descrizione, r.difficolta, 
                   r.tempo_preparazione, r.numero_porzioni, r.istruzioni, r.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef
            FROM ricette r
            JOIN chef ch ON r.chef_id = ch.id
            WHERE r.chef_id = ?
            AND r.id NOT IN (
                SELECT ricetta_id FROM sessioni_ricette WHERE sessione_id = ?
            )
            ORDER BY r.nome
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);
            stmt.setInt(2, sessione_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ricette.add(mapResultSetToRicetta(rs));
                }
            }

            logger.debug("Trovate {} ricette disponibili per sessione ID: {}", ricette.size(), sessione_id);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle ricette disponibili per sessione", e);
        }

        return ricette;
    }

    /**
     * Mappa un ResultSet a un oggetto Ricetta
     */
    private Ricetta mapResultSetToRicetta(ResultSet rs) throws SQLException {
        Ricetta ricetta = new Ricetta();
        ricetta.setId(rs.getInt("id"));
        ricetta.setChefId(rs.getInt("chef_id"));
        ricetta.setNome(rs.getString("nome"));
        ricetta.setDescrizione(rs.getString("descrizione"));
        ricetta.setDifficolta(rs.getInt("difficolta"));
        ricetta.setTempoPreparazione(rs.getInt("tempo_preparazione"));
        ricetta.setNumeroPortions(rs.getInt("numero_porzioni"));
        ricetta.setIstruzioni(rs.getString("istruzioni"));
        ricetta.setNomeChef(rs.getString("nome_chef"));
        
        Timestamp created_at = rs.getTimestamp("created_at");
        if (created_at != null) {
            ricetta.setCreatedAt(created_at.toLocalDateTime());
        }

        return ricetta;
    }
}
