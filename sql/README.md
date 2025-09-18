# UninaFoodLab - Script SQL

Questa cartella contiene gli script SQL aggiornati e sincronizzati con l'ultimo dump del database (settembre 2025).

## File disponibili

### 1. setup_with_predata.sql
**Setup completo (struttura + funzioni + viste + trigger + dati di esempio)**
- Crea struttura tabelle
- Definisce funzioni, trigger, viste
- Popola con dati di esempio coerenti
- Non contiene istruzioni CREATE DATABASE (eseguirlo su un DB già creato)
- **Uso**: inizializzazione rapida ambiente di sviluppo / reset completo

### 2. structure_with_views_triggers.sql  
**Struttura completa + funzioni + viste + trigger (nessun dato)**
- Struttura tabelle con vincoli / indici
- Tutte le funzioni e trigger
- Tutte le viste di reporting
- Nessun inserimento dati
- **Uso**: ambienti di test/stage/prod dove i dati vengono caricati da altre pipeline

### 3. structure_only.sql
**Struttura pura (tabelle + vincoli + indici)**
- Solo DDL tabelle
- Nessuna funzione, trigger o vista
- Nessun dato
- **Uso**: scenari minimali o migrazioni progressive

### 4. predata_only.sql
**Solo dati di test**
- Contiene unicamente i dati di esempio
- Da eseguire DOPO aver creato la struttura
- Include: chef, utenti, corsi, sessioni, ricette, iscrizioni
- **Uso**: Per popolare un database vuoto con dati di test

### 5. dump.sql
**Dump completo generato automaticamente**
- Backup completo del database corrente
- Generato automaticamente dal sistema
- Include struttura e dati aggiornati
- **Uso**: Come backup o per analisi avanzate

## Ordine di esecuzione consigliato

### A. Ambiente di sviluppo (rapido)
```sql
\i setup_with_predata.sql
```

### B. Ambiente di test / staging
```sql
\i structure_with_views_triggers.sql
-- eventuali script di migrazione / caricamento dati separati
```

### C. Ambiente minimale / personalizzato
```sql
\i structure_only.sql
-- caricare solo gli oggetti avanzati necessari oppure usare file dedicati
\i predata_only.sql  -- opzionale (solo se servono dati esempio)
```

## Note tecniche

- **Versione PostgreSQL**: 17.5
- **Encoding**: UTF8  
- **Locale**: Italian_Italy.1252
- **User predefinito**: postgres

## Funzionalità principali incluse

- Funzioni: calcolo età, prezzo scontato, statistiche corso, generazione codice, validazione email, verifica posti disponibili
- Trigger: validazione email, generazione codice iscrizione, controllo posti, audit log, aggiornamento timestamp
- View: dashboard_admin, corsi_dettaglio, iscrizioni_complete, analisi_iscrizioni_mensili, notifiche_sistema, report_chef
- Vincoli: PK, FK con ON DELETE appropriati, unique business, check logici
- Indici: automatici (PK/FK) + supporto query di report
- Dati di esempio coerenti e relazionalmente consistenti

## Troubleshooting

1. Verificare versione PostgreSQL (>= 17.0)
2. Confermare che il DB sia stato creato manualmente (gli script non lo creano)
3. Se falliscono funzioni/trigger: assicurarsi di NON aver usato solo `structure_only.sql`
4. Se mancano dati test: eseguire `predata_only.sql` dopo la struttura completa
5. Per analisi differenze: confrontare con `dump.sql` generato

## Aggiornamenti

Ultima modifica: 18 settembre 2025 - Ore 12:00
- Allineamento completo a nuovo dump
- Rimosso script inesistente `verify_data.sql`
- Separazione chiara tra livelli di struttura
- Aggiornata sezione funzionalità e ordine esecuzione
