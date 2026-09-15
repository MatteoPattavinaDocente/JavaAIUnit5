package it.epicode.demo.messaggi.web;

import it.epicode.demo.messaggi.dto.MessaggioRichiesta;
import it.epicode.demo.messaggi.service.ChatService;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

	private static final Logger log = LoggerFactory.getLogger(ChatController.class);

	private final ChatService servizio;

	public ChatController(ChatService servizio) {
		this.servizio = servizio;
	}

	/** Il client pubblica su /app/chat: il prefisso lo aggiunge la configurazione. */
	@MessageMapping("/chat")
	public void ricevi(MessaggioRichiesta richiesta, Principal mittente) {
		if (mittente == null) {
			log.warn("messaggio da una sessione anonima: scartato");
			return;
		}
		servizio.inoltra(mittente.getName(), richiesta.destinatario(), richiesta.testo());
	}

	/** Solo per la lezione: consegna senza salvare (slide 18). */
	@MessageMapping("/chat-difettoso")
	public void riceviDifettoso(MessaggioRichiesta richiesta, Principal mittente) {
		if (mittente == null) {
			return;
		}
		servizio.inoltraSenzaSalvare(mittente.getName(), richiesta.destinatario(), richiesta.testo());
	}
}
