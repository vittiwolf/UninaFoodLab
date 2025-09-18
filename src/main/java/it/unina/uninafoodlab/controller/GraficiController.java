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
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import java.text.DecimalFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*; // Usato per rendering grafici (Font, Color, Dimension)
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;

// PDF
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
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
    private ReportMensile reportCorrente; // Mantiene l'ultimo report caricato
    private String currentChartType; // Tipo di grafico attualmente visualizzato
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
            Map<String, Object> reportData = service.generaReportMensileWrapper(chefId, anno, mese);
            ReportMensile report = convertiMapAReportMensile(reportData);
            this.reportCorrente = report;
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
        // Mostra solo totale ricette (richiesta: rimuovere media)
        int ricetteTotali = report.getRicetteTotali();
        lblRicetteAssociate.setText(String.valueOf(ricetteTotali));
        lblCategoriaPopolare.setText("Cucina Italiana"); // Valore di esempio
    }
    
    @FXML
    private void aggiornaGrafico() {
        String tipoGrafico = cmbTipoGrafico.getValue();
        if (tipoGrafico == null) return;
        currentChartType = tipoGrafico;
        
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
            
            // Recupera i dati reali dal service per lo chef corrente
            Integer chefId = chefLoggato != null ? chefLoggato.getId() : null;
            Map<String, Integer> distribuzioneCategorie = service.getDistribuzioneCorsiPerCategoria(chefId);
            
            if (distribuzioneCategorie.isEmpty()) {
                dataset.setValue("Nessun corso disponibile", 1);
            } else {
                for (Map.Entry<String, Integer> entry : distribuzioneCategorie.entrySet()) {
                    dataset.setValue(entry.getKey(), entry.getValue());
                }
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
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            
            return chart;
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico corsi per categoria", e);
            return creaGraficoVuoto("Errore nel caricamento dati");
        }
    }
    
    private JFreeChart creaGraficoSessioniPerModalita() {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Recupera i dati reali dal service per lo chef corrente
            Integer chefId = chefLoggato != null ? chefLoggato.getId() : null;
            Map<String, Integer> distribuzioneSessioni = service.getDistribuzioneSessioniPerModalita(chefId);
            
            if (distribuzioneSessioni.isEmpty()) {
                dataset.addValue(0, "Sessioni", "Presenza");
                dataset.addValue(0, "Sessioni", "Online");
            } else {
                for (Map.Entry<String, Integer> entry : distribuzioneSessioni.entrySet()) {
                    dataset.addValue(entry.getValue(), "Sessioni", entry.getKey());
                }
            }
            
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
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setRangeGridlinePaint(java.awt.Color.GRAY);
            
            return chart;
            
        } catch (Exception e) {
            logger.error("Errore nella creazione del grafico sessioni per modalità", e);
            return creaGraficoVuoto("Errore nel caricamento dati");
        }
    }
    
    private JFreeChart creaGraficoAndamentoMensile() {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            
            // Recupera i dati reali dal service per lo chef corrente
            Integer chefId = chefLoggato != null ? chefLoggato.getId() : null;
            Map<String, Map<String, Integer>> andamentoMensile = service.getAndamentoMensile(chefId);
            
            if (andamentoMensile.isEmpty()) {
                dataset.addValue(0, "Corsi Creati", "Nessun dato");
                dataset.addValue(0, "Sessioni Totali", "Nessun dato");
            } else {
                // I dati sono già in ordine cronologico crescente (ultimi 12 mesi)
                andamentoMensile.forEach((mese, dati) -> {
                    dataset.addValue(dati.getOrDefault("corsi", 0), "Corsi Creati", mese);
                    dataset.addValue(dati.getOrDefault("sessioni", 0), "Sessioni Totali", mese);
                });
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
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setRangeGridlinePaint(java.awt.Color.GRAY);
            
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
            
            // Recupera i dati reali dal service per lo chef corrente
            Integer chefId = chefLoggato != null ? chefLoggato.getId() : null;
            // Recupera mese/anno selezionato per considerare solo le ricette usate nelle sessioni del periodo
            int annoSel;
            int meseSel;
            String meseSelezionato = cmbMeseAnno.getValue();
            if (meseSelezionato != null) {
                String[] parti = meseSelezionato.split(" ");
                annoSel = Integer.parseInt(parti[1]);
                meseSel = java.time.Month.valueOf(parti[0]).getValue();
            } else {
                java.time.YearMonth ym = java.time.YearMonth.now();
                annoSel = ym.getYear();
                meseSel = ym.getMonthValue();
            }
            Map<String, Integer> distribuzioneRicette = service.getDistribuzioneRicettePerDifficoltaMensile(chefId, annoSel, meseSel);
            
            if (distribuzioneRicette.isEmpty()) {
                dataset.setValue("Nessuna ricetta disponibile", 1);
            } else {
                // Inserimento in ordine logico
                int facile = distribuzioneRicette.getOrDefault("FACILE", 0);
                int medio = distribuzioneRicette.getOrDefault("MEDIO", 0);
                int difficile = distribuzioneRicette.getOrDefault("DIFFICILE", 0);
                int sconosciuto = distribuzioneRicette.getOrDefault("SCONOSCIUTO", 0);
                if (facile+medio+difficile+sconosciuto==0) {
                    dataset.setValue("Nessuna ricetta", 1);
                } else {
                    if (facile>0) dataset.setValue("FACILE", facile);
                    if (medio>0) dataset.setValue("MEDIO", medio);
                    if (difficile>0) dataset.setValue("DIFFICILE", difficile);
                    if (sconosciuto>0) dataset.setValue("SCONOSCIUTO", sconosciuto);
                }
            }
            
            JFreeChart chart = ChartFactory.createPieChart(
                "Distribuzione Ricette per Livello di Difficoltà",
                dataset,
                true, // leggenda
                true, // tooltips
                false // urls
            );
            
            // Personalizzazione
            PiePlot<String> plot = (PiePlot<String>) chart.getPlot();
            plot.setBackgroundPaint(java.awt.Color.WHITE);
            plot.setLabelFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            
            // Colori personalizzati per difficoltà
            plot.setSectionPaint("FACILE", java.awt.Color.GREEN);
            plot.setSectionPaint("MEDIO", java.awt.Color.ORANGE);
            plot.setSectionPaint("DIFFICILE", java.awt.Color.RED);
            plot.setSectionPaint("SCONOSCIUTO", java.awt.Color.LIGHT_GRAY);
            plot.setSimpleLabels(false);
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0}: {1} ({2})", new DecimalFormat("0"), new DecimalFormat("0.0%")));
            
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
            javafx.scene.control.ChoiceDialog<String> dialog = new javafx.scene.control.ChoiceDialog<>("PNG", "PNG", "PDF");
            dialog.setTitle("Esporta Report");
            dialog.setHeaderText("Scegli il formato di esportazione");
            dialog.setContentText("Formato:");
            java.util.Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String formato = result.get();
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
                    default:
                        mostraErrore("Formato non supportato");
                }
            }
        } catch (Exception e) {
            logger.error("Errore nell'esportazione del report", e);
            mostraErrore("Errore nell'esportazione: " + e.getMessage());
        }
    }

    private void esportaComePNG(FileChooser fileChooser) throws IOException {
        File file = fileChooser.showSaveDialog(graficoContainer.getScene().getWindow());
        if (file != null) {
            if (!file.getName().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }
            org.jfree.chart.ChartUtils.saveChartAsPNG(file, graficoCorrente, 800, 600);
            mostraInfo("Grafico esportato come PNG: " + file.getName());
        }
    }

    @FXML
    private void stampaReport() {
        try {
            if (graficoCorrente == null) {
                mostraErrore("Nessun grafico disponibile per la stampa");
                return;
            }
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            ChartPanel chartPanel = new ChartPanel(graficoCorrente);
            chartPanel.setPreferredSize(new Dimension(800, 600));
            job.setPrintable(chartPanel);
            if (job.printDialog()) {
                job.print();
                mostraInfo("Stampa inviata con successo");
            }
        } catch (Exception e) {
            logger.error("Errore nella stampa del report", e);
            mostraErrore("Errore nella stampa: " + e.getMessage());
        }
    }

    @FXML
    private void esportaComePDF(FileChooser fileChooser) {
        File file = fileChooser.showSaveDialog(graficoContainer.getScene().getWindow());
        if (file == null) return;
        if (!file.getName().toLowerCase().endsWith(".pdf")) {
            file = new File(file.getAbsolutePath() + ".pdf");
        }
        if (graficoCorrente == null) {
            mostraErrore("Nessun grafico da esportare.");
            return;
        }
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            int imgWidth = 500;
            int imgHeight = 300;
            BufferedImage bufferedImage = graficoCorrente.createBufferedImage(imgWidth, imgHeight);
            PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, bufferedImageToPNGBytes(bufferedImage), "chart");

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 40f;
                float yStart = page.getMediaBox().getHeight() - margin;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(margin, yStart);
                contentStream.showText("Report Mensile UninaFoodLab");
                contentStream.endText();

                yStart -= 24;
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(margin, yStart);
                String sottotitolo = (currentChartType != null ? currentChartType : "Grafico") + (reportCorrente != null ? " - " + reportCorrente.getMese()+"/"+reportCorrente.getAnno() : "");
                contentStream.showText(sottotitolo);
                contentStream.endText();

                float imgX = margin;
                float imgY = yStart - imgHeight - 20;
                contentStream.drawImage(pdImage, imgX, imgY, imgWidth, imgHeight);

                if (reportCorrente != null) {
                    float statsY = imgY - 20;
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                    contentStream.newLineAtOffset(margin, statsY);
                    contentStream.showText("Statistiche principali:");
                    contentStream.endText();
                    statsY -= 14;
                    statsY = scriviRigaStat(contentStream, margin, statsY, "Corsi totali: "+reportCorrente.getNumeroCorsiTotali());
                    statsY = scriviRigaStat(contentStream, margin, statsY, "Sessioni online: "+reportCorrente.getNumeroSessioniOnline());
                    statsY = scriviRigaStat(contentStream, margin, statsY, "Sessioni presenza: "+reportCorrente.getNumeroSessioniPratiche());
                    scriviRigaStat(contentStream, margin, statsY, "Ricette totali: "+reportCorrente.getRicetteTotali());
                }
            }
            document.save(file);
            mostraInfo("PDF esportato: " + file.getName());
        } catch (Exception ex) {
            logger.error("Errore esportazione PDF", ex);
            mostraErrore("Errore esportazione PDF: " + ex.getMessage());
        }
    }

    private float scriviRigaStat(PDPageContentStream cs, float margin, float currentY, String testo) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 10);
        cs.newLineAtOffset(margin, currentY);
        cs.showText(testo);
        cs.endText();
        return currentY - 12;
    }

    // Converte BufferedImage in array PNG per PDFBox
    private byte[] bufferedImageToPNGBytes(BufferedImage image) throws IOException {
        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        }
    }
    
    /**
     * Converte una Map restituita dal service in un oggetto ReportMensile
     */
    private ReportMensile convertiMapAReportMensile(Map<String, Object> reportData) {
        ReportMensile report = new ReportMensile();
        
        try {
            // Estrai i dati dalla mappa e assegnali al report
            if (reportData.containsKey("mese")) {
                report.setMese(((Number) reportData.get("mese")).intValue());
            }
            if (reportData.containsKey("anno")) {
                report.setAnno(((Number) reportData.get("anno")).intValue());
            }
            if (reportData.containsKey("nomeChef")) {
                report.setNomeChef((String) reportData.get("nomeChef"));
            }
            if (reportData.containsKey("numeroCorsiTotali")) {
                report.setNumeroCorsiTotali(((Number) reportData.get("numeroCorsiTotali")).intValue());
            }
            if (reportData.containsKey("numeroSessioniOnline")) {
                report.setNumeroSessioniOnline(((Number) reportData.get("numeroSessioniOnline")).intValue());
            }
            if (reportData.containsKey("numeroSessioniPratiche")) {
                report.setNumeroSessioniPratiche(((Number) reportData.get("numeroSessioniPratiche")).intValue());
            }
            if (reportData.containsKey("ricetteTotali")) {
                report.setRicetteTotali(((Number) reportData.get("ricetteTotali")).intValue());
            }
            
            // Se il nome chef non è presente e abbiamo il chef loggato, usalo
            if (report.getNomeChef() == null && chefLoggato != null) {
                report.setNomeChef(chefLoggato.getNome() + " " + chefLoggato.getCognome());
            }
            
        } catch (Exception e) {
            logger.warn("Errore nella conversione dei dati del report", e);
            // Restituisci un report con valori di default
            return creaReportVuoto();
        }
        
        return report;
    }
    
    /**
     * Crea un report vuoto con valori di default
     */
    private ReportMensile creaReportVuoto() {
        ReportMensile report = new ReportMensile();
        report.setMese(LocalDate.now().getMonthValue());
        report.setAnno(LocalDate.now().getYear());
        report.setNomeChef(chefLoggato != null ? chefLoggato.getNome() + " " + chefLoggato.getCognome() : "N/A");
        report.setNumeroCorsiTotali(0);
        report.setNumeroSessioniOnline(0);
        report.setNumeroSessioniPratiche(0);
        report.setRicetteTotali(0);
        return report;
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
