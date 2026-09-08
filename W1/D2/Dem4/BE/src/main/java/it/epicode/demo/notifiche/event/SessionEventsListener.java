package it.epicode.demo.notifiche.event;

import java.security.Principal;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Scrive nel log ogni sessione che si apre e che si chiude, con il nome dell'utente.
 *
 * A cosa serve davvero: e' il primo posto dove guardare quando "la notifica non arriva".
 * Le due domande sono sempre le stesse, in quest'ordine:
 *   1. c'e' una sessione aperta?
 *   2. quella sessione ha un nome, o e' ANONIMO?
 *
 * Se e' ANONIMO, il CONNECT e' arrivato senza l'header login: Spring non sa a chi
 * consegnare e convertAndSendToUser butta via il messaggio senza dire niente.
 * E' il guasto piu' difficile da trovare di questa demo, e queste righe di log
 * sono il modo piu' rapido per vederlo.
 */
@Component
public class SessionEventsListener {

	private static final Logger log = LoggerFactory.getLogger(SessionEventsListener.class);

	// AtomicInteger e non int: gli eventi arrivano da thread diversi, e un semplice
	// contatore++ da piu' thread puo' perdere per strada degli incrementi.
	private final AtomicInteger sessioniAperte = new AtomicInteger();

	// SessionConnectedEvent, non SessionConnectEvent: il primo scatta a handshake
	// concluso, quando il Principal e' gia' stato assegnato.
	@EventListener
	public void onConnected(SessionConnectedEvent event) {
		log.info("sessione aperta   utente={} aperte={}", nome(event.getUser()), sessioniAperte.incrementAndGet());
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		log.info("sessione chiusa   utente={} aperte={}", nome(event.getUser()), sessioniAperte.decrementAndGet());
	}

	private String nome(Principal user) {
		return user == null ? "ANONIMO" : user.getName();
	}
}
