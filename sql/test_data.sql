-- SCRIPT DATI DI TEST PER DATABASE UNINAFOODLAB (PostgreSQL)
-- Basato sul dump del 24/06/2025
-- Questo script inserisce dati di test nel database

-- =====================================================
-- 1. CONFIGURAZIONE INIZIALE
-- =====================================================

\echo 'INSERIMENTO DATI DI TEST UNINAFOODLAB...'

-- =====================================================
-- 2. INSERIMENTO CATEGORIE CORSI
-- =====================================================

\echo 'Inserimento categorie corsi...'

INSERT INTO categorie_corsi (nome, descrizione) VALUES
    ('Cucina Asiatica', 'Corsi dedicati alla cucina orientale: sushi, ramen, curry'),
    ('Pasticceria', 'Arte della preparazione di dolci, torte e dessert'),
    ('Panificazione', 'Tecniche di preparazione di pane, pizza e lievitati'),
    ('Cucina Italiana', 'Tradizioni culinarie regionali italiane'),
    ('Cucina Vegana', 'Cucina a base vegetale, senza derivati animali'),
    ('Cucina Molecolare', 'Tecniche moderne e innovative di cucina')
ON CONFLICT (nome) DO NOTHING;

-- =====================================================
-- 3. INSERIMENTO CHEF
-- =====================================================

\echo 'Inserimento chef...'

INSERT INTO chef (username, password, nome, cognome, email, specializzazione) VALUES
    ('chef_mario', 'password123', 'Mario', 'Rossi', 'mario.rossi@uninafoodlab.it', 'Cucina Italiana'),
    ('chef_yuki', 'sushi2024', 'Yuki', 'Tanaka', 'yuki.tanaka@uninafoodlab.it', 'Cucina Asiatica'),
    ('chef_pierre', 'baguette456', 'Pierre', 'Dubois', 'pierre.dubois@uninafoodlab.it', 'Pasticceria'),
    ('chef_anna', 'vegan789', 'Anna', 'Verdi', 'anna.verdi@uninafoodlab.it', 'Cucina Vegana')
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- 4. INSERIMENTO UTENTI
-- =====================================================

\echo 'Inserimento utenti...'

INSERT INTO utenti (username, password, nome, cognome, email, telefono, data_nascita, livello_esperienza, attivo) VALUES
    ('user1', 'pass123', 'Marco', 'Bianchi', 'marco.bianchi@email.it', '3331234567', '1990-05-15', 'PRINCIPIANTE', TRUE),
    ('user2', 'pass456', 'Laura', 'Neri', 'laura.neri@email.it', '3337654321', '1985-08-22', 'INTERMEDIO', TRUE),
    ('user3', 'pass789', 'Giuseppe', 'Romano', 'giuseppe.romano@email.it', '3339876543', '1995-12-03', 'AVANZATO', TRUE),
    ('user4', 'pass111', 'Francesca', 'Esposito', 'francesca.esposito@email.it', '3334567890', '1992-03-10', 'PRINCIPIANTE', TRUE),
    ('user5', 'pass222', 'Antonio', 'Ricci', 'antonio.ricci@email.it', '3338901234', '1988-11-25', 'INTERMEDIO', TRUE)
ON CONFLICT (username) DO NOTHING;

-- =====================================================
-- 5. INSERIMENTO INGREDIENTI
-- =====================================================

\echo 'Inserimento ingredienti...'

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
    ('Latte di Mandorla', 'vegetale', 'litro', 2.50),
    ('Olio EVO', 'condimenti', 'litro', 8.00),
    ('Parmigiano Reggiano', 'latticini', 'kg', 30.00),
    ('Basilico', 'erbe', 'mazzo', 1.50),
    ('Aglio', 'verdura', 'kg', 2.00),
    ('Tonno', 'pesce', 'kg', 18.00)
ON CONFLICT (nome) DO NOTHING;

-- =====================================================
-- 6. INSERIMENTO CORSI
-- =====================================================

\echo 'Inserimento corsi...'

