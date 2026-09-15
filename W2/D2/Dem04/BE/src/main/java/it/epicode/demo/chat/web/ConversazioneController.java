package it.epicode.demo.chat.web;

import it.epicode.demo.chat.dto.ConversazioneRiepilogo;
import it.epicode.demo.chat.dto.LoginRequest;
import it.epicode.demo.chat.dto.LoginRisposta;
import it.epicode.demo.chat.dto.PaginaMessaggi;
import it.epicode.demo.chat.service.ChatService;
import it.epicode.demo.chat.service.TokenStore;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Le rotte REST. In un progetto con Spring Security l'utente arriverebbe dal
 * Principal della richiesta HTTP (slide 42): qui, senza autenticazione sul lato
 * web, lo passa il client come parametro. Da dire in aula, perche' e' una
 * scorciatoia della demo e non un modello.
 */
@RestController
@RequestMapping("/api")
public class ConversazioneController {

	private final ChatService servizio;
	private final TokenStore token;

	public ConversazioneController(ChatService servizio, TokenStore token) {
		this.servizio = servizio;
		this.token = token;
	}

	@PostMapping("/login")
	public LoginRisposta login(@RequestBody LoginRequest richiesta) {
		return new LoginRisposta(richiesta.utente(), token.emetti(richiesta.utente()));
	}

	@GetMapping("/conversazioni")
	public List<ConversazioneRiepilogo> elenco(@RequestParam String utente) {
		return servizio.riepilogo(utente);
	}

	/** L'ultima pagina della cronologia, letta all'apertura della conversazione. */
	@GetMapping("/messaggi")
	public PaginaMessaggi cronologia(@RequestParam String utente, @RequestParam String con) {
		return servizio.cronologia(utente, con);
	}

	/** Il recupero del buco temporale dopo una riconnessione (slide 38). */
	@GetMapping("/messaggi/dopo")
	public PaginaMessaggi dopo(@RequestParam String utente,
			@RequestParam String con,
			@RequestParam(defaultValue = "0") Long dopo) {
		return servizio.dopo(utente, con, dopo);
	}

	@PostMapping("/conversazioni/{id}/letti")
	public void segnaLetti(@PathVariable Long id, @RequestParam String utente) {
		servizio.segnaLetti(id, utente);
	}

	// --- solo per la lezione ----------------------------------------------------

	@PostMapping("/demo/riempi")
	public Map<String, Integer> riempi(@RequestParam String utente,
			@RequestParam String con,
			@RequestParam(defaultValue = "120") int quanti) {
		return Map.of("creati", servizio.riempi(utente, con, quanti));
	}

	@DeleteMapping("/demo/tutto")
	public void azzera() {
		servizio.azzera();
	}
}
