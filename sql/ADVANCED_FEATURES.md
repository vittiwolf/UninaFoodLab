# 🚀 Funzionalità Avanzate Database - UninaFoodLab

## 📋 Panoramica

Questo documento descrive le **funzionalità avanzate** attualmente implementate nel database PostgreSQL di UninaFoodLab (allineato all'ultimo dump), incluse **funzioni SQL**, **trigger** e **view di reporting / supporto applicativo**. Tutti gli oggetti elencati esistono realmente negli script aggiornati (`structure_with_views_triggers.sql` e `setup_with_predata.sql`).

## 🔧 Installazione / Ricostruzione

Gli script sono già consolidati; per un ambiente da zero usare (ordine consigliato):

```bash
# Creazione schema + funzioni + view + trigger + (eventuali) dati di esempio
psql -U postgres -d uninafoodlab -f sql/setup_with_predata.sql

# Solo struttura + viste + trigger (senza dati di esempio)
psql -U postgres -d uninafoodlab -f sql/structure_with_views_triggers.sql

# Solo struttura pura (senza view/trigger/funzioni)
psql -U postgres -d uninafoodlab -f sql/structure_only.sql

# Solo dati di esempio (richiede struttura + funzioni già presenti)
psql -U postgres -d uninafoodlab -f sql/predata_only.sql
```

### Verifica Oggetti Creati

```sql
-- Verifica funzioni
SELECT proname FROM pg_proc 
WHERE proname IN (
    'calcola_eta','calcola_prezzo_scontato','calcola_statistiche_corso',
    'genera_codice_iscrizione','valida_email','verifica_posti_disponibili'
) ORDER BY 1;

-- Verifica view principali
SELECT viewname FROM pg_views 
WHERE schemaname = 'public'
    AND viewname IN ('corsi_dettaglio','dashboard_admin','iscrizioni_complete','analisi_iscrizioni_mensili','notifiche_sistema','report_chef')
ORDER BY 1;

-- Verifica trigger utente (non interni)
SELECT tgname, relname AS tabella
FROM pg_trigger JOIN pg_class ON pg_trigger.tgrelid = pg_class.oid
WHERE NOT tgisinternal
ORDER BY relname, tgname;
```

## 📊 Funzioni SQL

### Funzioni di Calcolo / Statistiche

```sql
-- Calcolo età utente
SELECT calcola_eta('1990-05-15'::DATE); -- Età in anni (intero)

-- Verifica posti disponibili
SELECT verifica_posti_disponibili(1); -- Posti ancora prenotabili (intero)

-- Calcolo prezzo scontato (20% di sconto)
SELECT calcola_prezzo_scontato(1, 20); -- Prezzo dopo sconto (numeric)

-- Statistiche complete corso
SELECT * FROM calcola_statistiche_corso(1);
```

### Funzioni di Validazione / Supporto

```sql
-- Validazione email
SELECT valida_email('utente@example.com'); -- true
SELECT valida_email('email-invalida');    -- false

-- Generazione codice iscrizione univoco
SELECT genera_codice_iscrizione();        -- UFL2025XXXX (pattern)
```

## ⚡ Trigger

I trigger implementano regole di business e data quality:

- Validazione email (tabella `utenti`) tramite `trigger_valida_email`
- Generazione codice univoco iscrizione (`iscrizioni`) tramite `trigger_genera_codice_iscrizione`
- Controllo posti disponibili (`iscrizioni`) tramite `trigger_controlla_posti_disponibili`
- Audit log modifiche iscrizioni (`iscrizioni`) tramite `trigger_log_iscrizioni`
- Aggiornamento automatico campi timestamp (es. `modified_at`) tramite `update_modified_at`

Ogni trigger richiama una funzione dedicata (prefisso `trigger_` oppure funzione di utilità) ed è definito come NON DEFERRABLE per garantire coerenza immediata.

### Esempio Inserimento con Trigger Multipli

```sql
-- Inserimento iscrizione (trigger automatici)
INSERT INTO iscrizioni (utente_id, corso_id, stato) VALUES (1, 1, 'ATTIVA');
-- ✅ Codice generato automaticamente
-- ✅ Posti controllati automaticamente  
-- ✅ Log creato automaticamente

-- Aggiornamento stato
UPDATE iscrizioni SET stato = 'COMPLETATA' WHERE id = 1;
-- ✅ modified_at aggiornato automaticamente
-- ✅ Log modifica creato automaticamente
```

## 📈 View di Reporting

Le principali viste forniscono aggregazioni e arricchimento dati per la UI.

### `dashboard_admin`

```sql
-- Metriche principali
SELECT * FROM dashboard_admin;
/*
metrica          | valore | tipo
Utenti Totali    | 50     | success
Corsi Attivi     | 12     | primary  
Iscrizioni Attive| 145    | info
Ricavo Mensile   | € 3500 | warning
*/
```

### `corsi_dettaglio`

```sql
-- Vista completa corsi con statistiche
SELECT titolo, chef_nome, iscritti_attivi, posti_disponibili, ricavo_corso
FROM corsi_dettaglio
WHERE stato_corso = 'In Corso';
```

### `report_chef`

```sql
-- Report performance chef
SELECT chef_nome, corsi_totali, tasso_completamento_percentuale, ricavo_totale
FROM report_chef
ORDER BY ricavo_totale DESC;
```

### `analisi_iscrizioni_mensili`

```sql
-- Trend mensile iscrizioni
SELECT periodo, totale_iscrizioni, ricavo_mensile
FROM analisi_iscrizioni_mensili
WHERE anno = 2025
ORDER BY mese DESC;
```

### `notifiche_sistema`

```sql
-- Notifiche automatiche
SELECT messaggio, priorita
FROM notifiche_sistema
WHERE priorita = 'warning';
```

### `iscrizioni_complete`

```sql
SELECT * FROM iscrizioni_complete WHERE corso_id = 1;
```

Fornisce la join già risolta tra iscrizioni, corsi, utenti e chef.

## 💻 Integrazione Java

### DAO Avanzato

```java
// Utilizzo delle nuove funzionalità
IscrizioneDAOAdvanced dao = new IscrizioneDAOAdvanced();

// Verifica posti disponibili
int posti = dao.getPostiDisponibili(1);

// Statistiche corso
Map<String, Object> stats = dao.getStatisticheCorso(1);

// Calcolo prezzo scontato
Double prezzo = dao.calcolaPrezzoScontato(1, 20.0);

// Iscrizioni complete con tutti i dettagli
List<Map<String, Object>> iscrizioni = dao.getIscrizioniComplete();
```

### Dashboard DAO

```java
// Metriche dashboard
DashboardDAO dashboardDAO = new DashboardDAO();

// Metriche principali
List<Map<String, String>> metriche = dashboardDAO.getDashboardMetrics();

// Report chef
List<Map<String, Object>> reportChef = dashboardDAO.getReportChef();

// Corsi che necessitano attenzione
List<Map<String, Object>> corsiAttenzione = dashboardDAO.getCorsiAttenzione();
```

## 🛡️ Sicurezza e Integrità

### Controlli Automatici

- ✅ Validazione formato email
- ✅ Unicità codici iscrizione
- ✅ Controllo posti disponibili
- ✅ Audit trail modifiche stato iscrizioni
- ✅ Integrità referenziale (FK e ON DELETE/UPDATE)

### Log e Tracciabilità

```sql
-- Visualizzare log modifiche iscrizione
SELECT * FROM log_iscrizioni 
WHERE iscrizione_id = 1 
ORDER BY timestamp_modifica DESC;

-- Tracciare modifiche per utente
SELECT li.*, u.nome, u.cognome
FROM log_iscrizioni li
JOIN iscrizioni i ON li.iscrizione_id = i.id
JOIN utenti u ON i.utente_id = u.id
WHERE u.id = 1;
```

## 🎯 Benefici

### Performance

- Funzioni SQL: riducono roundtrip e logica lato applicazione
- Indici mirati sulle chiavi di ricerca (PK/FK + colonne di filtro frequenti)
- Viste: centralizzano logica di aggregazione evitando duplicazioni

### Sicurezza

- Validazione e regole di business persistenti
- Audit e riproducibilità degli stati
- Minimizzazione logica duplicata lato Java

### Manutenibilità

- Logica centralizzata (meno drift tra servizi)
- Trigger riducono boilerplate negli strati DAO
- Viste standardizzano le query complesse

## 🔍 Debugging e Monitoring

### Controllo Funzioni

```sql
-- Test funzioni
SELECT 'calcola_eta' as funzione, calcola_eta('1990-01-01'::DATE) as risultato
UNION ALL
SELECT 'valida_email', valida_email('test@example.com')::TEXT
UNION ALL
SELECT 'genera_codice', genera_codice_iscrizione();
```

### Monitoraggio Trigger

```sql
-- Verificare log recenti
SELECT * FROM log_iscrizioni 
WHERE timestamp_modifica >= CURRENT_DATE 
ORDER BY timestamp_modifica DESC;

-- Statistiche trigger
SELECT 
    azione, 
    COUNT(*) as numero_operazioni,
    DATE(timestamp_modifica) as data
FROM log_iscrizioni 
WHERE timestamp_modifica >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY azione, DATE(timestamp_modifica)
ORDER BY data DESC, azione;
```

### Performance View

```sql
-- Statistiche utilizzo view
SELECT 
    schemaname, 
    viewname, 
    'OK' as stato
FROM pg_views 
WHERE schemaname = 'public' 
AND viewname IN ('dashboard_admin', 'corsi_dettaglio', 'report_chef');
```

## 📝 Note Tecniche

### Requisiti

-- **PostgreSQL 17+**: Allineato al dump corrente
- **Encoding UTF-8**: Per supporto caratteri internazionali
- **Privilegi**: Creazione funzioni e trigger

### Compatibilità

-- ✅ Java 17+ consigliato (retro-compatibile con 11)
- ✅ **Connection Pooling**: Compatibile con HikariCP
- ✅ **Transazioni**: Supporto completo transazioni ACID

### Backup

```bash
# Backup completo con funzioni
pg_dump -U postgres -d uninafoodlab --clean --create > backup_completo.sql

# Restore
psql -U postgres < backup_completo.sql
```

---

**Documentazione aggiornata al: 18 settembre 2025**  
**Versione funzionalità avanzate: 1.1 (schema sincronizzato con ultimo dump)**
