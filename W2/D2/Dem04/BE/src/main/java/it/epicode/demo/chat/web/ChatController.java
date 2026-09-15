package it.epicode.demo.chat.web;

import it.epicode.demo.chat.dto.MessaggioRichiesta;
import it.epicode.demo.chat.dto.Scrittura;
import it.epicode.demo.chat.dto.SegnaLetti;
import it.epicode.demo.chat.service.ChatService;
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

	@MessageMapping("/chat")
	public void ricevi(MessaggioRichiesta richiesta, Principal mittente) {
		if (mittente == null) {
			log.warn("messaggio da una sessione anonima: scartato");
			return;
		}
		servizio.inoltra(mittente.getName(), richiesta.destinatario(),
				richiesta.testo(), richiesta.idTemporaneo());
	}

	/**
	 * La conferma di lettura arriva come un messaggio qualsiasi, su una
	 * destinazione dedicata (slide 23).
	 */
	@MessageMapping("/letti")
	public void letti(SegnaLetti richiesta, Principal utente) {
		if (utente == null) {
			return;
		}
		servizio.segnaLetti(richiesta.conversazione(), utente.getName());
	}

	/** L'indicatore di scrittura: non salvato, e limitato lato client. */
	@MessageMapping("/scrivendo")
	public void scrivendo(Scrittura richiesta, Principal utente) {
		if (utente == null) {
			return;
		}
		servizio.scrive(utente.getName(), richiesta.destinatario());
	}
}
