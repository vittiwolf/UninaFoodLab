package it.unina.uninafoodlab.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Manager per la gestione della connessione al database PostgreSQL con HikariCP
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static HikariDataSource dataSource;
    
    // Blocco di inizializzazione statico
    static {
        initializeDataSource();
    }
    
    /**
     * Inizializza il DataSource HikariCP
     */
    private static void initializeDataSource() {
        try {
            Properties props = loadDatabaseProperties();
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setDriverClassName("org.postgresql.Driver");
            
            // Configurazioni del pool di connessioni
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximum", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimum", "2")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.timeout", "30000")));
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // Configurazioni aggiuntive per PostgreSQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            
            logger.info("Pool di connessioni HikariCP inizializzato con successo");
            
        } catch (Exception e) {
            logger.error("Errore nell'inizializzazione del pool di connessioni", e);
            throw new RuntimeException("Impossibile inizializzare il pool di connessioni", e);
        }
    }
    
    /**
     * Carica le proprietà di configurazione del database
     */
    private static Properties loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = DatabaseManager.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (input == null) {
                // Usa valori di default se il file non esiste
                props.setProperty("db.url", "jdbc:postgresql://localhost:5432/uninafoodlab");
                props.setProperty("db.username", "postgres");
                props.setProperty("db.password", "vittiwolf");
                props.setProperty("db.pool.maximum", "10");
                props.setProperty("db.pool.minimum", "2");
                props.setProperty("db.pool.timeout", "30000");
                logger.warn("File database.properties non trovato, utilizzando configurazione di default");
            } else {
                props.load(input);
                logger.info("Configurazione database caricata da database.properties");
            }
        }
        return props;
    }
    
    /**
     * Ottiene una connessione dal pool
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            logger.warn("DataSource non disponibile, reinizializzazione...");
            initializeDataSource();
        }
        return dataSource.getConnection();
    }
    
    /**
     * Testa la connessione al database
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed() && conn.isValid(5);
        } catch (SQLException e) {
            logger.error("Test connessione fallito", e);
            return false;
        }
    }
    
    /**
     * Ottieni informazioni sullo stato del pool di connessioni
     */
    public static String getPoolStatus() {
        if (dataSource != null) {
            return String.format("Pool Status - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "DataSource non inizializzato";
    }
    
    /**
     * Chiude il pool di connessioni
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Chiusura pool di connessioni...");
            dataSource.close();
            logger.info("Pool di connessioni chiuso correttamente");
        }
    }
    
    /**
     * Verifica se il DataSource è disponibile
     */
    public static boolean isDataSourceAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }
}
