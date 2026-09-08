package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.config.UtenteHandshakeInterceptor;
import it.epicode.demo.notifiche.dto.Busta;
import it.epicode.demo.notifiche.dto.ComandoWs;
import it.epicode.demo.notifiche.dto.NotificationDto;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * IL CUORE DELLA DEM 3. Se leggete un file solo, leggete questo.
 *
 * Nella Dem 2 le sessioni erano tutte uguali e ogni messaggio andava a tutti.
 * Qui vogliamo tre cose diverse:
 *   - mandare una notifica a UN utente preciso;
 *   - mandare un annuncio a TUTTI;
 *   - mandare un messaggio solo a chi SEGUE un certo ordine.
 *
 * Per farlo servono due elenchi (li trovate qui sotto) e parecchio codice che li
 * tiene aggiornati. Nessuno fa questo lavoro al posto nostro.
 *
 * Sono circa 140 righe: tenetelo a mente, perche' nella Dem 4 questa classe NON
 * ESISTE PIU'. Quegli elenchi li tiene il broker STOMP, e le tre destinazioni
 * diventano tre stringhe di configurazione.
 */
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(NotificationWebSocketHandler.class);

	// Chiave con cui, su ogni sessione, teniamo l'elenco degli ordini che quella
	// sessione ha chiesto di seguire.
	private static final String ATTRIBUTO_ORDINI = "ordini";

	/**
	 * PRIMO ELENCO: chi e' collegato, diviso per nome utente.
	 *
	 * Perche' un insieme di sessioni e non una sessione sola per utente?
	 * Perche' mario puo' avere due schede aperte, o il telefono e il computer.
	 * Sono sessioni diverse dello stesso utente e la notifica deve arrivare a tutte.
	 *
	 * ConcurrentHashMap perche' le connessioni si aprono e si chiudono su thread
	 * diversi da quelli che spediscono: una HashMap normale qui si corromperebbe.
	 */
	private final ConcurrentHashMap<String, Set<WebSocketSession>> perUtente = new ConcurrentHashMap<>();

	/**
	 * SECONDO ELENCO: tutte le sessioni collegate, senza distinzione di nome.
	 *
	 * Serve perche' i canali non personali non guardano il nome. Una sessione arrivata
	 * senza ?utente= non compare in perUtente: se filtrassimo sempre da li', quella
	 * sessione non riceverebbe ne' gli annunci a tutti ne' gli ordini che ha chiesto
	 * di seguire, e nessuno capirebbe il perche'.
	 */
	private final Set<WebSocketSession> tutte = new CopyOnWriteArraySet<>();

	private final ObjectMapper json;

	public NotificationWebSocketHandler(ObjectMapper json) {
		this.json = json;
	}

	// --- Apertura e chiusura: qui i due elenchi si tengono aggiornati -----------

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		String utente = utenteDi(session);

		// Ogni sessione nasce con il suo elenco (vuoto) di ordini seguiti.
		session.getAttributes().put(ATTRIBUTO_ORDINI, new CopyOnWriteArraySet<Long>());
		tutte.add(session);

		if (utente == null) {
			log.warn("sessione aperta senza utente   id={}", session.getId());
			return;
		}

		// computeIfAbsent significa "dammi l'insieme di questo utente, e se non c'e'
		// crealo", ma in una mossa sola. Con un get seguito da un put, due handshake
		// dello stesso utente arrivati nello stesso istante potrebbero sovrascriversi
		// a vicenda e una delle due sessioni sparirebbe dall'elenco.
		perUtente.computeIfAbsent(utente, chiave -> new CopyOnWriteArraySet<>()).add(session);
		log.info("sessione aperta   utente={} id={} sessioni={}", utente, session.getId(), aperte());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		String utente = utenteDi(session);
		tutte.remove(session);

		if (utente != null) {
			// Togliamo la sessione e, se era l'ultima di quell'utente, togliamo anche la
			// voce dalla mappa restituendo null. Senza, la mappa si riempirebbe di insiemi
			// vuoti, uno per ogni utente collegatosi anche una volta sola: una perdita
			// di memoria lenta ma inesorabile.
			perUtente.computeIfPresent(utente, (chiave, sessioni) -> {
				sessioni.remove(session);
				return sessioni.isEmpty() ? null : sessioni;
			});
		}
		log.info("sessione chiusa   utente={} id={} motivo={} sessioni={}",
				utente, session.getId(), status, aperte());
	}

	// --- Quello che arriva dal client ------------------------------------------

	/**
	 * Il client ci manda un comando: "segui l'ordine 42", "smetti", "pubblica un testo".
	 *
	 * Il formato l'abbiamo inventato noi (vedi ComandoWs), quindi tocca a noi anche
	 * capirlo, controllarlo e decidere cosa fare quando e' sbagliato. E notate come
	 * finisce ogni caso storto: con una riga di log e basta. Il client non riceve nessun
	 * errore, perche' nel nostro protocollo un messaggio di errore non esiste.
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			ComandoWs comando = json.readValue(message.getPayload(), ComandoWs.class);

			if (comando.ordine() == null) {
				log.warn("comando senza ordine da {}: {}", session.getId(), message.getPayload());
				return;
			}

			Set<Long> ordini = ordiniDi(session);
			switch (comando.azione()) {
				// "Iscriversi" qui vuol dire aggiungere un numero a un insieme attaccato
				// alla sessione. Tutto qua: e' una SUBSCRIBE scritta a mano.
				case "segui" -> ordini.add(comando.ordine());
				case "smetti" -> ordini.remove(comando.ordine());
				case "pubblica" -> {
					pubblica(session, ordini, comando);
					return;
				}
				default -> {
					log.warn("azione sconosciuta da {}: {}", session.getId(), comando.azione());
					return;
				}
			}
			log.info("ordini seguiti   utente={} id={} ordini={}", utenteDi(session), session.getId(), ordini);

		} catch (JacksonException e) {
			// Il client puo' mandare qualunque stringa, anche non JSON. In Jackson 3 non
			// e' obbligatorio catturare questa eccezione: se non lo facessimo, morirebbe
			// il thread che serve la sessione e la connessione si chiuderebbe senza
			// nessuna spiegazione, ne' qui ne' nel browser.
			log.warn("payload non leggibile da {}: {}", session.getId(), message.getPayload());
		}
	}

	/**
	 * Un utente scrive nella "chat" di un ordine.
	 *
	 * La regola e' semplice: si scrive solo dove si e' entrati. E quella regola e'
	 * la riga di contains qui sotto, scritta a mano. Senza, qualsiasi sessione potrebbe
	 * scrivere su qualsiasi ordine: non c'e' nessun broker che sappia chi e' iscritto
	 * a cosa e possa rifiutare al posto nostro.
	 */
	private void pubblica(WebSocketSession session, Set<Long> ordini, ComandoWs comando) {
		String utente = utenteDi(session);

		if (!ordini.contains(comando.ordine())) {
			// Rifiuto muto: il client non riceve niente e continua a credere di aver
			// pubblicato. Anche questo e' il prezzo di un protocollo fatto in casa.
			log.warn("pubblicazione rifiutata   utente={} id={} ordine={} non seguito",
					utente, session.getId(), comando.ordine());
			return;
		}
		if (comando.testo() == null || comando.testo().isBlank()) {
			log.warn("pubblicazione senza testo   utente={} id={}", utente, session.getId());
			return;
		}

		NotificationDto dto = NotificationDto.diCanale(utente, comando.ordine(), comando.testo());

		// Il messaggio va a tutti quelli che seguono l'ordine, mittente compreso: visto
		// che il server non manda nessuna conferma, rivedere arrivare il proprio messaggio
		// e' l'unica prova che sia davvero passato.
		aChiSegue(comando.ordine(), Busta.ordine(comando.ordine(), dto));
		log.info("pubblicato   utente={} ordine={} testo=\"{}\"", utente, comando.ordine(), comando.testo());
	}

	// --- Le tre destinazioni ---------------------------------------------------

	/**
	 * A UN UTENTE SOLO: le sue sessioni, quelle e basta.
	 *
	 * Se l'utente non e' collegato non c'e' niente da consegnare e il messaggio si perde.
	 * Non e' un dramma: la notifica e' comunque salvata sul database e quando l'utente
	 * tornera' la trovera' nello storico REST. E' il motivo per cui il canale non
	 * sostituisce le chiamate HTTP, ma si affianca a loro.
	 */
	public void aUtente(String utente, Busta busta) {
		Set<WebSocketSession> sessioni = perUtente.get(utente);
		if (sessioni == null || sessioni.isEmpty()) {
			log.debug("nessuna sessione per {}: la notifica resta solo sul database", utente);
			return;
		}
		spedisci(sessioni, busta);
	}

	/** A TUTTI I COLLEGATI: nessun filtro. Qui usiamo l'elenco completo, non perUtente. */
	public void aTutti(Busta busta) {
		spedisci(tutte, busta);
	}

	/**
	 * A CHI SEGUE UN ORDINE: il filtro lo applichiamo noi, sessione per sessione.
	 * Scorriamo tutte le connessioni aperte e teniamo solo quelle che hanno quel
	 * numero nel proprio insieme di ordini seguiti.
	 */
	public void aChiSegue(Long ordineId, Busta busta) {
		TextMessage frame = testo(busta);
		tutte.stream()
				.filter(sessione -> ordiniDi(sessione).contains(ordineId))
				.forEach(sessione -> invia(sessione, frame));
	}

	/** Quante sessioni sono aperte adesso. */
	public int aperte() {
		return tutte.size();
	}

	// --- Il lavoro sporco: trasformare in JSON e scrivere sul tubo ---------------

	private void spedisci(Set<WebSocketSession> sessioni, Busta busta) {
		// Il JSON si costruisce una volta sola, fuori dal ciclo.
		TextMessage frame = testo(busta);
		sessioni.forEach(sessione -> invia(sessione, frame));
	}

	private TextMessage testo(Busta busta) {
		try {
			return new TextMessage(json.writeValueAsString(busta));
		} catch (JacksonException e) {
			// Se non riusciamo a trasformare in JSON una busta costruita da noi, e'
			// un errore di programmazione, non una situazione da gestire con eleganza.
			throw new IllegalStateException("busta non serializzabile", e);
		}
	}

	/**
	 * L'invio vero e proprio, con le due precauzioni di sempre: la sessione potrebbe
	 * essersi appena chiusa (isOpen), e una WebSocketSession non regge due sendMessage
	 * contemporanei (synchronized), altrimenti i frame si mescolano, il client riceve
	 * spazzatura e la connessione cade.
	 */
	private void invia(WebSocketSession sessione, TextMessage frame) {
		if (!sessione.isOpen()) {
			return;
		}
		try {
			synchronized (sessione) {
				sessione.sendMessage(frame);
			}
		} catch (IOException e) {
			log.warn("invio fallito su {}: {}", sessione.getId(), e.getMessage());
		}
	}

	// --- Due scorciatoie per leggere gli attributi della sessione ---------------

	/** Il nome catturato dall'interceptor durante l'handshake. Null se la sessione e' anonima. */
	private String utenteDi(WebSocketSession session) {
		return (String) session.getAttributes().get(UtenteHandshakeInterceptor.ATTRIBUTO_UTENTE);
	}

	// Gli attributi della sessione sono una Map<String, Object>: quello che ci abbiamo
	// messo dentro esce come Object e va ricondotto al tipo giusto a mano. Il compilatore
	// non puo' verificare quel cast, da qui il @SuppressWarnings.
	@SuppressWarnings("unchecked")
	private Set<Long> ordiniDi(WebSocketSession session) {
		return (Set<Long>) session.getAttributes().getOrDefault(ATTRIBUTO_ORDINI, Set.<Long>of());
	}
}
