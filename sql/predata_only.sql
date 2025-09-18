-- =====================================================================
-- UninaFoodLab - Dati di Test (PRE-DATA ONLY)
-- Basato su schema del 18/09/2025 (PostgreSQL 17.5)
-- Contenuto: SOLO dati iniziali per sviluppo/test.
--   Nessuna definizione di tabelle, funzioni, viste o trigger.
--   Allineato alle nuove colonne (corsi: durata_corso, max_partecipanti; utenti senza username/password).
--   Escluse tabelle obsolete: notifiche, iscrizioni_corsi.
-- Ordine di caricamento: categorie_corsi -> chef -> utenti -> corsi -> ingredienti -> ricette -> ricette_ingredienti -> sessioni -> iscrizioni -> adesioni_sessioni -> sessioni_ricette -> log_iscrizioni.
-- NOTE: I valori di durata_corso derivano da numero_sessioni (troncati a 8 se superiori). max_partecipanti impostato a valori plausibili.
-- =====================================================================

SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;

-- =====================================================================
-- CATEGORIE CORSI
-- =====================================================================


--
-- Data for Name: categorie_corsi; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.categorie_corsi (id, nome, descrizione) FROM stdin;
1	Cucina Asiatica	Corsi dedicati alla cucina orientale: sushi, ramen, curry
2	Pasticceria	Arte della preparazione di dolci, torte e dessert
3	Panificazione	Tecniche di preparazione di pane, pizza e lievitati
4	Cucina Italiana	Tradizioni culinarie regionali italiane
5	Cucina Vegana	Cucina a base vegetale, senza derivati animali
6	Cucina Molecolare	Tecniche moderne e innovative di cucina
\.


--
-- Data for Name: chef; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.chef (id, username, password, nome, cognome, email, specializzazione, created_at, modified_at) FROM stdin;
1	chef_mario	password123	Mario	Rossi	mario.rossi@uninafoodlab.it	Cucina Italiana	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
2	chef_yuki	sushi2024	Yuki	Tanaka	yuki.tanaka@uninafoodlab.it	Cucina Asiatica	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
3	chef_pierre	baguette456	Pierre	Dubois	pierre.dubois@uninafoodlab.it	Pasticceria	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
4	chef_anna	vegan789	Anna	Verdi	anna.verdi@uninafoodlab.it	Cucina Vegana	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
\.

-- =====================================================================
-- UTENTI (schema aggiornato: senza username / password)
-- =====================================================================
COPY public.utenti (id, nome, cognome, email, telefono, created_at, data_nascita, livello_esperienza, attivo, modified_at) FROM stdin;
1	Marco	Bianchi	marco.bianchi@email.it	3331234567	2025-06-10 17:22:45.790624	1990-05-15	PRINCIPIANTE	t	2025-09-10 13:43:42.2589
2	Laura	Neri	laura.neri@email.it	3337654321	2025-06-10 17:22:45.790624	1985-08-22	INTERMEDIO	t	2025-09-10 13:43:42.2589
3	Giuseppe	Romano	giuseppe.romano@email.it	3339876543	2025-06-10 17:22:45.790624	1995-12-03	AVANZATO	t	2025-09-10 13:43:42.2589
\.


--
-- Data for Name: corsi; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- =====================================================================
-- CORSI (nuove colonne: durata_corso, max_partecipanti)
-- durata_corso derivata da numero_sessioni (max 8)
-- =====================================================================
COPY public.corsi (id, chef_id, categoria_id, titolo, descrizione, data_inizio, frequenza, numero_sessioni, prezzo, created_at, modified_at, durata_corso, max_partecipanti) FROM stdin;
4	1	1	a	a	2025-09-17	settimanale	1	1.00	2025-09-10 14:05:40.717577	2025-09-10 14:05:40.717577	1	10
1	1	4	Corso Base di Cucina Italiana	Impara i fondamenti della cucina italiana tradizionale	2025-01-20	settimanale	8	299.00	2025-06-10 17:22:45.790624	2025-09-10 14:14:48.318327	8	20
2	2	1	Sushi e Cucina Giapponese	Tecniche tradizionali per la preparazione del sushi	2025-02-15	ogni_due_giorni	6	399.00	2025-06-10 17:22:45.790624	2025-09-10 14:14:48.319822	6	18
3	3	2	Pasticceria Francese Avanzata	Dolci e dessert della tradizione francese	2025-03-01	settimanale	10	599.00	2025-06-10 17:22:45.790624	2025-09-10 14:14:48.32021	8	15
\.


--
-- Data for Name: ingredienti; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- =====================================================================
-- INGREDIENTI
-- =====================================================================
COPY public.ingredienti (id, nome, categoria, unita_misura, costo_unitario, created_at) FROM stdin;
1	Farina 00	cereali	kg	1.20	2025-06-10 17:22:45.790624
2	Pomodori San Marzano	verdura	kg	3.50	2025-06-10 17:22:45.790624
3	Mozzarella di Bufala	latticini	kg	12.00	2025-06-10 17:22:45.790624
4	Salmone	pesce	kg	25.00	2025-06-10 17:22:45.790624
5	Riso per Sushi	cereali	kg	4.50	2025-06-10 17:22:45.790624
6	Alga Nori	alghe	confezione	8.00	2025-06-10 17:22:45.790624
7	Burro	latticini	kg	6.00	2025-06-10 17:22:45.790624
8	Uova	proteine	dozzina	3.00	2025-06-10 17:22:45.790624
9	Zucchero	dolcificanti	kg	1.00	2025-06-10 17:22:45.790624
10	Latte di Mandorla	vegetale	litro	2.50	2025-06-10 17:22:45.790624
\.


