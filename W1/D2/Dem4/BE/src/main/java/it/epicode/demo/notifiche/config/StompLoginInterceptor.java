package it.epicode.demo.notifiche.config;

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
 * Chi e' la sessione? Qui lo decidiamo.
 *
 * Ogni messaggio in arrivo dai client passa da questo metodo. A noi ne interessa
 * uno solo: il primo, il frame CONNECT. Da li' leggiamo l'intestazione "login"
 * e la trasformiamo nel Principal della sessione.
 *
 * Da quel momento in poi Spring sa chi e' quella sessione, e possiamo:
 *  - farci passare il Principal come parametro nei metodi @MessageMapping;
 *  - mandare messaggi a un utente con convertAndSendToUser, senza sapere niente
 *    delle sue sessioni.
 *
 * COSA CAMBIA RISPETTO ALLA DEM 3
 * La' il nome viaggiava nell'indirizzo (?utente=mario). Un'intestazione del CONNECT
 * e' meglio: non finisce nei log del proxy e non resta scritta nella barra degli
 * indirizzi. Ma attenzione, la sostanza NON cambia: nessuno sta verificando niente.
 * Chiunque puo' scrivere "login: lucia".
 *
 * In produzione qui ci sarebbe un token (JWT o simile) da validare, e in caso di
 * token non valido si restituirebbe null per rifiutare la connessione.
 */
@Component
public class StompLoginInterceptor implements ChannelInterceptor {

	private static final Logger log = LoggerFactory.getLogger(StompLoginInterceptor.class);

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		// StompHeaderAccessor e' la "lente" per leggere comando e intestazioni del frame.
		StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
			String login = accessor.getFirstNativeHeader("login");

			if (login == null) {
				// Senza Principal la sessione resta anonima: convertAndSendToUser non
				// avra' nessuno a cui consegnare e i messaggi personali spariranno
				// senza un errore. E' l'errore piu' difficile da diagnosticare di
				// tutta la demo, per questo lo scriviamo nel log.
				log.warn("CONNECT senza header login: sessione senza Principal, i messaggi personali verranno scartati in silenzio");
			} else {
				// setUser vuole un java.security.Principal, che e' un'interfaccia con
				// un solo metodo: getName(). Questa lambda e' l'implementazione piu'
				// piccola possibile, restituisce sempre il nome che abbiamo letto.
				accessor.setUser(() -> login);
			}
		}

		// Il messaggio prosegue comunque. Restituire null lo fermerebbe qui.
		return message;
	}
}
