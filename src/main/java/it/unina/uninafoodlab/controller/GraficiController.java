package it.unina.uninafoodlab.controller;

import it.unina.uninafoodlab.model.Chef;
import it.unina.uninafoodlab.model.ReportMensile;
import it.unina.uninafoodlab.service.UninaFoodLabService;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller per la gestione dei report mensili e grafici per corsi di cucina
 */
public class GraficiController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(GraficiController.class);
    
    @FXML private BorderPane graficoContainer;
    @FXML private ComboBox<String> cmbTipoGrafico;
    @FXML private ComboBox<String> cmbMeseAnno;
    @FXML private VBox statistichePanel;
    @FXML private Label lblTotaleCorsi;
    @FXML private Label lblSessioniTotali;
    @FXML private Label lblSessioniOnline;
    @FXML private Label lblSessioniPresenza;
    @FXML private Label lblRicetteAssociate;
    @FXML private Label lblCategoriaPopolare;
      private UninaFoodLabService service;
    private Chef chefLoggato;
    private JFreeChart graficoCorrente; // Riferimento al grafico corrente per esportazione/stampa
      @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Inizializzazione controller report mensili");
        service = new UninaFoodLabService();
        inizializzaComboBox();
        inizializzaMesi();
        // Non caricare i dati finché il chef non è impostato
    }
    
    public void setChefLoggato(Chef chef) {
        this.chefLoggato = chef;
        // Solo ora carichiamo i dati del report
        caricaReportCorrente();
    }
    
    private void inizializzaComboBox() {
        cmbTipoGrafico.getItems().addAll(
            "Corsi per Categoria",
            "Sessioni per Modalità", 
            "Andamento Mensile Corsi",
            "Distribuzione Ricette per Difficoltà"
        );
        cmbTipoGrafico.setValue("Corsi per Categoria");
        cmbTipoGrafico.setOnAction(e -> aggiornaGrafico());
    }
    
    private void inizializzaMesi() {
        // Aggiungi gli ultimi 12 mesi
        LocalDate oggi = LocalDate.now();
        for (int i = 0; i < 12; i++) {
            YearMonth mese = YearMonth.from(oggi.minusMonths(i));
            String meseString = mese.getMonth().name() + " " + mese.getYear();
            cmbMeseAnno.getItems().add(meseString);
        }
        cmbMeseAnno.setValue(cmbMeseAnno.getItems().get(0)); // Mese corrente
        cmbMeseAnno.setOnAction(e -> caricaReportMese());
    }
    
    private void caricaReportCorrente() {
        YearMonth meseCorrente = YearMonth.now();
        caricaReport(meseCorrente.getYear(), meseCorrente.getMonthValue());
    }
    
    @FXML
    private void caricaReportMese() {
        String meseSelezionato = cmbMeseAnno.getValue();
        if (meseSelezionato != null) {
            String[] parti = meseSelezionato.split(" ");
            int anno = Integer.parseInt(parti[1]);
            int mese = java.time.Month.valueOf(parti[0]).getValue();
            caricaReport(anno, mese);
        }
    }
      private void caricaReport(int anno, int mese) {
        try {
            Integer chefId = chefLoggato != null ? chefLoggato.getId() : null;
            ReportMensile report = service.generaReportMensileWrapper(chefId, anno, mese);
            aggiornaStatistiche(report);
            aggiornaGrafico();
        } catch (Exception e) {
            logger.error("Errore nel caricamento del report", e);
            mostraErrore("Errore nel caricamento del report: " + e.getMessage());
        }
    }
    
    private void aggiornaStatistiche(ReportMensile report) {
        lblTotaleCorsi.setText(String.valueOf(report.getNumeroCorsiTotali()));
        lblSessioniTotali.setText(String.valueOf(report.getNumeroSessioniOnline() + report.getNumeroSessioniPratiche()));
        lblSessioniOnline.setText(String.valueOf(report.getNumeroSessioniOnline()));
        lblSessioniPresenza.setText(String.valueOf(report.getNumeroSessioniPratiche()));  
        lblRicetteAssociate.setText(String.valueOf((int)report.getMediaRicettePerSessione()));
        lblCategoriaPopolare.setText("Cucina Italiana"); // Valore di esempio
    }
    
    @FXML
    private void aggiornaGrafico() {
        String tipoGrafico = cmbTipoGrafico.getValue();
        if (tipoGrafico == null) return;
        
        try {
            JFreeChart chart = null;
            
            switch (tipoGrafico) {
                case "Corsi per Categoria":
                    chart = creaGraficoCorsiPerCategoria();
                    break;
                case "Sessioni per Modalità":
                    chart = creaGraficoSessioniPerModalita();
                    break;
                case "Andamento Mensile Corsi":
                    chart = creaGraficoAndamentoMensile();
                    break;
                case "Distribuzione Ricette per Difficoltà":
                    chart = creaGraficoRicettePerDifficolta();
                    break;
                default:
                    chart = creaGraficoCorsiPerCategoria();
            }
            
            if (chart != null) {
                mostraGrafico(chart);
            }
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico", e);
            mostraErrore("Errore nella creazione del grafico: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private JFreeChart creaGraficoCorsiPerCategoria() {
        try {
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            
            // Dati di esempio (sostituire con dati reali dal service)
            Map<String, Integer> datiEsempio = new HashMap<>();
            datiEsempio.put("Cucina Italiana", 5);
            datiEsempio.put("Cucina Internazionale", 3);
            datiEsempio.put("Pasticceria", 2);
            datiEsempio.put("Cucina Salutare", 4);
            
            for (Map.Entry<String, Integer> entry : datiEsempio.entrySet()) {
                dataset.setValue(entry.getKey(), entry.getValue());
            }
            
            JFreeChart chart = ChartFactory.createPieChart(
                "Distribuzione Corsi per Categoria",
                dataset,
                true, // leggenda
                true, // tooltips
                false // urls
            );
            
            // Personalizzazione grafico
            PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
            
            return chart;
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico corsi per categoria", e);
            return creaGraficoVuoto("Errore nel caricamento dati");
        }
    }
    
    private JFreeChart creaGraficoSessioniPerModalita() {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Dati di esempio (sostituire con dati reali dal service)
            dataset.addValue(15, "Sessioni", "Presenza");
            dataset.addValue(8, "Sessioni", "Online");
            
            JFreeChart chart = ChartFactory.createBarChart(
                "Sessioni per Modalità (Presenza vs Online)",
                "Modalità",
                "Numero Sessioni",
                dataset,
                PlotOrientation.VERTICAL,
                true, // leggenda
                true, // tooltips
                false // urls
            );
            
            // Personalizzazione
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.GRAY);
            
            return chart;
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico sessioni per modalità", e);
            return creaGraficoVuoto("Errore nel caricamento dati");
        }
    }
    
    private JFreeChart creaGraficoAndamentoMensile() {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Dati di esempio per gli ultimi 6 mesi
            String[] mesi = {"Gen", "Feb", "Mar", "Apr", "Mag", "Giu"};
            int[] corsi = {2, 3, 1, 4, 2, 3};
            int[] sessioni = {8, 12, 4, 16, 8, 12};
            
            for (int i = 0; i < mesi.length; i++) {
                dataset.addValue(corsi[i], "Corsi Creati", mesi[i]);
                dataset.addValue(sessioni[i], "Sessioni Totali", mesi[i]);
            }
            
            JFreeChart chart = ChartFactory.createLineChart(
                "Andamento Mensile - Corsi e Sessioni",
                "Mese",
                "Numero",
                dataset,
                PlotOrientation.VERTICAL,
                true, // leggenda
                true, // tooltips
                false // urls
            );
            
            // Personalizzazione
            CategoryPlot plot = chart.getCategoryPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.GRAY);
            
            return chart;
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico andamento mensile", e);
            return creaGraficoVuoto("Errore nel caricamento dati");
        }
    }
    
    @SuppressWarnings("unchecked")
    private JFreeChart creaGraficoRicettePerDifficolta() {
        try {
            DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
            
            // Dati di esempio (sostituire con dati reali dal service)
            dataset.setValue("FACILE", 8);
            dataset.setValue("MEDIO", 12);
            dataset.setValue("DIFFICILE", 5);
            
            JFreeChart chart = ChartFactory.createPieChart(
                "Distribuzione Ricette per Livello di Difficoltà",
                dataset,
                true, // leggenda
                true, // tooltips
                false // urls
            );
            
            // Personalizzazione
            PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
            plot.setBackgroundPaint(Color.WHITE);
            plot.setLabelFont(new Font("Arial", Font.PLAIN, 12));
            
            // Colori personalizzati per difficoltà
            plot.setSectionPaint("FACILE", Color.GREEN);
            plot.setSectionPaint("MEDIO", Color.ORANGE);
            plot.setSectionPaint("DIFFICILE", Color.RED);
            
            return chart;
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico ricette per difficoltà", e);
            return creaGraficoVuoto("Errore nel caricamento dati");
        }
    }
    
    private JFreeChart creaGraficoVuoto(String messaggio) {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue("Nessun dato", 1);
        
        JFreeChart chart = ChartFactory.createPieChart(
            messaggio,
            dataset,
            false, // leggenda
            false, // tooltips
            false // urls
        );
        
        return chart;
    }
      private void mostraGrafico(JFreeChart chart) {
        // Salva il riferimento al grafico corrente per esportazione/stampa
        this.graficoCorrente = chart;
        
        // Rimuovi il grafico precedente
        graficoContainer.setCenter(null);
        
        // Crea un nuovo SwingNode per il grafico
        SwingNode swingNode = new SwingNode();
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(600, 400));
        
        swingNode.setContent(chartPanel);
        graficoContainer.setCenter(swingNode);
    }
      @FXML
    private void esportaReport() {
        try {
            if (graficoCorrente == null) {
                mostraErrore("Nessun grafico disponibile per l'esportazione");
                return;
            }
            
            // Crea un dialog per scegliere il tipo di esportazione
            javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>("PNG", "PNG", "PDF", "Excel");
            dialog.setTitle("Esporta Report");
            dialog.setHeaderText("Scegli il formato di esportazione");
            dialog.setContentText("Formato:");
            
            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String formato = result.get();
                
                // Crea FileChooser
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Salva Report");
                
                switch (formato) {
                    case "PNG":
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG Files", "*.png"));
                        esportaComePNG(fileChooser);
                        break;
                    case "PDF":
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                        esportaComePDF(fileChooser);
                        break;
                    case "Excel":
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
                        esportaComeExcel(fileChooser);
                        break;
                }
            }
        } catch (Exception e) {
            logger.error("Errore nell'esportazione del report", e);
            mostraErrore("Errore nell'esportazione: " + e.getMessage());
        }
    }
    
    @FXML
    private void stampaReport() {
        try {
            if (graficoCorrente == null) {
                mostraErrore("Nessun grafico disponibile per la stampa");
                return;
            }
            
            // Crea un PrinterJob per stampare il grafico
            PrinterJob job = PrinterJob.getPrinterJob();
            
            // Crea un ChartPanel per la stampa
            ChartPanel chartPanel = new ChartPanel(graficoCorrente);
            chartPanel.setPreferredSize(new Dimension(800, 600));
            
            // Imposta il ChartPanel come printable
            job.setPrintable(chartPanel);
            
            // Mostra il dialog di stampa
            if (job.printDialog()) {
                job.print();
                mostraInfo("Stampa inviata con successo");
            }
        } catch (PrinterException e) {
            logger.error("Errore nella stampa del report", e);
            mostraErrore("Errore nella stampa: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Errore nella stampa del report", e);
            mostraErrore("Errore nella stampa: " + e.getMessage());
        }
    }
    
    private void esportaComePNG(FileChooser fileChooser) throws IOException {
        File file = fileChooser.showSaveDialog(graficoContainer.getScene().getWindow());
        if (file != null) {
            // Assicurati che abbia l'estensione corretta
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            
            ChartUtils.saveChartAsPNG(file, graficoCorrente, 800, 600);
            mostraInfo("Grafico esportato con successo come PNG: " + file.getName());
        }
    }
    
    private void esportaComePDF(FileChooser fileChooser) {
        // Per ora mostra un messaggio - implementazione PDF richiederebbe librerie aggiuntive
        mostraInfo("Esportazione PDF disponibile nella versione completa.\nUsa l'esportazione PNG per ora.");
    }
    
    private void esportaComeExcel(FileChooser fileChooser) {
        // Per ora mostra un messaggio - implementazione Excel richiederebbe Apache POI
        mostraInfo("Esportazione Excel disponibile nella versione completa.\nI dati sono visibili nel pannello statistiche.");
    }
    
    private void mostraErrore(String messaggio) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Errore");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
    
    private void mostraInfo(String messaggio) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}