INSERT INTO corsi (chef_id, categoria_id, titolo, descrizione, data_inizio, frequenza, numero_sessioni, prezzo) VALUES
    (1, 4, 'Corso Base di Cucina Italiana', 'Impara i fondamenti della cucina italiana tradizionale', '2025-07-01', 'settimanale', 8, 299.00),
    (2, 1, 'Sushi e Cucina Giapponese', 'Tecniche tradizionali per la preparazione del sushi', '2025-07-15', 'ogni_due_giorni', 6, 399.00),
    (3, 2, 'Pasticceria Francese Avanzata', 'Dolci e dessert della tradizione francese', '2025-08-01', 'settimanale', 10, 599.00),
    (4, 5, 'Cucina Vegana Creativa', 'Piatti vegani gustosi e creativi', '2025-08-15', 'settimanale', 6, 249.00),
    (1, 3, 'Panificazione Artigianale', 'Tecniche per pane e pizza fatti in casa', '2025-09-01', 'bisettimanale', 8, 349.00);

-- =====================================================
-- 7. INSERIMENTO RICETTE
-- =====================================================

\echo 'Inserimento ricette...'

INSERT INTO ricette (chef_id, nome, descrizione, difficolta, tempo_preparazione, numero_porzioni, istruzioni) VALUES
    (1, 'Pizza Margherita', 'Classica pizza napoletana con pomodoro e mozzarella', 3, 180, 4, 'Preparare l''impasto, stendere, aggiungere condimenti e cuocere in forno'),
    (2, 'Sushi Maki', 'Rotolini di sushi con salmone e cetriolo', 4, 45, 6, 'Preparare il riso, stendere su nori, aggiungere ingredienti e arrotolare'),
    (3, 'Croissant', 'Cornetti francesi sfogliati', 5, 240, 8, 'Preparare pasta sfoglia, dare forma e cuocere'),
    (4, 'Buddha Bowl Vegano', 'Ciotola completa con quinoa e verdure', 2, 30, 2, 'Cuocere quinoa, preparare verdure e comporre la bowl'),
    (1, 'Pasta Carbonara', 'Classica pasta romana con uova e guanciale', 3, 25, 4, 'Cuocere pasta, preparare crema con uova e pecorino, manteccare'),
    (2, 'Ramen Tradizionale', 'Zuppa giapponese con noodles e brodo', 4, 120, 2, 'Preparare brodo, cuocere noodles, assemblare con condimenti'),
    (3, 'Tiramisù', 'Dolce italiano con caffè e mascarpone', 2, 30, 8, 'Alternare savoiardi e crema al mascarpone, lasciare riposare'),
    (4, 'Burger Vegano', 'Hamburger con polpetta di legumi', 3, 40, 4, 'Preparare polpette, grigliare, assemblare burger');

-- =====================================================
-- 8. INSERIMENTO SESSIONI
-- =====================================================

\echo 'Inserimento sessioni...'

INSERT INTO sessioni (corso_id, numero_sessione, data_sessione, tipo, titolo, descrizione, durata_minuti) VALUES
    -- Corso Base di Cucina Italiana
    (1, 1, '2025-07-01', 'presenza', 'Introduzione e Pasta Fresca', 'Prima lezione pratica sulla pasta fatta in casa', 120),
    (1, 2, '2025-07-08', 'online', 'Storia della Cucina Italiana', 'Lezione teorica sulle origini regionali', 120),
    (1, 3, '2025-07-15', 'presenza', 'Pizza e Lievitati', 'Tecniche di panificazione e pizza napoletana', 120),
    (1, 4, '2025-07-22', 'presenza', 'Sughi e Condimenti', 'Preparazione di sughi classici italiani', 120),
    
    -- Sushi e Cucina Giapponese
    (2, 1, '2025-07-15', 'presenza', 'Preparazione del Riso', 'Basi del riso per sushi', 120),
    (2, 2, '2025-07-17', 'presenza', 'Sushi Maki e Nigiri', 'Tecniche di preparazione avanzate', 120),
    (2, 3, '2025-07-19', 'presenza', 'Sashimi e Presentazione', 'Taglio del pesce e presentazione', 120),
    
    -- Pasticceria Francese
    (3, 1, '2025-08-01', 'presenza', 'Pasta Sfoglia', 'Tecniche base della sfoglia', 150),
    (3, 2, '2025-08-08', 'presenza', 'Croissant e Dolci Sfogliati', 'Preparazione croissant e pain au chocolat', 150),
    
    -- Cucina Vegana
    (4, 1, '2025-08-15', 'presenza', 'Proteine Vegetali', 'Legumi e alternative alla carne', 120),
    (4, 2, '2025-08-22', 'presenza', 'Buddha Bowl e Insalate', 'Composizione di piatti completi', 120);

