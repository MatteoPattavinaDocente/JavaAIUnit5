package it.epicode.demo.api.web;

import it.epicode.demo.api.dto.GenerazioneRichiesta;
import it.epicode.demo.api.dto.GenerazioneRisposta;
import it.epicode.demo.api.service.GenerazioneService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GenerazioneController {

	private final GenerazioneService servizio;
	private final String baseUrl;
	private final String modello;
	private final int maxTokens;
	private final boolean chiavePresente;

	public GenerazioneController(GenerazioneService servizio,
			@Value("${app.llm.base-url}") String baseUrl,
			@Value("${app.llm.model}") String modello,
			@Value("${app.llm.max-tokens}") int maxTokens,
			@Value("${app.llm.api-key:}") String apiKey) {
		this.servizio = servizio;
		this.baseUrl = baseUrl;
		this.modello = modello;
		this.maxTokens = maxTokens;
		this.chiavePresente = apiKey != null && !apiKey.isBlank();
	}

	@PostMapping("/genera")
	public GenerazioneRisposta genera(@RequestBody GenerazioneRichiesta richiesta) {
		return servizio.genera(richiesta.testo(), richiesta.maxTokens());
	}

	@GetMapping("/configurazione")
	public Map<String, Object> configurazione() {
		return Map.of(
				"baseUrl", baseUrl,
				"modello", modello,
				"maxTokens", maxTokens,
				"chiavePresente", chiavePresente);
	}
}
