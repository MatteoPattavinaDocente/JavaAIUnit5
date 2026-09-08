package it.epicode.demo.notifiche.event;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * Chi e' iscritto a cosa.
 *
 * "Ma non l'avevamo appena tolto, questo registro?" E' la domanda giusta, e la
 * risposta e' la parte piu' interessante della Dem 4.
 *
 * Il broker questo elenco ce l'ha, e' il suo mestiere. Ma non lo espone: a lui
 * serve per CONSEGNARE, non per rispondere a domande di terzi. E a noi qui serve
 * per una cosa che il broker non fa: AUTORIZZARE.
 *
 * Sul broker semplice, infatti, le destinazioni non hanno un padrone. /topic/ordini/42
 * non appartiene a nessuno, e chiunque puo' scriverci. Se vogliamo la regola
 * "puoi scrivere solo dove sei iscritto", quella regola dobbiamo scriverla noi,
 * e per applicarla ci serve sapere chi e' iscritto dove.
 *
 * Quindi si': assomiglia al registro della Dem 3, ma il motivo e' diverso.
 * La' serviva per far arrivare i messaggi (lavoro che oggi fa il broker),
 * qui serve per decidere chi ha il diritto di mandarli.
 *
 * Come lo riempiamo: senza toccare nessun frame. Spring pubblica un evento per ogni
 * SUBSCRIBE e ogni UNSUBSCRIBE, e noi ci mettiamo in ascolto.
 */
@Component
public class SubscriptionRegistry {

	private static final Logger log = LoggerFactory.getLogger(SubscriptionRegistry.class);

	/**
	 * idSessione -> (idSottoscrizione -> destinazione)
	 *
	 * La chiave esterna e' la SESSIONE, non l'utente: la regola e' "questa sessione
	 * e' iscritta a questo topic". Con due schede aperte ci sono due sessioni, e una
	 * non eredita i diritti dell'altra.
	 *
	 * Perche' serve anche la mappa interna, e non basta un insieme di destinazioni?
	 * Perche' il frame UNSUBSCRIBE porta solo l'id della sottoscrizione ("sub-0"),
	 * non la destinazione. Senza questa corrispondenza, alla disiscrizione non
	 * sapremmo cosa togliere.
	 */
	private final Map<String, Map<String, String>> perSessione = new ConcurrentHashMap<>();

	@EventListener
	public void onSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessione = accessor.getSessionId();
		String idSottoscrizione = accessor.getSubscriptionId();
		String destinazione = accessor.getDestination();

		if (sessione == null || idSottoscrizione == null || destinazione == null) {
			return;
		}
		perSessione.computeIfAbsent(sessione, s -> new ConcurrentHashMap<>()).put(idSottoscrizione, destinazione);
		log.debug("SUBSCRIBE   sessione={} id={} destinazione={}", sessione, idSottoscrizione, destinazione);
	}

	@EventListener
	public void onUnsubscribe(SessionUnsubscribeEvent event) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
		String sessione = accessor.getSessionId();
		String idSottoscrizione = accessor.getSubscriptionId();

		if (sessione == null || idSottoscrizione == null) {
			return;
		}
		Map<String, String> sottoscrizioni = perSessione.get(sessione);
		if (sottoscrizioni == null) {
			return;
		}
		String destinazione = sottoscrizioni.remove(idSottoscrizione);
		log.debug("UNSUBSCRIBE sessione={} id={} destinazione={}", sessione, idSottoscrizione, destinazione);
	}

	/**
	 * Quando la sessione se ne va, i suoi diritti se ne vanno con lei.
	 *
	 * Senza questa riga il registro crescerebbe per sempre: e' la perdita di memoria
	 * classica di tutti i registri tenuti a mano. Vale la pena notarlo: ogni volta
	 * che teniamo uno stato per sessione, dobbiamo ricordarci anche di buttarlo via.
	 */
	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		perSessione.remove(event.getSessionId());
	}

	/** La domanda che ci interessa: questa sessione puo' scrivere su questa destinazione? */
	public boolean eSottoscritta(String sessione, String destinazione) {
		Map<String, String> sottoscrizioni = perSessione.get(sessione);
		return sottoscrizioni != null && sottoscrizioni.containsValue(destinazione);
	}

	/** Solo per diagnosi a lezione: cosa vede il server delle iscrizioni di questa sessione. */
	public Set<String> destinazioniDi(String sessione) {
		Map<String, String> sottoscrizioni = perSessione.get(sessione);
		return sottoscrizioni == null ? Set.of() : Set.copyOf(sottoscrizioni.values());
	}

	/** Tutto il registro, in sola lettura: utile da proiettare mentre si prova la demo. */
	public Map<String, Map<String, String>> tutte() {
		return Collections.unmodifiableMap(perSessione);
	}
}
