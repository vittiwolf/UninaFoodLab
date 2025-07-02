-- SCRIPT COMPLETO PER CONFIGURAZIONE DATABASE UNINAFOODLAB (PostgreSQL)
-- Basato sul dump più recente del 24/06/2025
-- Questo script crea la struttura completa del database con tutti i dati aggiornati

-- =====================================================
-- 1. CONFIGURAZIONE INIZIALE
-- =====================================================

\echo 'CONFIGURAZIONE DATABASE UNINAFOODLAB...'

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';
SET default_table_access_method = heap;

-- =====================================================
-- 2. BACKUP DI SICUREZZA
-- =====================================================

\echo 'CREAZIONE BACKUP TABELLE ESISTENTI...'

DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
        EXECUTE format('DROP TABLE IF EXISTS %I_backup CASCADE', r.tablename);
        EXECUTE format('CREATE TABLE %I_backup AS SELECT * FROM %I', r.tablename, r.tablename);
        RAISE NOTICE 'Backup creato per tabella: %', r.tablename;
    END LOOP;
EXCEPTION
    WHEN others THEN
        RAISE NOTICE 'Backup non necessario - database vuoto o errore: %', SQLERRM;
END $$;

\echo 'Backup completato!'

-- =====================================================
-- 3. PULIZIA TABELLE OBSOLETE
-- =====================================================

\echo 'PULIZIA TABELLE OBSOLETE...'

-- Rimuovi tabella iscrizioni_corsi obsoleta (sostituita da iscrizioni)
DROP TABLE IF EXISTS iscrizioni_corsi CASCADE;

\echo 'Pulizia completata!'

-- =====================================================
-- 4. CREAZIONE STRUTTURA COMPLETA DATABASE
-- =====================================================

\echo 'CREAZIONE STRUTTURA DATABASE...'

