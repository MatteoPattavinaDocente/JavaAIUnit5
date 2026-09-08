package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.TestMessage;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Il giro piu' semplice possibile, per verificare che STOMP funzioni prima di
 * metterci dentro il dominio.
 */
@RestController
public class TestController {

	private final SimpMessagingTemplate messagingTemplate;

	public TestController(SimpMessagingTemplate messagingTemplate) {
		this.messagingTemplate = messagingTemplate;
	}

	/**
	 * Il client manda un frame SEND a /app/ping.
	 *
	 * Il prefisso /app porta il messaggio qui, a codice nostro, e non al broker.
	 * @SendTo prende quello che il metodo restituisce e lo rimette su /topic/test,
	 * che invece e' una destinazione del broker: lo riceve chiunque sia iscritto li'.
	 *
	 * Due righe di annotazioni per un giro completo andata e ritorno. Nella Dem 2
	 * la stessa cosa richiedeva un handler, un elenco di sessioni e un ciclo for.
	 */
	@MessageMapping("/ping")
	@SendTo("/topic/test")
	public TestMessage ping(TestMessage in) {
		return in;
	}

	/**
	 * La stessa pubblicazione, ma da codice qualunque.
	 *
	 * SimpMessagingTemplate non ha bisogno che ci sia una richiesta WebSocket in corso:
	 * si puo' chiamare da un controller HTTP, da un lavoro schedulato, da un service.
	 * Percio' la demo si puo' pilotare anche da curl, senza browser:
	 *
	 *   curl -X POST "http://localhost:8080/api/demo/broadcast?text=ciao"
	 */
	@PostMapping("/api/demo/broadcast")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void broadcast(@RequestParam String text) {
		messagingTemplate.convertAndSend("/topic/test", new TestMessage(text));
	}
}
