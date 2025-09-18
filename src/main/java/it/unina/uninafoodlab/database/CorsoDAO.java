package it.unina.uninafoodlab.database;

import it.unina.uninafoodlab.model.Corso;
import it.unina.uninafoodlab.model.CategoriaCorso;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO per la gestione dei Corsi nel database
 */
public class CorsoDAO {
    private static final Logger logger = LoggerFactory.getLogger(CorsoDAO.class);

    /**
     * Ottieni tutti i corsi di uno chef
     */
    public List<Corso> findByChefId(Integer chefId) {
        List<Corso> corsi = new ArrayList<>();
        String sql = """
            SELECT c.id, c.chef_id, c.categoria_id, c.titolo, c.descrizione, 
                   c.data_inizio, c.frequenza, c.numero_sessioni, c.prezzo, 
                   c.durata_corso, c.max_partecipanti, c.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef,
                   cat.nome as nome_categoria
            FROM corsi c
            JOIN chef ch ON c.chef_id = ch.id
            JOIN categorie_corsi cat ON c.categoria_id = cat.id
            WHERE c.chef_id = ?
            ORDER BY c.data_inizio DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    corsi.add(mapResultSetToCorso(rs));
                }
            }

            logger.debug("Trovati {} corsi per chef ID: {}", corsi.size(), chefId);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei corsi per chef ID: " + chefId, e);
        }

        return corsi;
    }

    /**
     * Ottieni tutti i corsi filtrati per categoria
     */
    public List<Corso> findByChefIdAndCategoria(Integer chefId, Integer categoria_id) {
        List<Corso> corsi = new ArrayList<>();
        String sql = """
            SELECT c.id, c.chef_id, c.categoria_id, c.titolo, c.descrizione, 
                   c.data_inizio, c.frequenza, c.numero_sessioni, c.prezzo, 
                   c.durata_corso, c.max_partecipanti, c.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef,
                   cat.nome as nome_categoria
            FROM corsi c
            JOIN chef ch ON c.chef_id = ch.id
            JOIN categorie_corsi cat ON c.categoria_id = cat.id
            WHERE c.chef_id = ? AND c.categoria_id = ?
            ORDER BY c.data_inizio DESC
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, chefId);
            stmt.setInt(2, categoria_id);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    corsi.add(mapResultSetToCorso(rs));
                }
            }

            logger.debug("Trovati {} corsi per chef ID: {} e categoria ID: {}", corsi.size(), chefId, categoria_id);
        } catch (SQLException e) {
            logger.error("Errore durante il recupero dei corsi filtrati", e);
        }

        return corsi;
    }

    /**
     * Trova un corso per ID
     */
    public Optional<Corso> findById(Integer id) {
        String sql = """
            SELECT c.id, c.chef_id, c.categoria_id, c.titolo, c.descrizione, 
                   c.data_inizio, c.frequenza, c.numero_sessioni, c.prezzo, 
                   c.durata_corso, c.max_partecipanti, c.created_at,
                   ch.nome || ' ' || ch.cognome as nome_chef,
                   cat.nome as nome_categoria
            FROM corsi c
            JOIN chef ch ON c.chef_id = ch.id
            JOIN categorie_corsi cat ON c.categoria_id = cat.id
            WHERE c.id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCorso(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante la ricerca del corso con ID: " + id, e);
        }

        return Optional.empty();
    }

    /**
     * Salva un nuovo corso
     */
    public Corso save(Corso corso) {
        String sql = """
            INSERT INTO corsi (chef_id, categoria_id, titolo, descrizione, data_inizio, 
                              frequenza, numero_sessioni, prezzo, durata_corso, max_partecipanti)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, corso.getChefId());
            stmt.setInt(2, corso.getCategoriaId());
            stmt.setString(3, corso.getTitolo());
            stmt.setString(4, corso.getDescrizione());
            stmt.setDate(5, Date.valueOf(corso.getDataInizio()));
            stmt.setString(6, corso.getFrequenza());
            stmt.setInt(7, corso.getNumeroSessioni());
            stmt.setBigDecimal(8, corso.getPrezzo());
            stmt.setInt(9, corso.getDurata() != null ? corso.getDurata() : 2);
            stmt.setInt(10, corso.getMaxPartecipanti() != null ? corso.getMaxPartecipanti() : 10);

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        corso.setId(rs.getInt(1));
                        logger.info("Corso salvato con successo con ID: {}", corso.getId());
                        return corso;
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Errore durante il salvataggio del corso", e);
            throw new RuntimeException(getErrorMessage(e), e);
        }

        throw new RuntimeException("Impossibile salvare il corso");
    }

    /**
     * Aggiorna un corso esistente
     */
    public Corso update(Corso corso) {
        String sql = """
            UPDATE corsi 
            SET categoria_id = ?, titolo = ?, descrizione = ?, data_inizio = ?, 
                frequenza = ?, numero_sessioni = ?, prezzo = ?, durata_corso = ?, max_partecipanti = ?
            WHERE id = ?
            """;

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, corso.getCategoriaId());
            stmt.setString(2, corso.getTitolo());
            stmt.setString(3, corso.getDescrizione());
            stmt.setDate(4, Date.valueOf(corso.getDataInizio()));
            stmt.setString(5, corso.getFrequenza());
            stmt.setInt(6, corso.getNumeroSessioni());
            stmt.setBigDecimal(7, corso.getPrezzo());
            stmt.setInt(8, corso.getDurata() != null ? corso.getDurata() : 2);
            stmt.setInt(9, corso.getMaxPartecipanti() != null ? corso.getMaxPartecipanti() : 10);
            stmt.setInt(10, corso.getId());

            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Corso aggiornato con successo: ID {}", corso.getId());
                return corso;
            } else {
                throw new RuntimeException("Nessuna riga aggiornata per il corso con ID: " + corso.getId());
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'aggiornamento del corso", e);
            throw new RuntimeException(getErrorMessage(e), e);
        }
    }

    /**
     * Elimina un corso
     */
    public boolean delete(Integer id) {
        String sql = "DELETE FROM corsi WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Corso eliminato con successo: ID {}", id);
                return true;
            } else {
                logger.warn("Nessun corso trovato con ID: {}", id);
                return false;
            }
        } catch (SQLException e) {
            logger.error("Errore durante l'eliminazione del corso con ID: " + id, e);
            return false;
        }
    }

    /**
     * Ottieni tutte le categorie corsi
     */
    public List<CategoriaCorso> findAllCategorie() {
        List<CategoriaCorso> categorie = new ArrayList<>();
        String sql = "SELECT id, nome, descrizione FROM categorie_corsi ORDER BY nome";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                CategoriaCorso categoria = new CategoriaCorso();
                categoria.setId(rs.getInt("id"));
                categoria.setNome(rs.getString("nome"));
                categoria.setDescrizione(rs.getString("descrizione"));
                categorie.add(categoria);
            }

            logger.debug("Trovate {} categorie corsi", categorie.size());
        } catch (SQLException e) {
            logger.error("Errore durante il recupero delle categorie corsi", e);
        }

        return categorie;
    }

    /**
     * Mappa un ResultSet a un oggetto Corso
     */
    private Corso mapResultSetToCorso(ResultSet rs) throws SQLException {
        Corso corso = new Corso();
        corso.setId(rs.getInt("id"));
        corso.setChefId(rs.getInt("chef_id"));
        corso.setCategoriaId(rs.getInt("categoria_id"));
        corso.setTitolo(rs.getString("titolo"));
        corso.setDescrizione(rs.getString("descrizione"));
        
        Date data_inizio = rs.getDate("data_inizio");
        if (data_inizio != null) {
            corso.setDataInizio(data_inizio.toLocalDate());
        }
        
        corso.setFrequenza(rs.getString("frequenza"));
        corso.setNumeroSessioni(rs.getInt("numero_sessioni"));
        corso.setPrezzo(rs.getBigDecimal("prezzo"));
        corso.setNomeChef(rs.getString("nome_chef"));
        corso.setNomeCategoria(rs.getString("nome_categoria"));
        
        // Aggiungi i nuovi campi
        corso.setDurata(rs.getInt("durata_corso"));
        corso.setMaxPartecipanti(rs.getInt("max_partecipanti"));
        
        Timestamp created_at = rs.getTimestamp("created_at");
        if (created_at != null) {
            corso.setCreatedAt(created_at.toLocalDateTime());
        }

        return corso;
    }

    /**
     * Metodo helper per gestire gli errori di violazione dei vincoli CHECK
     */
    private String getErrorMessage(SQLException e) {
        String errorMessage = e.getMessage();
        
        // Gestione errori specifici per i vincoli CHECK
        if (errorMessage.contains("chk_durata_corso_valida")) {
            return "Errore: La durata del corso deve essere compresa tra 1 e 8 ore.";
        } else if (errorMessage.contains("chk_max_partecipanti_valido")) {
            return "Errore: Il numero massimo di partecipanti deve essere compreso tra 1 e 50.";
        } else if (errorMessage.contains("CHECK")) {
            return "Errore: I dati inseriti non rispettano i vincoli di validazione.";
        } else if (errorMessage.contains("UNIQUE")) {
            return "Errore: Esiste già un corso con questo titolo.";
        } else if (errorMessage.contains("NOT NULL")) {
            return "Errore: Tutti i campi obbligatori devono essere compilati.";
        } else if (errorMessage.contains("FOREIGN KEY")) {
            return "Errore: Riferimento non valido a chef o categoria.";
        } else {
            return "Errore durante l'operazione sul corso: " + errorMessage;
        }
    }
}
