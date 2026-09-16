package it.epicode.demo.estrazione.web;

import it.epicode.demo.estrazione.dto.EstrazioneRichiesta;
import it.epicode.demo.estrazione.dto.EstrazioneRisposta;
import it.epicode.demo.estrazione.service.EstrazioneService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class EstrazioneController {

	private final EstrazioneService servizio;
	private final String modello;
	private final int maxTokens;

	public EstrazioneController(EstrazioneService servizio,
			@Value("${app.llm.model}") String modello,
			@Value("${app.llm.max-tokens}") int maxTokens) {
		this.servizio = servizio;
		this.modello = modello;
		this.maxTokens = maxTokens;
	}

	@PostMapping("/estrai")
	public EstrazioneRisposta estrai(@RequestBody EstrazioneRichiesta richiesta) {
		return servizio.estrai(richiesta.testo(), richiesta.maxTokens(),
				richiesta.conSchema() == null || richiesta.conSchema());
	}

	@GetMapping("/configurazione")
	public Map<String, Object> configurazione() {
		return Map.of("modello", modello, "maxTokens", maxTokens);
	}
}
