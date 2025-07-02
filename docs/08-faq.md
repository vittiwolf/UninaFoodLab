# 09 - FAQ e Troubleshooting

## Indice
- [Domande Frequenti Generali](#domande-frequenti-generali)
- [Problemi di Installazione](#problemi-di-installazione)
- [Problemi Database](#problemi-database)
- [Problemi Interfaccia Utente](#problemi-interfaccia-utente)
- [Problemi Performance](#problemi-performance)
- [Messaggi di Errore Comuni](#messaggi-di-errore-comuni)
- [Debugging e Logging](#debugging-e-logging)
- [Best Practices](#best-practices)

## Domande Frequenti Generali

### Q: Cos'è UninaFoodLab?
**A:** UninaFoodLab è un sistema di gestione corsi di cucina tematici sviluppato per l'Università di Napoli Federico II. Permette agli chef di creare e gestire corsi di cucina, organizzare sessioni in presenza o online, e monitorare l'andamento attraverso report dettagliati.

### Q: Quali sono i requisiti minimi di sistema?
**A:** 
- **Java JDK 11+** (raccomandato 17)
- **PostgreSQL 12+**
- **4 GB RAM** (raccomandati 8 GB)
- **2 GB spazio libero su disco**
- **Risoluzione 1024x768** (raccomandata 1920x1080)

### Q: Il sistema supporta più utenti contemporaneamente?
**A:** Sì, il sistema è progettato per supportare accessi concorrenti di più chef. Il database PostgreSQL gestisce le transazioni concorrenti e il connection pool ottimizza le performance.

### Q: È possibile esportare i dati?
**A:** Sì, il sistema permette l'esportazione di:
- Report corsi in formato PDF/Excel
- Statistiche di utilizzo
- Backup completo del database

### Q: Il sistema funziona offline?
**A:** Il sistema richiede una connessione al database PostgreSQL. Per utilizzo offline, è possibile configurare un database locale.

## Problemi di Installazione

### Errore: "JAVA_HOME non trovato"

**Problema:** Il sistema non riesce a trovare l'installazione Java.

**Soluzione:**
```bash
# Windows
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.5.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

# macOS/Linux
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
```

**Verifica:**
```bash
java -version
javac -version
echo $JAVA_HOME  # Linux/macOS
echo %JAVA_HOME% # Windows
```

### Errore: "Maven non riconosciuto"

**Problema:** Maven non è installato o non è nel PATH.

**Soluzione:**
```bash
# Installazione Maven
# Ubuntu/Debian
sudo apt install maven

# macOS
brew install maven

# Windows - Download da maven.apache.org
# Aggiungere al PATH: C:\Program Files\Apache\Maven\bin
```

**Verifica:**
```bash
mvn -version
```

### Errore durante compilazione: "Package javafx does not exist"

**Problema:** JavaFX non è incluso nel JDK o non è configurato correttamente.

**Soluzione:**
1. Verificare che il progetto usi il plugin JavaFX corretto:
```xml
<plugin>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-maven-plugin</artifactId>
    <version>0.0.8</version>
</plugin>
```

2. Eseguire con il profilo corretto:
```bash
mvn javafx:run
```

3. Se necessario, scaricare JavaFX SDK e configurare:
```bash
--module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml
```

## Problemi Database

### Errore: "Connection refused to PostgreSQL"

**Problema:** Non è possibile connettersi al database PostgreSQL.

**Diagnosi:**
```bash
# Verificare se PostgreSQL è in esecuzione
sudo systemctl status postgresql  # Linux
brew services list | grep postgres  # macOS
# Windows: Services -> PostgreSQL

# Test connessione
psql -U postgres -h localhost -c "SELECT version();"
```

**Soluzioni:**
1. **Avviare PostgreSQL:**
```bash
# Linux
sudo systemctl start postgresql

# macOS
brew services start postgresql

# Windows
net start postgresql-x64-14
```

2. **Verificare configurazione:**
```bash
# Controllare file postgresql.conf
# Listen su tutte le interfacce
listen_addresses = '*'

# Controllare file pg_hba.conf
# Permettere connessioni locali
local   all             all                                     md5
host    all             all             127.0.0.1/32            md5
```

3. **Verificare firewall:**
```bash
# Linux - aprire porta 5432
sudo ufw allow 5432

# Windows - aggiungere regola firewall per porta 5432
```

### Errore: "Password authentication failed"

**Problema:** Le credenziali del database non sono corrette.

**Soluzione:**
1. **Reset password postgres:**
```bash
# Linux
sudo -u postgres psql
\password postgres

# macOS
psql postgres
\password postgres
```

2. **Verificare configurazione applicazione:**
```properties
# File: src/main/resources/database.properties
db.username=foodlab_user
db.password=PasswordCorretta
```

3. **Creare utente se non esiste:**
```sql
CREATE USER foodlab_user WITH ENCRYPTED PASSWORD 'SecurePassword123!';
GRANT ALL PRIVILEGES ON DATABASE uninafoodlab TO foodlab_user;
```

### Errore: "Database uninafoodlab does not exist"

**Problema:** Il database non è stato creato.

**Soluzione:**
```sql
-- Connettersi come utente postgres
psql -U postgres -h localhost

-- Creare database
CREATE DATABASE uninafoodlab
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'it_IT.UTF-8'
    LC_CTYPE = 'it_IT.UTF-8';

-- Eseguire script inizializzazione
\c uninafoodlab
\i database/01_create_tables.sql
\i database/02_initial_data.sql
```

### Performance lente del database

**Problema:** Query lente o timeout.

**Diagnosi:**
```sql
-- Controllare query lente
SELECT query, mean_time, calls 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Verificare connessioni attive
SELECT count(*) FROM pg_stat_activity;

-- Controllare indici mancanti
SELECT schemaname, tablename, attname, n_distinct, correlation 
FROM pg_stats 
WHERE schemaname = 'public';
```

**Soluzioni:**
1. **Ottimizzare configuration:**
```postgresql
# postgresql.conf
shared_buffers = 256MB
effective_cache_size = 1GB
maintenance_work_mem = 64MB
checkpoint_completion_target = 0.9
wal_buffers = 16MB
default_statistics_target = 100
random_page_cost = 1.1
```

2. **Aggiungere indici:**
```sql
-- Indici per performance
CREATE INDEX CONCURRENTLY idx_corso_chef_id ON corso(chef_id);
CREATE INDEX CONCURRENTLY idx_sessione_corso_id ON sessione(corso_id);
CREATE INDEX CONCURRENTLY idx_sessione_data_ora ON sessione(data_ora);
```

## Problemi Interfaccia Utente

### Errore: "Application window not showing"

**Problema:** L'applicazione si avvia ma non mostra la finestra.

**Diagnosi:**
```bash
# Verificare se processo è attivo
jps -v | grep UninaFoodLab

# Controllare log
tail -f logs/uninafoodlab.log
```

**Soluzioni:**
1. **Verificare display (Linux):**
```bash
export DISPLAY=:0.0
echo $DISPLAY
```

2. **Eseguire con debug JavaFX:**
```bash
java -Dprism.verbose=true -Djavafx.verbose=true -jar uninafoodlab.jar
```

3. **Verificare librerie grafiche:**
```bash
# Linux - installare dipendenze
sudo apt install libopenjfx-java
sudo apt install openjfx

# macOS - verificare XQuartz se necessario
```

### Interfaccia bloccata o non responsiva

**Problema:** L'interfaccia si blocca durante operazioni.

**Diagnosi:**
```java
// Aggiungere logging per identificare il punto di blocco
logger.debug("Prima dell'operazione lunga");
// ... operazione
logger.debug("Dopo l'operazione lunga");
```

**Soluzioni:**
1. **Spostare operazioni lunghe in background:**
```java
// Usare Task per operazioni asincrone
Task<List<Corso>> task = new Task<List<Corso>>() {
    @Override
    protected List<Corso> call() throws Exception {
        return service.getAllCorsi();
    }
};

task.setOnSucceeded(e -> {
    Platform.runLater(() -> {
        tableView.setItems(FXCollections.observableArrayList(task.getValue()));
    });
});

new Thread(task).start();
```

2. **Ottimizzare binding e listener:**
```java
// Evitare listener complessi nella UI
// Usare weak references quando possibile
```

### Errore: "FXML Load Exception"

**Problema:** Errore nel caricamento dei file FXML.

**Diagnosi:**
```java
// Verificare path del file FXML
URL fxmlUrl = getClass().getResource("/fxml/MainView.fxml");
if (fxmlUrl == null) {
    logger.error("File FXML non trovato");
}
```

**Soluzioni:**
1. **Verificare struttura directory:**
```
src/main/resources/
├── fxml/
│   ├── LoginView.fxml
│   ├── MainView.fxml
│   └── ...
└── css/
    └── application.css
```

2. **Controllare syntax FXML:**
```xml
<!-- Verificare namespace corretto -->
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<!-- Verificare controller binding -->
<BorderPane xmlns="http://javafx.com/javafx/11.0.1" 
           xmlns:fx="http://javafx.com/fxml/1" 
           fx:controller="it.unina.uninafoodlab.controller.MainController">
```

## Problemi Performance

### Applicazione lenta all'avvio

**Problema:** Tempo di avvio elevato.

**Diagnosi:**
```bash
# Profiling JVM
java -XX:+FlightRecorder 
     -XX:StartFlightRecording=duration=60s,filename=startup.jfr 
     -jar uninafoodlab.jar

# Analizzare con JProfiler o VisualVM
```

**Soluzioni:**
1. **Ottimizzazione JVM:**
```bash
# Parametri JVM ottimizzati
-Xms512m -Xmx2g
-XX:+UseG1GC
-XX:+TieredCompilation
-XX:TieredStopAtLevel=1  # Per sviluppo
```

2. **Lazy loading:**
```java
// Caricare dati solo quando necessario
@FXML
private void initialize() {
    // Caricare dati essenziali all'avvio
    loadEssentialData();
    
    // Caricare dati secondari in background
    Platform.runLater(this::loadSecondaryData);
}
```

3. **Connection pool configuration:**
```properties
# Ottimizzare pool connessioni
db.hikari.maximumPoolSize=10
db.hikari.minimumIdle=2
db.hikari.connectionTimeout=20000
```

### Memoria insufficiente

**Problema:** OutOfMemoryError durante l'esecuzione.

**Diagnosi:**
```java
// Monitoring memoria
Runtime runtime = Runtime.getRuntime();
long totalMemory = runtime.totalMemory();
long freeMemory = runtime.freeMemory();
long usedMemory = totalMemory - freeMemory;

logger.info("Memory usage: {} MB / {} MB", 
    usedMemory / 1024 / 1024, 
    totalMemory / 1024 / 1024);
```

**Soluzioni:**
1. **Aumentare heap size:**
```bash
java -Xms1g -Xmx4g -jar uninafoodlab.jar
```

2. **Ottimizzare garbage collection:**
```bash
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
```

3. **Memory leak detection:**
```bash
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/tmp/heapdump.hprof
```

## Messaggi di Errore Comuni

### "ServiceException: Database connection failed"

**Causa:** Problemi di connessione al database.

**Soluzioni:**
1. Verificare che PostgreSQL sia in esecuzione
2. Controllare parametri di connessione
3. Verificare firewall e network
4. Controllare credenziali database

### "ValidationException: Nome corso obbligatorio"

**Causa:** Validazione dati fallita.

**Soluzioni:**
1. Verificare che tutti i campi obbligatori siano compilati
2. Controllare lunghezza massima dei campi
3. Verificare formato dati (email, date, etc.)

### "AuthenticationException: Credenziali non valide"

**Causa:** Username o password errati.

**Soluzioni:**
1. Verificare username e password
2. Controllare che l'utente esista nel database
3. Verificare che la password sia stata hashata correttamente

### "IllegalStateException: FXML file not found"

**Causa:** File FXML mancante o path errato.

**Soluzioni:**
1. Verificare che il file FXML esista in `src/main/resources/fxml/`
2. Controllare che il nome file sia corretto
3. Verificare che il file sia incluso nel JAR compilato

## Debugging e Logging

### Abilitare Debug Logging

**Configurazione logback.xml:**
```xml
<configuration>
    <!-- Console appender per debug -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Logger applicazione in modalità DEBUG -->
    <logger name="it.unina.uninafoodlab" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE" />
    </logger>
    
    <!-- Logger database queries -->
    <logger name="org.postgresql" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE" />
    </logger>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

### Debug Parametri JVM

```bash
# Debug generale
-Dlogback.statusListenerClass=ch.qos.logback.core.status.OnConsoleStatusListener

# Debug JavaFX
-Djavafx.verbose=true
-Dprism.verbose=true

# Debug SQL
-Dhibernate.show_sql=true
-Dhibernate.format_sql=true

# Debug connection pool
-Dcom.zaxxer.hikari.housekeeping.periodMs=30000
```

### Raccolta Informazioni Debug

**Script di diagnostic:**
```bash
#!/bin/bash
# debug-info.sh

echo "=== SISTEMA ==="
uname -a
java -version
mvn -version

echo -e "\n=== JAVA PROCESSES ==="
jps -v

echo -e "\n=== DATABASE ==="
psql -U foodlab_user -d uninafoodlab -c "SELECT version();"
psql -U foodlab_user -d uninafoodlab -c "SELECT count(*) FROM pg_stat_activity;"

echo -e "\n=== MEMORIA ==="
free -h

echo -e "\n=== DISCO ==="
df -h

echo -e "\n=== LOG RECENTI ==="
tail -50 logs/uninafoodlab.log
```

## Best Practices

### Sviluppo e Testing

1. **Sempre usare transazioni per operazioni multiple**
```java
@Transactional
public void operazioneComplessa() {
    // Operazioni multiple che devono essere atomiche
}
```

2. **Validare input sempre lato server**
```java
public ValidationResult validate(Object input) {
    // Non fidarsi mai della validazione client-side
}
```

3. **Logging appropriato per debugging**
```java
// Livelli di log appropriati
logger.debug("Dettagli tecnici"); // Solo in sviluppo
logger.info("Operazioni importanti"); // Sempre
logger.warn("Situazioni anomale"); // Sempre
logger.error("Errori gravi", exception); // Sempre con stack trace
```

### Deployment e Produzione

1. **Configurazioni separate per ambienti**
```properties
# dev.properties
db.host=localhost
logging.level=DEBUG

# prod.properties
db.host=prod-server
logging.level=WARN
```

2. **Monitoring proattivo**
```java
// Health checks regolari
@Scheduled(fixedRate = 60000)
public void healthCheck() {
    // Verificare stato sistema
}
```

3. **Backup automatizzati**
```bash
# Backup giornaliero
0 2 * * * /usr/local/bin/backup-uninafoodlab.sh
```

### Sicurezza

1. **Password sempre hashate**
```java
// Mai salvare password in chiaro
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
```

2. **Validazione e sanitizzazione input**
```java
// Prevenire SQL injection e XSS
public String sanitizeInput(String input) {
    return StringEscapeUtils.escapeHtml4(input.trim());
}
```

3. **Connessioni database sicure**
```properties
# Usare SSL in produzione
db.ssl=true
db.sslMode=require
```

### Performance

1. **Connection pooling appropriato**
```properties
# Configurazione ottimale per il carico previsto
db.hikari.maximumPoolSize=20
db.hikari.minimumIdle=5
```

2. **Indici database appropriati**
```sql
-- Indici per query frequenti
CREATE INDEX idx_corso_chef_created ON corso(chef_id, created_at);
```

3. **Caching quando appropriato**
```java
// Cache per dati letti frequentemente
@Cacheable("categorieCorso")
public List<CategoriaCorso> getAllCategorie() {
    // ...
}
```

---

*Questa FAQ copre i problemi più comuni e le loro soluzioni. Per problemi specifici non coperti, consultare i log dell'applicazione e utilizzare le tecniche di debugging descritte.*
