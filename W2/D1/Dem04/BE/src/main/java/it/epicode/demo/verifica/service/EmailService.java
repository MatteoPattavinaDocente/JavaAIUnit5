package it.epicode.demo.verifica.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/** Lo stesso servizio delle demo 2 e 3: riceve HTML pronto e lo spedisce. */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final String mittente;

	public EmailService(JavaMailSender mailSender, @Value("${app.mail.mittente}") String mittente) {
		this.mailSender = mailSender;
		this.mittente = mittente;
	}

	public void inviaHtml(String destinatario, String oggetto, String testo, String html)
			throws MessagingException {

		MimeMessage messaggio = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(messaggio, true, "UTF-8");
		helper.setFrom(mittente);
		helper.setTo(destinatario);
		helper.setSubject(oggetto);
		helper.setText(testo, html);
		helper.addInline("logo", new ClassPathResource("static/logo.png"));

		mailSender.send(messaggio);
		log.info("Email di conferma accettata dal server per {}", destinatario);
	}
}
