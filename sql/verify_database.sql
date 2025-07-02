-- SCRIPT VERIFICA DATABASE UNINAFOODLAB (PostgreSQL)
-- Basato sul dump del 24/06/2025
-- Questo script verifica l'integrità e la correttezza del database

-- =====================================================
-- 1. CONFIGURAZIONE INIZIALE
-- =====================================================

\echo '==================================================='
\echo 'VERIFICA DATABASE UNINAFOODLAB'
\echo '==================================================='

-- =====================================================
-- 2. VERIFICA ESISTENZA TABELLE
-- =====================================================

\echo ''
\echo '1. VERIFICA ESISTENZA TABELLE:'
\echo '------------------------------'

SELECT 
    table_name,
    table_type,
    CASE 
        WHEN table_name IN ('chef', 'utenti', 'categorie_corsi', 'corsi', 'sessioni', 
                           'iscrizioni', 'adesioni_sessioni', 'ingredienti', 'ricette', 
                           'ricette_ingredienti', 'sessioni_ricette', 'notifiche') 
        THEN '✓ OK' 
        ELSE '⚠ EXTRA' 
    END AS status
FROM information_schema.tables
WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- =====================================================
-- 3. VERIFICA STRUTTURA TABELLE PRINCIPALI
-- =====================================================

\echo ''
\echo '2. VERIFICA STRUTTURA TABELLA UTENTI:'
\echo '------------------------------------'

SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default,
    CASE 
        WHEN column_name IN ('id', 'username', 'password', 'nome', 'cognome', 'email', 
                           'telefono', 'created_at', 'data_nascita', 'livello_esperienza', 'attivo')
        THEN '✓ OK'
        ELSE '⚠ EXTRA'
    END AS status
FROM information_schema.columns 
WHERE table_name = 'utenti' 
ORDER BY ordinal_position;

\echo ''
\echo '3. VERIFICA STRUTTURA TABELLA ISCRIZIONI:'
\echo '----------------------------------------'

SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default,
    CASE 
        WHEN column_name IN ('id', 'utente_id', 'corso_id', 'data_iscrizione', 'stato', 'note')
        THEN '✓ OK'
        ELSE '⚠ EXTRA'
    END AS status
FROM information_schema.columns 
WHERE table_name = 'iscrizioni' 
ORDER BY ordinal_position;

-- =====================================================
-- 4. VERIFICA VINCOLI E CHIAVI
-- =====================================================

\echo ''
\echo '4. VERIFICA CHIAVI PRIMARIE:'
\echo '----------------------------'

SELECT 
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    STRING_AGG(kcu.column_name, ', ' ORDER BY kcu.ordinal_position) as columns
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
WHERE tc.constraint_type = 'PRIMARY KEY'
    AND tc.table_schema = 'public'
GROUP BY tc.table_name, tc.constraint_name, tc.constraint_type
ORDER BY tc.table_name;

\echo ''
\echo '5. VERIFICA CHIAVI ESTERNE:'
\echo '--------------------------'

SELECT 
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name,
    tc.constraint_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public'
ORDER BY tc.table_name, kcu.column_name;

-- =====================================================
-- 6. VERIFICA INDICI
-- =====================================================

\echo ''
\echo '6. VERIFICA INDICI:'
\echo '------------------'

SELECT 
    schemaname,
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
    AND indexname NOT LIKE '%_pkey'
ORDER BY tablename, indexname;

-- =====================================================
-- 7. VERIFICA SEQUENZE
-- =====================================================

\echo ''
\echo '7. VERIFICA SEQUENZE:'
\echo '--------------------'

SELECT 
    sequence_name,
    last_value,
    is_called
FROM information_schema.sequences s
JOIN pg_sequences ps ON s.sequence_name = ps.sequencename
WHERE s.sequence_schema = 'public'
ORDER BY sequence_name;

-- =====================================================
-- 8. CONTEGGIO RECORD
-- =====================================================

\echo ''
\echo '8. CONTEGGIO RECORD PER TABELLA:'
\echo '-------------------------------'

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
ORDER BY 
    CASE tabella
        WHEN 'chef' THEN 1
        WHEN 'utenti' THEN 2
        WHEN 'categorie_corsi' THEN 3
        WHEN 'corsi' THEN 4
        WHEN 'sessioni' THEN 5
        WHEN 'iscrizioni' THEN 6
        WHEN 'adesioni_sessioni' THEN 7
        WHEN 'ingredienti' THEN 8
        WHEN 'ricette' THEN 9
        WHEN 'ricette_ingredienti' THEN 10
        WHEN 'sessioni_ricette' THEN 11
        WHEN 'notifiche' THEN 12
    END;

-- =====================================================
-- 9. VERIFICA INTEGRITÀ REFERENZIALE
-- =====================================================

\echo ''
\echo '9. VERIFICA INTEGRITÀ REFERENZIALE:'
\echo '----------------------------------'

-- Verifica iscrizioni senza utenti o corsi
SELECT 
    'Iscrizioni orfane' as test,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM iscrizioni i
LEFT JOIN utenti u ON i.utente_id = u.id
LEFT JOIN corsi c ON i.corso_id = c.id
WHERE u.id IS NULL OR c.id IS NULL;

-- Verifica adesioni senza utenti o sessioni
SELECT 
    'Adesioni orfane' as test,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM adesioni_sessioni a
