package it.epicode.demo.verifica.service;

import jakarta.mail.MessagingException;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

/** Riempie il contesto, chiede l'HTML al motore, lo passa a EmailService. */
@Service
public class MailTemplateService {

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

	public void inviaConferma(String destinatario, String token, long oreValidita)
			throws MessagingException {

		// Il collegamento e' assoluto e porta il token in coda all'indirizzo
		// (slide 37). Il valore e' gia' sicuro per un URL: lo ha prodotto
		// l'encoder Base64 per URL.
		String collegamento = baseUrl + "/api/auth/verify?token=" + token;

		Locale lingua = Locale.ITALIAN;
		var ctx = new Context(lingua);
		ctx.setVariables(Map.of(
				"collegamento", collegamento,
				"ore", oreValidita));

		String html = motore.process("email/conferma", ctx);
		String oggetto = messaggi.getMessage("email.oggetto", null, lingua);
		String testo = messaggi.getMessage("email.testo", new Object[] { oreValidita, collegamento }, lingua);

		email.inviaHtml(destinatario, oggetto, testo, html);
	}
}
