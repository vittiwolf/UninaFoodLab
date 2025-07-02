# 🚀 Installazione e Deployment UninaFoodLab

## 📋 Indice
- [Requisiti di Sistema](#requisiti-di-sistema)
- [Installazione Database](#installazione-database)
- [Setup Database Automatico](#setup-database-automatico) ⭐ **NUOVO**
- [Setup Ambiente Java](#setup-ambiente-java)
- [Configurazione Progetto](#configurazione-progetto)
- [Compilazione e Build](#compilazione-e-build)
- [Avvio Applicazione](#avvio-applicazione) ⭐ **AGGIORNATO**
- [Risoluzione Problemi](#risoluzione-problemi) ⭐ **NUOVO**
- [Deployment](#deployment)
- [Configurazioni di Produzione](#configurazioni-di-produzione)
- [Monitoraggio](#monitoraggio)

## 💻 Requisiti di Sistema

### Hardware Minimi

| Componente | Requisito Minimo | Raccomandato |
|------------|------------------|--------------|
| **CPU** | Dual-core 2.0 GHz | Quad-core 2.5 GHz |
| **RAM** | 4 GB | 8 GB |
| **Storage** | 2 GB liberi | 5 GB liberi |
| **Risoluzione** | 1024x768 | 1920x1080 |

### Software

| Software | Versione Minima | Note |
|----------|----------------|------|
| **Java JDK** | 17+ | ⭐ **AGGIORNATO**: OpenJDK o Oracle JDK |
| **PostgreSQL** | 12+ | Con estensioni UUID |
| **Maven** | 3.8+ | ⭐ **AGGIORNATO**: Per build e dependency management |
| **Git** | 2.20+ | Per versionamento codice |

### Sistemi Operativi Supportati

- **Windows**: Windows 10/11 (64-bit) ⭐ **TESTATO**
- **macOS**: macOS 10.14+ (Mojave)
- **Linux**: Ubuntu 18.04+, CentOS 7+, Debian 9+

## Installazione Database

### 1. Installazione PostgreSQL

#### Windows

```bash
# Download installer da postgresql.org
# Eseguire installer PostgreSQL 14.x
# Durante installazione, annotare password per utente postgres
```

#### macOS

```bash
# Usando Homebrew
brew install postgresql@14
brew services start postgresql@14

# Impostare password utente postgres
psql postgres
\password postgres
```

#### Ubuntu/Debian

```bash
# Aggiornare repository
sudo apt update

# Installare PostgreSQL
sudo apt install postgresql postgresql-contrib

# Configurare password postgres
sudo -u postgres psql
\password postgres
```

### 2. Creazione Database

```sql
-- Connessione come utente postgres
psql -U postgres -h localhost

-- Creazione database
CREATE DATABASE uninafoodlab
    WITH ENCODING = 'UTF8'
    LC_COLLATE = 'it_IT.UTF-8'
    LC_CTYPE = 'it_IT.UTF-8'
    TEMPLATE = template0;

-- Creazione utente applicazione
CREATE USER foodlab_user WITH ENCRYPTED PASSWORD 'SecurePassword123!';

-- Assegnazione privilegi
GRANT ALL PRIVILEGES ON DATABASE uninafoodlab TO foodlab_user;

-- Connessione al database
\c uninafoodlab

-- Abilitazione estensione UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Assegnazione privilegi su schema
GRANT ALL ON SCHEMA public TO foodlab_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO foodlab_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO foodlab_user;
```

### 3. Esecuzione Script Database

```bash
# Posizionarsi nella directory del progetto
cd UninaFoodLab

# Eseguire script creazione tabelle
psql -U foodlab_user -d uninafoodlab -f database/01_create_tables.sql

# Eseguire script dati iniziali
psql -U foodlab_user -d uninafoodlab -f database/02_initial_data.sql

# Eseguire script funzioni e procedure
psql -U foodlab_user -d uninafoodlab -f database/03_functions_procedures.sql

# Verificare installazione
psql -U foodlab_user -d uninafoodlab -c "\dt"
```

## Setup Ambiente Java

### 1. Installazione Java JDK

#### Windows

```bash
# Download OpenJDK 17 da adoptium.net
# Eseguire installer
# Verificare installazione
java -version
javac -version

# Impostare JAVA_HOME
# Pannello di Controllo > Sistema > Variabili Ambiente
# JAVA_HOME = C:\Program Files\Eclipse Adoptium\jdk-17.0.x.x-hotspot
```

#### macOS

```bash
# Usando Homebrew
brew install openjdk@17

# Aggiungere a PATH in ~/.zshrc o ~/.bash_profile
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# Ricaricare configurazione
source ~/.zshrc
```

#### Linux

```bash
# Ubuntu/Debian
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# Impostare JAVA_HOME in ~/.bashrc
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

source ~/.bashrc
```

### 2. Installazione Maven

#### Windows

```bash
# Download Maven da maven.apache.org
# Estrarre in C:\Program Files\Apache\maven
# Aggiungere alle variabili ambiente:
# MAVEN_HOME = C:\Program Files\Apache\maven\apache-maven-3.8.x
# PATH += %MAVEN_HOME%\bin
```

#### macOS

```bash
# Usando Homebrew
brew install maven

# Verificare installazione
mvn -version
```

#### Linux

```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# Verificare installazione
mvn -version
```

## Configurazione Progetto

### 1. Clone Repository

```bash
# Clone del progetto
git clone https://github.com/your-username/UninaFoodLab.git
cd UninaFoodLab

# Verificare struttura
ls -la
```

### 2. Configurazione Database

Creare file `src/main/resources/database.properties`:

```properties
# Database Configuration
db.host=localhost
db.port=5432
db.name=uninafoodlab
db.username=foodlab_user
db.password=SecurePassword123!

# Connection Pool Settings
db.pool.initialSize=5
db.pool.maxActive=20
db.pool.maxIdle=10
db.pool.minIdle=5
db.pool.maxWaitMillis=30000

# HikariCP Settings
db.hikari.maximumPoolSize=20
db.hikari.minimumIdle=5
db.hikari.connectionTimeout=30000
db.hikari.idleTimeout=300000
db.hikari.maxLifetime=1800000
```

### 3. Configurazione Logging

File `src/main/resources/logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/uninafoodlab.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/uninafoodlab.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <!-- Root Logger -->
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </root>
    
    <!-- Application Logger -->
    <logger name="it.unina.uninafoodlab" level="DEBUG" additivity="false">
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="FILE" />
    </logger>
</configuration>
```

### 4. Configurazione JavaFX

File `src/main/resources/application.properties`:

```properties
# Application Settings
app.name=UninaFoodLab
app.version=1.0.0
app.title=UninaFoodLab - Sistema Gestione Corsi di Cucina

# JavaFX Settings
javafx.stage.width=1200
javafx.stage.height=800
javafx.stage.resizable=true
javafx.stage.maximized=false

# UI Settings
ui.theme=default
ui.language=it_IT
ui.date.format=dd/MM/yyyy
ui.time.format=HH:mm

# Security Settings
security.password.minLength=8
security.session.timeoutMinutes=60
security.bcrypt.rounds=12
```

## Compilazione e Build

### 1. Verifica Dipendenze

```bash
# Verificare che Maven possa risolvere tutte le dipendenze
mvn dependency:resolve

# Analizzare albero dipendenze
mvn dependency:tree

# Verificare conflitti
mvn dependency:analyze
```

### 2. Compilazione

```bash
# Compilazione semplice
mvn compile

# Compilazione con test
mvn clean compile test

# Package completo
mvn clean package

# Installazione in repository locale
mvn clean install
```

### 3. Build con Profile

#### Profile Sviluppo

```bash
# Build per sviluppo (include debug)
mvn clean package -Pdev

# Esecuzione in modalità sviluppo
mvn javafx:run -Pdev
```

#### Profile Produzione

```bash
# Build ottimizzato per produzione
mvn clean package -Pprod

# Creazione distribuzione
mvn clean package -Pprod -Dgenerate.distribution=true
```

### 4. Creazione Eseguibile

```bash
# Usando Maven JavaFX Plugin
mvn javafx:jlink -Pprod

# Risultato in target/javafx-app/
ls target/javafx-app/

# Test eseguibile
./target/javafx-app/bin/UninaFoodLab
```

## Deployment

### 1. Deployment Standalone

#### Creazione Distribuzione

```bash
# Creazione package completo
mvn clean package -Pprod

# Creazione directory distribuzione
mkdir -p distribution/uninafoodlab-1.0.0
cp target/uninafoodlab-1.0.0.jar distribution/uninafoodlab-1.0.0/
cp -r src/main/resources/config distribution/uninafoodlab-1.0.0/
```

### Deployment Server

#### Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

# Metadata
LABEL maintainer="uninafoodlab@unina.it"
LABEL version="1.0.0"
LABEL description="UninaFoodLab - Sistema Gestione Corsi di Cucina"

# Installazione dipendenze di sistema
RUN apt-get update && apt-get install -y \
    libxrender1 \
    libxtst6 \
    libxi6 \
    libgl1-mesa-glx \
    libgtk-3-0 \
    && rm -rf /var/lib/apt/lists/*

# Creazione utente applicazione
RUN groupadd -r uninafoodlab && useradd -r -g uninafoodlab uninafoodlab

# Directory applicazione
WORKDIR /opt/uninafoodlab

# Copia files applicazione
COPY target/uninafoodlab-1.0.0.jar ./app.jar
COPY src/main/resources/config ./config
COPY scripts/docker-entrypoint.sh ./entrypoint.sh

# Permessi
RUN chown -R uninafoodlab:uninafoodlab /opt/uninafoodlab
RUN chmod +x entrypoint.sh

# Switch a utente applicazione
USER uninafoodlab

# Porta esposta (se necessaria per monitoring)
EXPOSE 8080

# Variabili ambiente
ENV JAVA_OPTS="-Xms512m -Xmx2g -XX:+UseG1GC"
ENV APP_OPTS="-Dconfig.dir=/opt/uninafoodlab/config"

# Entry point
ENTRYPOINT ["./entrypoint.sh"]
```

#### Docker Compose

File `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14
    container_name: uninafoodlab-db
    environment:
      POSTGRES_DB: uninafoodlab
      POSTGRES_USER: foodlab_user
      POSTGRES_PASSWORD: SecurePassword123!
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    networks:
      - uninafoodlab-network

  app:
    build: .
    container_name: uninafoodlab-app
    depends_on:
      - postgres
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: uninafoodlab
      DB_USERNAME: foodlab_user
      DB_PASSWORD: SecurePassword123!
    volumes:
      - app_logs:/opt/uninafoodlab/logs
    networks:
      - uninafoodlab-network
    restart: unless-stopped

volumes:
  postgres_data:
  app_logs:

networks:
  uninafoodlab-network:
    driver: bridge
```

## Configurazioni di Produzione

### 1. Ottimizzazioni JVM

```bash
# Parametri JVM ottimizzati per produzione
-Xms1g -Xmx4g
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+UseStringDeduplication
-XX:+OptimizeStringConcat
-server
-Djava.awt.headless=false
-Dfile.encoding=UTF-8
```

### 2. Configurazione Database Produzione

```properties
# Configurazione produzione PostgreSQL
db.host=${DB_HOST:localhost}
db.port=${DB_PORT:5432}
db.name=${DB_NAME:uninafoodlab}
db.username=${DB_USERNAME:foodlab_user}
db.password=${DB_PASSWORD}

# Connection Pool ottimizzato
db.hikari.maximumPoolSize=50
db.hikari.minimumIdle=10
db.hikari.connectionTimeout=20000
db.hikari.idleTimeout=300000
db.hikari.maxLifetime=1200000
db.hikari.leakDetectionThreshold=60000

# SSL Configuration
db.ssl=true
db.sslMode=require
```

### 3. Logging Produzione

```xml
<!-- Configurazione logging produzione -->
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${log.dir}/uninafoodlab.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${log.dir}/uninafoodlab.%d{yyyy-MM-dd}.%i.log.gz</fileNamePattern>
            <maxFileSize>50MB</maxFileSize>
            <maxHistory>60</maxHistory>
            <totalSizeCap>5GB</totalSizeCap>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="WARN">
        <appender-ref ref="FILE" />
    </root>
    
    <logger name="it.unina.uninafoodlab" level="INFO" additivity="false">
        <appender-ref ref="FILE" />
    </logger>
</configuration>
```

## Monitoraggio

### 1. Health Check

```java
@Component
public class HealthCheckService {
    
    @Scheduled(fixedRate = 60000) // Ogni minuto
    public void performHealthCheck() {
        HealthStatus status = new HealthStatus();
        
        // Check database
        status.setDatabaseStatus(checkDatabaseConnection());
        
        // Check memory
        status.setMemoryStatus(checkMemoryUsage());
        
        // Check disk space
        status.setDiskStatus(checkDiskSpace());
        
        logger.info("Health Check: {}", status);
        
        if (!status.isHealthy()) {
            // Invia alert
            alertService.sendHealthAlert(status);
        }
    }
}
```

### 2. Metriche Applicazione

```bash
# Script monitoraggio sistema
#!/bin/bash

LOG_FILE="/opt/uninafoodlab/logs/system-metrics.log"
TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')

# CPU Usage
CPU_USAGE=$(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)

# Memory Usage
MEMORY_USAGE=$(free | grep Mem | awk '{printf("%.2f", $3/$2 * 100.0)}')

# Disk Usage
DISK_USAGE=$(df -h /opt/uninafoodlab | awk 'NR==2 {print $5}' | cut -d'%' -f1)

# Database Connections
DB_CONNECTIONS=$(psql -U foodlab_user -d uninafoodlab -t -c "SELECT count(*) FROM pg_stat_activity WHERE datname='uninafoodlab';")

echo "$TIMESTAMP,CPU:$CPU_USAGE%,MEM:$MEMORY_USAGE%,DISK:$DISK_USAGE%,DB_CONN:$DB_CONNECTIONS" >> $LOG_FILE
```

### 3. Backup Automatico

```bash
#!/bin/bash
# Script backup database

BACKUP_DIR="/opt/backups/uninafoodlab"
TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
BACKUP_FILE="$BACKUP_DIR/uninafoodlab_backup_$TIMESTAMP.sql"

# Creazione directory backup
mkdir -p $BACKUP_DIR

# Backup database
pg_dump -U foodlab_user -h localhost -d uninafoodlab > $BACKUP_FILE

# Compressione
gzip $BACKUP_FILE

# Pulizia backup vecchi (mantenere 30 giorni)
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup completato: $BACKUP_FILE.gz"
```

---

*Questa guida fornisce tutte le informazioni necessarie per installare, configurare e deployare UninaFoodLab in diversi ambienti, dalla sviluppo alla produzione.*

## 🚀 Avvio Applicazione ⭐


### Metodo 1: Maven Exec

```cmd
# Compilazione e avvio
cd c:\Users\Vittorio\Desktop\UninaFoodLab
mvn clean compile exec:java

# Solo avvio (se già compilato)
mvn exec:java
```

### Metodo 3: JAR Eseguibile

```cmd
# Creazione JAR
mvn clean package

# Avvio JAR
java -jar target/uninafoodlab-1.0-SNAPSHOT.jar
```

## 🔧 Risoluzione Problemi ⭐

### Problemi Database

#### Errore: "colonna 'livello_esperienza' non esiste"
```sql
-- Soluzione: Eseguire script di fix
-- In pgAdmin, eseguire: fix_database_complete.sql
```

#### Errore: "tabella 'iscrizioni' non esiste"
```sql
-- Soluzione: Eseguire script di fix
-- Il script crea automaticamente la tabella
```

#### Errore: "connessione database fallita"
```bash
# Verificare che PostgreSQL sia in esecuzione
# Windows: Servizi > PostgreSQL
# Controllare parametri in database.properties
```

### Problemi Java/JavaFX

#### Errore: "module javafx.controls not found"
```bash
# Soluzione: Verificare Java 17+ installato
java -version

# Verificare librerie JavaFX in cartella lib/
dir lib\javafx-*.jar
```

#### Errore: "OutOfMemoryError"
```bash
# Aumentare memoria heap
set MAVEN_OPTS=-Xmx2048m
mvn exec:java
```

### Problemi Compilazione

#### Errore: "package does not exist"
```bash
# Soluzione: Pulire e ricompilare
mvn clean compile

# Se persiste, verificare dipendenze
mvn dependency:tree
```

#### Errore: "Class not found"
```bash
# Verificare classpath
mvn dependency:build-classpath

# Ricompilare completamente
mvn clean install
```

### Problemi di Avvio

#### L'applicazione non si avvia
```bash
# 1. Verificare log
type logs\uninafoodlab.log

# 2. Verificare database connessione
# 3. Controllare porte in uso (5432 per PostgreSQL)
netstat -an | find "5432"
```

#### Finestra non viene visualizzata
```bash
# Problema JavaFX - verificare:
# 1. Java 17+ installato
# 2. Variabili ambiente corrette
# 3. Scheda grafica supportata
```

### Log e Debug

#### Abilitare Debug Logging
```bash
# Modificare logback.xml
<logger name="it.unina.uninafoodlab" level="DEBUG">
```

#### Verificare Log Applicazione
```bash
# Windows
type logs\uninafoodlab.log | find "ERROR"

# Controllare log recenti
tail logs\uninafoodlab.log
```

## ✅ Verifica Installazione Completa

### Test Funzionalità Base

1. **Login Chef**: Verifica autenticazione con credenziali di test
2. **Gestione Corsi**: Crea, modifica ed elimina un corso
3. **Gestione Sessioni**: Aggiungi sessioni teoriche e pratiche
4. **Gestione Ricette**: Crea ricette e associale alle sessioni
5. **✅ NUOVO: Gestione Utenti**: Crea e gestisci utenti/partecipanti
6. **✅ NUOVO: Gestione Iscrizioni**: Iscriva utenti ai corsi e gestisci stati
7. **Report**: Genera report mensili con grafici

### Checklist Post-Installazione

- [ ] Database PostgreSQL operativo
- [ ] Tutte le 10 tabelle create correttamente
- [ ] Dati di test inseriti
- [ ] Applicazione avviabile senza errori
- [ ] Login funzionante
- [ ] Tutte le 5 tab principali accessibili
- [ ] CRUD operations su tutte le entità
- [ ] Generazione report senza errori
- [ ] Log applicazione generati in `logs/`

### Test Avanzati

```bash
# Test connessione database
mvn test -Dtest=DatabaseConnectionTest

# Test integrazione completa
mvn test -Dtest=IntegrationTest

# Test interfaccia utente (se configurato TestFX)
mvn test -Dtest=UITest
```

## 🚀 Quick Start per Sviluppatori

Per un setup rapido dell'ambiente di sviluppo:

```bash
# 1. Clone repository
git clone <repository-url>
cd UninaFoodLab

# 2. Configura properties
cp src/main/resources/database.properties.template src/main/resources/database.properties
# Modifica le credenziali database

# 3. Build e avvio
mvn clean compile
mvn javafx:run
```

## 📦 Packaging per Distribuzione

### Creazione JAR Eseguibile

```bash
# Build JAR con dipendenze
mvn clean package

# Il JAR sarà disponibile in:
# target/uninafoodlab-1.0-SNAPSHOT.jar

# Avvio JAR
java -jar target/uninafoodlab-1.0-SNAPSHOT.jar
```

### Creazione Installer (Windows)

```bash
# Usando jpackage (Java 17+)
jpackage --input target/ \
         --name "UninaFoodLab" \
         --main-jar uninafoodlab-1.0-SNAPSHOT.jar \
         --main-class it.unina.uninafoodlab.App \
         --type msi \
         --app-version 1.0 \
         --vendor "Università di Napoli" \
         --description "Sistema di gestione corsi di cucina"
```
