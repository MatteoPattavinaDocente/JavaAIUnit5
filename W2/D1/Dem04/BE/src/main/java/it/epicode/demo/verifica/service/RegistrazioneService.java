package it.epicode.demo.verifica.service;

import it.epicode.demo.verifica.dto.EsitoVerifica;
import it.epicode.demo.verifica.dto.TokenView;
import it.epicode.demo.verifica.dto.UtenteView;
import it.epicode.demo.verifica.model.TokenVerifica;
import it.epicode.demo.verifica.model.Utente;
import it.epicode.demo.verifica.repository.TokenVerificaRepository;
import it.epicode.demo.verifica.repository.UtenteRepository;
import jakarta.mail.MessagingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Il flusso della slide 37: registra, genera, invia, apre, consuma, invalida.
 *
 * Registrazione riuscita ed email consegnata sono due eventi distinti: ognuno
 * puo' fallire da solo, e il codice lo deve rendere evidente.
 */
@Service
public class RegistrazioneService {

	private static final Logger log = LoggerFactory.getLogger(RegistrazioneService.class);

	private static final SecureRandom RNG = new SecureRandom();

	private final UtenteRepository utenti;
	private final TokenVerificaRepository token;
	private final TokenService tokenService;
	private final MailTemplateService posta;
	private final Duration validita;

	public RegistrazioneService(UtenteRepository utenti,
			TokenVerificaRepository token,
			TokenService tokenService,
			MailTemplateService posta,
			@Value("${app.token.validita-ore}") long validitaOre) {
		this.utenti = utenti;
		this.token = token;
		this.tokenService = tokenService;
		this.posta = posta;
		this.validita = Duration.ofHours(validitaOre);
	}

	/**
	 * Non restituisce niente di diverso a seconda che l'indirizzo esista o no
	 * (slide 41): se la risposta cambiasse, chiunque potrebbe scoprire chi e'
	 * iscritto provando indirizzi. La differenza la comunica l'email.
	 */
	@Transactional
	public void registra(String email, String password) {
		Optional<Utente> esistente = utenti.findByEmail(email);

		if (esistente.isPresent() && esistente.get().isVerificato()) {
			// Account gia' attivo: nessun token, nessuna email di conferma.
			// Verso l'esterno la risposta e' la stessa.
			log.info("Registrazione su indirizzo gia' verificato: nessuna email inviata");
			return;
		}

		Utente utente = esistente.orElseGet(() -> creaUtente(email, password));

		// Il rinvio invalida i token precedenti: se ne restassero due validi,
		// il vecchio collegamento continuerebbe a funzionare (slide 41).
		Instant adesso = Instant.now();
		List<TokenVerifica> precedenti = token.findByUtenteAndUsatoIlIsNull(utente);
		precedenti.forEach(t -> t.invalida(adesso));
		if (!precedenti.isEmpty()) {
			log.info("Invalidati {} token precedenti di {}", precedenti.size(), email);
		}

		TokenVerifica nuovo = token.save(
				new TokenVerifica(tokenService.genera(), utente, adesso.plus(validita)));

		try {
			posta.inviaConferma(email, nuovo.getValore(), validita.toHours());
		} catch (MessagingException | RuntimeException ex) {
			// L'utente e' registrato: manca soltanto l'email (slide 22). Non
			// annulliamo la registrazione, perche' l'email si puo' rimandare.
			log.error("Registrazione salvata ma email non inviata a {}: {}", email, ex.getMessage());
		}
	}

	/**
	 * Cerca il token, controlla scadenza e uso, poi marca l'utente come
	 * verificato e segna il token come usato (slide 37, punti 5 e 6).
	 */
	@Transactional
	public EsitoVerifica verifica(String valore) {
		Optional<TokenVerifica> trovato = token.trovaPerValore(valore);
		if (trovato.isEmpty()) {
			return EsitoVerifica.nonTrovato();
		}

		TokenVerifica t = trovato.get();
		Instant adesso = Instant.now();

		// L'ordine conta: prima l'uso, poi la scadenza. Un token usato ieri e
		// scaduto oggi deve dire "gia' usato", che e' l'informazione utile.
		if (t.usato()) {
			return EsitoVerifica.giaUsato();
		}
		if (t.scaduto(adesso)) {
			return EsitoVerifica.scaduto();
		}

		t.consuma(adesso);
		t.getUtente().verifica();
		log.info("Indirizzo verificato: {}", t.getUtente().getEmail());
		return EsitoVerifica.verificato(t.getUtente().getEmail());
	}

	/** Solo per la pagina della demo. */
	@Transactional(readOnly = true)
	public List<UtenteView> elenco() {
		Instant adesso = Instant.now();
		return utenti.findAllByOrderByCreatoIlDesc().stream()
				.map(u -> new UtenteView(u.getId(), u.getEmail(), u.isVerificato(), u.getCreatoIl(),
						token.findByUtenteOrderByIdDesc(u).stream()
								.map(t -> new TokenView(t.getId(), t.getValore(), t.getScadenza(),
										t.getUsatoIl(), !t.usato() && !t.scaduto(adesso)))
								.toList()))
				.toList();
	}

	/** Solo per la pagina della demo: azzera tutto fra una prova e l'altra. */
	@Transactional
	public void azzera() {
		token.deleteAll();
		utenti.deleteAll();
	}

	// --- credenziali ------------------------------------------------------------

	private Utente creaUtente(String email, String password) {
		byte[] sale = new byte[16];
		RNG.nextBytes(sale);
		String saleBase64 = Base64.getEncoder().encodeToString(sale);
		return utenti.save(new Utente(email, digest(password, saleBase64), saleBase64));
	}

	/**
	 * Un digest con sale, non la password in chiaro. Non e' il modo giusto di
	 * conservare una password: serve un algoritmo lento (bcrypt, argon2), che
	 * arriva con Spring Security. Qui l'argomento della giornata e' il token.
	 */
	private String digest(String password, String sale) {
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			sha.update(sale.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(sha.digest(password.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 non disponibile", ex);
		}
	}
}
