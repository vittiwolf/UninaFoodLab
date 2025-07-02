# Script Database UninaFoodLab

Questa cartella contiene tutti gli script SQL necessari per configurare e gestire il database PostgreSQL di UninaFoodLab.

**🚀 AGGIORNATO AL 24/06/2025** - Basato sul dump più recente del database

## 📁 File Disponibili

### 🎯 Script Principali

#### `setup_database.sql` ⭐ **PRINCIPALE**
**Descrizione**: Script completo per configurazione del database  
**Quando usare**: Prima installazione o setup completo  
**Cosa fa**:
- Configura completamente il database con tutte le tabelle
- Crea backup automatico delle tabelle esistenti
- Rimuove tabelle obsolete (es. `iscrizioni_corsi`)
- Crea la struttura completa aggiornata
- Inserisce dati di test per sviluppo
- Configura indici per performance ottimali
- Aggiorna le sequenze automaticamente

**Utilizzo**:
```sql
psql -U postgres -d uninafoodlab
\i setup_database.sql
```

---

#### `structure_only.sql` 🏗️
**Descrizione**: Solo struttura database, senza dati  
**Quando usare**: Ambiente di produzione o quando hai già dati  
**Cosa fa**:
- Crea backup di sicurezza
- Rimuove tabelle obsolete
- Crea tutta la struttura del database
- Configura indici e vincoli
- Reset delle sequenze a 1
- NON inserisce dati di test

**Utilizzo**:
```sql
psql -U postgres -d uninafoodlab
\i structure_only.sql
```

---

#### `test_data.sql` 📊
**Descrizione**: Inserimento dati di test  
**Quando usare**: Dopo `structure_only.sql` per ambienti di sviluppo  
**Cosa fa**:
- Inserisce categorie corsi complete
- Crea chef di esempio
- Aggiunge utenti di test con profili diversi
- Inserisce corsi realistici
- Crea ingredienti e ricette
- Configura sessioni e iscrizioni
- Aggiorna automaticamente le sequenze

**Utilizzo**:
```sql
psql -U postgres -d uninafoodlab
\i test_data.sql
```

---

#### `verify_database.sql` 🔍
**Descrizione**: Verifica completa integrità database  
**Quando usare**: Dopo qualsiasi installazione per controllo  
**Cosa fa**:
- Verifica esistenza di tutte le tabelle
- Controlla struttura e vincoli
- Verifica integrità referenziale
- Analizza performance indici
- Mostra statistiche dettagliate
- Report finale sullo stato del database

**Utilizzo**:
```sql
psql -U postgres -d uninafoodlab
\i verify_database.sql
```

## 🗂️ Struttura Database Attuale

### 📋 Tabelle Principali
- **`chef`** - Gestione chef e istruttori
- **`utenti`** - Utenti dell'applicazione con profilo completo
- **`categorie_corsi`** - Categorie dei corsi (Asiatica, Pasticceria, etc.)
- **`corsi`** - Corsi disponibili con dettagli completi
- **`sessioni`** - Singole lezioni dei corsi
- **`iscrizioni`** - Iscrizioni utenti ai corsi (NUOVA - sostituisce `iscrizioni_corsi`)

### 🔗 Tabelle di Relazione
- **`adesioni_sessioni`** - Partecipazione utenti alle singole sessioni
- **`ingredienti`** - Ingredienti per ricette
- **`ricette`** - Ricette dei chef
- **`ricette_ingredienti`** - Ingredienti per ricetta
- **`sessioni_ricette`** - Ricette utilizzate nelle sessioni
- **`notifiche`** - Sistema notifiche

### 🆕 Novità Struttura
- ✅ **Tabella `iscrizioni` principale** - Gestione moderna delle iscrizioni
- ❌ **Rimossa `iscrizioni_corsi`** - Tabella obsoleta
- ✅ **Campo `data_nascita` in utenti** - Gestione anagrafica completa
- ✅ **Campo `livello_esperienza`** - PRINCIPIANTE, INTERMEDIO, AVANZATO
- ✅ **Campo `attivo` per utenti** - Gestione stato account
- ✅ **Vincoli CHECK** - Validazione dati a livello database
- ✅ **Indici ottimizzati** - Performance migliorate