--
-- Data for Name: iscrizioni; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- =====================================================================
-- ISCRIZIONI (codice_iscrizione lasciato NULL per generazione trigger)
-- =====================================================================
COPY public.iscrizioni (id, utente_id, corso_id, data_iscrizione, stato, note, modified_at, codice_iscrizione) FROM stdin;
3	1	2	2025-06-10 21:21:56.134502	ATTIVA	Iscrizione di test automatica	2025-09-10 13:43:42.2589	\N
1	1	1	2025-01-15 10:00:00	COMPLETATA	Primo corso completato con successo	2025-09-10 18:01:58.240923	\N
2	2	1	2025-03-10 14:30:00	COMPLETATA	Ottima esperienza culinaria	2025-09-10 18:01:58.247124	\N
\.


--
-- Data for Name: iscrizioni_corsi; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- (Tabella iscrizioni_corsi rimossa dallo schema)


--
-- Data for Name: log_iscrizioni; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.log_iscrizioni (id, iscrizione_id, azione, stato_precedente, stato_nuovo, utente_modifica, timestamp_modifica, note) FROM stdin;
1	1	UPDATE	ATTIVA	COMPLETATA	postgres	2025-09-10 18:01:58.240923	Cambio stato iscrizione
2	2	UPDATE	ATTIVA	COMPLETATA	postgres	2025-09-10 18:01:58.247124	Cambio stato iscrizione
\.


--
-- Data for Name: notifiche; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- (Tabella notifiche rimossa dallo schema)


--
-- Data for Name: ricette; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- =====================================================================
-- RICETTE
-- =====================================================================
COPY public.ricette (id, chef_id, nome, descrizione, difficolta, tempo_preparazione, numero_porzioni, istruzioni, created_at) FROM stdin;
1	1	Pizza Margherita	Classica pizza napoletana con pomodoro e mozzarella	3	180	4	Preparare l'impasto, stendere, aggiungere condimenti e cuocere in forno	2025-06-10 17:22:45.790624
2	2	Sushi Maki	Rotolini di sushi con salmone e cetriolo	4	45	6	Preparare il riso, stendere su nori, aggiungere ingredienti e arrotolare	2025-06-10 17:22:45.790624
3	3	Croissant	Cornetti francesi sfogliati	5	240	8	Preparare pasta sfoglia, dare forma e cuocere	2025-06-10 17:22:45.790624
4	4	Buddha Bowl Vegano	Ciotola completa con quinoa e verdure	2	30	2	Cuocere quinoa, preparare verdure e comporre la bowl	2025-06-10 17:22:45.790624
\.


--
-- Data for Name: ricette_ingredienti; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- =====================================================================
-- RICETTE_INGREDIENTI
-- =====================================================================
COPY public.ricette_ingredienti (id, ricetta_id, ingrediente_id, quantita, note) FROM stdin;
1	1	1	0.50	Per l'impasto
2	1	2	0.30	Per il sugo
3	1	3	0.25	Per la farcitura
4	2	5	0.30	Per la base
5	2	4	0.20	Per il ripieno
6	2	6	2.00	Per avvolgere
7	3	1	0.50	Per la pasta
8	3	7	0.30	Per la sfogliatura
9	3	8	3.00	Per l'impasto
\.


--
-- Data for Name: sessioni; Type: TABLE DATA; Schema: public; Owner: postgres
--

-- =====================================================================
-- SESSIONI
-- =====================================================================
COPY public.sessioni (id, corso_id, numero_sessione, data_sessione, tipo, titolo, descrizione, durata_minuti, created_at, modified_at) FROM stdin;
1	1	1	2025-07-01	presenza	Introduzione e Pasta Fresca	Prima lezione pratica sulla pasta fatta in casa	120	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
2	1	2	2025-07-08	online	Storia della Cucina Italiana	Lezione teorica sulle origini regionali	120	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
3	1	3	2025-07-15	presenza	Pizza e Lievitati	Tecniche di panificazione e pizza napoletana	120	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
4	2	1	2025-07-15	presenza	Preparazione del Riso	Basi del riso per sushi	120	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
5	2	2	2025-07-17	presenza	Sushi Maki e Nigiri	Tecniche di preparazione avanzate	120	2025-06-10 17:22:45.790624	2025-09-10 13:43:42.2589
6	4	1	2025-09-17	presenza	Sessione Pratica 1	Sessione pratica con preparazione di ricette	120	2025-09-10 14:05:40.721194	2025-09-10 14:05:40.721194
\.

-- =====================================================================
-- ADESIONI_SESSIONI
-- =====================================================================
COPY public.adesioni_sessioni (id, utente_id, sessione_id, data_adesione, confermata, note) FROM stdin;
1	1	1	2025-06-10 17:22:45.790624	t	\N
2	2	1	2025-06-10 17:22:45.790624	t	\N
3	1	3	2025-06-10 17:22:45.790624	f	\N
4	3	4	2025-06-10 17:22:45.790624	t	\N
5	1	5	2025-06-10 17:22:45.790624	t	\N
\.

-- =====================================================================
-- SESSIONI_RICETTE
-- (dati di associazione sessione-ricetta; esempi minimi per consistenza)
-- =====================================================================
COPY public.sessioni_ricette (id, sessione_id, ricetta_id, ordine_esecuzione, note) FROM stdin;
1	1	1	1	\N
2	3	1	1	\N
3	4	2	1	\N
4	5	2	1	\N
12	2	3	1	\N
13	3	4	1	\N
17	6	1	1	\N
\.

-- =====================================================================
-- (FINE DATI DI TEST)
-- =====================================================================
