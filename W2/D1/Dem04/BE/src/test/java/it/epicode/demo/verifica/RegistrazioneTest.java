package it.epicode.demo.verifica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import it.epicode.demo.verifica.dto.EsitoVerifica;
import it.epicode.demo.verifica.repository.TokenVerificaRepository;
import it.epicode.demo.verifica.repository.UtenteRepository;
import it.epicode.demo.verifica.service.RegistrazioneService;
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Il test NON invia davvero (slide 42). Inviare attraverso Gmail dentro una
 * suite vorrebbe dire: rete obbligatoria, una credenziale dentro la CI, quota
 * consumata a ogni esecuzione, e per rileggere il messaggio un giro via IMAP
 * con tempi di consegna non deterministici. Risultato: test lento e instabile.
 *
 * Al suo posto, @MockitoBean sostituisce il bean JavaMailSender nel contesto
 * Spring. Tutto il resto viene esercitato davvero - il servizio, il motore
 * Thymeleaf, il token, il database: solo l'ultimo passo, la consegna, non
 * avviene. Il messaggio viene catturato e riletto come lo leggerebbe un client.
 *
 * Quello che questo test NON copre, e va detto in aula: che le credenziali
 * siano giuste, che la rete lasci uscire la 587, che Gmail accetti il
 * messaggio. Quella verifica resta manuale, alla demo.
 *
 * Verificare che il messaggio parta non basta: il test legge il collegamento e
 * lo usa (slide 43).
 */
@SpringBootTest
class RegistrazioneTest {

	/**
	 * Serve solo a fabbricare MimeMessage veri. Non e' configurato e non apre
	 * nessuna connessione: createMimeMessage() costruisce un messaggio vuoto
	 * su una sessione locale, niente di piu'.
	 */
	private static final JavaMailSenderImpl FABBRICA = new JavaMailSenderImpl();

	private static final Pattern COLLEGAMENTO =
			Pattern.compile("/api/auth/verify\\?token=([A-Za-z0-9_-]+)");

	@MockitoBean
	JavaMailSender posta;

	@Autowired
	RegistrazioneService servizio;

	@Autowired
	UtenteRepository utenti;

	@Autowired
	TokenVerificaRepository token;

	@BeforeEach
	void pulisci() {
		token.deleteAll();
		utenti.deleteAll();

		// La trappola del doppio: su un mock createMimeMessage() restituisce
		// null, e EmailService va in NullPointerException prima ancora di
		// arrivare a send(). Va stubbato con un messaggio vero.
		// willAnswer e non willReturn: ogni invio deve avere il PROPRIO
		// messaggio, altrimenti due chiamate si sovrascrivono a vicenda.
		given(posta.createMimeMessage()).willAnswer(chiamata -> FABBRICA.createMimeMessage());
	}

	@Test
	void inviaLEmailDiConferma() throws Exception {
		servizio.registra("anna@example.com", "segretissima");

		List<MimeMessage> inviati = messaggiInviati();

		// Quantita': un solo messaggio, per escludere gli invii duplicati.
		assertThat(inviati).hasSize(1);

		MimeMessage messaggio = inviati.getFirst();

		// A chi: l'indirizzo coincide con quello usato nella registrazione.
		assertThat(messaggio.getAllRecipients()[0].toString()).isEqualTo("anna@example.com");

		// Oggetto: quello che l'utente vede nella lista dei messaggi.
		assertThat(messaggio.getSubject()).isEqualTo("Conferma il tuo indirizzo");

		// Corpo: contiene il collegamento con un token non vuoto.
		String corpo = corpoDecodificato(messaggio);
		Matcher trovato = COLLEGAMENTO.matcher(corpo);
		assertThat(trovato.find()).as("il corpo contiene il collegamento di conferma").isTrue();

		String valore = trovato.group(1);
		assertThat(valore).isNotBlank();

		// Token: esiste nel database e risulta non ancora usato.
		assertThat(token.trovaPerValore(valore)).isPresent();
		assertThat(token.trovaPerValore(valore).orElseThrow().usato()).isFalse();
	}

	@Test
	void ilTokenFunzionaUnaVoltaSola() throws Exception {
		servizio.registra("bruno@example.com", "segretissima");

		String valore = tokenDalCorpo(messaggiInviati().getFirst());

		EsitoVerifica primo = servizio.verifica(valore);
		assertThat(primo.stato()).isEqualTo("VERIFICATO");
		assertThat(utenti.findByEmail("bruno@example.com").orElseThrow().isVerificato()).isTrue();

		// Il secondo clic non deve riattivare nulla (slide 41).
		EsitoVerifica secondo = servizio.verifica(valore);
		assertThat(secondo.stato()).isEqualTo("GIA_USATO");
	}

	@Test
	void ilRinvioInvalidaIlTokenPrecedente() throws Exception {
		servizio.registra("carla@example.com", "segretissima");
		servizio.registra("carla@example.com", "segretissima");

		List<MimeMessage> inviati = messaggiInviati();
		assertThat(inviati).hasSize(2);

		String vecchio = tokenDalCorpo(inviati.get(0));
		String nuovo = tokenDalCorpo(inviati.get(1));

		assertThat(nuovo).isNotEqualTo(vecchio);
		assertThat(servizio.verifica(vecchio).stato()).isEqualTo("GIA_USATO");
		assertThat(servizio.verifica(nuovo).stato()).isEqualTo("VERIFICATO");
	}

	@Test
	void unTokenSconosciutoNonVerificaNiente() {
		assertThat(servizio.verifica("questo-non-esiste").stato()).isEqualTo("NON_TROVATO");
	}

	/**
	 * Cattura tutti i messaggi passati a send(). atLeast(0) perche' il captor
	 * serve a raccogliere, non ad affermare: la quantita' la controlla il test.
	 */
	private List<MimeMessage> messaggiInviati() {
		ArgumentCaptor<MimeMessage> cattura = ArgumentCaptor.forClass(MimeMessage.class);
		verify(posta, atLeast(0)).send(cattura.capture());
		return cattura.getAllValues();
	}

	private String tokenDalCorpo(MimeMessage messaggio) throws Exception {
		Matcher trovato = COLLEGAMENTO.matcher(corpoDecodificato(messaggio));
		assertThat(trovato.find()).isTrue();
		return trovato.group(1);
	}

	/**
	 * Il messaggio e' multiparte: getContent() restituisce un Multipart, non
	 * una stringa. Attraversarlo e' anche il modo di far vedere in aula com'e'
	 * fatto dentro - ed e' il pezzo di codice che in un progetto vero si
	 * sostituisce con MimeMessageParser di Apache Commons Email.
	 */
	private String corpoDecodificato(MimeMessage messaggio) throws Exception {
		Object contenuto = messaggio.getContent();
		if (contenuto instanceof String testo) {
			return testo;
		}
		return testoDa((Multipart) contenuto);
	}

	private String testoDa(Multipart multiparte) throws Exception {
		var corpo = new StringBuilder();
		for (int i = 0; i < multiparte.getCount(); i++) {
			BodyPart parte = multiparte.getBodyPart(i);
			Object contenuto = parte.getContent();
			if (contenuto instanceof String testo) {
				corpo.append(testo).append(System.lineSeparator());
			} else if (contenuto instanceof Multipart annidato) {
				corpo.append(testoDa(annidato));
			}
		}
		return corpo.toString();
	}
}