## 🚀 Quick Start

### Installazione Completa (Sviluppo)
```bash
# 1. Connetti al database
psql -U postgres -d uninafoodlab

# 2. Esegui setup completo
\i setup_database.sql

# 3. Verifica installazione
\i verify_database.sql
```

### Installazione Solo Struttura (Produzione)
```bash
# 1. Connetti al database
psql -U postgres -d uninafoodlab

# 2. Crea solo struttura
\i structure_only.sql

# 3. Verifica installazione
\i verify_database.sql
```

### Aggiunta Dati di Test
```bash
# Dopo structure_only.sql
\i test_data.sql
\i verify_database.sql
```

## 📊 Dati di Test Inclusi

Quando esegui `setup_database.sql` o `test_data.sql`, ottieni:

### �‍🍳 Chef (4)
- Mario Rossi (Cucina Italiana)
- Yuki Tanaka (Cucina Asiatica)  
- Pierre Dubois (Pasticceria)
- Anna Verdi (Cucina Vegana)

### 👥 Utenti (5)
- Marco Bianchi (Principiante)
- Laura Neri (Intermedio)
- Giuseppe Romano (Avanzato)
- Francesca Esposito (Principiante)
- Antonio Ricci (Intermedio)

### 📚 Corsi (5)
- Corso Base di Cucina Italiana (€299)
- Sushi e Cucina Giapponese (€399)
- Pasticceria Francese Avanzata (€599)
- Cucina Vegana Creativa (€249)
- Panificazione Artigianale (€349)

### 🥘 Ricette (8)
- Pizza Margherita
- Sushi Maki
- Croissant
- Buddha Bowl Vegano
- Pasta Carbonara
- Ramen Tradizionale
- Tiramisù
- Burger Vegano

### 🧑‍🎓 Iscrizioni (9)
- Distribuiti tra i corsi con stati diversi (ATTIVA, COMPLETATA, ANNULLATA)

## 🔧 Troubleshooting

### Errore "tabella non esiste"
```sql
-- Verifica tabelle esistenti
SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';
```

### Errore "vincolo violato"
```sql
-- Verifica vincoli
SELECT * FROM information_schema.table_constraints WHERE table_schema = 'public';
```

### Errore sequenze
```sql
-- Reset manuale sequenze
SELECT setval('utenti_id_seq', (SELECT MAX(id) FROM utenti), true);
```

### Backup corrotto
```sql
-- Le tabelle backup sono create automaticamente con suffisso _backup
SELECT table_name FROM information_schema.tables WHERE table_name LIKE '%_backup';
```

## 📈 Performance

### Indici Ottimizzati
- `idx_corsi_chef` - Ricerca corsi per chef
- `idx_corsi_categoria` - Ricerca per categoria
- `idx_iscrizioni_stato` - Filtro per stato iscrizione
- `idx_adesioni_utente` - Partecipazioni utente
- `idx_sessioni_corso` - Sessioni per corso

### Query Consigliate
```sql
-- Corsi attivi per utente
SELECT c.* FROM corsi c
JOIN iscrizioni i ON c.id = i.corso_id
WHERE i.utente_id = ? AND i.stato = 'ATTIVA';

-- Sessioni future
SELECT * FROM sessioni 
WHERE data_sessione > CURRENT_DATE
ORDER BY data_sessione;
```

## 🆘 Supporto

Per problemi con gli script SQL:
1. Controlla i log di PostgreSQL
2. Esegui `verify_database.sql` per diagnosticare
3. Controlla le tabelle backup (suffisso `_backup`)
4. Consulta la documentazione PostgreSQL

---

