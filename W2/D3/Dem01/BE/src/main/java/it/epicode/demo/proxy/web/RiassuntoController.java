package it.epicode.demo.proxy.web;

import it.epicode.demo.proxy.dto.RiassuntoRichiesta;
import it.epicode.demo.proxy.dto.RiassuntoRisposta;
import it.epicode.demo.proxy.service.RiassuntoService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Il nostro endpoint (slide 6). Il browser chiama questo, e questo chiama il
 * servizio esterno: il browser non parla mai direttamente con openrouter.ai.
 */
@RestController
@RequestMapping("/api")
public class RiassuntoController {

	private final RiassuntoService servizio;
	private final String baseUrl;
	private final String modello;
	private final boolean chiavePresente;

	public RiassuntoController(RiassuntoService servizio,
			@Value("${app.llm.base-url}") String baseUrl,
			@Value("${app.llm.model}") String modello,
			@Value("${app.llm.api-key:}") String apiKey) {
		this.servizio = servizio;
		this.baseUrl = baseUrl;
		this.modello = modello;
		this.chiavePresente = apiKey != null && !apiKey.isBlank();
	}

	@PostMapping("/riassunto")
	public RiassuntoRisposta riassunto(@RequestBody RiassuntoRichiesta richiesta) {
		return servizio.riassumi(richiesta.testo());
	}

	/**
	 * Che cosa il frontend puo' sapere della configurazione: l'indirizzo base e
	 * il modello si possono mostrare, la chiave no - nemmeno abbreviata
	 * (slide 37). Qui si dice soltanto SE c'e'.
	 */
	@GetMapping("/configurazione")
	public Map<String, Object> configurazione() {
		return Map.of(
				"baseUrl", baseUrl,
				"modello", modello,
				"chiavePresente", chiavePresente);
	}
}