-- TABELLA CHEF
DROP TABLE IF EXISTS chef CASCADE;
CREATE TABLE chef (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    specializzazione VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABELLA UTENTI
DROP TABLE IF EXISTS utenti CASCADE;
CREATE TABLE utenti (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_nascita DATE,
    livello_esperienza VARCHAR(20) DEFAULT 'PRINCIPIANTE',
    attivo BOOLEAN DEFAULT TRUE
);

COMMENT ON COLUMN utenti.data_nascita IS 'Data di nascita dell utente';

-- TABELLA CATEGORIE CORSI
DROP TABLE IF EXISTS categorie_corsi CASCADE;
CREATE TABLE categorie_corsi (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL,
    descrizione TEXT
);

-- TABELLA CORSI
DROP TABLE IF EXISTS corsi CASCADE;
CREATE TABLE corsi (
    id SERIAL PRIMARY KEY,
    chef_id INTEGER REFERENCES chef(id) ON DELETE CASCADE,
    categoria_id INTEGER REFERENCES categorie_corsi(id),
    titolo VARCHAR(200) NOT NULL,
    descrizione TEXT,
    data_inizio DATE NOT NULL,
    frequenza VARCHAR(50) NOT NULL,
    numero_sessioni INTEGER NOT NULL,
    prezzo NUMERIC(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABELLA SESSIONI
DROP TABLE IF EXISTS sessioni CASCADE;
CREATE TABLE sessioni (
    id SERIAL PRIMARY KEY,
    corso_id INTEGER REFERENCES corsi(id) ON DELETE CASCADE,
    numero_sessione INTEGER NOT NULL,
    data_sessione DATE NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    titolo VARCHAR(200),
    descrizione TEXT,
    durata_minuti INTEGER DEFAULT 120,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sessioni_corso_id_numero_sessione_key UNIQUE (corso_id, numero_sessione)
);

-- TABELLA ISCRIZIONI (principale)
DROP TABLE IF EXISTS iscrizioni CASCADE;
CREATE TABLE iscrizioni (
    id SERIAL PRIMARY KEY,
    utente_id INTEGER NOT NULL REFERENCES utenti(id) ON DELETE CASCADE,
    corso_id INTEGER NOT NULL REFERENCES corsi(id) ON DELETE CASCADE,
    data_iscrizione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    stato VARCHAR(20) DEFAULT 'ATTIVA',
    note TEXT,
    CONSTRAINT iscrizioni_utente_id_corso_id_key UNIQUE (utente_id, corso_id),
    CONSTRAINT iscrizioni_stato_check CHECK (stato IN ('ATTIVA', 'COMPLETATA', 'ANNULLATA'))
);

COMMENT ON TABLE iscrizioni IS 'Tabella delle iscrizioni degli utenti ai corsi';
COMMENT ON COLUMN iscrizioni.id IS 'Identificativo univoco iscrizione';
COMMENT ON COLUMN iscrizioni.utente_id IS 'Riferimento all utente iscritto';
COMMENT ON COLUMN iscrizioni.corso_id IS 'Riferimento al corso';
COMMENT ON COLUMN iscrizioni.data_iscrizione IS 'Data e ora di iscrizione';
COMMENT ON COLUMN iscrizioni.stato IS 'Stato iscrizione: ATTIVA, COMPLETATA, ANNULLATA';
COMMENT ON COLUMN iscrizioni.note IS 'Note aggiuntive sull iscrizione';

-- TABELLA ADESIONI SESSIONI
DROP TABLE IF EXISTS adesioni_sessioni CASCADE;
CREATE TABLE adesioni_sessioni (
    id SERIAL PRIMARY KEY,
    utente_id INTEGER REFERENCES utenti(id) ON DELETE CASCADE,
    sessione_id INTEGER REFERENCES sessioni(id) ON DELETE CASCADE,
    data_adesione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confermata BOOLEAN DEFAULT FALSE,
    note TEXT,
    CONSTRAINT adesioni_sessioni_utente_id_sessione_id_key UNIQUE (utente_id, sessione_id)
);

-- TABELLA INGREDIENTI
DROP TABLE IF EXISTS ingredienti CASCADE;
CREATE TABLE ingredienti (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL,
    categoria VARCHAR(50),
    unita_misura VARCHAR(20),
    costo_unitario NUMERIC(8,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- TABELLA RICETTE
DROP TABLE IF EXISTS ricette CASCADE;
CREATE TABLE ricette (
    id SERIAL PRIMARY KEY,
    chef_id INTEGER REFERENCES chef(id),
    nome VARCHAR(200) NOT NULL,
    descrizione TEXT,
    difficolta INTEGER,
    tempo_preparazione INTEGER,
    numero_porzioni INTEGER DEFAULT 4,
    istruzioni TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ricette_difficolta_check CHECK (difficolta >= 1 AND difficolta <= 5)
);

-- TABELLA RICETTE-INGREDIENTI
DROP TABLE IF EXISTS ricette_ingredienti CASCADE;
CREATE TABLE ricette_ingredienti (
    id SERIAL PRIMARY KEY,
    ricetta_id INTEGER REFERENCES ricette(id) ON DELETE CASCADE,
    ingrediente_id INTEGER REFERENCES ingredienti(id),
    quantita NUMERIC(8,2) NOT NULL,
    note VARCHAR(200),
    CONSTRAINT ricette_ingredienti_ricetta_id_ingrediente_id_key UNIQUE (ricetta_id, ingrediente_id)
);

-- TABELLA SESSIONI-RICETTE
DROP TABLE IF EXISTS sessioni_ricette CASCADE;
CREATE TABLE sessioni_ricette (
    id SERIAL PRIMARY KEY,
    sessione_id INTEGER REFERENCES sessioni(id) ON DELETE CASCADE,
    ricetta_id INTEGER REFERENCES ricette(id),
    ordine_esecuzione INTEGER DEFAULT 1,
    note TEXT,
    CONSTRAINT sessioni_ricette_sessione_id_ricetta_id_key UNIQUE (sessione_id, ricetta_id)
);

-- TABELLA NOTIFICHE
DROP TABLE IF EXISTS notifiche CASCADE;
CREATE TABLE notifiche (
    id SERIAL PRIMARY KEY,
    corso_id INTEGER REFERENCES corsi(id) ON DELETE CASCADE,
    titolo VARCHAR(200) NOT NULL,
    messaggio TEXT NOT NULL,
    tipo VARCHAR(50),
    data_invio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    inviata BOOLEAN DEFAULT FALSE
);

\echo 'Struttura database creata!'

-- =====================================================
-- 5. CREAZIONE INDICI PER PERFORMANCE
-- =====================================================

\echo 'CREAZIONE INDICI...'

-- Indici per performance
CREATE INDEX idx_adesioni_sessione ON adesioni_sessioni USING btree (sessione_id);
CREATE INDEX idx_adesioni_utente ON adesioni_sessioni USING btree (utente_id);
CREATE INDEX idx_corsi_categoria ON corsi USING btree (categoria_id);
CREATE INDEX idx_corsi_chef ON corsi USING btree (chef_id);
CREATE INDEX idx_iscrizioni_stato ON iscrizioni USING btree (stato);
CREATE INDEX idx_sessioni_corso ON sessioni USING btree (corso_id);

\echo 'Indici creati!'

-- =====================================================
-- 6. INSERIMENTO DATI DI BASE
-- =====================================================

\echo 'INSERIMENTO DATI DI BASE...'

-- Inserimento categorie corsi
INSERT INTO categorie_corsi (nome, descrizione) VALUES
    ('Cucina Asiatica', 'Corsi dedicati alla cucina orientale: sushi, ramen, curry'),
    ('Pasticceria', 'Arte della preparazione di dolci, torte e dessert'),
    ('Panificazione', 'Tecniche di preparazione di pane, pizza e lievitati'),
    ('Cucina Italiana', 'Tradizioni culinarie regionali italiane'),
    ('Cucina Vegana', 'Cucina a base vegetale, senza derivati animali'),
    ('Cucina Molecolare', 'Tecniche moderne e innovative di cucina');

-- Inserimento chef
INSERT INTO chef (username, password, nome, cognome, email, specializzazione) VALUES
    ('chef_mario', 'password123', 'Mario', 'Rossi', 'mario.rossi@uninafoodlab.it', 'Cucina Italiana'),
    ('chef_yuki', 'sushi2024', 'Yuki', 'Tanaka', 'yuki.tanaka@uninafoodlab.it', 'Cucina Asiatica'),
    ('chef_pierre', 'baguette456', 'Pierre', 'Dubois', 'pierre.dubois@uninafoodlab.it', 'Pasticceria'),
    ('chef_anna', 'vegan789', 'Anna', 'Verdi', 'anna.verdi@uninafoodlab.it', 'Cucina Vegana');

-- Inserimento utenti di test
INSERT INTO utenti (username, password, nome, cognome, email, telefono, data_nascita, livello_esperienza, attivo) VALUES
    ('user1', 'pass123', 'Marco', 'Bianchi', 'marco.bianchi@email.it', '3331234567', '1990-05-15', 'PRINCIPIANTE', TRUE),
    ('user2', 'pass456', 'Laura', 'Neri', 'laura.neri@email.it', '3337654321', '1985-08-22', 'INTERMEDIO', TRUE),
    ('user3', 'pass789', 'Giuseppe', 'Romano', 'giuseppe.romano@email.it', '3339876543', '1995-12-03', 'AVANZATO', TRUE);

-- Inserimento corsi
INSERT INTO corsi (chef_id, categoria_id, titolo, descrizione, data_inizio, frequenza, numero_sessioni, prezzo) VALUES
    (1, 4, 'Corso Base di Cucina Italiana', 'Impara i fondamenti della cucina italiana tradizionale', '2025-06-01', 'settimanale', 8, 299.00),
    (2, 1, 'Sushi e Cucina Giapponese', 'Tecniche tradizionali per la preparazione del sushi', '2025-06-15', 'ogni_due_giorni', 6, 399.00),
    (3, 2, 'Pasticceria Francese Avanzata', 'Dolci e dessert della tradizione francese', '2025-06-01', 'settimanale', 10, 599.00);

-- Inserimento ingredienti
INSERT INTO ingredienti (nome, categoria, unita_misura, costo_unitario) VALUES
    ('Farina 00', 'cereali', 'kg', 1.20),
    ('Pomodori San Marzano', 'verdura', 'kg', 3.50),
    ('Mozzarella di Bufala', 'latticini', 'kg', 12.00),
    ('Salmone', 'pesce', 'kg', 25.00),
    ('Riso per Sushi', 'cereali', 'kg', 4.50),
    ('Alga Nori', 'alghe', 'confezione', 8.00),
    ('Burro', 'latticini', 'kg', 6.00),
    ('Uova', 'proteine', 'dozzina', 3.00),
    ('Zucchero', 'dolcificanti', 'kg', 1.00),
    ('Latte di Mandorla', 'vegetale', 'litro', 2.50);

-- Inserimento ricette
INSERT INTO ricette (chef_id, nome, descrizione, difficolta, tempo_preparazione, numero_porzioni, istruzioni) VALUES
    (1, 'Pizza Margherita', 'Classica pizza napoletana con pomodoro e mozzarella', 3, 180, 4, 'Preparare l''impasto, stendere, aggiungere condimenti e cuocere in forno'),
    (2, 'Sushi Maki', 'Rotolini di sushi con salmone e cetriolo', 4, 45, 6, 'Preparare il riso, stendere su nori, aggiungere ingredienti e arrotolare'),
    (3, 'Croissant', 'Cornetti francesi sfogliati', 5, 240, 8, 'Preparare pasta sfoglia, dare forma e cuocere'),
    (4, 'Buddha Bowl Vegano', 'Ciotola completa con quinoa e verdure', 2, 30, 2, 'Cuocere quinoa, preparare verdure e comporre la bowl');

-- Inserimento sessioni
INSERT INTO sessioni (corso_id, numero_sessione, data_sessione, tipo, titolo, descrizione, durata_minuti) VALUES
    (1, 1, '2025-07-01', 'presenza', 'Introduzione e Pasta Fresca', 'Prima lezione pratica sulla pasta fatta in casa', 120),
    (1, 2, '2025-07-08', 'online', 'Storia della Cucina Italiana', 'Lezione teorica sulle origini regionali', 120),
    (1, 3, '2025-07-15', 'presenza', 'Pizza e Lievitati', 'Tecniche di panificazione e pizza napoletana', 120),
    (2, 1, '2025-07-15', 'presenza', 'Preparazione del Riso', 'Basi del riso per sushi', 120),
    (2, 2, '2025-07-17', 'presenza', 'Sushi Maki e Nigiri', 'Tecniche di preparazione avanzate', 120);

-- Inserimento ricette-ingredienti
INSERT INTO ricette_ingredienti (ricetta_id, ingrediente_id, quantita, note) VALUES
    (1, 1, 0.50, 'Per l''impasto'),
    (1, 2, 0.30, 'Per il sugo'),
    (1, 3, 0.25, 'Per la farcitura'),
    (2, 5, 0.30, 'Per la base'),
    (2, 4, 0.20, 'Per il ripieno'),
    (2, 6, 2.00, 'Per avvolgere'),
    (3, 1, 0.50, 'Per la pasta'),
    (3, 7, 0.30, 'Per la sfogliatura'),
    (3, 8, 3.00, 'Per l''impasto');

-- Inserimento sessioni-ricette
INSERT INTO sessioni_ricette (sessione_id, ricetta_id, ordine_esecuzione) VALUES
    (1, 1, 1),
    (3, 1, 1),
    (4, 2, 1),
    (5, 2, 1);

-- Inserimento iscrizioni di test
INSERT INTO iscrizioni (utente_id, corso_id, stato, note) VALUES
    (1, 1, 'ATTIVA', 'Iscrizione di test automatica'),
    (2, 1, 'ATTIVA', 'Iscrizione di test automatica'),
    (1, 2, 'ATTIVA', 'Iscrizione di test automatica');

-- Inserimento adesioni sessioni
INSERT INTO adesioni_sessioni (utente_id, sessione_id, confermata) VALUES
    (1, 1, TRUE),
    (2, 1, TRUE),
    (1, 3, FALSE),
    (3, 4, TRUE),
    (1, 5, TRUE);

\echo 'Dati di base inseriti!'

-- =====================================================
-- 7. AGGIORNAMENTO SEQUENZE
-- =====================================================

\echo 'AGGIORNAMENTO SEQUENZE...'

SELECT setval('adesioni_sessioni_id_seq', 5, true);
SELECT setval('categorie_corsi_id_seq', 6, true);
SELECT setval('chef_id_seq', 4, true);
SELECT setval('corsi_id_seq', 3, true);
SELECT setval('ingredienti_id_seq', 10, true);
SELECT setval('iscrizioni_id_seq', 9, true);
SELECT setval('notifiche_id_seq', 1, false);
SELECT setval('ricette_id_seq', 4, true);
SELECT setval('ricette_ingredienti_id_seq', 9, true);
SELECT setval('sessioni_id_seq', 5, true);
SELECT setval('sessioni_ricette_id_seq', 4, true);
SELECT setval('utenti_id_seq', 3, true);

\echo 'Sequenze aggiornate!'

-- =====================================================
-- 8. VERIFICA FINALE
-- =====================================================

\echo 'VERIFICA FINALE STRUTTURA DATABASE:'

-- Conta record nelle tabelle
SELECT 
    'chef' as tabella, COUNT(*) as records FROM chef
UNION ALL
SELECT 'utenti' as tabella, COUNT(*) as records FROM utenti
UNION ALL
SELECT 'categorie_corsi' as tabella, COUNT(*) as records FROM categorie_corsi
UNION ALL
SELECT 'corsi' as tabella, COUNT(*) as records FROM corsi
UNION ALL
SELECT 'sessioni' as tabella, COUNT(*) as records FROM sessioni
UNION ALL
SELECT 'iscrizioni' as tabella, COUNT(*) as records FROM iscrizioni
UNION ALL
SELECT 'adesioni_sessioni' as tabella, COUNT(*) as records FROM adesioni_sessioni
UNION ALL
SELECT 'ingredienti' as tabella, COUNT(*) as records FROM ingredienti
UNION ALL
SELECT 'ricette' as tabella, COUNT(*) as records FROM ricette
UNION ALL
SELECT 'ricette_ingredienti' as tabella, COUNT(*) as records FROM ricette_ingredienti
UNION ALL
SELECT 'sessioni_ricette' as tabella, COUNT(*) as records FROM sessioni_ricette
UNION ALL
SELECT 'notifiche' as tabella, COUNT(*) as records FROM notifiche
ORDER BY tabella;

\echo 'SCRIPT COMPLETATO CON SUCCESSO!'
\echo 'Il database è ora completamente configurato per l''applicazione UninaFoodLab.'
\echo 'Struttura database aggiornata in base al dump del 24/06/2025.'
