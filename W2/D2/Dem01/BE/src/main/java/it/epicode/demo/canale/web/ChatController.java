package it.epicode.demo.canale.web;

import it.epicode.demo.canale.dto.MessaggioPrivato;
import it.epicode.demo.canale.service.CanaleService;
import java.security.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

/**
 * @Controller, non @RestController: qui non si risponde a richieste HTTP. I
 * metodi ricevono i frame pubblicati dal client sul prefisso /app.
 */
@Controller
public class ChatController {

	private static final Logger log = LoggerFactory.getLogger(ChatController.class);

	private final CanaleService servizio;

	public ChatController(CanaleService servizio) {
		this.servizio = servizio;
	}

	/**
	 * Il percorso completo che il client usa e' /app/ping: il prefisso lo
	 * aggiunge la configurazione, non l'annotazione (slide 17).
	 *
	 * Il Principal lo passa Spring: e' l'utente della sessione, quello impostato
	 * dall'interceptor sul frame CONNECT.
	 */
	@MessageMapping("/ping")
	public void ping(Principal mittente) {
		if (mittente == null) {
			log.warn("ping da una sessione anonima: nessun Principal");
			return;
		}
		log.info("ping da {}", mittente.getName());
		servizio.inviaA(mittente.getName(), "server", "pong per " + mittente.getName());
	}

	/**
	 * Il mittente NON arriva dal payload: arriva dal Principal (slide 11). Il
	 * campo «da» dichiarato dal client si potrebbe scrivere a piacere.
	 */
	@MessageMapping("/privato")
	public void privato(MessaggioPrivato richiesta, Principal mittente) {
		if (mittente == null) {
			log.warn("messaggio da una sessione anonima: scartato");
			return;
		}
		servizio.inviaA(richiesta.destinatario(), mittente.getName(), richiesta.testo());
	}
}
