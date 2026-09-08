package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.TestMessage;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Due endpoint HTTP di servizio per pilotare il canale dal terminale.
 *
 * Sono utili perche' fanno vedere una cosa importante: il messaggio non deve per forza
 * partire da un browser collegato. Puo' partire da un punto qualsiasi del backend
 * (qui un controller, nella Dem 3 il service delle notifiche).
 */
@RestController
public class TestController {

	private final EchoWebSocketHandler handler;

	public TestController(EchoWebSocketHandler handler) {
		this.handler = handler;
	}

	/**
	 * POST /api/demo/broadcast?text=ciao
	 *
	 * Manda una stringa a tutti i collegati partendo da una normalissima chiamata HTTP.
	 * Da provare: curl -X POST "http://localhost:8080/api/demo/broadcast?text=ciao"
	 * e guardare il messaggio comparire nella pagina React senza averla toccata.
	 */
	@PostMapping("/api/demo/broadcast")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void broadcast(@RequestParam String text) {
		handler.aTutti(new TestMessage(text));
	}

	/**
	 * GET /api/demo/sessions -> { "aperte": 2 }
	 *
	 * Il primo posto dove guardare quando "il messaggio non arriva":
	 * se qui c'e' scritto 0, il problema non e' l'invio, e' che nessuno e' collegato.
	 */
	@GetMapping("/api/demo/sessions")
	public Map<String, Integer> sessioni() {
		return Map.of("aperte", handler.aperte());
	}
}
