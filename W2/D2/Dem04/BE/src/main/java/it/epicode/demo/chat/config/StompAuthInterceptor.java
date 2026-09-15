package it.epicode.demo.chat.config;

import it.epicode.demo.chat.service.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * L'identita' della sessione si legge dalle intestazioni del frame CONNECT
 * (slide 8 e 9). Non dall'indirizzo: gli URL finiscono nei log dei proxy e nella
 * cronologia del browser, e un token in query string ci resta.
 *
 * Il controllo avviene UNA VOLTA, all'apertura. Da quel momento la connessione
 * resta: la scadenza del token non la chiude (slide 9).
 */
@Component
public class StompAuthInterceptor implements ChannelInterceptor {

	private static final Logger log = LoggerFactory.getLogger(StompAuthInterceptor.class);

	private static final String PREFISSO = "Bearer ";

	private final TokenStore token;

	public StompAuthInterceptor(TokenStore token) {
		this.token = token;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
		if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
			return message;
		}

		String intestazione = accessor.getFirstNativeHeader("Authorization");
		if (intestazione == null || !intestazione.startsWith(PREFISSO)) {
			// Senza Principal la sessione resta anonima: le destinazioni utente
			// non funzionano e i messaggi vengono scartati in silenzio (slide 8).
			log.warn("CONNECT senza Authorization: sessione anonima, nessuna destinazione utente");
			return message;
		}

		String valore = intestazione.substring(PREFISSO.length());
		token.utenteDi(valore).ifPresentOrElse(
				utente -> {
					// La chiave con cui Spring risolve /user/... e' proprio
					// questo nome: se cambia fra due connessioni, i messaggi non
					// raggiungono l'utente (slide 8).
					accessor.setUser(() -> utente);
					log.info("CONNECT accettato   utente={}", utente);
				},
				() -> log.warn("CONNECT con token sconosciuto: sessione anonima"));

		return message;
	}
}
