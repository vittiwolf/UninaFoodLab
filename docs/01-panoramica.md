# 📋 Panoramica del Progetto UninaFoodLab

## 🎯 Obiettivi del Sistema

**UninaFoodLab** è un sistema software progettato per gestire corsi di cucina tematici, offrendo agli chef uno strumento completo per organizzare, monitorare e analizzare le proprie attività didattiche culinarie.

### Obiettivi Principali

1. **Gestione Centralizzata**: Centralizzare la gestione di tutti i corsi di cucina in un'unica piattaforma
2. **Gestione Utenti Completa**: Sistema integrato per la gestione di partecipanti e iscrizioni
3. **Monitoraggio Efficace**: Fornire strumenti di monitoraggio e reportistica avanzata
4. **Organizzazione Ottimale**: Facilitare l'organizzazione di sessioni teoriche e pratiche
5. **Tracciabilità Completa**: Mantenere traccia di ricette, partecipanti e progressi
6. **Analisi dei Dati**: Generare insight attraverso report e visualizzazioni grafiche
7. **Architettura Pulita**: Codice modulare e manutenibile senza duplicazioni

## 🎭 Attori del Sistema

### 👨‍🍳 Chef/Istruttore
**Ruolo primario** del sistema con le seguenti responsabilità:

- Creazione e gestione dei propri corsi di cucina
- **Gestione Utenti**: Creazione, modifica e gestione partecipanti
- **Gestione Iscrizioni**: Controllo iscrizioni, annullamenti e stati
- Organizzazione di sessioni teoriche e pratiche
- Associazione di ricette alle sessioni pratiche
- Monitoraggio dell'andamento dei corsi
- Generazione e analisi di report mensili

### 👥 Utenti/Partecipanti
**Entità gestite** dal sistema:

- Profili completi con livello di esperienza
- Storico iscrizioni e partecipazioni
- Dati anagrafici e contatti
- Stati di attivazione/disattivazione

### 🖥️ Sistema Amministrativo
**Ruolo di supporto** che gestisce:

- Autenticazione e autorizzazione degli chef
- Mantenimento dei dati di sistema
- Backup e sicurezza dei dati
- Integrità referenziale del database
- Configurazione delle categorie di corsi

## 🔧 Funzionalità Principali

### 1. 🔐 Autenticazione e Sicurezza

```java
// Esempio di autenticazione
Optional<Chef> chefAutenticato = service.autenticaChef(username, password);
if (chefAutenticato.isPresent()) {
    // Accesso consentito al sistema
    mainController.setChefLoggato(chefAutenticato.get());
}
```

**Caratteristiche:**
- Login sicuro con username e password
- Sessioni utente personalizzate
- Controllo accessi basato sui ruoli

### 2. 📚 Gestione Corsi

**Creazione Corsi:**
- Definizione di titolo, descrizione e categoria
- Impostazione di durata, prezzo e numero partecipanti
- Pianificazione con data inizio e frequenza
- Generazione automatica delle sessioni

**Monitoraggio:**
- Visualizzazione di tutti i corsi dello chef
- Filtri per categoria e stato
- Statistiche di partecipazione

```java
// Esempio di creazione corso
Corso nuovoCorso = new Corso();
nuovoCorso.setTitolo("Cucina Italiana Tradizionale");
nuovoCorso.setCategoria("Cucina Italiana");
nuovoCorso.setDurata(20); // ore
nuovoCorso.setMaxPartecipanti(12);
nuovoCorso.setPrezzo(new BigDecimal("150.00"));

boolean successo = service.creaCorso(nuovoCorso);
```

### 3. 🗓️ Gestione Sessioni

**Tipologie di Sessioni:**

| Tipo | Descrizione | Modalità |
|------|-------------|----------|
| **TEORICA** | Lezioni frontali, spiegazioni teoriche | Online/Presenza |
| **PRATICA** | Laboratori pratici con ricette | Solo Presenza |

**Funzionalità:**
- Pianificazione automatica basata sulla frequenza del corso
- Associazione di ricette alle sessioni pratiche
- Tracciamento del completamento
- Gestione delle modalità (online/presenza)

### 4. 🍝 Gestione Ricette

**Caratteristiche delle Ricette:**
- Nome e descrizione dettagliata
- Categoria culinaria
- Livello di difficoltà (FACILE, MEDIO, DIFFICILE)
- Tempo di preparazione stimato
- Ingredienti e procedimento

**Associazione alle Sessioni:**
```java
// Esempio di associazione ricetta a sessione
boolean successo = service.associaRicettaASessione(
    sessione_id, 
    ricetta_id, 
    ordineEsecuzione
);
```

### 5. � Gestione Utenti/Partecipanti

**Profili Utente Completi:**
- Dati anagrafici (nome, cognome, email, telefono)
- Data di nascita e età calcolata automaticamente
- Livello di esperienza (PRINCIPIANTE, INTERMEDIO, AVANZATO)
- Stato attivazione/disattivazione
- Timestamp di creazione