-- =====================================================
-- 9. INSERIMENTO RICETTE-INGREDIENTI
-- =====================================================

\echo 'Inserimento ricette-ingredienti...'

INSERT INTO ricette_ingredienti (ricetta_id, ingrediente_id, quantita, note) VALUES
    -- Pizza Margherita
    (1, 1, 0.50, 'Per l''impasto'),
    (1, 2, 0.30, 'Per il sugo'),
    (1, 3, 0.25, 'Per la farcitura'),
    (1, 11, 0.05, 'Per condire'),
    (1, 13, 0.02, 'Per aromatizzare'),
    
    -- Sushi Maki
    (2, 5, 0.30, 'Per la base'),
    (2, 4, 0.20, 'Per il ripieno'),
    (2, 6, 2.00, 'Per avvolgere'),
    
    -- Croissant
    (3, 1, 0.50, 'Per la pasta'),
    (3, 7, 0.30, 'Per la sfogliatura'),
    (3, 8, 3.00, 'Per l''impasto'),
    
    -- Buddha Bowl
    (4, 10, 0.20, 'Come base liquida'),
    
    -- Carbonara
    (5, 1, 0.40, 'Pasta'),
    (5, 8, 4.00, 'Per la crema'),
    (5, 12, 0.10, 'Formaggio'),
    
    -- Tiramisù
    (7, 8, 6.00, 'Per la crema'),
    (7, 9, 0.15, 'Per dolcificare');

-- =====================================================
-- 10. INSERIMENTO SESSIONI-RICETTE
-- =====================================================

\echo 'Inserimento sessioni-ricette...'

INSERT INTO sessioni_ricette (sessione_id, ricetta_id, ordine_esecuzione, note) VALUES
    (1, 1, 1, 'Ricetta principale della lezione'),
    (1, 5, 2, 'Ricetta aggiuntiva per confronto'),
    (3, 1, 1, 'Focus sulla pizza napoletana'),
    (5, 2, 1, 'Preparazione maki classici'),
    (6, 2, 1, 'Perfezionamento tecnica maki'),
    (8, 3, 1, 'Croissant base'),
    (10, 4, 1, 'Bowl vegano completo'),
    (11, 8, 1, 'Burger plant-based');

-- =====================================================
-- 11. INSERIMENTO ISCRIZIONI
-- =====================================================

\echo 'Inserimento iscrizioni...'

INSERT INTO iscrizioni (utente_id, corso_id, stato, note) VALUES
    (1, 1, 'ATTIVA', 'Iscrizione regolare'),
    (2, 1, 'ATTIVA', 'Iscrizione regolare'),
    (3, 2, 'ATTIVA', 'Interesse per cucina asiatica'),
    (1, 2, 'ATTIVA', 'Secondo corso frequentato'),
    (4, 3, 'ATTIVA', 'Prima esperienza in pasticceria'),
    (5, 4, 'ATTIVA', 'Dieta vegana'),
    (2, 4, 'COMPLETATA', 'Corso completato con successo'),
    (3, 5, 'ATTIVA', 'Interesse per panificazione'),
    (4, 1, 'ANNULLATA', 'Annullamento per motivi personali');

-- =====================================================
-- 12. INSERIMENTO ADESIONI SESSIONI
-- =====================================================

\echo 'Inserimento adesioni sessioni...'

INSERT INTO adesioni_sessioni (utente_id, sessione_id, confermata, note) VALUES
    (1, 1, TRUE, 'Presente alla lezione'),
    (2, 1, TRUE, 'Presente alla lezione'),
    (1, 2, TRUE, 'Lezione online seguita'),
    (2, 2, FALSE, 'Non ha seguito la lezione online'),
    (1, 3, FALSE, 'Prenotato ma non confermato'),
    (3, 5, TRUE, 'Prima lezione sushi'),
    (1, 5, TRUE, 'Molto interessato al sushi'),
    (3, 6, TRUE, 'Continua il percorso sushi'),
    (4, 8, TRUE, 'Prima lezione pasticceria'),
    (5, 10, TRUE, 'Lezione cucina vegana');

