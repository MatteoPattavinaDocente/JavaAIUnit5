package it.epicode.demo.benvenuto.service;

import it.epicode.demo.benvenuto.dto.BenvenutoRequest;
import it.epicode.demo.benvenuto.dto.EsitoInvio;
import jakarta.mail.MessagingException;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/**
 * Riempie il contesto, chiede l'HTML al motore e lo passa al servizio di invio
 * gia' scritto (slide 31). L'email non passa da un ViewResolver: non c'e' una
 * risposta HTTP da produrre (slide 27).
 */
@Service
public class MailTemplateService {

	private static final Logger log = LoggerFactory.getLogger(MailTemplateService.class);

	private static final int ORE_VALIDITA = 24;

	private final SpringTemplateEngine motore;
	private final EmailService email;
	private final MessageSource messaggi;
	private final String baseUrl;

	public MailTemplateService(SpringTemplateEngine motore,
			EmailService email,
			MessageSource messaggi,
			@Value("${app.base-url}") String baseUrl) {
		this.motore = motore;
		this.email = email;
		this.messaggi = messaggi;
		this.baseUrl = baseUrl;
	}

	/** Il solo metodo che il resto dell'applicazione chiama. */
	public EsitoInvio inviaBenvenuto(BenvenutoRequest richiesta) {
		Locale lingua = Locale.forLanguageTag(richiesta.lingua());
		String html = componi(richiesta.nome(), lingua);
		String oggetto = messaggi.getMessage("email.oggetto", null, lingua);

		long inizio = System.nanoTime();
		try {
			email.inviaHtml(richiesta.destinatario(), oggetto, testoAlternativo(richiesta.nome(), lingua), html);
			long millis = (System.nanoTime() - inizio) / 1_000_000;
			return new EsitoInvio(true, millis, "il server ha accettato il messaggio", null);
		} catch (MessagingException | RuntimeException ex) {
			long millis = (System.nanoTime() - inizio) / 1_000_000;
			Throwable radice = ex;
			while (radice.getCause() != null) {
				radice = radice.getCause();
			}
			log.warn("Invio fallito dopo {} ms", millis, ex);
			return new EsitoInvio(false, millis, "invio fallito",
					radice.getClass().getName() + ": " + radice.getMessage());
		}
	}

	/**
	 * L'HTML, senza inviare niente. Serve all'anteprima del frontend e ai test:
	 * il risultato di process() e' una stringa qualsiasi (slide 32).
	 */
	public String componi(String nome, Locale lingua) {
		// Fuori da una richiesta HTTP si usa org.thymeleaf.context.Context: una
		// semplice mappa di variabili (slide 27).
		var ctx = new Context(lingua);
		ctx.setVariables(Map.of(
				"nome", nome,
				"collegamento", baseUrl + "/api/auth/verify?token=ESEMPIO",
				"ore", ORE_VALIDITA));
		// Il nome non contiene l'estensione e non inizia con una barra (slide 34).
		return motore.process("email/benvenuto", ctx);
	}

	// La parte testuale del multiparte: serve a chi non vede l'HTML e alla
	// lettura vocale (slide 18).
	private String testoAlternativo(String nome, Locale lingua) {
		return messaggi.getMessage("email.testo", new Object[] { nome, ORE_VALIDITA }, lingua);
	}
}
