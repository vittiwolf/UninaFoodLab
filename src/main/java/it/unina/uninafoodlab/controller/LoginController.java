package it.unina.uninafoodlab.controller;

import it.unina.uninafoodlab.App;
import it.unina.uninafoodlab.model.Chef;
import it.unina.uninafoodlab.service.UninaFoodLabService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Controller per la schermata di login degli chef
 */
public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    private UninaFoodLabService service;
    private static Chef chefAutenticato;
    private Timeline errorTimer;
    
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblErrore;
    @FXML private ProgressIndicator progressLogin;
    
    @FXML
    private void initialize() {
        service = new UninaFoodLabService();
        
        // Nascondi il progress indicator inizialmente
        progressLogin.setVisible(false);
          // Nascondi il messaggio di errore inizialmente
        lblErrore.setVisible(false);
        
        // Configura i listener per i campi
        // Il messaggio di errore scompare solo dopo aver digitato almeno 2 caratteri
        txtUsername.textProperty().addListener((obs, oldText, newText) -> {
            if (lblErrore.isVisible() && newText != null && newText.length() >= 2) {
                nascondiErrore();
            }
        });
        
        txtPassword.textProperty().addListener((obs, oldText, newText) -> {
            if (lblErrore.isVisible() && newText != null && newText.length() >= 2) {
                nascondiErrore();
            }
        });
        
        // Enter per fare login
        txtPassword.setOnKeyPressed(this::handleKeyPressed);
        txtUsername.setOnKeyPressed(this::handleKeyPressed);
        
        logger.info("Controller di login inizializzato");
    }
    
    @FXML
    private void handleLogin() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
          // Validazione input
        if (username == null || username.trim().isEmpty()) {
            mostraErrore("⚠️ Il campo username è obbligatorio");
            txtUsername.requestFocus();
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            mostraErrore("⚠️ Il campo password è obbligatorio");
            txtPassword.requestFocus();
            return;
        }
        
        if (username.trim().length() < 3) {
            mostraErrore("⚠️ L'username deve contenere almeno 3 caratteri");
            txtUsername.requestFocus();
            return;
        }
        
        // Mostra loading
        setLoadingState(true);
        
        // Esegui autenticazione in background
        Thread authThread = new Thread(() -> {
            try {
                Optional<Chef> chefOpt = service.autenticaChef(username.trim(), password);
                
                javafx.application.Platform.runLater(() -> {
                    setLoadingState(false);
                      if (chefOpt.isPresent()) {
                        // Autenticazione riuscita
                        chefAutenticato = chefOpt.get();
                        logger.info("Login riuscito per chef: {}", chefAutenticato.getUsername());
                        
                        // Passa alla finestra principale con lo chef
                        App.showMainWindow(chefAutenticato);
                          } else {
                        // Autenticazione fallita
                        mostraErrore("❌ Username o password non corretti. Riprova.");
                        txtPassword.clear();
                        txtUsername.requestFocus();
                        logger.warn("Tentativo di login fallito per username: {}", username);
                    }
                });
                  } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    setLoadingState(false);
                    mostraErrore("🔌 Errore di connessione: " + e.getMessage());
                    logger.error("Errore durante l'autenticazione", e);
                });
            }
        });
        
        authThread.setDaemon(true);
        authThread.start();
    }
    
    @FXML
    private void handleEsci() {
        logger.info("Chiusura applicazione richiesta dall'utente");
        javafx.application.Platform.exit();
    }
    
    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin();
        }
    }    private void mostraErrore(String messaggio) {
        // Cancella il timer precedente se esiste
        if (errorTimer != null) {
            errorTimer.stop();
        }
        
        lblErrore.setText(messaggio);
        lblErrore.setVisible(true);
        
        // Aggiungi effetto di animazione per attirare l'attenzione
        lblErrore.setOpacity(0.0);
        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(
            javafx.util.Duration.millis(300), lblErrore);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        
        // Crea un timer per nascondere il messaggio dopo 8 secondi
        errorTimer = new Timeline(new KeyFrame(Duration.seconds(8), e -> {
            if (lblErrore.isVisible()) {
                nascondiErrore();
            }
        }));
        errorTimer.play();
        
        // Effetto shake per i campi di input
        if (messaggio.toLowerCase().contains("username")) {
            shakeField(txtUsername);
        } else if (messaggio.toLowerCase().contains("password")) {
            shakeField(txtPassword);
        } else {
            // Shake entrambi i campi per errori generici
            shakeField(txtUsername);
            shakeField(txtPassword);
        }
    }
    
    /**
     * Effetto shake per un campo di input per indicare errore
     */
    private void shakeField(javafx.scene.control.Control field) {
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(
            javafx.util.Duration.millis(50), field);
        shake.setFromX(0);
        shake.setToX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> field.setTranslateX(0));
        shake.play();
    }
      private void setLoadingState(boolean loading) {
        progressLogin.setVisible(loading);
        btnLogin.setDisable(loading);
        txtUsername.setDisable(loading);
        txtPassword.setDisable(loading);
        
        if (loading) {
            btnLogin.setText("Accesso in corso...");
            // Nascondi eventuali messaggi di errore durante il caricamento
            if (lblErrore.isVisible()) {
                nascondiErrore();
            }
        } else {
            btnLogin.setText("Accedi");
        }
    }
    
    /**
     * Ottieni lo chef attualmente autenticato
     */
    public static Chef getChefAutenticato() {
        return chefAutenticato;
    }
    
    /**
     * Pulisci i dati dell'autenticazione (per logout)
     */
    public static void logout() {
        chefAutenticato = null;
        logger.info("Logout effettuato");
    }
      /**
     * Nascondi gradualmente il messaggio di errore
     */
    private void nascondiErrore() {
        // Cancella il timer se esiste
        if (errorTimer != null) {
            errorTimer.stop();
        }
        
        if (lblErrore.isVisible()) {
            javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(200), lblErrore);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> lblErrore.setVisible(false));
            fadeOut.play();
        }
    }
}
