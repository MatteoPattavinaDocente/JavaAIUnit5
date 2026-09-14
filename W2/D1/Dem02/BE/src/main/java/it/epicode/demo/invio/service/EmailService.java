package it.epicode.demo.invio.service;

import it.epicode.demo.invio.dto.EsitoInvio;
import it.epicode.demo.invio.dto.InvioRequest;
import it.epicode.demo.invio.dto.VoceRegistro;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Un solo servizio, quattro modi di comporre lo stesso messaggio. Chi chiama
 * non conosce ne' JavaMailSender ne' l'indirizzo del server (slide 17).
 */
@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final RegistroInvii registro;
	private final String mittente;
	private final String rispondiA;

	public EmailService(JavaMailSender mailSender,
			RegistroInvii registro,
			@Value("${app.mail.mittente}") String mittente,
			@Value("${app.mail.rispondi-a}") String rispondiA) {
		this.mailSender = mailSender;
		this.registro = registro;
		this.mittente = mittente;
		this.rispondiA = rispondiA;
	}

	/**
	 * Il metodo che fa il lavoro. Nessuna @Async qui: l'asincronia la decide
	 * chi chiama, passando da InvioAsincrono.
	 */
	public EsitoInvio invia(InvioRequest richiesta) {
		long inizio = System.nanoTime();
		try {
			// Il ritardo finge un server di posta lento: senza, in locale tutto
			// dura 20 ms e la differenza fra sincrono e asincrono non si vede.
			if (richiesta.ritardoMs() > 0) {
				Thread.sleep(richiesta.ritardoMs());
			}

			switch (richiesta.modo()) {
				case TESTO -> inviaTesto(richiesta);
				case HTML -> inviaMime(richiesta, false, false);
				case ALLEGATO -> inviaMime(richiesta, true, false);
				case INLINE -> inviaMime(richiesta, false, true);
			}

			long millis = (System.nanoTime() - inizio) / 1_000_000;
			log.info("Inviato [{}] a {} in {} ms sul thread {}",
					richiesta.modo(), richiesta.destinatario(), millis, Thread.currentThread());
			registro.aggiungi(new VoceRegistro(Instant.now(), richiesta.destinatario(),
					richiesta.modo().name(), nomeThread(), true, millis, null));
			return new EsitoInvio(richiesta.modo().name(), richiesta.asincrono(), true,
					0, millis, "il server ha accettato il messaggio", null, null);

		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return fallito(richiesta, inizio, ex);
		} catch (MessagingException | RuntimeException ex) {
			// MessagingException e' controllata (slide 19); MailException e' a
			// runtime (slide 15). Qui le trattiamo allo stesso modo perche' al
			// frontend interessa solo che l'invio non e' riuscito.
			return fallito(richiesta, inizio, ex);
		}
	}

	// --- I tre modi di comporre -------------------------------------------------

	/** Solo testo: nessuna parte MIME, nessun helper (slide 16). */
	private void inviaTesto(InvioRequest richiesta) {
		var messaggio = new SimpleMailMessage();
		messaggio.setFrom(mittente);
		messaggio.setReplyTo(rispondiA);
		messaggio.setTo(richiesta.destinatario());
		messaggio.setSubject("Benvenuto, " + richiesta.nome());
		messaggio.setText(testoSemplice(richiesta.nome()));
		mailSender.send(messaggio);
	}

	/**
	 * Testo e HTML nello stesso messaggio, piu' eventuale allegato o immagine.
	 * Il secondo argomento del costruttore attiva il multiparte, richiesto dagli
	 * allegati; il terzo tiene le lettere accentate (slide 18).
	 */
	private void inviaMime(InvioRequest richiesta, boolean conAllegato, boolean conImmagine)
			throws MessagingException {

		MimeMessage messaggio = mailSender.createMimeMessage();
		var helper = new MimeMessageHelper(messaggio, true, "UTF-8");

		helper.setFrom(mittente);
		helper.setReplyTo(rispondiA);
		helper.setTo(richiesta.destinatario());
		helper.setSubject("Benvenuto, " + richiesta.nome());

		String html = conImmagine ? htmlConLogo(richiesta.nome()) : html(richiesta.nome());

		// Due argomenti: prima la versione testuale, poi quella HTML. Il client
		// mostra l'HTML se puo', altrimenti ricade sul testo - che serve anche
		// alla lettura vocale (slide 18).
		helper.setText(testoSemplice(richiesta.nome()), html);

		if (conAllegato) {
			helper.addAttachment("ricevuta.txt", new ClassPathResource("allegati/ricevuta.txt"));
		}
		if (conImmagine) {
			// addInline va chiamato DOPO setText, non prima: l'ordine conta
			// perche' l'helper costruisce le parti man mano (slide 21).
			helper.addInline("logo", new ClassPathResource("static/logo.png"));
		}

		mailSender.send(messaggio);
	}

	// --- Contenuti --------------------------------------------------------------

	private String testoSemplice(String nome) {
		return """
				Ciao %s,
				il tuo account e' stato creato.

				Se leggi questo testo, il tuo client non ha mostrato la versione HTML.
				""".formatted(nome);
	}

	// HTML scritto a mano dentro il codice: nella demo 3 finisce in un template.
	private String html(String nome) {
		return """
				<!DOCTYPE html>
				<html><body style="font-family: Arial, sans-serif; color:#1c1f24;">
				  <table width="600" cellpadding="0" cellspacing="0"><tr><td>
				    <h2 style="color:#061e37;">Benvenuto, %s</h2>
				    <p>Il tuo account e' stato creato. Le lettere accentate: pero', citta', gia'.</p>
				    <a href="https://epicode.com" style="background:#061e37;color:#fff;padding:12px 18px;
				       text-decoration:none;border-radius:6px;display:inline-block;">Vai al profilo</a>
				  </td></tr></table>
				</body></html>
				""".formatted(nome);
	}

	// cid:logo richiama il nome dato a addInline (slide 21).
	private String htmlConLogo(String nome) {
		return """
				<!DOCTYPE html>
				<html><body style="font-family: Arial, sans-serif; color:#1c1f24;">
				  <table width="600" cellpadding="0" cellspacing="0"><tr><td>
				    <img src="cid:logo" alt="EPICODE" width="120" style="display:block;margin-bottom:16px;">
				    <h2 style="color:#061e37;">Benvenuto, %s</h2>
				    <p>L'immagine qui sopra viaggia dentro il messaggio, non su un server esterno.</p>
				  </td></tr></table>
				</body></html>
				""".formatted(nome);
	}

	// --- Errori -----------------------------------------------------------------

	private EsitoInvio fallito(InvioRequest richiesta, long inizio, Exception ex) {
		long millis = (System.nanoTime() - inizio) / 1_000_000;
		Throwable radice = ex;
		while (radice.getCause() != null) {
			radice = radice.getCause();
		}
		String causa = radice.getClass().getName() + ": " + radice.getMessage();

		log.warn("Invio [{}] fallito dopo {} ms: {}", richiesta.modo(), millis, causa);
		registro.aggiungi(new VoceRegistro(Instant.now(), richiesta.destinatario(),
				richiesta.modo().name(), nomeThread(), false, millis, causa));

		return new EsitoInvio(richiesta.modo().name(), richiesta.asincrono(), false, 0, millis,
				"invio fallito", ex.getClass().getSimpleName() + ": " + ex.getMessage(), causa);
	}

	// Il nome da solo non dice se il thread e' virtuale: SimpleAsyncTaskExecutor
	// li chiama "task-N" in entrambi i casi. isVirtual() lo dice.
	private String nomeThread() {
		Thread t = Thread.currentThread();
		String nome = t.getName().isBlank() ? "#" + t.threadId() : t.getName();
		return nome + (t.isVirtual() ? " (virtuale)" : " (di piattaforma)");
	}
}