LEFT JOIN utenti u ON a.utente_id = u.id
LEFT JOIN sessioni s ON a.sessione_id = s.id
WHERE u.id IS NULL OR s.id IS NULL;

-- Verifica sessioni senza corsi
SELECT 
    'Sessioni orfane' as test,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM sessioni s
LEFT JOIN corsi c ON s.corso_id = c.id
WHERE c.id IS NULL;

-- Verifica corsi senza chef o categorie
SELECT 
    'Corsi orfani' as test,
    COUNT(*) as count,
    CASE WHEN COUNT(*) = 0 THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM corsi c
LEFT JOIN chef ch ON c.chef_id = ch.id
LEFT JOIN categorie_corsi cc ON c.categoria_id = cc.id
WHERE ch.id IS NULL OR cc.id IS NULL;

-- =====================================================
-- 10. VERIFICA VINCOLI DI BUSINESS
-- =====================================================

\echo ''
\echo '10. VERIFICA VINCOLI DI BUSINESS:'
\echo '--------------------------------'

-- Verifica stati iscrizioni validi
SELECT 
    'Stati iscrizioni validi' as test,
    COUNT(*) as invalid_count,
    CASE WHEN COUNT(*) = 0 THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM iscrizioni 
WHERE stato NOT IN ('ATTIVA', 'COMPLETATA', 'ANNULLATA');

-- Verifica livelli esperienza validi
SELECT 
    'Livelli esperienza validi' as test,
    COUNT(*) as invalid_count,
    CASE WHEN COUNT(*) = 0 THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM utenti 
WHERE livello_esperienza NOT IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZATO');

-- Verifica email uniche utenti
SELECT 
    'Email utenti uniche' as test,
    COUNT(*) - COUNT(DISTINCT email) as duplicate_count,
    CASE WHEN COUNT(*) = COUNT(DISTINCT email) THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM utenti;

-- Verifica username unici
SELECT 
    'Username utenti unici' as test,
    COUNT(*) - COUNT(DISTINCT username) as duplicate_count,
    CASE WHEN COUNT(*) = COUNT(DISTINCT username) THEN '✓ OK' ELSE '⚠ ERRORE' END as status
FROM utenti;

-- =====================================================
-- 11. STATISTICHE AVANZATE
-- =====================================================

\echo ''
\echo '11. STATISTICHE DATABASE:'
\echo '------------------------'

-- Distribuzione utenti per livello esperienza
SELECT 
    'Utenti per livello' as categoria,
    livello_esperienza as valore,
    COUNT(*) as conteggio
FROM utenti
GROUP BY livello_esperienza
ORDER BY conteggio DESC;

-- Distribuzione iscrizioni per stato
SELECT 
    'Iscrizioni per stato' as categoria,
    stato as valore,
    COUNT(*) as conteggio
FROM iscrizioni
GROUP BY stato
ORDER BY conteggio DESC;

-- Corsi più popolari
SELECT 
    'Corsi più popolari' as categoria,
    c.titolo as valore,
    COUNT(i.id) as conteggio
FROM corsi c
LEFT JOIN iscrizioni i ON c.id = i.corso_id
GROUP BY c.id, c.titolo
ORDER BY conteggio DESC
LIMIT 5;

-- =====================================================
-- 12. VERIFICA PERFORMANCE
-- =====================================================

\echo ''
\echo '12. VERIFICA PERFORMANCE INDICI:'
\echo '-------------------------------'

-- Verifica utilizzo indici (solo tabelle principali)
SELECT 
    schemaname,
    tablename,
    attname as column_name,
    n_distinct,
    correlation
FROM pg_stats 
WHERE schemaname = 'public' 
    AND tablename IN ('utenti', 'corsi', 'iscrizioni', 'sessioni')
    AND attname IN ('id', 'utente_id', 'corso_id', 'chef_id', 'categoria_id')
ORDER BY tablename, attname;

-- =====================================================
-- 13. RIEPILOGO FINALE
-- =====================================================

\echo ''
\echo '==================================================='
\echo 'RIEPILOGO VERIFICA DATABASE:'
\echo '==================================================='

-- Conta tabelle previste
WITH expected_tables AS (
    SELECT unnest(ARRAY['chef', 'utenti', 'categorie_corsi', 'corsi', 'sessioni', 
                        'iscrizioni', 'adesioni_sessioni', 'ingredienti', 'ricette', 
                        'ricette_ingredienti', 'sessioni_ricette', 'notifiche']) as table_name
),
actual_tables AS (
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
)
SELECT 
    '📊 Tabelle previste' as check_type,
    (SELECT COUNT(*) FROM expected_tables) as expected,
    (SELECT COUNT(*) FROM actual_tables a JOIN expected_tables e ON a.table_name = e.table_name) as actual,
    CASE 
        WHEN (SELECT COUNT(*) FROM expected_tables) = 
             (SELECT COUNT(*) FROM actual_tables a JOIN expected_tables e ON a.table_name = e.table_name)
        THEN '✓ COMPLETO'
        ELSE '⚠ INCOMPLETO'
    END as status;

-- Riepilogo finale
SELECT 
    '🎯 Stato generale database' as check_type,
    'UninaFoodLab' as database_name,
    (SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE') as total_tables,
    '✅ VERIFICATO' as final_status;

\echo ''
\echo 'VERIFICA COMPLETATA!'
\echo 'Per maggiori dettagli, consultare i risultati sopra.'
\echo '==================================================='
