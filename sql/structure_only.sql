-- =====================================================================
-- SCRIPT: STRUTTURA SOLO (NO DATI, NO FUNZIONI, NO TRIGGER, NO VIEW)
-- Progetto: UninaFoodLab
-- Basato sul dump del 18/09/2025 (PostgreSQL 17.5)
-- Descrizione: Crea esclusivamente la struttura delle tabelle, sequenze
--              (via SERIAL), constraint (PK, FK, UNIQUE, CHECK) e indici.
--              Nessuna funzione, vista, trigger o dato applicativo.
-- =====================================================================

-- =====================================================
-- 1. CONFIGURAZIONE INIZIALE
-- =====================================================

\echo '==> CREAZIONE STRUTTURA DATABASE UNINAFOODLAB'

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

-- (Backup automatico rimosso per versione "structure only")

-- =====================================================
-- 3. PULIZIA TABELLE OBSOLETE
-- =====================================================

-- Pulizia tabelle obsolete (solo se presenti)
DROP TABLE IF EXISTS notifiche CASCADE;  -- tabella non più presente nello schema
DROP TABLE IF EXISTS iscrizioni_corsi CASCADE; -- sostituita da iscrizioni

-- =====================================================
-- 4. CREAZIONE STRUTTURA COMPLETA DATABASE
-- =====================================================

\echo '-> Tabelle principali'

-- CHEF
DROP TABLE IF EXISTS chef CASCADE;
CREATE TABLE chef (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    specializzazione VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- UTENTI
DROP TABLE IF EXISTS utenti CASCADE;
CREATE TABLE utenti (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    data_nascita DATE,
    livello_esperienza VARCHAR(20) DEFAULT 'PRINCIPIANTE',
    attivo BOOLEAN DEFAULT TRUE,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON COLUMN utenti.data_nascita IS 'Data di nascita dell utente';

-- TABELLA CATEGORIE CORSI
DROP TABLE IF EXISTS categorie_corsi CASCADE;
CREATE TABLE categorie_corsi (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) UNIQUE NOT NULL,
    descrizione TEXT
);

-- CORSI
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    durata_corso INTEGER NOT NULL,
    max_partecipanti INTEGER NOT NULL,
    CONSTRAINT chk_durata_corso_valida CHECK (durata_corso >= 1 AND durata_corso <= 8),
    CONSTRAINT chk_max_partecipanti_valido CHECK (max_partecipanti >= 1 AND max_partecipanti <= 50)
);

-- SESSIONI
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
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT sessioni_corso_id_numero_sessione_key UNIQUE (corso_id, numero_sessione)
);

