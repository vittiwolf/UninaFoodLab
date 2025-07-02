# UninaFoodLab - Documentazione del Progetto

## 📋 Indice della Documentazione

Benvenuto nella documentazione completa del progetto **UninaFoodLab** - Sistema per la gestione di corsi di cucina tematici.

### 📁 Struttura della Documentazione

| Documento | Descrizione |
|-----------|-------------|
| ⭐ [**Installation Guide**](./installation.md) | **INIZIA QUI** - Setup completo e avvio rapido |
| ⭐ [**Setup Complete**](./setup-complete.md) | Riepilogo implementazione e modifiche |
| [**Panoramica del Progetto**](./01-panoramica.md) | Introduzione generale, obiettivi e funzionalità principali |
| [**Architettura del Sistema**](./02-architettura.md) | Struttura tecnica, design patterns e tecnologie utilizzate |
| [**Database Design**](./03-database.md) | Schema del database, relazioni e documentazione SQL |
| [**API e Servizi**](./04-api-servizi.md) | Documentazione dei servizi business e DAO |
| [**Interfaccia Utente**](./05-interfaccia.md) | Controller JavaFX, FXML e interazioni utente |
| [**Guida all'Installazione**](./06-installazione.md) | Setup, configurazione e deployment (versione tecnica) |
| [**Testing e Debugging**](./07-testing.md) | Strategie di test e debugging |
| [**FAQ e Troubleshooting**](./08-faq.md) | Domande frequenti e risoluzione problemi |

## 🎯 Panoramica Rapida

**UninaFoodLab** è un sistema per la gestione di corsi di cucina tematici che permette agli chef di:

- ✅ **Autenticarsi** nel sistema con credenziali personalizzate
- ✅ **Creare e gestire corsi** di cucina per diverse categorie
- ✅ **Organizzare sessioni** teoriche e pratiche
- ✅ **Associare ricette** alle sessioni pratiche
- ✅ **Gestire utenti/partecipanti** con profili completi
- ✅ **Gestire iscrizioni** ai corsi con controllo stati
- ✅ **Generare report mensili** con grafici e statistiche
- ✅ **Monitorare** l'andamento dei propri corsi in tempo reale

## 🚀 Quick Start

```bash
# 1. Clona il progetto
git clone <repository-url>

# 2. Configura il database PostgreSQL
psql -U postgres -f database_init.sql

# 3. Compila il progetto
mvn clean compile

# 4. Avvia l'applicazione
mvn javafx:run
```

## 🏗️ Tecnologie Utilizzate

- **Java 17+** - Linguaggio di programmazione principale
- **JavaFX 19** - Framework per l'interfaccia grafica
- **PostgreSQL** - Database relazionale
- **HikariCP** - Connection pooling
- **JFreeChart** - Generazione grafici per report
- **Maven** - Build automation e dependency management
- **SLF4J + Logback** - Logging system

## 📊 Statistiche del Progetto

```
📁 Struttura del Codice:
├── 🎯 3 Controller (Login, Main, Grafici)
├── 🗃️ 8 DAO (Chef, Corso, Sessione, Ricetta, Report, Utente, Iscrizione, Database)
├── 📋 8 Modelli di Dominio (Chef, Corso, Sessione, Ricetta, Utente, Iscrizione, Categoria, Report)
├── ⚙️ 1 Service Layer Unificato (Business Logic)
├── 🔧 5 Helper Classes (Dialog, Table, Message, Form, Validation)
├── 🎨 3 File FXML (Interfacce)
└── 🗄️ Schema Database (10 tabelle)

📈 Funzionalità Implementate:
✅ Autenticazione Chef
✅ Gestione Corsi Completa
✅ Gestione Sessioni (Teoriche/Pratiche)
✅ Gestione Ricette con Associazioni
✅ Gestione Utenti/Partecipanti Completa
✅ Sistema Iscrizioni con Stati
✅ Report Mensili con Grafici
✅ Validazione Input Avanzata
✅ Error Handling Robusto
✅ Architettura Helper Pattern
✅ Dialog System Modulare
```

## 🎓 Contesto Accademico

Questo progetto è stato sviluppato per il corso di **Basi di Dati e Programmazione a Oggetti** presso l'**Università di Napoli Federico II**.

### Traccia del Progetto
Il sistema implementa la gestione di corsi di cucina tematici seguendo i requisiti specificati nella traccia ufficiale, con particolare attenzione a:

- Modellazione corretta del dominio
- Implementazione di pattern di design appropriati
- Gestione robusta dei dati con PostgreSQL
- Interfaccia utente intuitiva con JavaFX
- Generazione di report con visualizzazioni grafiche

## 📊 Stato della Documentazione

| Documento | Stato | Completamento | Descrizione |
|-----------|-------|---------------|-------------|
| **01-panoramica.md** | ✅ Completato | 100% | Obiettivi, funzionalità e architettura generale |
| **02-architettura.md** | ✅ Completato | 100% | Design patterns, struttura tecnica e principi |
| **03-database.md** | ✅ Completato | 100% | Schema PostgreSQL, ER diagram, query e ottimizzazioni |
| **04-api-servizi.md** | ✅ Completato | 100% | Service layer, DAO pattern e business logic |
| **05-interfaccia.md** | ✅ Completato | 100% | Controller JavaFX, FXML e interazioni utente |
| **06-installazione.md** | ✅ Completato | 100% | Setup, configurazione e deployment |
| **07-testing.md** | ✅ Completato | 100% | Strategie di test e debugging |
| **08-faq.md** | ✅ Completato | 100% | Domande frequenti e troubleshooting |

**🎉 Progresso Totale: 100% (9/9 documenti completati)**

---