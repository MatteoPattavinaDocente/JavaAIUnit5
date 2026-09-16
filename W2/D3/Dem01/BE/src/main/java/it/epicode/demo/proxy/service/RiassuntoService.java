package it.epicode.demo.proxy.service;

import it.epicode.demo.proxy.dto.LlmCorpo;
import it.epicode.demo.proxy.dto.LlmRisposta;
import it.epicode.demo.proxy.dto.RiassuntoRisposta;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/**
 * Il servizio applicativo del proxy (slide 6): costruisce la richiesta esterna,
 * la invia e traduce la risposta nel NOSTRO formato.
 *
 * Il frontend non vede mai niente di quello che passa da qui: ne' la chiave,
 * ne' il corpo grezzo, ne' il messaggio d'errore del servizio esterno.
 */
@Service
public class RiassuntoService {

	private static final Logger log = LoggerFactory.getLogger(RiassuntoService.class);

	private static final String ISTRUZIONI = """
			Sei un assistente che riassume testi in italiano.
			Rispondi con tre frasi al massimo, senza introduzioni e senza elenchi.
			""";

	private final RestClient client;
	private final String modello;
	private final int maxTokens;
	private final boolean ragionamento;

	public RiassuntoService(RestClient llmClient,
			@Value("${app.llm.model}") String modello,
			@Value("${app.llm.max-tokens}") int maxTokens,
			@Value("${app.llm.reasoning:false}") boolean ragionamento) {
		this.client = llmClient;
		this.modello = modello;
		this.maxTokens = maxTokens;
		this.ragionamento = ragionamento;
	}

	public RiassuntoRisposta riassumi(String testo) {
		var corpo = LlmCorpo.di(modello, maxTokens, ragionamento, ISTRUZIONI,
				"Riassumi il testo seguente in una sola riga, massimo 15 parole:\n\n" + testo);

		long inizio = System.nanoTime();
		LlmRisposta risposta = chiama(corpo);
		long millis = (System.nanoTime() - inizio) / 1_000_000;

		// Il registro: utente, modello, token, esito, durata (slide 43). Senza
		// questi dati non si risponde alla domanda «perche' la fattura e'
		// cresciuta». La tabella arriva nella demo 4.
		//
		// Il modello si legge dalla RISPOSTA, non dalla richiesta: OpenRouter
		// puo' servire la richiesta con una variante di quello chiesto.
		// Con un modello di ragionamento parte dei token di uscita NON finisce
		// nella risposta: finisce nei passaggi intermedi. Nel registro vanno
		// distinti, altrimenti il conto dei token sembra sbagliato.
		log.info("chiamata esterna: modello={} in={} out={} ragionamento={}car stop={} durata={}ms",
				risposta.model(), risposta.tokenIngresso(), risposta.tokenUscita(),
				risposta.lunghezzaRagionamento(), risposta.motivoArresto(), millis);

		// Il caso tipico dei modelli di ragionamento: il tetto dei token si
		// esaurisce durante i passaggi intermedi e il testo finale resta vuoto.
		// Il rimedio e' alzare LLM_MAX_TOKENS, non riprovare.
		if (risposta.primoTesto().isBlank()) {
			log.warn("risposta senza testo: stop={} - se stop=length alzare app.llm.max-tokens",
					risposta.motivoArresto());
		}

		return new RiassuntoRisposta(
				risposta.primoTesto(),
				risposta.model(),
				risposta.tokenIngresso(),
				risposta.tokenUscita(),
				risposta.motivoArresto(),
				millis);
	}

	/**
	 * RestClient permette di intercettare per FASCE di codice di stato
	 * (slide 10). Le eccezioni che lanciamo sono nostre, non quelle del client
	 * HTTP: il resto dell'applicazione non deve conoscere il servizio esterno.
	 */
	private LlmRisposta chiama(LlmCorpo corpo) {
		try {
			return client.post()
					.uri("/chat/completions")
					.body(corpo)
					.retrieve()

					// Il 429 ha un rimedio proprio: attendere. Va distinto dagli
					// altri 4xx (slide 11). Su OpenRouter arriva sia dal loro
					// limite sia da quello del fornitore a valle.
					.onStatus(stato -> stato.value() == 429, (richiesta, risposta) -> {
						String riprova = risposta.getHeaders().getFirst("retry-after");
						log.warn("429 dal servizio esterno, retry-after={}", riprova);
						throw new LimiteRaggiuntoException(riprova);
					})

					// Un 4xx e' un errore nostro: richiesta malformata, chiave
					// sbagliata, modello inesistente. Il 402 di OpenRouter dice
					// che il credito e' finito.
					.onStatus(HttpStatusCode::is4xxClientError, (richiesta, risposta) -> {
						// Il corpo va LETTO e messo nel log, non nella risposta:
						// contiene dettagli interni (slide 9 e 10).
						log.error("4xx dal servizio esterno: {} - corpo: {}",
								risposta.getStatusCode(), corpoDi(risposta));
						throw new RichiestaEsternaNonValidaException(
								"il servizio ha rifiutato la richiesta: " + risposta.getStatusCode());
					})

					// Un 5xx e' un guasto loro: si puo' ritentare, con attesa
					// crescente (slide 9 e 23).
					.onStatus(HttpStatusCode::is5xxServerError, (richiesta, risposta) -> {
						log.error("5xx dal servizio esterno: {} - corpo: {}",
								risposta.getStatusCode(), corpoDi(risposta));
						throw new ServizioNonDisponibileException(
								"il servizio non ha risposto correttamente: " + risposta.getStatusCode());
					})

					.body(LlmRisposta.class);

		} catch (ResourceAccessException ex) {
			// Host sbagliato, porta chiusa, timeout di lettura scaduto: qui non
			// c'e' nessun codice di stato, perche' non c'e' stata una risposta.
			log.error("il servizio esterno non e' raggiungibile: {}", ex.getMessage());
			throw new ServizioNonDisponibileException("il servizio esterno non e' raggiungibile");
		}
	}

	/** Il corpo dell'errore serve al log, non al browser. */
	private String corpoDi(org.springframework.http.client.ClientHttpResponse risposta) {
		try (var flusso = risposta.getBody()) {
			return new String(flusso.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			return "(corpo non leggibile: " + ex.getMessage() + ")";
		}
	}
}