-- ISCRIZIONI
DROP TABLE IF EXISTS iscrizioni CASCADE;
CREATE TABLE iscrizioni (
    id SERIAL PRIMARY KEY,
    utente_id INTEGER NOT NULL REFERENCES utenti(id) ON DELETE CASCADE,
    corso_id INTEGER NOT NULL REFERENCES corsi(id) ON DELETE CASCADE,
    data_iscrizione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    stato VARCHAR(20) DEFAULT 'ATTIVA',
    note TEXT,
    modified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    codice_iscrizione VARCHAR(20) UNIQUE,
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

-- ADESIONI_SESSIONI
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

-- RICETTE
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

-- RICETTE_INGREDIENTI
DROP TABLE IF EXISTS ricette_ingredienti CASCADE;
CREATE TABLE ricette_ingredienti (
    id SERIAL PRIMARY KEY,
    ricetta_id INTEGER REFERENCES ricette(id) ON DELETE CASCADE,
    ingrediente_id INTEGER REFERENCES ingredienti(id),
    quantita NUMERIC(8,2) NOT NULL,
    note VARCHAR(200),
    CONSTRAINT ricette_ingredienti_ricetta_id_ingrediente_id_key UNIQUE (ricetta_id, ingrediente_id)
);

-- SESSIONI_RICETTE
DROP TABLE IF EXISTS sessioni_ricette CASCADE;
CREATE TABLE sessioni_ricette (
    id SERIAL PRIMARY KEY,
    sessione_id INTEGER REFERENCES sessioni(id) ON DELETE CASCADE,
    ricetta_id INTEGER REFERENCES ricette(id),
    ordine_esecuzione INTEGER DEFAULT 1,
    note TEXT,
    CONSTRAINT sessioni_ricette_sessione_id_ricetta_id_key UNIQUE (sessione_id, ricetta_id)
);

-- (Tabella notifiche rimossa nello schema attuale – non ricreata)

\echo '-> Tabelle create.'

-- =====================================================
-- 5. CREAZIONE INDICI PER PERFORMANCE
-- =====================================================

\echo '-> Creazione indici base'

-- Indici per performance
CREATE INDEX idx_adesioni_sessione ON adesioni_sessioni USING btree (sessione_id);
CREATE INDEX idx_adesioni_utente ON adesioni_sessioni USING btree (utente_id);
CREATE INDEX idx_corsi_categoria ON corsi USING btree (categoria_id);
CREATE INDEX idx_corsi_chef ON corsi USING btree (chef_id);
CREATE INDEX idx_iscrizioni_stato ON iscrizioni USING btree (stato);
CREATE INDEX idx_sessioni_corso ON sessioni USING btree (corso_id);
CREATE INDEX idx_iscrizioni_data ON iscrizioni USING btree (data_iscrizione);
CREATE INDEX idx_iscrizioni_utente_corso ON iscrizioni USING btree (utente_id, corso_id);
CREATE INDEX idx_log_iscrizioni_iscrizione_id ON log_iscrizioni USING btree (iscrizione_id);
CREATE INDEX idx_log_iscrizioni_timestamp ON log_iscrizioni USING btree (timestamp_modifica);

\echo 'Indici creati!'

-- =====================================================
-- 6. RESET SEQUENZE
-- =====================================================

\echo '-> Reset sequenze'

SELECT setval('adesioni_sessioni_id_seq', 1, false);
SELECT setval('categorie_corsi_id_seq', 1, false);
SELECT setval('chef_id_seq', 1, false);
SELECT setval('corsi_id_seq', 1, false);
SELECT setval('ingredienti_id_seq', 1, false);
SELECT setval('iscrizioni_id_seq', 1, false);
SELECT setval('ricette_id_seq', 1, false);
SELECT setval('ricette_ingredienti_id_seq', 1, false);
SELECT setval('sessioni_id_seq', 1, false);
SELECT setval('sessioni_ricette_id_seq', 1, false);
SELECT setval('utenti_id_seq', 1, false);

\echo 'Sequenze resettate!'

-- =====================================================
-- 7. FUNZIONI, TRIGGERS E VIEW AVANZATE
-- =====================================================

\echo 'Implementazione funzioni, triggers e view avanzate...'

-- =====================================================
-- 7.1. FUNZIONI PERSONALIZZATE
-- =====================================================

-- Funzione per calcolare l'età di un utente
CREATE OR REPLACE FUNCTION calcola_eta(data_nascita DATE)
RETURNS INTEGER AS $$
BEGIN
    IF data_nascita IS NULL THEN
        RETURN NULL;
    END IF;
    
    RETURN EXTRACT(YEAR FROM AGE(CURRENT_DATE, data_nascita));
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Funzione per validare l'email
CREATE OR REPLACE FUNCTION valida_email(email TEXT)
RETURNS BOOLEAN AS $$
BEGIN
    RETURN email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$';
END;
$$ LANGUAGE plpgsql IMMUTABLE;

-- Funzione per calcolare il prezzo totale di un corso con sconti
CREATE OR REPLACE FUNCTION calcola_prezzo_scontato(corso_id INTEGER, sconto_percentuale NUMERIC DEFAULT 0)
RETURNS NUMERIC AS $$
DECLARE
    prezzo_base NUMERIC;
    prezzo_finale NUMERIC;
BEGIN
    SELECT prezzo INTO prezzo_base 
    FROM corsi 
    WHERE id = corso_id;
    
    IF prezzo_base IS NULL THEN
        RETURN NULL;
    END IF;
    
    prezzo_finale := prezzo_base * (1 - sconto_percentuale / 100);
    
    RETURN ROUND(prezzo_finale, 2);
END;
$$ LANGUAGE plpgsql STABLE;

-- Funzione per generare codice iscrizione unico
CREATE OR REPLACE FUNCTION genera_codice_iscrizione()
RETURNS TEXT AS $$
DECLARE
    codice TEXT;
    esiste BOOLEAN;
BEGIN
    LOOP
        codice := 'UFL' || TO_CHAR(CURRENT_DATE, 'YYYY') || 
                  LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0');
        
        SELECT EXISTS(SELECT 1 FROM iscrizioni WHERE codice_iscrizione = codice) INTO esiste;
        \echo '-> Sequenze resettate.'

        \echo '==> STRUTTURA COMPLETATA.'

-- Trigger per log delle modifiche iscrizioni
CREATE TABLE IF NOT EXISTS log_iscrizioni (
    id SERIAL PRIMARY KEY,
    iscrizione_id INTEGER,
    azione VARCHAR(20),
    stato_precedente VARCHAR(20),
    stato_nuovo VARCHAR(20),
    utente_modifica VARCHAR(100),
    timestamp_modifica TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note TEXT
);

CREATE OR REPLACE FUNCTION trigger_log_iscrizioni()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO log_iscrizioni (iscrizione_id, azione, stato_nuovo, utente_modifica, note)
        VALUES (NEW.id, 'INSERT', NEW.stato, current_user, 'Nuova iscrizione creata');
        RETURN NEW;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.stato != NEW.stato THEN
            INSERT INTO log_iscrizioni (iscrizione_id, azione, stato_precedente, stato_nuovo, utente_modifica, note)
            VALUES (NEW.id, 'UPDATE', OLD.stato, NEW.stato, current_user, 'Cambio stato iscrizione');
        END IF;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO log_iscrizioni (iscrizione_id, azione, stato_precedente, utente_modifica, note)
        VALUES (OLD.id, 'DELETE', OLD.stato, current_user, 'Iscrizione eliminata');
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_log_iscrizioni ON iscrizioni;
CREATE TRIGGER trigger_log_iscrizioni
    AFTER INSERT OR UPDATE OR DELETE ON iscrizioni
    FOR EACH ROW EXECUTE FUNCTION trigger_log_iscrizioni();

-- =====================================================
-- 7.3. VIEW AVANZATE
-- =====================================================

-- View per dashboard amministratore
CREATE OR REPLACE VIEW dashboard_admin AS
SELECT 
    'Utenti Totali' as metrica,
    COUNT(*)::TEXT as valore,
    'success' as tipo
FROM utenti WHERE attivo = TRUE
UNION ALL
SELECT 
    'Corsi Attivi' as metrica,
    COUNT(*)::TEXT as valore,
    'primary' as tipo
FROM corsi WHERE data_inizio >= CURRENT_DATE
UNION ALL
SELECT 
    'Iscrizioni Attive' as metrica,
    COUNT(*)::TEXT as valore,
    'info' as tipo
FROM iscrizioni WHERE stato = 'ATTIVA'
UNION ALL
SELECT 
    'Ricavo Mensile' as metrica,
    '€ ' || COALESCE(SUM(c.prezzo), 0)::TEXT as valore,
    'warning' as tipo
FROM iscrizioni i
JOIN corsi c ON i.corso_id = c.id
WHERE i.stato IN ('ATTIVA', 'COMPLETATA')
AND EXTRACT(MONTH FROM i.data_iscrizione) = EXTRACT(MONTH FROM CURRENT_DATE)
AND EXTRACT(YEAR FROM i.data_iscrizione) = EXTRACT(YEAR FROM CURRENT_DATE);

-- View dettagliata corsi con statistiche
CREATE OR REPLACE VIEW corsi_dettaglio AS
SELECT 
    c.id,
    c.titolo,
    c.descrizione,
    c.data_inizio,
    c.frequenza,
    c.numero_sessioni,
    c.prezzo,
    ch.nome || ' ' || ch.cognome as chef_nome,
    ch.specializzazione as chef_specializzazione,
    cat.nome as categoria,
    COUNT(DISTINCT i.id) FILTER (WHERE i.stato = 'ATTIVA') as iscritti_attivi,
    COUNT(DISTINCT i.id) FILTER (WHERE i.stato = 'COMPLETATA') as iscritti_completati,
    COUNT(DISTINCT i.id) FILTER (WHERE i.stato = 'ANNULLATA') as iscritti_annullati,
    COUNT(DISTINCT s.id) as numero_sessioni_programmate,
    verifica_posti_disponibili(c.id) as posti_disponibili,
    CASE 
        WHEN c.data_inizio > CURRENT_DATE THEN 'Programmato'
        WHEN c.data_inizio <= CURRENT_DATE THEN 'In Corso'
        ELSE 'Completato'
    END as stato_corso,
    c.prezzo * COUNT(DISTINCT i.id) FILTER (WHERE i.stato IN ('ATTIVA', 'COMPLETATA')) as ricavo_corso
FROM corsi c
LEFT JOIN chef ch ON c.chef_id = ch.id
LEFT JOIN categorie_corsi cat ON c.categoria_id = cat.id
LEFT JOIN iscrizioni i ON c.id = i.corso_id
LEFT JOIN sessioni s ON c.id = s.corso_id
GROUP BY c.id, ch.nome, ch.cognome, ch.specializzazione, cat.nome
ORDER BY c.data_inizio DESC;

-- View per iscrizioni complete
CREATE OR REPLACE VIEW iscrizioni_complete AS
SELECT 
    i.id,
    i.codice_iscrizione,
    i.data_iscrizione,
    i.stato,
    i.note,
    u.nome || ' ' || u.cognome as utente_nome_completo,
    u.email as utente_email,
    u.telefono as utente_telefono,
    calcola_eta(u.data_nascita) as utente_eta,
    u.livello_esperienza,
    c.titolo as corso_titolo,
    c.data_inizio as corso_data_inizio,
    c.prezzo as corso_prezzo,
    ch.nome || ' ' || ch.cognome as chef_nome,
    cat.nome as categoria_corso,
    EXTRACT(DAYS FROM (c.data_inizio - i.data_iscrizione)) as giorni_anticipo_iscrizione
FROM iscrizioni i
JOIN utenti u ON i.utente_id = u.id
JOIN corsi c ON i.corso_id = c.id
JOIN chef ch ON c.chef_id = ch.id
LEFT JOIN categorie_corsi cat ON c.categoria_id = cat.id
ORDER BY i.data_iscrizione DESC;

-- View per reporting chef
CREATE OR REPLACE VIEW report_chef AS
SELECT 
    ch.id as chef_id,
    ch.nome || ' ' || ch.cognome as chef_nome,
    ch.specializzazione,
    COUNT(DISTINCT c.id) as corsi_totali,
    COUNT(DISTINCT c.id) FILTER (WHERE c.data_inizio >= CURRENT_DATE) as corsi_futuri,
    COUNT(DISTINCT i.id) as iscrizioni_totali,
    COUNT(DISTINCT i.id) FILTER (WHERE i.stato = 'ATTIVA') as iscrizioni_attive,
    COUNT(DISTINCT i.id) FILTER (WHERE i.stato = 'COMPLETATA') as iscrizioni_completate,
    ROUND(
        CASE 
            WHEN COUNT(DISTINCT i.id) > 0 THEN
                COUNT(DISTINCT i.id) FILTER (WHERE i.stato = 'COMPLETATA')::NUMERIC / 
                COUNT(DISTINCT i.id) * 100
            ELSE 0
        END, 2
    ) as tasso_completamento_percentuale,
    SUM(c.prezzo * (SELECT COUNT(*) FROM iscrizioni WHERE corso_id = c.id AND stato IN ('ATTIVA', 'COMPLETATA'))) as ricavo_totale,
    AVG(c.prezzo) as prezzo_medio_corsi
FROM chef ch
LEFT JOIN corsi c ON ch.id = c.chef_id
LEFT JOIN iscrizioni i ON c.id = i.corso_id
GROUP BY ch.id, ch.nome, ch.cognome, ch.specializzazione
ORDER BY ricavo_totale DESC NULLS LAST;

-- View per analisi temporale iscrizioni
CREATE OR REPLACE VIEW analisi_iscrizioni_mensili AS
SELECT 
    EXTRACT(YEAR FROM data_iscrizione) as anno,
    EXTRACT(MONTH FROM data_iscrizione) as mese,
    TO_CHAR(data_iscrizione, 'Month YYYY') as periodo,
    COUNT(*) as totale_iscrizioni,
    COUNT(*) FILTER (WHERE stato = 'ATTIVA') as iscrizioni_attive,
    COUNT(*) FILTER (WHERE stato = 'COMPLETATA') as iscrizioni_completate,
    COUNT(*) FILTER (WHERE stato = 'ANNULLATA') as iscrizioni_annullate,
    SUM(
        (SELECT prezzo FROM corsi WHERE id = corso_id)
    ) FILTER (WHERE stato IN ('ATTIVA', 'COMPLETATA')) as ricavo_mensile
FROM iscrizioni
GROUP BY EXTRACT(YEAR FROM data_iscrizione), EXTRACT(MONTH FROM data_iscrizione)
ORDER BY anno DESC, mese DESC;

-- =====================================================
-- 7.4. INDICI PER PERFORMANCE
-- =====================================================

\echo 'Creazione indici per performance...'

CREATE INDEX IF NOT EXISTS idx_iscrizioni_stato ON iscrizioni(stato);
CREATE INDEX IF NOT EXISTS idx_iscrizioni_data ON iscrizioni(data_iscrizione);
CREATE INDEX IF NOT EXISTS idx_iscrizioni_utente_corso ON iscrizioni(utente_id, corso_id);
CREATE INDEX IF NOT EXISTS idx_corsi_data_inizio ON corsi(data_inizio);
CREATE INDEX IF NOT EXISTS idx_corsi_chef ON corsi(chef_id);
CREATE INDEX IF NOT EXISTS idx_corsi_categoria ON corsi(categoria_id);
CREATE INDEX IF NOT EXISTS idx_sessioni_corso ON sessioni(corso_id);
CREATE INDEX IF NOT EXISTS idx_utenti_email ON utenti(email);
CREATE INDEX IF NOT EXISTS idx_utenti_attivo ON utenti(attivo);
CREATE INDEX IF NOT EXISTS idx_log_iscrizioni_iscrizione_id ON log_iscrizioni(iscrizione_id);
CREATE INDEX IF NOT EXISTS idx_log_iscrizioni_timestamp ON log_iscrizioni(timestamp_modifica);

-- =====================================================
-- 8. VERIFICA FINALE STRUTTURA DATABASE
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
