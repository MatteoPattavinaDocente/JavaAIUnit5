package it.epicode.demo.verifica.web;

import it.epicode.demo.verifica.dto.UtenteView;
import it.epicode.demo.verifica.service.RegistrazioneService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint che esistono SOLO per la lezione: mostrano lo stato del database e i
 * valori dei token. In un'applicazione vera non devono esistere - chi vede il
 * valore di un token puo' attivare l'account di un altro.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoController {

	private final RegistrazioneService servizio;

	public DemoController(RegistrazioneService servizio) {
		this.servizio = servizio;
	}

	@GetMapping("/utenti")
	public List<UtenteView> utenti() {
		return servizio.elenco();
	}

	@DeleteMapping("/utenti")
	public void azzera() {
		servizio.azzera();
	}
}
