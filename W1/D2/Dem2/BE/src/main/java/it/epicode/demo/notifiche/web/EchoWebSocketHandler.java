package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.TestMessage;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

/**
 * Il cuore della Dem 2. Qui si vede cosa vuol dire davvero "WebSocket nudo".
 *
 * Estendendo TextWebSocketHandler, Spring ci chiama in tre momenti:
 *  - quando qualcuno si collega        -> afterConnectionEstablished
 *  - quando qualcuno manda una stringa -> handleTextMessage
 *  - quando qualcuno si scollega       -> afterConnectionClosed
 *
 * QUELLO CHE NON C'E':
 * non esistono canali, argomenti, iscrizioni, destinatari. Non c'e' nessuno
 * che tenga il conto di chi e' collegato: quell'elenco lo teniamo noi, a mano,
 * nel campo "sessioni" qui sotto. E se vogliamo mandare un messaggio a qualcuno,
 * dobbiamo scorrere l'elenco con un for.
 *
 * Tutto questo file, nella Dem 4, sparisce: lo fa il broker STOMP.
 */
@Component
public class EchoWebSocketHandler extends TextWebSocketHandler {

	private static final Logger log = LoggerFactory.getLogger(EchoWebSocketHandler.class);

	/**
	 * L'elenco di chi e' collegato adesso. Una WebSocketSession e' una singola
	 * scheda del browser collegata: aprine due e qui dentro ce ne sono due.
	 *
	 * Perche' CopyOnWriteArraySet e non un normale HashSet?
	 * Perche' Spring gestisce ogni connessione su un thread diverso: mentre il ciclo
	 * for di aTutti() sta scorrendo l'elenco, un altro thread puo' aggiungere o togliere
	 * una sessione. Con un HashSet quel ciclo esploderebbe con ConcurrentModificationException.
	 * Questa versione e' fatta apposta per essere letta da tanti thread insieme.
	 */
	private final Set<WebSocketSession> sessioni = new CopyOnWriteArraySet<>();

	// ObjectMapper e' l'oggetto di Jackson che converte Java <-> JSON.
	// Ce lo fornisce Spring: e' lo stesso che usa per i @RestController.
	private final ObjectMapper json;

	public EchoWebSocketHandler(ObjectMapper json) {
		this.json = json;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) {
		sessioni.add(session);
		log.info("sessione aperta   id={} aperte={}", session.getId(), sessioni.size());
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		// Togliere la sessione e' obbligatorio: se ce ne dimentichiamo, l'elenco cresce
		// all'infinito e continuiamo a provare a scrivere su connessioni morte.
		sessioni.remove(session);
		log.info("sessione chiusa   id={} motivo={} aperte={}", session.getId(), status, sessioni.size());
	}

	/**
	 * Arriva una stringa da un client.
	 *
	 * Nota bene: e' SOLO una stringa. Non c'e' un comando, non c'e' un'intestazione,
	 * non c'e' scritto a chi e' destinata. Il significato lo decidiamo noi qui,
	 * e in questa demo la regola e': "quello che mi mandi lo rimando a tutti".
	 */
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) {
		log.debug("ricevuto da {}: {}", session.getId(), message.getPayload());
		TestMessage ricevuto = json.readValue(message.getPayload(), TestMessage.class);
		aTutti(ricevuto);
	}

	/**
	 * Manda un messaggio a tutti i collegati.
	 *
	 * Guardate il for: e' questo il punto della demo. "Mandare a tutti" qui significa
	 * scorrere una lista che ci gestiamo da soli. Se domani volessimo mandare solo a mario,
	 * dovremmo anche tenerci una mappa utente -> sessioni: e' esattamente quello
	 * che faremo nella Dem 3, a mano, e che nella Dem 4 ci verra' regalato dal broker.
	 */
	public void aTutti(TestMessage messaggio) {
		// Il JSON lo costruiamo una volta sola fuori dal ciclo, non una per sessione.
		TextMessage frame = new TextMessage(json.writeValueAsString(messaggio));
		for (WebSocketSession sessione : sessioni) {
			invia(sessione, frame);
		}
	}

	/** Quanti sono collegati adesso. Lo espone TestController per poterlo vedere da curl. */
	public int aperte() {
		return sessioni.size();
	}

	/**
	 * L'invio vero e proprio, con le due precauzioni che servono sempre.
	 *
	 * 1) isOpen(): la sessione potrebbe essersi chiusa un istante fa.
	 * 2) synchronized: una WebSocketSession non regge due sendMessage contemporanei.
	 *    Se due thread scrivono insieme, i due messaggi si mescolano, il client riceve
	 *    spazzatura e la connessione cade. Il lock e' il prezzo minimo per evitarlo.
	 *
	 * E se un invio fallisce? Lo registriamo e andiamo avanti: un client morto
	 * non deve impedire agli altri di ricevere.
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
}
