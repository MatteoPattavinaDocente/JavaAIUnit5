package it.epicode.demo.verifica.web;

import it.epicode.demo.verifica.dto.EsitoVerifica;
import it.epicode.demo.verifica.dto.RegistrazioneRequest;
import it.epicode.demo.verifica.service.RegistrazioneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final RegistrazioneService servizio;

	public AuthController(RegistrazioneService servizio) {
		this.servizio = servizio;
	}

	/**
	 * 202 Accepted, non 201 Created: l'account esiste ma non e' utilizzabile
	 * finche' l'indirizzo non e' confermato.
	 *
	 * Il corpo e' vuoto e la risposta e' identica per un indirizzo nuovo e per
	 * uno gia' presente (slide 41).
	 */
	@PostMapping("/register")
	public ResponseEntity<Void> registra(@RequestBody RegistrazioneRequest richiesta) {
		servizio.registra(richiesta.email(), richiesta.password());
		return ResponseEntity.accepted().build();
	}

	/**
	 * Il collegamento dell'email punta qui. Con Spring Security questo percorso
	 * e /register vanno dichiarati liberi, altrimenti la conferma chiede un
	 * accesso - e l'utente non puo' accedere, perche' non e' ancora verificato
	 * (slide 40).
	 */
	@GetMapping("/verify")
	public ResponseEntity<EsitoVerifica> verifica(@RequestParam String token) {
		EsitoVerifica esito = servizio.verifica(token);
		return switch (esito.stato()) {
			case "VERIFICATO" -> ResponseEntity.ok(esito);
			// Gia' usato, scaduto e non trovato sono tutti richieste non valide:
			// il 404 della slide 46 e' la variante piu' diffusa.
			default -> ResponseEntity.status(410).body(esito);
		};
	}
}
