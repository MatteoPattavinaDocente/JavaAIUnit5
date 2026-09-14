package it.epicode.demo.smtp.service;

import it.epicode.demo.smtp.dto.EsitoInvio;
import it.epicode.demo.smtp.dto.InvioRequest;
import it.epicode.demo.smtp.dto.StatoMail;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/**
 * Tutto l'invio sta qui: il resto dell'applicazione non conosce ne'
 * JavaMailSender ne' l'indirizzo del server di posta (slide 17).
 */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final String mittente;

	// Il bean lo ha creato l'auto-configurazione perche' in application.yml
	// c'e' spring.mail.host: se togliamo quella chiave, qui parte un errore di
	// avvio "no qualifying bean of type JavaMailSender" (slide 6).
	public EmailService(JavaMailSender mailSender, @Value("${app.mail.mittente}") String mittente) {
		this.mailSender = mailSender;
		this.mittente = mittente;
	}

	public EsitoInvio inviaTesto(InvioRequest richiesta) {
		var messaggio = new SimpleMailMessage();
		messaggio.setFrom(mittente);
		messaggio.setTo(richiesta.destinatario());
		messaggio.setSubject(richiesta.oggetto());
		messaggio.setText(richiesta.corpo());

		long inizio = System.nanoTime();
		try {
			mailSender.send(messaggio);
			long millis = (System.nanoTime() - inizio) / 1_000_000;
			log.info("Messaggio accettato dal server in {} ms, destinatario {}", millis, richiesta.destinatario());
			return EsitoInvio.ok(millis, "il server ha accettato il messaggio");
		} catch (MailException ex) {
			// MailException e' a runtime: nessuno ci obbliga a questo try. Lo
			// scriviamo perche' vogliamo restituire l'errore al frontend invece
			// di lasciare uscire un 500 muto (slide 15).
			long millis = (System.nanoTime() - inizio) / 1_000_000;
			log.warn("Invio fallito dopo {} ms: {}", millis, ex.getMessage());
			return EsitoInvio.errore(millis, ex);
		}
	}

	/** Che cosa sa davvero l'applicazione del server: letto dal bean, non dal file. */
	public StatoMail stato() {
		if (!(mailSender instanceof JavaMailSenderImpl impl)) {
			return new StatoMail("sconosciuto", -1, "", "", "", "", false,
					"il bean non e' un JavaMailSenderImpl");
		}
		Properties p = impl.getJavaMailProperties();
		try {
			// Apre la connessione, saluta il server e chiude: e' il modo piu'
			// corto per distinguere "configurazione sbagliata" da "server spento".
			impl.testConnection();
			return new StatoMail(impl.getHost(), impl.getPort(), sicura(impl.getUsername()),
					p.getProperty("mail.smtp.connectiontimeout", "illimitato"),
					p.getProperty("mail.smtp.timeout", "illimitato"),
					p.getProperty("mail.smtp.writetimeout", "illimitato"),
					true, null);
		} catch (Exception ex) {
			Throwable radice = ex;
			while (radice.getCause() != null) {
				radice = radice.getCause();
			}
			return new StatoMail(impl.getHost(), impl.getPort(), sicura(impl.getUsername()),
					p.getProperty("mail.smtp.connectiontimeout", "illimitato"),
					p.getProperty("mail.smtp.timeout", "illimitato"),
					p.getProperty("mail.smtp.writetimeout", "illimitato"),
					false, radice.getClass().getSimpleName() + ": " + radice.getMessage());
		}
	}

	// L'utenza si mostra, la password no: nemmeno in una demo.
	private String sicura(String utenza) {
		return (utenza == null || utenza.isBlank()) ? "(nessuna)" : utenza;
	}
}
