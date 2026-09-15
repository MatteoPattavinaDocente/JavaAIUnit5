package it.epicode.demo.messaggi.web;

import it.epicode.demo.messaggi.dto.LoginRequest;
import it.epicode.demo.messaggi.dto.LoginRisposta;
import it.epicode.demo.messaggi.dto.MessaggioRisposta;
import it.epicode.demo.messaggi.dto.Presenza;
import it.epicode.demo.messaggi.service.ChatService;
import it.epicode.demo.messaggi.service.TokenStore;
import java.util.Comparator;
import java.util.List;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ApiController {

	private final TokenStore token;
	private final ChatService chat;
	private final SimpUserRegistry registro;

	public ApiController(TokenStore token, ChatService chat, SimpUserRegistry registro) {
		this.token = token;
		this.chat = chat;
		this.registro = registro;
	}

	@PostMapping("/login")
	public LoginRisposta login(@RequestBody LoginRequest richiesta) {
		return new LoginRisposta(richiesta.utente(), token.emetti(richiesta.utente()));
	}

	/**
	 * La cronologia, letta una volta all'apertura della conversazione: il tempo
	 * reale consegna quello che accade mentre l'utente guarda la pagina, tutto
	 * il resto si recupera da qui (slide 22).
	 */
	@GetMapping("/messaggi")
	public List<MessaggioRisposta> cronologia(@RequestParam String utente, @RequestParam String con) {
		return chat.cronologia(utente, con);
	}

	@GetMapping("/presenza")
	public Presenza presenza() {
		List<Presenza.UtenteCollegato> collegati = registro.getUsers().stream()
				.map(u -> new Presenza.UtenteCollegato(u.getName(), u.getSessions().size()))
				.sorted(Comparator.comparing(Presenza.UtenteCollegato::nome))
				.toList();
		int sessioni = collegati.stream().mapToInt(Presenza.UtenteCollegato::sessioni).sum();
		return new Presenza(collegati.size(), sessioni, collegati);
	}

	/** Solo per la lezione: azzera l'archivio fra una prova e l'altra. */
	@DeleteMapping("/messaggi")
	public void azzera() {
		chat.azzera();
	}
}
