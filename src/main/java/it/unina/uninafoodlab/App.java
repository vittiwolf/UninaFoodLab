package it.unina.uninafoodlab;

import it.unina.uninafoodlab.controller.MainController;
import it.unina.uninafoodlab.database.DatabaseManager;
import it.unina.uninafoodlab.model.Chef;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Applicazione principale UninaFoodLab
 * Sistema per la gestione di corsi di cucina tematici
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    private static Stage primaryStage;
    
    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        
        logger.info("Avvio UninaFoodLab - Sistema Gestione Corsi di Cucina");
          // Verifica connessione database
        if (!DatabaseManager.testConnection()) {
            logger.error("Impossibile connettersi al database");
            showErrorAndExit("Errore di Connessione", 
                           "Impossibile connettersi al database PostgreSQL.\n" +
                           "Verificare che il database sia avviato e le configurazioni siano corrette.");
            return;
        }
        
        // Carica la schermata di login
        showLoginWindow();
    }
    
    /**
     * Mostra la finestra di login
     */
    public static void showLoginWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            // Assicura che lo stylesheet principale sia caricato anche nella schermata di login
            try {
                scene.getStylesheets().add(App.class.getResource("/css/application.css").toExternalForm());
            } catch (Exception cssEx) {
                logger.warn("Stylesheet application.css non trovato: {}", cssEx.getMessage());
            }
            
            primaryStage.setTitle("UninaFoodLab - Accesso Chef");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            
            // Icona dell'applicazione
            try {
                primaryStage.getIcons().add(new Image(App.class.getResourceAsStream("/images/chef-icon.png")));
            } catch (Exception e) {
                logger.warn("Icona applicazione non trovata");
            }
            
            primaryStage.show();
            
            logger.info("Finestra di login mostrata");
            
        } catch (Exception e) {
            logger.error("Errore nel caricamento della finestra di login", e);
            showErrorAndExit("Errore di Avvio", 
                           "Impossibile caricare l'interfaccia di login.\n" + e.getMessage());
        }
    }
      /**
     * Mostra la finestra principale dell'applicazione
     */
    public static void showMainWindow(Chef chef) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/MainView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(App.class.getResource("/css/application.css").toExternalForm());
            
            // Ottieni il controller e imposta lo chef loggato
            MainController controller = loader.getController();
            controller.setChefLoggato(chef);
              primaryStage.setTitle("UninaFoodLab - Gestione Corsi di Cucina");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            
            primaryStage.show();
            
            logger.info("Finestra principale mostrata");
            
        } catch (Exception e) {
            logger.error("Errore nel caricamento della finestra principale", e);
            showErrorAndExit("Errore di Avvio", 
                           "Impossibile caricare l'interfaccia principale.\n" + e.getMessage());
        }
    }
    
    /**
     * Ottieni il palco principale
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Mostra un errore critico e chiude l'applicazione
     */
    private static void showErrorAndExit(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        System.exit(1);
    }
    
    @Override
    public void stop() throws Exception {
        logger.info("Chiusura UninaFoodLab");
        
        // Chiudi il pool di connessioni
        try {
            DatabaseManager.closeDataSource();
            logger.info("Pool di connessioni chiuso correttamente");
        } catch (Exception e) {
            logger.error("Errore nella chiusura del pool di connessioni", e);
        }
        
        super.stop();
    }
      public static void main(String[] args) {
        logger.info("Avvio applicazione UninaFoodLab con argomenti: {}", (Object) args);
        
        try {
            launch(args);
        } catch (Exception e) {
            logger.error("Errore fatale durante l'avvio dell'applicazione", e);
            System.exit(1);
        }
    }
}
