package it.epicode.demo.chiavi.web;

import it.epicode.demo.chiavi.config.LlmProperties;
import it.epicode.demo.chiavi.dto.GenerazioneRichiesta;
import it.epicode.demo.chiavi.dto.GenerazioneRisposta;
import it.epicode.demo.chiavi.dto.VoceRegistro;
import it.epicode.demo.chiavi.repository.ChiamataLlmRepository;
import it.epicode.demo.chiavi.service.GenerazioneService;
import it.epicode.demo.chiavi.service.LimiteService;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Limit;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class GenerazioneController {

	private final GenerazioneService servizio;
	private final LimiteService limiti;
	private final ChiamataLlmRepository repository;
	private final LlmProperties proprieta;

	public GenerazioneController(GenerazioneService servizio, LimiteService limiti,
			ChiamataLlmRepository repository, LlmProperties proprieta) {
		this.servizio = servizio;
		this.limiti = limiti;
		this.repository = repository;
		this.proprieta = proprieta;
	}

	@PostMapping("/genera")
	public GenerazioneRisposta genera(@RequestBody GenerazioneRichiesta richiesta) {
		return servizio.genera(richiesta.utente(), richiesta.testo());
	}

	/**
	 * Della configurazione si mostra tutto TRANNE la chiave: di quella si dice
	 * soltanto se c'e' (slide 37).
	 */
	@GetMapping("/configurazione")
	public Map<String, Object> configurazione() {
		return Map.of(
				"baseUrl", proprieta.baseUrl(),
				"modello", proprieta.model(),
				"maxTokens", proprieta.maxTokens(),
				"limiteGiornalieroToken", proprieta.limiteGiornalieroToken(),
				"chiavePresente", proprieta.chiavePresente());
	}

	@GetMapping("/consumo")
	public Map<String, Object> consumo(@RequestParam String utente) {
		int usati = limiti.usatiOggi(utente);
		return Map.of(
				"utente", utente,
				"usatiOggi", usati,
				"limiteGiornaliero", limiti.limiteGiornaliero(),
				"residui", Math.max(0, limiti.limiteGiornaliero() - usati));
	}

	@GetMapping("/registro")
	public List<VoceRegistro> registro() {
		return repository.findAllByOrderByIdDesc(Limit.of(30)).stream()
				.map(VoceRegistro::da)
				.toList();
	}

	/** Solo per la lezione: azzera il registro fra una prova e l'altra. */
	@DeleteMapping("/registro")
	public void azzera() {
		repository.deleteAll();
	}
}
