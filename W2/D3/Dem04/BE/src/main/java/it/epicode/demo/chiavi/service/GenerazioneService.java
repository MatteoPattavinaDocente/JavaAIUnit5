package it.epicode.demo.chiavi.service;

import it.epicode.demo.chiavi.config.LlmProperties;
import it.epicode.demo.chiavi.dto.GenerazioneRisposta;
import it.epicode.demo.chiavi.dto.LlmRichiesta;
import it.epicode.demo.chiavi.dto.LlmRisposta;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Service
public class GenerazioneService {

	private static final Logger log = LoggerFactory.getLogger(GenerazioneService.class);

	private static final String ISTRUZIONI = """
			Sei un assistente che riassume testi.
			Rispondi sempre in italiano, con tre frasi al massimo.
			Non aggiungere introduzioni, titoli o elenchi: soltanto il riassunto.
			""";

	private final RestClient client;
	private final LlmProperties proprieta;
	private final LimiteService limiti;

	public GenerazioneService(RestClient llmClient, LlmProperties proprieta, LimiteService limiti) {
		this.client = llmClient;
		this.proprieta = proprieta;
		this.limiti = limiti;
	}

	public GenerazioneRisposta genera(String utente, String testo) {
		int maxTokens = proprieta.maxTokens();

		// 1. PRIMA della chiamata: il controllo del limite, su una stima.
		int stimati = limiti.stima(testo, maxTokens);
		limiti.verifica(utente, stimati);

		var corpo = LlmRichiesta.di(proprieta.model(), maxTokens, ISTRUZIONI,
				"Riassumi il testo seguente:\n\n" + testo, proprieta.effort());

		long inizio = System.nanoTime();
		LlmRisposta risposta;
		try {
			risposta = chiama(corpo);
		} catch (RuntimeException ex) {
			// 2b. Anche il fallimento si registra: l'esito e' parte del registro
			// (slide 43). Una chiamata fallita puo' comunque essere costata.
			long millis = (System.nanoTime() - inizio) / 1_000_000;
			limiti.registra(utente, proprieta.model(), 0, 0, esitoDi(ex), millis);
			throw ex;
		}
		long millis = (System.nanoTime() - inizio) / 1_000_000;

		// Un 200 con l'errore nel corpo va trattato come un guasto: la chiamata
		// e' comunque avvenuta, quindi si registra prima di lanciare.
		if (risposta.inErrore()) {
			limiti.registra(utente, risposta.model(), risposta.tokenIngresso(),
					risposta.tokenUscita(), "errore-nel-corpo", millis);
			log.error("200 con errore nel corpo: {}", risposta.error());
			throw new ServizioNonDisponibileException(
					"il servizio ha risposto 200 con un errore nel corpo");
		}

		// 2. DOPO la chiamata: il consumo reale, con i valori di usage.
		//
		// Il modello si legge dalla RISPOSTA e non dalla richiesta: OpenRouter
		// puo' servirla con una variante di quello chiesto, e nel registro deve
		// finire quello che si e' pagato davvero.
		limiti.registra(utente, risposta.model(), risposta.tokenIngresso(), risposta.tokenUscita(),
				risposta.motivoArresto() == null ? "sconosciuto" : risposta.motivoArresto(), millis);

		return new GenerazioneRisposta(
				risposta.testo().orElse(""),
				risposta.completa(),
				risposta.model(),
				risposta.provider(),
				risposta.motivoArresto(),
				risposta.tokenIngresso(),
				risposta.tokenUscita(),
				millis,
				limiti.usatiOggi(utente),
				limiti.limiteGiornaliero());
	}

	private String esitoDi(RuntimeException ex) {
		if (ex instanceof LimiteRaggiuntoException) {
			return "429";
		}
		if (ex instanceof RichiestaEsternaNonValidaException) {
			return "errore-4xx";
		}
		if (ex instanceof ServizioNonDisponibileException) {
			return "errore-5xx";
		}
		return "errore";
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
