package it.epicode.demo.estrazione.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;

/**
 * Come nella demo 2, con in piu' response_format: lo schema JSON che vincola la
 * risposta (slide 31).
 *
 * Nel protocollo di OpenAI - quello che usa OpenRouter - lo schema NON sta
 * dentro le opzioni della risposta: e' un campo di primo livello,
 * response_format, con una struttura sua.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LlmRichiesta(
		String model,
		int max_tokens,
		List<Turno> messages,
		Reasoning reasoning,
		ResponseFormat response_format) {

	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Reasoning(String effort) {
	}

	/**
	 * Con questo il servizio vincola la risposta allo schema dichiarato: il
	 * risultato e' valido per costruzione, non per fortuna (slide 31).
	 *
	 * Attenzione: non tutti i modelli di OpenRouter lo sostengono. Quelli che
	 * non lo sostengono possono ignorarlo in silenzio, e allora la risposta
	 * torna a essere testo libero - per questo la ripulitura nel servizio resta
	 * al suo posto. L'elenco dei modelli adatti si filtra su
	 * openrouter.ai/models con «structured outputs».
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record ResponseFormat(String type, JsonSchema json_schema) {

		public static ResponseFormat jsonSchema(String nome, Map<String, Object> schema) {
			return new ResponseFormat("json_schema", new JsonSchema(nome, true, schema));
		}
	}

	/**
	 * strict = true e' il punto: senza di lui lo schema e' un suggerimento, non
	 * un vincolo, e il modello puo' consegnare campi in piu' o mancanti.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record JsonSchema(String name, boolean strict, Map<String, Object> schema) {
	}

	public static LlmRichiesta di(String modello, int maxTokens, String istruzioni,
			String testoUtente, String effort, ResponseFormat formato) {
		return new LlmRichiesta(modello, maxTokens,
				List.of(Turno.sistema(istruzioni), Turno.utente(testoUtente)),
				effort == null ? null : new Reasoning(effort),
				formato);
	}
}
