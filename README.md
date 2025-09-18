# UninaFoodLab

🍽️ **Sistema di Gestione per Laboratorio di Cucina Universitario**

## 🎯 Descrizione
UninaFoodLab è un'applicazione desktop JavaFX per la gestione completa di corsi di cucina tematici, che include gestione utenti, corsi, iscrizioni, ricette e reportistica.

## ✅ Stato del Progetto
**✨ COMPLETAMENTE FUNZIONANTE E PRONTO PER LA PRODUZIONE**

- ✅ **Architettura unificata** - Eliminata duplicazione servizi
- ✅ **Database completo** - PostgreSQL con tutte le tabelle necessarie
- ✅ **Interfaccia completa** - JavaFX con gestione utenti e iscrizioni
- ✅ **Script automatici** - Setup database e avvio applicazione
- ✅ **Documentazione completa** - Guide dettagliate e troubleshooting

## 🚀 Avvio Rapido

### Prerequisiti
- **Java 17+** - `java -version`
- **Maven 3.6+** - `mvn -version` 
- **PostgreSQL 12+** - Database attivo

### Setup e Avvio
```bash
# 1. Setup database (una tantum)
psql -U postgres -d uninafoodlab
\i sql/setup_with_predata.sql

# 2. Avvio applicazione
mvn clean javafx:run
```

## 📁 Struttura Progetto

```
UninaFoodLab/
├── README.md                    # Questo file
├── pom.xml                      # Configurazione Maven
├── src/                        # Codice sorgente
│   ├── main/java/              # Applicazione principale
│   └── main/resources/         # Risorse (FXML, CSS, config)
├── sql/                        # Script database
│   ├── setup_with_predata.sql  # Setup completo database
│   ├── structure_only.sql      # Solo struttura
│   ├── predata_only.sql        # Dati di test
│   └── structure_with_views_triggers.sql # Struttura + triggers e views
├── docs/                       # Documentazione tecnica
│   ├── README.md              # Indice documentazione
│   ├── installation.md       # Guida installazione
│   ├── setup-complete.md     # Riepilogo implementazione
│   └── ...                   # Altri documenti tecnici
└── logs/                      # File di log applicazione
```

## 🎓 Funzionalità Principali

### 👥 Gestione Utenti
- Creazione, modifica, disattivazione utenti
- Ricerca e filtri avanzati
- Gestione livelli esperienza

### 📚 Gestione Corsi
- Catalogo corsi con descrizioni
- Gestione prezzi e durata
- Stati corso (attivo/inattivo)

### 📝 Gestione Iscrizioni  
- Iscrizione utenti ai corsi
- Stati: ATTIVA, COMPLETATA, ANNULLATA
- Prevenzione iscrizioni duplicate

### 🍳 Gestione Ricette
- Database ricette con ingredienti
- Categorizzazione e ricerca
- Associazione ai corsi

### 📊 Reportistica
- Grafici statistiche corsi
- Report mensili iscrizioni
- Analisi performance

## 🛠️ Script Disponibili

### Database
- `sql/setup_with_predata.sql` - **PRINCIPALE** - Setup completo
- `sql/structure_only.sql` - Solo correzioni struttura
- `sql/predata_only.sql` - Inserimento dati di test
- `sql/structure_with_views_triggers.sql` - Struttura + triggers e views

## 📚 Documentazione

La documentazione completa è disponibile nella cartella `docs/`:
- **Quick Start**: `docs/installation.md` ⭐ **INIZIA QUI**
- **Setup Completo**: `docs/setup-complete.md`
- **Architettura**: `docs/02-architettura.md`
- **Database**: `docs/03-database.md`
- **Script SQL**: `sql/README.md`

## 🔧 Troubleshooting

### Problemi Comuni
1. **Errore connessione database**: Verificare che PostgreSQL sia avviato
2. **Errore compilazione**: Controllare versione Java (richiesta 17+)
3. **Interfaccia non carica**: Verificare dipendenze JavaFX

### Log e Debug
- File log: `logs/uninafoodlab.log`
- Console applicazione per errori runtime
- Maven: `mvn clean compile` per verificare compilazione

## 🏗️ Tecnologie Utilizzate

- **Frontend**: JavaFX 19+ con FXML
- **Backend**: Java 17+ con Maven
- **Database**: PostgreSQL 12+
- **ORM**: JDBC nativo con DAO Pattern
- **Logging**: SLF4J + Logback
- **Testing**: JUnit 5
- **Charts**: JFreeChart

## 👨‍💻 Sviluppo

### Compilazione
```bash
mvn clean compile          # Compilazione
mvn test                   # Esecuzione test
mvn javafx:run            # Avvio applicazione
```

### Struttura Codice
- `controller/` - Controller JavaFX
- `model/` - Modelli dati  
- `database/` - DAO e connessioni
- `service/` - Logica business
- `utils/` - Utility e helper

## 📄 Licenza

Progetto universitario - Università di Napoli Federico II

---

**🚀 Ready for Production!**

Per maggiori dettagli consultare la documentazione in `docs/`
