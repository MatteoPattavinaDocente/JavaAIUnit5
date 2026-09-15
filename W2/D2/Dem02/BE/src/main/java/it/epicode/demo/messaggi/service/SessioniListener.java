package it.epicode.demo.messaggi.service;

import java.security.Principal;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Il primo posto dove guardare quando un messaggio non arriva: c'e' una
 * sessione, e ha un nome? (slide 12)
 *
 * Attenzione al significato: la chiusura di una scheda produce un evento di
 * disconnessione, un cavo staccato spesso no. Lo stato "in linea" va confermato
 * dal traffico, non solo da questi eventi.
 */
@Component
public class SessioniListener {

	private static final Logger log = LoggerFactory.getLogger(SessioniListener.class);

	private final AtomicInteger aperte = new AtomicInteger();

	@EventListener
	public void suConnessione(SessionConnectedEvent evento) {
		log.info("sessione aperta   utente={} aperte={}", nome(evento.getUser()), aperte.incrementAndGet());
	}

	@EventListener
	public void suDisconnessione(SessionDisconnectEvent evento) {
		log.info("sessione chiusa   utente={} aperte={}", nome(evento.getUser()), aperte.decrementAndGet());
	}

	public int aperte() {
		return aperte.get();
	}

	private String nome(Principal utente) {
		return utente == null ? "ANONIMO" : utente.getName();
	}
}
