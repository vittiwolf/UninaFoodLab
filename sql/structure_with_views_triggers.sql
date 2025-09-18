-- =====================================================================
-- UninaFoodLab - Struttura + Funzioni + View + Trigger (SENZA DATI)
-- Basato sul dump del 18/09/2025 (PostgreSQL 17.5)
-- NOTE:
--  * Non crea il database (gestirlo esternamente se necessario)
--  * Nessun blocco COPY/INSERT o dati
--  * Include funzioni, tabelle, constraint, indici, viste, trigger
--  * Valori SEQUENCE impostati come da ultimo dump (puoi rimuoverli se non necessari)
-- =====================================================================

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

-- Funzioni
CREATE OR REPLACE FUNCTION public.calcola_eta(data_nascita date) RETURNS integer
    LANGUAGE plpgsql IMMUTABLE
AS $$
BEGIN
    IF data_nascita IS NULL THEN
        RETURN NULL;
    END IF;

    

    RETURN EXTRACT(YEAR FROM AGE(CURRENT_DATE, data_nascita));

END;

$$;


ALTER FUNCTION public.calcola_eta(data_nascita date) OWNER TO postgres;

--
-- Name: FUNCTION calcola_eta(data_nascita date); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.calcola_eta(data_nascita date) IS 'Calcola l eta di un utente in base alla data di nascita';


--
-- Name: calcola_prezzo_scontato(integer, numeric); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.calcola_prezzo_scontato(corso_id integer, sconto_percentuale numeric DEFAULT 0) RETURNS numeric
    LANGUAGE plpgsql STABLE
    AS $$

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

$$;


ALTER FUNCTION public.calcola_prezzo_scontato(corso_id integer, sconto_percentuale numeric) OWNER TO postgres;

--
-- Name: FUNCTION calcola_prezzo_scontato(corso_id integer, sconto_percentuale numeric); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.calcola_prezzo_scontato(corso_id integer, sconto_percentuale numeric) IS 'Calcola il prezzo scontato di un corso';


