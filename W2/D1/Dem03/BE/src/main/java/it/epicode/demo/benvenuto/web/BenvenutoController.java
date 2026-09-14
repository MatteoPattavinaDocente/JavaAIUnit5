package it.epicode.demo.benvenuto.web;

import it.epicode.demo.benvenuto.dto.BenvenutoRequest;
import it.epicode.demo.benvenuto.dto.EsitoInvio;
import it.epicode.demo.benvenuto.service.MailTemplateService;
import java.util.Locale;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/benvenuto")
public class BenvenutoController {

	private final MailTemplateService servizio;

	public BenvenutoController(MailTemplateService servizio) {
		this.servizio = servizio;
	}

	@PostMapping("/invia")
	public EsitoInvio invia(@RequestBody BenvenutoRequest richiesta) {
		return servizio.inviaBenvenuto(richiesta);
	}

	/**
	 * L'HTML elaborato, senza inviare niente. Il motore restituisce una stringa:
	 * qui la consegniamo come corpo della risposta, nella demo 2 la passavamo a
	 * MimeMessageHelper. E' lo stesso testo (slide 26).
	 */
	@GetMapping(value = "/anteprima", produces = MediaType.TEXT_HTML_VALUE)
	public String anteprima(@RequestParam(defaultValue = "Anna") String nome,
			@RequestParam(defaultValue = "it") String lingua) {
		return servizio.componi(nome, Locale.forLanguageTag(lingua));
	}
}
