package it.epicode.demo.client.service;

import java.security.Principal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Oltre al conteggio, tiene lo storico degli ultimi eventi: e' quello che rende
 * visibile in pagina il doppio effetto di StrictMode (slide 30) e le sessioni
 * abbandonate da una pulizia mancante (slide 34).
 */
@Component
public class SessioniListener {

	private static final Logger log = LoggerFactory.getLogger(SessioniListener.class);

	private static final int MASSIMO = 20;

	public record Evento(Instant istante, String tipo, String utente, int aperteDopo) {
	}

	private final AtomicInteger aperte = new AtomicInteger();
	private final ConcurrentLinkedDeque<Evento> storico = new ConcurrentLinkedDeque<>();

	@EventListener
	public void suConnessione(SessionConnectedEvent evento) {
		int adesso = aperte.incrementAndGet();
		annota("APERTA", nome(evento.getUser()), adesso);
		log.info("sessione aperta   utente={} aperte={}", nome(evento.getUser()), adesso);
	}

	@EventListener
	public void suDisconnessione(SessionDisconnectEvent evento) {
		int adesso = aperte.decrementAndGet();
		annota("CHIUSA", nome(evento.getUser()), adesso);
		log.info("sessione chiusa   utente={} aperte={}", nome(evento.getUser()), adesso);
	}

	public int aperte() {
		return aperte.get();
	}

	/** Dal piu' recente. */
	public List<Evento> storico() {
		return storico.stream().toList();
	}

	public void azzeraStorico() {
		storico.clear();
	}

	private void annota(String tipo, String utente, int aperteDopo) {
		storico.addFirst(new Evento(Instant.now(), tipo, utente, aperteDopo));
		while (storico.size() > MASSIMO) {
			storico.pollLast();
		}
	}

	private String nome(Principal utente) {
		return utente == null ? "ANONIMO" : utente.getName();
	}
}
