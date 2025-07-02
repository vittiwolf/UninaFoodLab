-- SCRIPT SOLO STRUTTURA DATABASE UNINAFOODLAB (PostgreSQL)
-- Basato sul dump del 24/06/2025
-- Questo script crea SOLO la struttura del database SENZA dati di test

-- =====================================================
-- 1. CONFIGURAZIONE INIZIALE
-- =====================================================

\echo 'CREAZIONE STRUTTURA DATABASE UNINAFOODLAB...'

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
-- 6. RESET SEQUENZE
-- =====================================================

\echo 'RESET SEQUENZE...'

SELECT setval('adesioni_sessioni_id_seq', 1, false);
SELECT setval('categorie_corsi_id_seq', 1, false);
SELECT setval('chef_id_seq', 1, false);
SELECT setval('corsi_id_seq', 1, false);
SELECT setval('ingredienti_id_seq', 1, false);
SELECT setval('iscrizioni_id_seq', 1, false);
SELECT setval('notifiche_id_seq', 1, false);
SELECT setval('ricette_id_seq', 1, false);
SELECT setval('ricette_ingredienti_id_seq', 1, false);
SELECT setval('sessioni_id_seq', 1, false);
SELECT setval('sessioni_ricette_id_seq', 1, false);
SELECT setval('utenti_id_seq', 1, false);

\echo 'Sequenze resettate!'

-- =====================================================
-- 7. VERIFICA FINALE
-- =====================================================

\echo 'VERIFICA FINALE STRUTTURA DATABASE:'

-- Mostra tutte le tabelle create
SELECT 
    table_name,
    table_type
FROM information_schema.tables
WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
ORDER BY table_name;

\echo '';
\echo 'STRUTTURA DATABASE CREATA CON SUCCESSO!'
\echo 'Database pronto per l''inserimento dei dati.'
\echo 'Per inserire dati di test, eseguire successivamente test_data.sql';