--
-- Name: calcola_statistiche_corso(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.calcola_statistiche_corso(corso_id integer) RETURNS TABLE(totale_iscritti integer, iscritti_attivi integer, iscritti_completati integer, iscritti_annullati integer, tasso_completamento numeric, ricavo_totale numeric)
    LANGUAGE plpgsql STABLE
    AS $$

BEGIN

    RETURN QUERY

    SELECT 

        COUNT(*)::INTEGER as totale_iscritti,

        COUNT(*) FILTER (WHERE stato = 'ATTIVA')::INTEGER as iscritti_attivi,

        COUNT(*) FILTER (WHERE stato = 'COMPLETATA')::INTEGER as iscritti_completati,

        COUNT(*) FILTER (WHERE stato = 'ANNULLATA')::INTEGER as iscritti_annullati,

        CASE 

            WHEN COUNT(*) > 0 THEN 

                ROUND((COUNT(*) FILTER (WHERE stato = 'COMPLETATA')::NUMERIC / COUNT(*)) * 100, 2)

            ELSE 0

        END as tasso_completamento,

        COALESCE(

            (SELECT c.prezzo * COUNT(*) FILTER (WHERE i.stato IN ('ATTIVA', 'COMPLETATA'))

             FROM corsi c 

             WHERE c.id = calcola_statistiche_corso.corso_id), 

            0

        ) as ricavo_totale

    FROM iscrizioni i

    WHERE i.corso_id = calcola_statistiche_corso.corso_id;

END;

$$;


ALTER FUNCTION public.calcola_statistiche_corso(corso_id integer) OWNER TO postgres;

--
-- Name: FUNCTION calcola_statistiche_corso(corso_id integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.calcola_statistiche_corso(corso_id integer) IS 'Calcola statistiche complete per un corso';


--
-- Name: genera_codice_iscrizione(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.genera_codice_iscrizione() RETURNS text
    LANGUAGE plpgsql
    AS $$

DECLARE

    codice TEXT;

    esiste BOOLEAN;

BEGIN

    LOOP

        codice := 'UFL' || TO_CHAR(CURRENT_DATE, 'YYYY') || 

                  LPAD(FLOOR(RANDOM() * 10000)::TEXT, 4, '0');

        

        SELECT EXISTS(SELECT 1 FROM iscrizioni WHERE codice_iscrizione = codice) INTO esiste;

        

        EXIT WHEN NOT esiste;

    END LOOP;

    

    RETURN codice;

END;

$$;


ALTER FUNCTION public.genera_codice_iscrizione() OWNER TO postgres;

--
-- Name: FUNCTION genera_codice_iscrizione(); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.genera_codice_iscrizione() IS 'Genera un codice univoco per le iscrizioni';


--
-- Name: trigger_controlla_posti_disponibili(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_controlla_posti_disponibili() RETURNS trigger
    LANGUAGE plpgsql
    AS $$

DECLARE

    posti_disponibili INTEGER;

BEGIN

    IF NEW.stato = 'ATTIVA' THEN

        SELECT verifica_posti_disponibili(NEW.corso_id) INTO posti_disponibili;

        

        IF posti_disponibili <= 0 THEN

            RAISE EXCEPTION 'Corso pieno: non ci sono posti disponibili per il corso ID %', NEW.corso_id;

        END IF;

    END IF;

    

    RETURN NEW;

END;

$$;


ALTER FUNCTION public.trigger_controlla_posti_disponibili() OWNER TO postgres;

--
-- Name: trigger_genera_codice_iscrizione(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_genera_codice_iscrizione() RETURNS trigger
    LANGUAGE plpgsql
    AS $$

BEGIN

    IF NEW.codice_iscrizione IS NULL THEN

        NEW.codice_iscrizione = genera_codice_iscrizione();

    END IF;

    RETURN NEW;

END;

$$;


ALTER FUNCTION public.trigger_genera_codice_iscrizione() OWNER TO postgres;

--
-- Name: trigger_log_iscrizioni(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_log_iscrizioni() RETURNS trigger
    LANGUAGE plpgsql
    AS $$

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

$$;


ALTER FUNCTION public.trigger_log_iscrizioni() OWNER TO postgres;

--
-- Name: trigger_valida_email(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.trigger_valida_email() RETURNS trigger
    LANGUAGE plpgsql
    AS $$

BEGIN

    IF NEW.email IS NOT NULL AND NOT valida_email(NEW.email) THEN

        RAISE EXCEPTION 'Email non valida: %', NEW.email;

    END IF;

    RETURN NEW;

END;

$$;


ALTER FUNCTION public.trigger_valida_email() OWNER TO postgres;

--
-- Name: update_modified_at(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.update_modified_at() RETURNS trigger
    LANGUAGE plpgsql
    AS $$

BEGIN

    NEW.modified_at = CURRENT_TIMESTAMP;

    RETURN NEW;

END;

$$;


ALTER FUNCTION public.update_modified_at() OWNER TO postgres;

--
-- Name: valida_email(text); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.valida_email(email text) RETURNS boolean
    LANGUAGE plpgsql IMMUTABLE
    AS $_$

BEGIN

    RETURN email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$';

END;

$_$;


ALTER FUNCTION public.valida_email(email text) OWNER TO postgres;

--
-- Name: FUNCTION valida_email(email text); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.valida_email(email text) IS 'Valida il formato di un indirizzo email';


--
-- Name: verifica_posti_disponibili(integer); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.verifica_posti_disponibili(corso_id integer) RETURNS integer
    LANGUAGE plpgsql STABLE
    AS $$

DECLARE

    posti_totali INTEGER := 20; -- Limite predefinito

    posti_occupati INTEGER;

    posti_disponibili INTEGER;

BEGIN

    SELECT COUNT(*) INTO posti_occupati

    FROM iscrizioni 

    WHERE corso_id = verifica_posti_disponibili.corso_id 

    AND stato = 'ATTIVA';

    

    posti_disponibili := posti_totali - posti_occupati;

    

    RETURN GREATEST(posti_disponibili, 0);

END;

$$;


ALTER FUNCTION public.verifica_posti_disponibili(corso_id integer) OWNER TO postgres;

--
-- Name: FUNCTION verifica_posti_disponibili(corso_id integer); Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON FUNCTION public.verifica_posti_disponibili(corso_id integer) IS 'Verifica il numero di posti disponibili per un corso';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: adesioni_sessioni; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.adesioni_sessioni (
    id integer NOT NULL,
    utente_id integer,
    sessione_id integer,
    data_adesione timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    confermata boolean DEFAULT false,
    note text
);


ALTER TABLE public.adesioni_sessioni OWNER TO postgres;

--
-- Name: adesioni_sessioni_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.adesioni_sessioni_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.adesioni_sessioni_id_seq OWNER TO postgres;

--
-- Name: adesioni_sessioni_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.adesioni_sessioni_id_seq OWNED BY public.adesioni_sessioni.id;


--
-- Name: corsi; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.corsi (
    id integer NOT NULL,
    chef_id integer,
    categoria_id integer,
    titolo character varying(200) NOT NULL,
    descrizione text,
    data_inizio date NOT NULL,
    frequenza character varying(50) NOT NULL,
    numero_sessioni integer NOT NULL,
    prezzo numeric(10,2),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.corsi OWNER TO postgres;

--
-- Name: iscrizioni; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.iscrizioni (
    id integer NOT NULL,
    utente_id integer NOT NULL,
    corso_id integer NOT NULL,
    data_iscrizione timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    stato character varying(20) DEFAULT 'ATTIVA'::character varying,
    note text,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    codice_iscrizione character varying(20),
    CONSTRAINT iscrizioni_stato_check CHECK (((stato)::text = ANY ((ARRAY['ATTIVA'::character varying, 'COMPLETATA'::character varying, 'ANNULLATA'::character varying])::text[])))
);


ALTER TABLE public.iscrizioni OWNER TO postgres;

--
-- Name: TABLE iscrizioni; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.iscrizioni IS 'Tabella delle iscrizioni degli utenti ai corsi';


--
-- Name: COLUMN iscrizioni.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.iscrizioni.id IS 'Identificativo univoco iscrizione';


--
-- Name: COLUMN iscrizioni.utente_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.iscrizioni.utente_id IS 'Riferimento all utente iscritto';


--
-- Name: COLUMN iscrizioni.corso_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.iscrizioni.corso_id IS 'Riferimento al corso';


--
-- Name: COLUMN iscrizioni.data_iscrizione; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.iscrizioni.data_iscrizione IS 'Data e ora di iscrizione';


--
-- Name: COLUMN iscrizioni.stato; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.iscrizioni.stato IS 'Stato iscrizione: ATTIVA, COMPLETATA, ANNULLATA';


--
-- Name: COLUMN iscrizioni.note; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.iscrizioni.note IS 'Note aggiuntive sull iscrizione';


--
-- Name: analisi_iscrizioni_mensili; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.analisi_iscrizioni_mensili AS
 SELECT EXTRACT(year FROM data_iscrizione) AS anno,
    EXTRACT(month FROM data_iscrizione) AS mese,
    to_char(date_trunc('month'::text, data_iscrizione), 'Month YYYY'::text) AS periodo,
    count(*) AS totale_iscrizioni,
    count(*) FILTER (WHERE ((stato)::text = 'ATTIVA'::text)) AS iscrizioni_attive,
    count(*) FILTER (WHERE ((stato)::text = 'COMPLETATA'::text)) AS iscrizioni_completate,
    count(*) FILTER (WHERE ((stato)::text = 'ANNULLATA'::text)) AS iscrizioni_annullate,
    sum(( SELECT corsi.prezzo
           FROM public.corsi
          WHERE (corsi.id = iscrizioni.corso_id))) FILTER (WHERE ((stato)::text = ANY ((ARRAY['ATTIVA'::character varying, 'COMPLETATA'::character varying])::text[]))) AS ricavo_mensile
   FROM public.iscrizioni
  GROUP BY (EXTRACT(year FROM data_iscrizione)), (EXTRACT(month FROM data_iscrizione)), (date_trunc('month'::text, data_iscrizione))
  ORDER BY (EXTRACT(year FROM data_iscrizione)) DESC, (EXTRACT(month FROM data_iscrizione)) DESC;


ALTER VIEW public.analisi_iscrizioni_mensili OWNER TO postgres;

--
-- Name: VIEW analisi_iscrizioni_mensili; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.analisi_iscrizioni_mensili IS 'Analisi delle iscrizioni raggruppate per mese';


--
-- Name: categorie_corsi; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.categorie_corsi (
    id integer NOT NULL,
    nome character varying(100) NOT NULL,
    descrizione text
);


ALTER TABLE public.categorie_corsi OWNER TO postgres;

--
-- Name: categorie_corsi_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.categorie_corsi_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.categorie_corsi_id_seq OWNER TO postgres;

--
-- Name: categorie_corsi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.categorie_corsi_id_seq OWNED BY public.categorie_corsi.id;


--
-- Name: chef; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.chef (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    nome character varying(100) NOT NULL,
    cognome character varying(100) NOT NULL,
    email character varying(100),
    specializzazione character varying(200),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.chef OWNER TO postgres;

--
-- Name: chef_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.chef_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.chef_id_seq OWNER TO postgres;

--
-- Name: chef_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.chef_id_seq OWNED BY public.chef.id;


--
-- Name: corsi_dettaglio; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.corsi_dettaglio AS
SELECT
    NULL::integer AS id,
    NULL::character varying(200) AS titolo,
    NULL::text AS descrizione,
    NULL::date AS data_inizio,
    NULL::character varying(50) AS frequenza,
    NULL::integer AS numero_sessioni,
    NULL::numeric(10,2) AS prezzo,
    NULL::text AS chef_nome,
    NULL::character varying(200) AS chef_specializzazione,
    NULL::character varying(100) AS categoria,
    NULL::bigint AS iscritti_attivi,
    NULL::bigint AS iscritti_completati,
    NULL::bigint AS iscritti_annullati,
    NULL::bigint AS numero_sessioni_programmate,
    NULL::integer AS posti_disponibili,
    NULL::text AS stato_corso,
    NULL::numeric AS ricavo_corso;


ALTER VIEW public.corsi_dettaglio OWNER TO postgres;

--
-- Name: VIEW corsi_dettaglio; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.corsi_dettaglio IS 'Vista completa dei corsi con statistiche e dettagli';


--
-- Name: corsi_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.corsi_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.corsi_id_seq OWNER TO postgres;

--
-- Name: corsi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.corsi_id_seq OWNED BY public.corsi.id;


--
-- Name: utenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.utenti (
    id integer NOT NULL,
    username character varying(50) NOT NULL,
    password character varying(255) NOT NULL,
    nome character varying(100) NOT NULL,
    cognome character varying(100) NOT NULL,
    email character varying(100) NOT NULL,
    telefono character varying(20),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    data_nascita date,
    livello_esperienza character varying(20) DEFAULT 'PRINCIPIANTE'::character varying,
    attivo boolean DEFAULT true,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.utenti OWNER TO postgres;

--
-- Name: COLUMN utenti.data_nascita; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.utenti.data_nascita IS 'Data di nascita dell utente';


--
-- Name: dashboard_admin; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.dashboard_admin AS
 SELECT 'Utenti Totali'::text AS metrica,
    (count(*))::text AS valore,
    'success'::text AS tipo
   FROM public.utenti
  WHERE (utenti.attivo = true)
UNION ALL
 SELECT 'Corsi Attivi'::text AS metrica,
    (count(*))::text AS valore,
    'primary'::text AS tipo
   FROM public.corsi
  WHERE (corsi.data_inizio >= CURRENT_DATE)
UNION ALL
 SELECT 'Iscrizioni Attive'::text AS metrica,
    (count(*))::text AS valore,
    'info'::text AS tipo
   FROM public.iscrizioni
  WHERE ((iscrizioni.stato)::text = 'ATTIVA'::text)
UNION ALL
 SELECT 'Ricavo Mensile'::text AS metrica,
    ('├óÔÇÜ┬¼ '::text || (COALESCE(sum(c.prezzo), (0)::numeric))::text) AS valore,
    'warning'::text AS tipo
   FROM (public.iscrizioni i
     JOIN public.corsi c ON ((i.corso_id = c.id)))
  WHERE (((i.stato)::text = ANY ((ARRAY['ATTIVA'::character varying, 'COMPLETATA'::character varying])::text[])) AND (EXTRACT(month FROM i.data_iscrizione) = EXTRACT(month FROM CURRENT_DATE)) AND (EXTRACT(year FROM i.data_iscrizione) = EXTRACT(year FROM CURRENT_DATE)));


ALTER VIEW public.dashboard_admin OWNER TO postgres;

--
-- Name: VIEW dashboard_admin; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.dashboard_admin IS 'Metriche principali per la dashboard amministratore';


--
-- Name: ingredienti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ingredienti (
    id integer NOT NULL,
    nome character varying(100) NOT NULL,
    categoria character varying(50),
    unita_misura character varying(20),
    costo_unitario numeric(8,2),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.ingredienti OWNER TO postgres;

--
-- Name: ingredienti_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ingredienti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ingredienti_id_seq OWNER TO postgres;

--
-- Name: ingredienti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ingredienti_id_seq OWNED BY public.ingredienti.id;


--
-- Name: iscrizioni_complete; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.iscrizioni_complete AS
 SELECT i.id,
    i.codice_iscrizione,
    i.data_iscrizione,
    i.stato,
    i.note,
    (((u.nome)::text || ' '::text) || (u.cognome)::text) AS utente_nome_completo,
    u.email AS utente_email,
    u.telefono AS utente_telefono,
    public.calcola_eta(u.data_nascita) AS utente_eta,
    u.livello_esperienza,
    c.titolo AS corso_titolo,
    c.data_inizio AS corso_data_inizio,
    c.prezzo AS corso_prezzo,
    (((ch.nome)::text || ' '::text) || (ch.cognome)::text) AS chef_nome,
    cat.nome AS categoria_corso,
    EXTRACT(days FROM ((c.data_inizio)::timestamp without time zone - i.data_iscrizione)) AS giorni_anticipo_iscrizione
   FROM ((((public.iscrizioni i
     JOIN public.utenti u ON ((i.utente_id = u.id)))
     JOIN public.corsi c ON ((i.corso_id = c.id)))
     JOIN public.chef ch ON ((c.chef_id = ch.id)))
     LEFT JOIN public.categorie_corsi cat ON ((c.categoria_id = cat.id)))
  ORDER BY i.data_iscrizione DESC;


ALTER VIEW public.iscrizioni_complete OWNER TO postgres;

--
-- Name: VIEW iscrizioni_complete; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.iscrizioni_complete IS 'Vista completa delle iscrizioni con tutti i dettagli correlati';


--
-- Name: iscrizioni_corsi; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.iscrizioni_corsi (
    id integer NOT NULL,
    utente_id integer,
    corso_id integer,
    data_iscrizione timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    stato character varying(20) DEFAULT 'attiva'::character varying
);


ALTER TABLE public.iscrizioni_corsi OWNER TO postgres;

--
-- Name: iscrizioni_corsi_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.iscrizioni_corsi_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.iscrizioni_corsi_id_seq OWNER TO postgres;

--
-- Name: iscrizioni_corsi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.iscrizioni_corsi_id_seq OWNED BY public.iscrizioni_corsi.id;


--
-- Name: iscrizioni_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.iscrizioni_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.iscrizioni_id_seq OWNER TO postgres;

--
-- Name: iscrizioni_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.iscrizioni_id_seq OWNED BY public.iscrizioni.id;


--
-- Name: log_iscrizioni; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.log_iscrizioni (
    id integer NOT NULL,
    iscrizione_id integer,
    azione character varying(20),
    stato_precedente character varying(20),
    stato_nuovo character varying(20),
    utente_modifica character varying(100),
    timestamp_modifica timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    note text
);


ALTER TABLE public.log_iscrizioni OWNER TO postgres;

--
-- Name: log_iscrizioni_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.log_iscrizioni_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.log_iscrizioni_id_seq OWNER TO postgres;

--
-- Name: log_iscrizioni_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.log_iscrizioni_id_seq OWNED BY public.log_iscrizioni.id;


--
-- Name: notifiche; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.notifiche (
    id integer NOT NULL,
    corso_id integer,
    titolo character varying(200) NOT NULL,
    messaggio text NOT NULL,
    tipo character varying(50),
    data_invio timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    inviata boolean DEFAULT false
);


ALTER TABLE public.notifiche OWNER TO postgres;

--
-- Name: notifiche_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.notifiche_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.notifiche_id_seq OWNER TO postgres;

--
-- Name: notifiche_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.notifiche_id_seq OWNED BY public.notifiche.id;


--
-- Name: notifiche_sistema; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.notifiche_sistema AS
 SELECT 'corso_inizio_imminente'::text AS tipo_notifica,
    'warning'::text AS priorita,
    ((('Il corso "'::text || (corsi.titolo)::text) || '" inizia il '::text) || (corsi.data_inizio)::text) AS messaggio,
    corsi.chef_id AS destinatario_id,
    'chef'::text AS tipo_destinatario,
    CURRENT_TIMESTAMP AS created_at
   FROM public.corsi
  WHERE ((corsi.data_inizio >= (CURRENT_DATE + '6 days'::interval)) AND (corsi.data_inizio <= (CURRENT_DATE + '8 days'::interval)))
UNION ALL
 SELECT 'posti_limitati'::text AS tipo_notifica,
    'info'::text AS priorita,
    (((('Il corso "'::text || (corsi.titolo)::text) || '" ha solo '::text) || public.verifica_posti_disponibili(corsi.id)) || ' posti disponibili'::text) AS messaggio,
    corsi.chef_id AS destinatario_id,
    'chef'::text AS tipo_destinatario,
    CURRENT_TIMESTAMP AS created_at
   FROM public.corsi
  WHERE ((public.verifica_posti_disponibili(corsi.id) <= 3) AND (public.verifica_posti_disponibili(corsi.id) > 0))
UNION ALL
 SELECT 'corso_pieno'::text AS tipo_notifica,
    'success'::text AS priorita,
    (('Il corso "'::text || (corsi.titolo)::text) || '" ├â┬¿ al completo!'::text) AS messaggio,
    corsi.chef_id AS destinatario_id,
    'chef'::text AS tipo_destinatario,
    CURRENT_TIMESTAMP AS created_at
   FROM public.corsi
  WHERE (public.verifica_posti_disponibili(corsi.id) = 0);


ALTER VIEW public.notifiche_sistema OWNER TO postgres;

--
-- Name: VIEW notifiche_sistema; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.notifiche_sistema IS 'Notifiche automatiche generate dal sistema';


--
-- Name: report_chef; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW public.report_chef AS
 SELECT ch.id AS chef_id,
    (((ch.nome)::text || ' '::text) || (ch.cognome)::text) AS chef_nome,
    ch.specializzazione,
    count(DISTINCT c.id) AS corsi_totali,
    count(DISTINCT c.id) FILTER (WHERE (c.data_inizio >= CURRENT_DATE)) AS corsi_futuri,
    count(DISTINCT i.id) AS iscrizioni_totali,
    count(DISTINCT i.id) FILTER (WHERE ((i.stato)::text = 'ATTIVA'::text)) AS iscrizioni_attive,
    count(DISTINCT i.id) FILTER (WHERE ((i.stato)::text = 'COMPLETATA'::text)) AS iscrizioni_completate,
    round(
        CASE
            WHEN (count(DISTINCT i.id) > 0) THEN (((count(DISTINCT i.id) FILTER (WHERE ((i.stato)::text = 'COMPLETATA'::text)))::numeric / (count(DISTINCT i.id))::numeric) * (100)::numeric)
            ELSE (0)::numeric
        END, 2) AS tasso_completamento_percentuale,
    sum((c.prezzo * (( SELECT count(*) AS count
           FROM public.iscrizioni
          WHERE ((iscrizioni.corso_id = c.id) AND ((iscrizioni.stato)::text = ANY ((ARRAY['ATTIVA'::character varying, 'COMPLETATA'::character varying])::text[])))))::numeric)) AS ricavo_totale,
    avg(c.prezzo) AS prezzo_medio_corsi
   FROM ((public.chef ch
     LEFT JOIN public.corsi c ON ((ch.id = c.chef_id)))
     LEFT JOIN public.iscrizioni i ON ((c.id = i.corso_id)))
  GROUP BY ch.id, ch.nome, ch.cognome, ch.specializzazione
  ORDER BY (sum((c.prezzo * (( SELECT count(*) AS count
           FROM public.iscrizioni
          WHERE ((iscrizioni.corso_id = c.id) AND ((iscrizioni.stato)::text = ANY ((ARRAY['ATTIVA'::character varying, 'COMPLETATA'::character varying])::text[])))))::numeric))) DESC NULLS LAST;


ALTER VIEW public.report_chef OWNER TO postgres;

--
-- Name: VIEW report_chef; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON VIEW public.report_chef IS 'Report dettagliato delle performance di ogni chef';


--
-- Name: ricette; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ricette (
    id integer NOT NULL,
    chef_id integer,
    nome character varying(200) NOT NULL,
    descrizione text,
    difficolta integer,
    tempo_preparazione integer,
    numero_porzioni integer DEFAULT 4,
    istruzioni text,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ricette_difficolta_check CHECK (((difficolta >= 1) AND (difficolta <= 5)))
);


ALTER TABLE public.ricette OWNER TO postgres;

--
-- Name: ricette_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ricette_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ricette_id_seq OWNER TO postgres;

--
-- Name: ricette_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ricette_id_seq OWNED BY public.ricette.id;


--
-- Name: ricette_ingredienti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.ricette_ingredienti (
    id integer NOT NULL,
    ricetta_id integer,
    ingrediente_id integer,
    quantita numeric(8,2) NOT NULL,
    note character varying(200)
);


ALTER TABLE public.ricette_ingredienti OWNER TO postgres;

--
-- Name: ricette_ingredienti_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.ricette_ingredienti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.ricette_ingredienti_id_seq OWNER TO postgres;

--
-- Name: ricette_ingredienti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.ricette_ingredienti_id_seq OWNED BY public.ricette_ingredienti.id;


--
-- Name: sessioni; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sessioni (
    id integer NOT NULL,
    corso_id integer,
    numero_sessione integer NOT NULL,
    data_sessione date NOT NULL,
    tipo character varying(20) NOT NULL,
    titolo character varying(200),
    descrizione text,
    durata_minuti integer DEFAULT 120,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    modified_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sessioni OWNER TO postgres;

--
-- Name: sessioni_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sessioni_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sessioni_id_seq OWNER TO postgres;

--
-- Name: sessioni_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sessioni_id_seq OWNED BY public.sessioni.id;


--
-- Name: sessioni_ricette; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sessioni_ricette (
    id integer NOT NULL,
    sessione_id integer,
    ricetta_id integer,
    ordine_esecuzione integer DEFAULT 1,
    note text
);


ALTER TABLE public.sessioni_ricette OWNER TO postgres;

--
-- Name: sessioni_ricette_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sessioni_ricette_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sessioni_ricette_id_seq OWNER TO postgres;

--
-- Name: sessioni_ricette_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sessioni_ricette_id_seq OWNED BY public.sessioni_ricette.id;


--
-- Name: utenti_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.utenti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.utenti_id_seq OWNER TO postgres;

--
-- Name: utenti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.utenti_id_seq OWNED BY public.utenti.id;


--
-- Name: adesioni_sessioni id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.adesioni_sessioni ALTER COLUMN id SET DEFAULT nextval('public.adesioni_sessioni_id_seq'::regclass);


--
-- Name: categorie_corsi id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorie_corsi ALTER COLUMN id SET DEFAULT nextval('public.categorie_corsi_id_seq'::regclass);


--
-- Name: chef id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chef ALTER COLUMN id SET DEFAULT nextval('public.chef_id_seq'::regclass);


--
-- Name: corsi id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi ALTER COLUMN id SET DEFAULT nextval('public.corsi_id_seq'::regclass);


--
-- Name: ingredienti id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ingredienti ALTER COLUMN id SET DEFAULT nextval('public.ingredienti_id_seq'::regclass);


--
-- Name: iscrizioni id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni ALTER COLUMN id SET DEFAULT nextval('public.iscrizioni_id_seq'::regclass);


--
-- Name: iscrizioni_corsi id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni_corsi ALTER COLUMN id SET DEFAULT nextval('public.iscrizioni_corsi_id_seq'::regclass);


--
-- Name: log_iscrizioni id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.log_iscrizioni ALTER COLUMN id SET DEFAULT nextval('public.log_iscrizioni_id_seq'::regclass);


--
-- Name: notifiche id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifiche ALTER COLUMN id SET DEFAULT nextval('public.notifiche_id_seq'::regclass);


--
-- Name: ricette id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette ALTER COLUMN id SET DEFAULT nextval('public.ricette_id_seq'::regclass);


--
-- Name: ricette_ingredienti id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette_ingredienti ALTER COLUMN id SET DEFAULT nextval('public.ricette_ingredienti_id_seq'::regclass);


--
-- Name: sessioni id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni ALTER COLUMN id SET DEFAULT nextval('public.sessioni_id_seq'::regclass);


--
-- Name: sessioni_ricette id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni_ricette ALTER COLUMN id SET DEFAULT nextval('public.sessioni_ricette_id_seq'::regclass);


--
-- Name: utenti id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utenti ALTER COLUMN id SET DEFAULT nextval('public.utenti_id_seq'::regclass);


--
-- Data for Name: adesioni_sessioni; Type: TABLE DATA; Schema: public; Owner: postgres
--



--
-- Data for Name: sessioni_ricette; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.sessioni_ricette (id, sessione_id, ricetta_id, ordine_esecuzione, note) FROM stdin;
1	1	1	1	\N
2	3	1	1	\N
3	4	2	1	\N
4	5	2	1	\N
12	2	3	1	\N
13	3	4	1	\N
17	6	1	1	\N
\.


--
-- Data for Name: utenti; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.utenti (id, username, password, nome, cognome, email, telefono, created_at, data_nascita, livello_esperienza, attivo, modified_at) FROM stdin;
1	user1	pass123	Marco	Bianchi	marco.bianchi@email.it	3331234567	2025-06-10 17:22:45.790624	1990-05-15	PRINCIPIANTE	t	2025-09-10 13:43:42.2589
2	user2	pass456	Laura	Neri	laura.neri@email.it	3337654321	2025-06-10 17:22:45.790624	1985-08-22	INTERMEDIO	t	2025-09-10 13:43:42.2589
3	user3	pass789	Giuseppe	Romano	giuseppe.romano@email.it	3339876543	2025-06-10 17:22:45.790624	1995-12-03	AVANZATO	t	2025-09-10 13:43:42.2589
\.


--
-- Name: adesioni_sessioni_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.adesioni_sessioni_id_seq', 5, true);


--
-- Name: categorie_corsi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.categorie_corsi_id_seq', 6, true);


--
-- Name: chef_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.chef_id_seq', 4, true);


--
-- Name: corsi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.corsi_id_seq', 4, true);


--
-- Name: ingredienti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ingredienti_id_seq', 10, true);


--
-- Name: iscrizioni_corsi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.iscrizioni_corsi_id_seq', 5, true);


--
-- Name: iscrizioni_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.iscrizioni_id_seq', 28, true);


--
-- Name: log_iscrizioni_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.log_iscrizioni_id_seq', 2, true);


--
-- Name: notifiche_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.notifiche_id_seq', 1, false);


--
-- Name: ricette_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ricette_id_seq', 4, true);


--
-- Name: ricette_ingredienti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.ricette_ingredienti_id_seq', 9, true);


--
-- Name: sessioni_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sessioni_id_seq', 7, true);


--
-- Name: sessioni_ricette_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.sessioni_ricette_id_seq', 17, true);


--
-- Name: utenti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.utenti_id_seq', 4, true);


--
-- Name: adesioni_sessioni adesioni_sessioni_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.adesioni_sessioni
    ADD CONSTRAINT adesioni_sessioni_pkey PRIMARY KEY (id);


--
-- Name: adesioni_sessioni adesioni_sessioni_utente_id_sessione_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.adesioni_sessioni
    ADD CONSTRAINT adesioni_sessioni_utente_id_sessione_id_key UNIQUE (utente_id, sessione_id);


--
-- Name: categorie_corsi categorie_corsi_nome_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorie_corsi
    ADD CONSTRAINT categorie_corsi_nome_key UNIQUE (nome);


--
-- Name: categorie_corsi categorie_corsi_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.categorie_corsi
    ADD CONSTRAINT categorie_corsi_pkey PRIMARY KEY (id);


--
-- Name: chef chef_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chef
    ADD CONSTRAINT chef_pkey PRIMARY KEY (id);


--
-- Name: chef chef_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.chef
    ADD CONSTRAINT chef_username_key UNIQUE (username);


--
-- Name: corsi corsi_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi
    ADD CONSTRAINT corsi_pkey PRIMARY KEY (id);


--
-- Name: ingredienti ingredienti_nome_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ingredienti
    ADD CONSTRAINT ingredienti_nome_key UNIQUE (nome);


--
-- Name: ingredienti ingredienti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ingredienti
    ADD CONSTRAINT ingredienti_pkey PRIMARY KEY (id);


--
-- Name: iscrizioni iscrizioni_codice_iscrizione_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni
    ADD CONSTRAINT iscrizioni_codice_iscrizione_key UNIQUE (codice_iscrizione);


--
-- Name: iscrizioni_corsi iscrizioni_corsi_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni_corsi
    ADD CONSTRAINT iscrizioni_corsi_pkey PRIMARY KEY (id);


--
-- Name: iscrizioni_corsi iscrizioni_corsi_utente_id_corso_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni_corsi
    ADD CONSTRAINT iscrizioni_corsi_utente_id_corso_id_key UNIQUE (utente_id, corso_id);


--
-- Name: iscrizioni iscrizioni_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni
    ADD CONSTRAINT iscrizioni_pkey PRIMARY KEY (id);


--
-- Name: log_iscrizioni log_iscrizioni_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.log_iscrizioni
    ADD CONSTRAINT log_iscrizioni_pkey PRIMARY KEY (id);


--
-- Name: notifiche notifiche_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifiche
    ADD CONSTRAINT notifiche_pkey PRIMARY KEY (id);


--
-- Name: ricette_ingredienti ricette_ingredienti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette_ingredienti
    ADD CONSTRAINT ricette_ingredienti_pkey PRIMARY KEY (id);


--
-- Name: ricette_ingredienti ricette_ingredienti_ricetta_id_ingrediente_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette_ingredienti
    ADD CONSTRAINT ricette_ingredienti_ricetta_id_ingrediente_id_key UNIQUE (ricetta_id, ingrediente_id);


--
-- Name: ricette ricette_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette
    ADD CONSTRAINT ricette_pkey PRIMARY KEY (id);


--
-- Name: sessioni sessioni_corso_id_numero_sessione_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni
    ADD CONSTRAINT sessioni_corso_id_numero_sessione_key UNIQUE (corso_id, numero_sessione);


--
-- Name: sessioni sessioni_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni
    ADD CONSTRAINT sessioni_pkey PRIMARY KEY (id);


--
-- Name: sessioni_ricette sessioni_ricette_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni_ricette
    ADD CONSTRAINT sessioni_ricette_pkey PRIMARY KEY (id);


--
-- Name: sessioni_ricette sessioni_ricette_sessione_id_ricetta_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni_ricette
    ADD CONSTRAINT sessioni_ricette_sessione_id_ricetta_id_key UNIQUE (sessione_id, ricetta_id);


--
-- Name: utenti utenti_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_email_key UNIQUE (email);


--
-- Name: utenti utenti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_pkey PRIMARY KEY (id);


--
-- Name: utenti utenti_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.utenti
    ADD CONSTRAINT utenti_username_key UNIQUE (username);


--
-- Name: idx_adesioni_sessione; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_adesioni_sessione ON public.adesioni_sessioni USING btree (sessione_id);


--
-- Name: idx_adesioni_utente; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_adesioni_utente ON public.adesioni_sessioni USING btree (utente_id);


--
-- Name: idx_corsi_categoria; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_corsi_categoria ON public.corsi USING btree (categoria_id);


--
-- Name: idx_corsi_chef; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_corsi_chef ON public.corsi USING btree (chef_id);


--
-- Name: idx_corsi_data_inizio; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_corsi_data_inizio ON public.corsi USING btree (data_inizio);


--
-- Name: idx_iscrizioni_corso; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_iscrizioni_corso ON public.iscrizioni_corsi USING btree (corso_id);


--
-- Name: idx_iscrizioni_data; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_iscrizioni_data ON public.iscrizioni USING btree (data_iscrizione);


--
-- Name: idx_iscrizioni_stato; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_iscrizioni_stato ON public.iscrizioni USING btree (stato);


--
-- Name: idx_iscrizioni_utente; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_iscrizioni_utente ON public.iscrizioni_corsi USING btree (utente_id);


--
-- Name: idx_iscrizioni_utente_corso; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_iscrizioni_utente_corso ON public.iscrizioni USING btree (utente_id, corso_id);


--
-- Name: idx_log_iscrizioni_iscrizione_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_log_iscrizioni_iscrizione_id ON public.log_iscrizioni USING btree (iscrizione_id);


--
-- Name: idx_log_iscrizioni_timestamp; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_log_iscrizioni_timestamp ON public.log_iscrizioni USING btree (timestamp_modifica);


--
-- Name: idx_sessioni_corso; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_sessioni_corso ON public.sessioni USING btree (corso_id);


--
-- Name: idx_utenti_attivo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_utenti_attivo ON public.utenti USING btree (attivo);


--
-- Name: idx_utenti_email; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_utenti_email ON public.utenti USING btree (email);


--
-- Name: corsi_dettaglio _RETURN; Type: RULE; Schema: public; Owner: postgres
--

CREATE OR REPLACE VIEW public.corsi_dettaglio AS
 SELECT c.id,
    c.titolo,
    c.descrizione,
    c.data_inizio,
    c.frequenza,
    c.numero_sessioni,
    c.prezzo,
    (((ch.nome)::text || ' '::text) || (ch.cognome)::text) AS chef_nome,
    ch.specializzazione AS chef_specializzazione,
    cat.nome AS categoria,
    count(DISTINCT i.id) FILTER (WHERE ((i.stato)::text = 'ATTIVA'::text)) AS iscritti_attivi,
    count(DISTINCT i.id) FILTER (WHERE ((i.stato)::text = 'COMPLETATA'::text)) AS iscritti_completati,
    count(DISTINCT i.id) FILTER (WHERE ((i.stato)::text = 'ANNULLATA'::text)) AS iscritti_annullati,
    count(DISTINCT s.id) AS numero_sessioni_programmate,
    public.verifica_posti_disponibili(c.id) AS posti_disponibili,
        CASE
            WHEN (c.data_inizio > CURRENT_DATE) THEN 'Programmato'::text
            WHEN (c.data_inizio <= CURRENT_DATE) THEN 'In Corso'::text
            ELSE 'Completato'::text
        END AS stato_corso,
    (c.prezzo * (count(DISTINCT i.id) FILTER (WHERE ((i.stato)::text = ANY ((ARRAY['ATTIVA'::character varying, 'COMPLETATA'::character varying])::text[]))))::numeric) AS ricavo_corso
   FROM ((((public.corsi c
     LEFT JOIN public.chef ch ON ((c.chef_id = ch.id)))
     LEFT JOIN public.categorie_corsi cat ON ((c.categoria_id = cat.id)))
     LEFT JOIN public.iscrizioni i ON ((c.id = i.corso_id)))
     LEFT JOIN public.sessioni s ON ((c.id = s.corso_id)))
  GROUP BY c.id, ch.nome, ch.cognome, ch.specializzazione, cat.nome
  ORDER BY c.data_inizio DESC;


--
-- Name: iscrizioni trigger_codice_iscrizione; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_codice_iscrizione BEFORE INSERT ON public.iscrizioni FOR EACH ROW EXECUTE FUNCTION public.trigger_genera_codice_iscrizione();


--
-- Name: iscrizioni trigger_log_iscrizioni; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_log_iscrizioni AFTER INSERT OR DELETE OR UPDATE ON public.iscrizioni FOR EACH ROW EXECUTE FUNCTION public.trigger_log_iscrizioni();


--
-- Name: iscrizioni trigger_posti_disponibili; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_posti_disponibili BEFORE INSERT OR UPDATE ON public.iscrizioni FOR EACH ROW EXECUTE FUNCTION public.trigger_controlla_posti_disponibili();


--
-- Name: chef trigger_update_chef_modified_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_chef_modified_at BEFORE UPDATE ON public.chef FOR EACH ROW EXECUTE FUNCTION public.update_modified_at();


--
-- Name: corsi trigger_update_corsi_modified_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_corsi_modified_at BEFORE UPDATE ON public.corsi FOR EACH ROW EXECUTE FUNCTION public.update_modified_at();


--
-- Name: iscrizioni trigger_update_iscrizioni_modified_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_iscrizioni_modified_at BEFORE UPDATE ON public.iscrizioni FOR EACH ROW EXECUTE FUNCTION public.update_modified_at();


--
-- Name: sessioni trigger_update_sessioni_modified_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_sessioni_modified_at BEFORE UPDATE ON public.sessioni FOR EACH ROW EXECUTE FUNCTION public.update_modified_at();


--
-- Name: utenti trigger_update_utenti_modified_at; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_update_utenti_modified_at BEFORE UPDATE ON public.utenti FOR EACH ROW EXECUTE FUNCTION public.update_modified_at();


--
-- Name: chef trigger_valida_email_chef; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_valida_email_chef BEFORE INSERT OR UPDATE ON public.chef FOR EACH ROW EXECUTE FUNCTION public.trigger_valida_email();


--
-- Name: utenti trigger_valida_email_utenti; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER trigger_valida_email_utenti BEFORE INSERT OR UPDATE ON public.utenti FOR EACH ROW EXECUTE FUNCTION public.trigger_valida_email();


--
-- Name: adesioni_sessioni adesioni_sessioni_sessione_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.adesioni_sessioni
    ADD CONSTRAINT adesioni_sessioni_sessione_id_fkey FOREIGN KEY (sessione_id) REFERENCES public.sessioni(id) ON DELETE CASCADE;


--
-- Name: adesioni_sessioni adesioni_sessioni_utente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.adesioni_sessioni
    ADD CONSTRAINT adesioni_sessioni_utente_id_fkey FOREIGN KEY (utente_id) REFERENCES public.utenti(id) ON DELETE CASCADE;


--
-- Name: corsi corsi_categoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi
    ADD CONSTRAINT corsi_categoria_id_fkey FOREIGN KEY (categoria_id) REFERENCES public.categorie_corsi(id);


--
-- Name: corsi corsi_chef_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi
    ADD CONSTRAINT corsi_chef_id_fkey FOREIGN KEY (chef_id) REFERENCES public.chef(id) ON DELETE CASCADE;


--
-- Name: iscrizioni_corsi iscrizioni_corsi_corso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni_corsi
    ADD CONSTRAINT iscrizioni_corsi_corso_id_fkey FOREIGN KEY (corso_id) REFERENCES public.corsi(id) ON DELETE CASCADE;


--
-- Name: iscrizioni_corsi iscrizioni_corsi_utente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni_corsi
    ADD CONSTRAINT iscrizioni_corsi_utente_id_fkey FOREIGN KEY (utente_id) REFERENCES public.utenti(id) ON DELETE CASCADE;


--
-- Name: iscrizioni iscrizioni_corso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni
    ADD CONSTRAINT iscrizioni_corso_id_fkey FOREIGN KEY (corso_id) REFERENCES public.corsi(id) ON DELETE CASCADE;


--
-- Name: iscrizioni iscrizioni_utente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.iscrizioni
    ADD CONSTRAINT iscrizioni_utente_id_fkey FOREIGN KEY (utente_id) REFERENCES public.utenti(id) ON DELETE CASCADE;


--
-- Name: notifiche notifiche_corso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.notifiche
    ADD CONSTRAINT notifiche_corso_id_fkey FOREIGN KEY (corso_id) REFERENCES public.corsi(id) ON DELETE CASCADE;


--
-- Name: ricette ricette_chef_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette
    ADD CONSTRAINT ricette_chef_id_fkey FOREIGN KEY (chef_id) REFERENCES public.chef(id);


--
-- Name: ricette_ingredienti ricette_ingredienti_ingrediente_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette_ingredienti
    ADD CONSTRAINT ricette_ingredienti_ingrediente_id_fkey FOREIGN KEY (ingrediente_id) REFERENCES public.ingredienti(id);


--
-- Name: ricette_ingredienti ricette_ingredienti_ricetta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.ricette_ingredienti
    ADD CONSTRAINT ricette_ingredienti_ricetta_id_fkey FOREIGN KEY (ricetta_id) REFERENCES public.ricette(id) ON DELETE CASCADE;


--
-- Name: sessioni sessioni_corso_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni
    ADD CONSTRAINT sessioni_corso_id_fkey FOREIGN KEY (corso_id) REFERENCES public.corsi(id) ON DELETE CASCADE;


--
-- Name: sessioni_ricette sessioni_ricette_ricetta_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni_ricette
    ADD CONSTRAINT sessioni_ricette_ricetta_id_fkey FOREIGN KEY (ricetta_id) REFERENCES public.ricette(id);


--
-- Name: sessioni_ricette sessioni_ricette_sessione_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sessioni_ricette
    ADD CONSTRAINT sessioni_ricette_sessione_id_fkey FOREIGN KEY (sessione_id) REFERENCES public.sessioni(id) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

