package it.epicode.demo.api.service;

import it.epicode.demo.api.dto.GenerazioneRisposta;
import it.epicode.demo.api.dto.LlmRichiesta;
import it.epicode.demo.api.dto.LlmRisposta;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Service
public class GenerazioneService {

	private static final Logger log = LoggerFactory.getLogger(GenerazioneService.class);

	/**
	 * Le istruzioni permanenti: il ruolo, le regole, il formato. Vanno nel turno
	 * con ruolo «system», che precede quello dell'utente (slide 17 e 26).
	 *
	 * La lingua va indicata esplicitamente, altrimenti segue quella del testo
	 * ricevuto (slide 26).
	 */
	private static final String ISTRUZIONI = """
			Sei un assistente che riassume testi.
			Rispondi sempre in italiano, con tre frasi al massimo.
			Non aggiungere introduzioni, titoli o elenchi: soltanto il riassunto.
			""";

	private final RestClient client;
	private final String modello;
	private final int maxTokensPredefiniti;
	private final String effort;

	public GenerazioneService(RestClient llmClient,
			@Value("${app.llm.model}") String modello,
			@Value("${app.llm.max-tokens}") int maxTokensPredefiniti,
			@Value("${app.llm.effort:}") String effort) {
		this.client = llmClient;
		this.modello = modello;
		this.maxTokensPredefiniti = maxTokensPredefiniti;
		this.effort = (effort == null || effort.isBlank()) ? null : effort;
	}

	public GenerazioneRisposta genera(String testo, Integer maxTokensRichiesti) {
		int maxTokens = maxTokensRichiesti == null ? maxTokensPredefiniti : maxTokensRichiesti;

		var corpo = LlmRichiesta.di(modello, maxTokens, ISTRUZIONI,
				"Riassumi il testo seguente:\n\n" + testo, effort);

		long inizio = System.nanoTime();
		LlmRisposta risposta = chiama(corpo);
		long millis = (System.nanoTime() - inizio) / 1_000_000;

		// I due numeri vanno salvati a ogni chiamata (slide 22 e 43): qui nel
		// log, nella demo 4 in una tabella. Il fornitore va con loro: lo stesso
		// modello servito da fornitori diversi ha prezzi e latenze diverse.
		log.info("modello={} fornitore={} in={} out={} stop={} durata={}ms",
				risposta.model(), risposta.provider(), risposta.tokenIngresso(),
				risposta.tokenUscita(), risposta.motivoArresto(), millis);

		return traduci(risposta, maxTokens, millis);
	}

	/**
	 * La traduzione nel nostro formato, con la verifica che va fatta SEMPRE:
	 * leggere finish_reason prima di usare il contenuto (slide 22 e 33).
	 */
	private GenerazioneRisposta traduci(LlmRisposta risposta, int maxTokens, long millis) {
		List<GenerazioneRisposta.SceltaVista> scelte = risposta.choices() == null
				? List.of()
				: risposta.choices().stream()
						.map(s -> {
							var m = s.message();
							String contenuto = m == null ? null : m.content();
							String rifiuto = m == null ? null : m.refusal();
							String ragionamento = m == null ? null : m.reasoning();
							return new GenerazioneRisposta.SceltaVista(
									s.index(),
									s.finish_reason(),
									s.native_finish_reason(),
									contenuto == null ? 0 : contenuto.length(),
									rifiuto != null && !rifiuto.isBlank(),
									ragionamento != null && !ragionamento.isBlank());
						})
						.toList();

		String avviso = null;
		boolean utilizzabile = true;

		if (risposta.inErrore()) {
			// 200 con l'errore nel corpo: il guasto arriva dal fornitore a valle
			// e il codice HTTP non lo racconta (slide 33).
			utilizzabile = false;
			avviso = "il servizio ha risposto 200 con un errore nel corpo: " + risposta.error().message();
			log.warn("200 con errore nel corpo: {}", risposta.error());
		} else if (risposta.rifiutata()) {
			// 200, ma il contenuto non c'e': va gestito prima di leggerlo.
			utilizzabile = false;
			String motivo = risposta.motivoRifiuto().orElse("non dichiarato");
			avviso = "il servizio ha declinato la richiesta (motivo: " + motivo + ")";
			log.warn("risposta rifiutata: {}", avviso);
		} else if (risposta.tagliata()) {
			utilizzabile = false;
			avviso = "la risposta e' stata tagliata dal tetto dei token: max_tokens vale "
					+ maxTokens + ", alzalo";
			log.warn("risposta troncata con max_tokens={}", maxTokens);
		} else if (risposta.testo().isEmpty()) {
			utilizzabile = false;
			avviso = risposta.choices() == null || risposta.choices().isEmpty()
					? "choices e' vuoto: nessuna risposta"
					: "il messaggio non contiene testo (content nullo o vuoto)";
		}

		return new GenerazioneRisposta(
				risposta.testo().orElse(""),
				risposta.testoIngenuo(),
				utilizzabile,
				avviso,
				risposta.model(),
				risposta.provider(),
				risposta.motivoArresto(),
				risposta.motivoArrestoNativo(),
				scelte,
				risposta.tokenIngresso(),
				risposta.tokenUscita(),
				maxTokens,
				millis);
	}

	private LlmRisposta chiama(LlmRichiesta corpo) {
		try {
			return client.post()
					.uri("/chat/completions")
					.body(corpo)
					.retrieve()
					.onStatus(stato -> stato.value() == 429, (req, res) -> {
						throw new LimiteRaggiuntoException(res.getHeaders().getFirst("retry-after"));
					})
					.onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
						log.error("4xx dal servizio esterno: {} - corpo: {}", res.getStatusCode(), corpoDi(res));
						throw new RichiestaEsternaNonValidaException(
								"il servizio ha rifiutato la richiesta: " + res.getStatusCode());
					})
					.onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
						log.error("5xx dal servizio esterno: {} - corpo: {}", res.getStatusCode(), corpoDi(res));
						throw new ServizioNonDisponibileException(
								"il servizio non ha risposto correttamente: " + res.getStatusCode());
					})
					.body(LlmRisposta.class);

		} catch (ResourceAccessException ex) {
			log.error("il servizio esterno non e' raggiungibile: {}", ex.getMessage());
			throw new ServizioNonDisponibileException("il servizio esterno non e' raggiungibile");
		}
	}

	private String corpoDi(org.springframework.http.client.ClientHttpResponse risposta) {
		try (var flusso = risposta.getBody()) {
			return new String(flusso.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			return "(corpo non leggibile)";
		}
	}
}
