package it.epicode.demo.canale.web;

import it.epicode.demo.canale.dto.LoginRequest;
import it.epicode.demo.canale.dto.LoginRisposta;
import it.epicode.demo.canale.dto.Presenza;
import it.epicode.demo.canale.service.CanaleService;
import it.epicode.demo.canale.service.SessioniListener;
import it.epicode.demo.canale.service.TokenStore;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

	private final TokenStore token;
	private final CanaleService canale;
	private final SessioniListener sessioni;

	public ApiController(TokenStore token, CanaleService canale, SessioniListener sessioni) {
		this.token = token;
		this.canale = canale;
		this.sessioni = sessioni;
	}

	/** Nessuna password: consegna un token per il nome che gli si passa. */
	@PostMapping("/login")
	public LoginRisposta login(@RequestBody LoginRequest richiesta) {
		return new LoginRisposta(richiesta.utente(), token.emetti(richiesta.utente()));
	}

	@GetMapping("/presenza")
	public Presenza presenza() {
		return canale.presenza();
	}

	/**
	 * Il conteggio degli eventi di ciclo di vita, che non coincide sempre con
	 * il numero di utenti del registro: una sessione anonima apre e chiude come
	 * le altre, ma nel registro non compare.
	 */
	@GetMapping("/sessioni")
	public Map<String, Integer> sessioni() {
		return Map.of("aperte", sessioni.aperte());
	}
}