**Ultimo aggiornamento**: 24 Giugno 2025  
**Versione database**: Basata su dump PostgreSQL 17.5  
**Compatibilità**: PostgreSQL 12+
- Inserisce corsi di esempio
- Crea iscrizioni di test
- Verifica inserimenti

**Utilizzo**:
```sql
-- Eseguire DOPO structure_only.sql
psql -U postgres -d uninafoodlab
\i test_data.sql
```

---

#### `verify_database.sql` ✅
**Descrizione**: Verifica che il database sia configurato correttamente  
**Quando usare**: Per testare che tutto funzioni  
**Cosa fa**:
- Mostra struttura tabelle
- Conta record
- Testa query principali
- Verifica vincoli

**Utilizzo**:
```sql
psql -U postgres -d uninafoodlab
\i verify_database.sql
```

## 🚀 Scenari di Utilizzo

### 📦 Prima Installazione
```sql
-- Setup completo con dati di test
\i setup_database.sql
```

### 🔄 Aggiornamento Database Esistente
```sql
-- Solo struttura (preserva dati esistenti)
\i structure_only.sql
```

### 🧪 Aggiunta Dati di Test
```sql
-- Prima la struttura, poi i dati
\i structure_only.sql
\i test_data.sql
```

### ✅ Verifica Funzionalità
```sql
-- Test completo database
\i verify_database.sql
```

## 📋 Checklist Pre-Esecuzione

### Prerequisiti
- [ ] PostgreSQL installato e avviato
- [ ] Database `uninafoodlab` creato
- [ ] Utente `postgres` con permessi adeguati
- [ ] Connessione al database testata

### Verifica Ambiente
```sql
-- Verifica connessione
\conninfo

-- Verifica database
\l

-- Verifica tabelle esistenti
\dt
```

## 🛡️ Sicurezza e Backup

### Backup Automatico
Tutti gli script creano automaticamente backup delle tabelle esistenti:
- `utenti_backup` - Backup tabella utenti
- `corsi_backup` - Backup tabella corsi

### Rollback Manuale
Se qualcosa va male:
```sql
-- Ripristina tabella utenti
DROP TABLE utenti;
ALTER TABLE utenti_backup RENAME TO utenti;

-- Ripristina tabella corsi  
DROP TABLE corsi;
ALTER TABLE corsi_backup RENAME TO corsi;
```

## 🔧 Risoluzione Problemi

### Errore: "relation does not exist"
- **Causa**: Tabella non esiste
- **Soluzione**: Eseguire prima `setup_database.sql`

### Errore: "column already exists"
- **Causa**: Colonna già presente
- **Soluzione**: Normale, lo script gestisce automaticamente

### Errore: "permission denied"
- **Causa**: Permessi insufficienti
- **Soluzione**: Connettersi come superuser: `psql -U postgres`

### Performance Lente
- **Causa**: Indici mancanti
- **Soluzione**: Gli script creano automaticamente tutti gli indici necessari

## 📊 Struttura Database Finale

### Tabella `utenti`
```sql
id               SERIAL PRIMARY KEY
nome             VARCHAR(100) NOT NULL
cognome          VARCHAR(100) NOT NULL  
email            VARCHAR(255) UNIQUE NOT NULL
telefono         VARCHAR(20)
data_nascita     DATE
livello_esperienza VARCHAR(20) DEFAULT 'PRINCIPIANTE'
attivo           BOOLEAN DEFAULT TRUE
created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP
```

### Tabella `iscrizioni`
```sql
id               SERIAL PRIMARY KEY
utente_id        INTEGER REFERENCES utenti(id)
corso_id         INTEGER REFERENCES corsi(id)
data_iscrizione  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
stato            VARCHAR(20) DEFAULT 'ATTIVA'
note             TEXT
```

## 📞 Supporto

Per problemi con gli script database:
1. Verificare log PostgreSQL
2. Controllare permessi utente
3. Consultare `docs/03-database.md` per dettagli architettura
4. Eseguire `verify_database.sql` per diagnostica

---

**Tutti gli script sono sicuri e includono controlli per evitare perdita di dati** ✅