-- =====================================================
-- 13. INSERIMENTO NOTIFICHE DI TEST
-- =====================================================

\echo 'Inserimento notifiche di test...'

INSERT INTO notifiche (corso_id, titolo, messaggio, tipo, inviata) VALUES
    (1, 'Benvenuto nel Corso di Cucina Italiana', 'Benvenuto! La prima lezione si terrà il 1° luglio alle 18:00', 'benvenuto', TRUE),
    (2, 'Materiali per il Corso Sushi', 'Ricorda di portare il grembiule e il coltello per la lezione di domani', 'promemoria', TRUE),
    (3, 'Cambio Orario Lezione', 'La lezione di pasticceria di giovedì è posticipata alle 19:00', 'aggiornamento', FALSE),
    (4, 'Ricette del Corso Vegano', 'Le ricette della scorsa lezione sono disponibili nell''area download', 'informazione', TRUE);

-- =====================================================
-- 14. AGGIORNAMENTO SEQUENZE
-- =====================================================

\echo 'Aggiornamento sequenze...'

SELECT setval('categorie_corsi_id_seq', (SELECT MAX(id) FROM categorie_corsi), true);
SELECT setval('chef_id_seq', (SELECT MAX(id) FROM chef), true);
SELECT setval('utenti_id_seq', (SELECT MAX(id) FROM utenti), true);
SELECT setval('ingredienti_id_seq', (SELECT MAX(id) FROM ingredienti), true);
SELECT setval('corsi_id_seq', (SELECT MAX(id) FROM corsi), true);
SELECT setval('ricette_id_seq', (SELECT MAX(id) FROM ricette), true);
SELECT setval('sessioni_id_seq', (SELECT MAX(id) FROM sessioni), true);
SELECT setval('ricette_ingredienti_id_seq', (SELECT MAX(id) FROM ricette_ingredienti), true);
SELECT setval('sessioni_ricette_id_seq', (SELECT MAX(id) FROM sessioni_ricette), true);
SELECT setval('iscrizioni_id_seq', (SELECT MAX(id) FROM iscrizioni), true);
SELECT setval('adesioni_sessioni_id_seq', (SELECT MAX(id) FROM adesioni_sessioni), true);
SELECT setval('notifiche_id_seq', (SELECT MAX(id) FROM notifiche), true);

-- =====================================================
-- 15. VERIFICA FINALE
-- =====================================================

\echo 'VERIFICA DATI INSERITI:'

-- Conta record nelle tabelle
SELECT 
    'categorie_corsi' as tabella, COUNT(*) as records FROM categorie_corsi
UNION ALL
SELECT 'chef' as tabella, COUNT(*) as records FROM chef
UNION ALL
SELECT 'utenti' as tabella, COUNT(*) as records FROM utenti
UNION ALL
SELECT 'ingredienti' as tabella, COUNT(*) as records FROM ingredienti
UNION ALL
SELECT 'corsi' as tabella, COUNT(*) as records FROM corsi
UNION ALL
SELECT 'ricette' as tabella, COUNT(*) as records FROM ricette
UNION ALL
SELECT 'sessioni' as tabella, COUNT(*) as records FROM sessioni
UNION ALL
SELECT 'ricette_ingredienti' as tabella, COUNT(*) as records FROM ricette_ingredienti
UNION ALL
SELECT 'sessioni_ricette' as tabella, COUNT(*) as records FROM sessioni_ricette
UNION ALL
SELECT 'iscrizioni' as tabella, COUNT(*) as records FROM iscrizioni
UNION ALL
SELECT 'adesioni_sessioni' as tabella, COUNT(*) as records FROM adesioni_sessioni
UNION ALL
SELECT 'notifiche' as tabella, COUNT(*) as records FROM notifiche
ORDER BY tabella;

\echo '';
\echo 'DATI DI TEST INSERITI CON SUCCESSO!'
\echo 'Database pronto per l''uso con dati di esempio.'
\echo 'Totale utenti: 5 | Totale corsi: 5 | Totale iscrizioni: 9';
