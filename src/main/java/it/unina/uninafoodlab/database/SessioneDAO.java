package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Sessione;
import it.unina.uninafoodlab.model.Ricetta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la gestione delle Sessioni nel database
 */
public class SessioneDAO {
    private static final Logger logger = LoggerFactory.getLogger(SessioneDAO.class);

    /**
     * Ottieni tutte le sessioni di un corso
     */
    public List<Sessione> findByCorsoId(Integer corso_id) {
        List<Sessione> sessioni = new ArrayList<>();
        String sql = """
            SELECT s.id, s.corso_id, s.numero_sessione, s.data_sessione, 
                   s.tipo, s.titolo, s.descrizione, s.durata_minuti, s.completata, s.created_at,
                   c.titolo as titolo_corso
            FROM sessioni s
            JOIN corsi c ON s.corso_id = c.id
            WHERE s.corso_id = ?
            ORDER BY s.numero_sessione
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, corso_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessioni.add(mapResultSetToSessione(rs));
                }
            }

            logger.debug("Trovate {} sessioni per corso ID: {}", sessioni.size(), corso_id);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle sessioni per corso ID: " + corso_id, e);
        }

        return sessioni;
    }

    /**
     * Ottieni solo le sessioni pratiche di un corso
     */
    public List<Sessione> findSessioniPraticheByCorsoId(Integer corso_id) {
        List<Sessione> sessioni = new ArrayList<>();
        String sql = """
            SELECT s.id, s.corso_id, s.numero_sessione, s.data_sessione, 
                   s.tipo, s.titolo, s.descrizione, s.durata_minuti, s.completata, s.created_at,
                   c.titolo as titolo_corso
            FROM sessioni s
            JOIN corsi c ON s.corso_id = c.id
            WHERE s.corso_id = ? AND s.tipo = 'presenza'
            ORDER BY s.numero_sessione
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, corso_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sessioni.add(mapResultSetToSessione(rs));
                }
            }

            logger.debug("Trovate {} sessioni pratiche per corso ID: {}", sessioni.size(), corso_id);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle sessioni pratiche per corso ID: " + corso_id, e);
        }

        return sessioni;
    }

    /**
     * Trova una sessione per ID
     */
    public Optional<Sessione> findById(Integer id) {
        String sql = """
            SELECT s.id, s.corso_id, s.numero_sessione, s.data_sessione, 
                   s.tipo, s.titolo, s.descrizione, s.durata_minuti, s.completata, s.created_at,
                   c.titolo as titolo_corso
            FROM sessioni s
            JOIN corsi c ON s.corso_id = c.id
            WHERE s.id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToSessione(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca della sessione con ID: " + id, e);
        }

        return Optional.empty();
    }

    /**
     * Salva una nuova sessione
     */
    public Sessione save(Sessione sessione) {
        String sql = """
            INSERT INTO sessioni (corso_id, numero_sessione, data_sessione, tipo, 
                                 titolo, descrizione, durata_minuti, completata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, sessione.getCorsoId());
            stmt.setInt(2, sessione.getNumeroSessione());
            stmt.setDate(3, Date.valueOf(sessione.getDataSessione()));
            stmt.setString(4, sessione.getTipo());
            stmt.setString(5, sessione.getTitolo());
            stmt.setString(6, sessione.getDescrizione());
            stmt.setInt(7, sessione.getDurataMinuti() != null ? sessione.getDurataMinuti() : 120);
            stmt.setBoolean(8, sessione.getCompletata() != null ? sessione.getCompletata() : false);

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        sessione.setId(rs.getInt(1));
                        logger.info("Sessione salvata con successo con ID: {}", sessione.getId());
                        return sessione;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio della sessione", e);
            throw new RuntimeException("Errore durante il salvataggio della sessione", e);
        }

        throw new RuntimeException("Impossibile salvare la sessione");
    }

    /**
     * Aggiorna una sessione esistente
     */
    public Sessione update(Sessione sessione) {
        String sql = """
            UPDATE sessioni 
            SET numero_sessione = ?, data_sessione = ?, tipo = ?, 
                titolo = ?, descrizione = ?, durata_minuti = ?, completata = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessione.getNumeroSessione());
            stmt.setDate(2, Date.valueOf(sessione.getDataSessione()));
            stmt.setString(3, sessione.getTipo());
            stmt.setString(4, sessione.getTitolo());
            stmt.setString(5, sessione.getDescrizione());
            stmt.setInt(6, sessione.getDurataMinuti() != null ? sessione.getDurataMinuti() : 120);
            stmt.setBoolean(7, sessione.getCompletata() != null ? sessione.getCompletata() : false);
            stmt.setInt(8, sessione.getId());

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Sessione aggiornata con successo: ID {}", sessione.getId());
                return sessione;
            } else {
                throw new RuntimeException("Nessuna riga aggiornata per la sessione con ID: " + sessione.getId());
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento della sessione", e);
            throw new RuntimeException("Errore durante l'aggiornamento della sessione", e);
        }
    }

    /**
     * Ottieni le ricette associate a una sessione pratica
     */
    public List<Ricetta> getRicetteBySessioneId(Integer sessione_id) {
        List<Ricetta> ricette = new ArrayList<>();
        String sql = """
            SELECT r.id, r.chef_id, r.nome, r.descrizione, r.difficolta, 
                   r.tempo_preparazione, r.numero_porzioni, r.istruzioni, r.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef,
                   sr.ordine_esecuzione
            FROM ricette r
            JOIN sessioni_ricette sr ON r.id = sr.ricetta_id
            JOIN chef ch ON r.chef_id = ch.id
            WHERE sr.sessione_id = ?
            ORDER BY sr.ordine_esecuzione
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessione_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
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
                    
                    ricette.add(ricetta);
                }
            }

            logger.debug("Trovate {} ricette per sessione ID: {}", ricette.size(), sessione_id);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle ricette per sessione ID: " + sessione_id, e);
        }

        return ricette;
    }

    /**
     * Associa una ricetta a una sessione pratica
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
            stmt.setInt(3, ordineEsecuzione != null ? ordineEsecuzione : 1);

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Ricetta {} associata alla sessione {} con ordine {}", ricetta_id, sessione_id, ordineEsecuzione);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'associazione ricetta-sessione", e);
        }

        return false;
    }

    /**
     * Rimuovi l'associazione ricetta-sessione
     */
    public boolean rimuoviRicetta(Integer sessione_id, Integer ricetta_id) {
        String sql = "DELETE FROM sessioni_ricette WHERE sessione_id = ? AND ricetta_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sessione_id);
            stmt.setInt(2, ricetta_id);

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Associazione ricetta {} rimossa dalla sessione {}", ricetta_id, sessione_id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Errore durante la rimozione associazione ricetta-sessione", e);
        }

        return false;
    }

    /**
     * Elimina una sessione
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM sessioni WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Sessione eliminata con successo: ID {}", id);
                return true;
            } else {
                logger.warn("Nessuna sessione trovata con ID: {}", id);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione della sessione con ID: " + id, e);
            return false;
        }
    }

    /**
     * Mappa un ResultSet a un oggetto Sessione
     */
    private Sessione mapResultSetToSessione(ResultSet rs) throws SQLException {
        Sessione sessione = new Sessione();
        sessione.setId(rs.getInt("id"));
        sessione.setCorsoId(rs.getInt("corso_id"));
        sessione.setNumeroSessione(rs.getInt("numero_sessione"));
        
        Date dataSessione = rs.getDate("data_sessione");
        if (dataSessione != null) {
            sessione.setDataSessione(dataSessione.toLocalDate());
        }
        
        sessione.setTipo(rs.getString("tipo"));
        sessione.setTitolo(rs.getString("titolo"));
        sessione.setDescrizione(rs.getString("descrizione"));
        sessione.setDurataMinuti(rs.getInt("durata_minuti"));
        sessione.setCompletata(rs.getBoolean("completata"));
        sessione.setTitoloCorso(rs.getString("titolo_corso"));
        
        Timestamp created_at = rs.getTimestamp("created_at");
        if (created_at != null) {
            sessione.setCreatedAt(created_at.toLocalDateTime());
        }

        return sessione;
    }
}