**Funzionalità CRUD:**
```java
// Esempio di gestione utenti
Utente nuovoUtente = new Utente(nome, cognome, email, telefono, dataNascita, livelloEsperienza);
boolean creato = service.creaUtente(nuovoUtente);
boolean aggiornato = service.aggiornaUtente(utente);
boolean disattivato = service.disattivaUtente(utente.getId());
```

### 6. 📝 Sistema Iscrizioni

**Gestione Completa Iscrizioni:**
- Iscrizione utenti ai corsi disponibili
- Tracciamento stati (ATTIVA, COMPLETATA, ANNULLATA)
- Note e motivazioni per annullamenti
- Controllo duplicati e vincoli
- Conteggio partecipanti per corso

**Operazioni Supportate:**
```java
// Esempio di gestione iscrizioni
boolean iscritto = service.iscriviUtenteACorso(utenteId, corsoId, note);
boolean annullata = service.annullaIscrizione(iscrizioneId, motivo);
int numeroIscritti = service.getNumeroIscrittiCorso(corsoId);
```

### 7. �📊 Report e Analytics

**Report Mensili Include:**
- Numero totale di corsi attivi
- Distribuzione sessioni per tipologia
- Statistiche di partecipazione
- Media ricette per sessione
- Categoria più popolare

**Visualizzazioni Grafiche:**
- **Torta**: Distribuzione corsi per categoria
- **Barre**: Sessioni per modalità (online vs presenza)
- **Linee**: Andamento mensile di corsi e sessioni
- **Torta**: Distribuzione ricette per difficoltà

## 🏗️ Architettura del Dominio

### Relazioni Chiave

1. **Chef → Corsi**: Un chef può creare e gestire multipli corsi
2. **Corso → Sessioni**: Ogni corso contiene multiple sessioni pianificate
3. **Sessioni ↔ Ricette**: Le sessioni pratiche possono utilizzare multiple ricette
4. **Corso ↔ Utenti**: Gli utenti possono iscriversi a più corsi tramite iscrizioni
5. **Utenti ↔ Iscrizioni**: Sistema completo di gestione iscrizioni con stati
6. **Categorie**: Sistema di categorizzazione per corsi e ricette
7. **Chef ↔ Utenti**: Gli chef gestiscono i propri utenti e le relative iscrizioni

## 📈 Flussi di Lavoro Principali

### Workflow Creazione Corso

1. **Login Chef** → Autenticazione nel sistema
2. **Nuovo Corso** → Compilazione form con dettagli corso
3. **Validazione** → Controllo dati inseriti
4. **Salvataggio** → Persistenza nel database
5. **Generazione Sessioni** → Creazione automatica calendario sessioni
6. **Conferma** → Notifica successo all'utente

### Workflow Gestione Sessione Pratica

1. **Selezione Corso** → Choice del corso attivo
2. **Visualizzazione Sessioni** → Lista sessioni programmate
3. **Selezione Sessione Pratica** → Focus su sessione di laboratorio
4. **Associazione Ricette** → Scelta ricette da utilizzare
5. **Salvataggio Associazioni** → Persistenza collegamenti
6. **Preparazione Sessione** → Sistema pronto per l'esecuzione

## 🎨 Design Principles

### 1. **Separation of Concerns**
- **Presentation Layer**: Controller JavaFX
- **Business Layer**: Service classes
- **Data Layer**: DAO pattern
- **Model Layer**: Domain entities

### 2. **Single Responsibility**
Ogni classe ha una responsabilità specifica:
- `ChefDAO`: Solo gestione dati chef
- `CorsoDAO`: Solo gestione dati corsi
- `UninaFoodLabService`: Solo logica di business

### 3. **Dependency Injection**
```java
public class UninaFoodLabService {
    private final ChefDAO chefDAO;
    private final CorsoDAO corsoDAO;
    // Dipendenze iniettate nel costruttore
}
```

## 🎯 Benefici del Sistema

### Per gli Chef
- **Organizzazione Semplificata**: Interface intuitiva per gestire tutti i corsi
- **Automatizzazione**: Generazione automatica di sessioni e planning
- **Insights**: Report dettagliati sull'andamento dei corsi
- **Tracciabilità**: Storia completa di ogni corso e sessione

### Per l'Istituzione
- **Controllo Qualità**: Monitoraggio delle attività didattiche
- **Analytics**: Dati aggregati per decision making
- **Standardizzazione**: Processo uniforme per tutti gli chef
- **Scalabilità**: Sistema progettato per crescere con l'organizzazione

## 📊 Metriche di Successo

Il sistema traccia le seguenti metriche chiave:

| Metrica | Descrizione | Valore Target |
|---------|-------------|---------------|
| **Corsi Attivi** | Numero corsi in corso | > 5 per chef |
| **Tasso Completamento** | % sessioni completate | > 85% |
| **Utilizzo Ricette** | Media ricette per sessione | > 2 ricette |
| **Feedback Qualità** | Soddisfazione utenti | > 4/5 stelle |

---

**Prossimo:** [Architettura del Sistema](./02-architettura.md)
