package it.epicode.demo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Il corpo della richiesta a /chat/completions (slide 15 e 17).
 *
 * OpenRouter parla il protocollo di OpenAI. Campi obbligatori: model e
 * messages. max_tokens e' facoltativo nel protocollo, ma senza di lui il tetto
 * di spesa per chiamata non esiste.
 *
 * Le istruzioni permanenti NON stanno in un campo a parte: sono il primo turno
 * dell'elenco, con ruolo «system». Chi arriva dal protocollo di Anthropic
 * cerca un campo system di primo livello e non lo trova: qui il ruolo
 * «system» esiste, ed e' il modo giusto.
 *
 * I nomi dei campi sono quelli del protocollo (max_tokens con il trattino
 * basso): e' un DTO di confine, non una classe di dominio, e deve somigliare al
 * JSON che invia.
 *
 * @JsonInclude(NON_NULL): i campi facoltativi non vanno inviati come null,
 * altrimenti il servizio risponde 400 su valori che non abbiamo impostato.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LlmRichiesta(
		String model,
		int max_tokens,
		List<Turno> messages,
		Reasoning reasoning) {

	/**
	 * Quanto lavoro il modello mette nel ragionamento prima di rispondere.
	 * E' il parametro unificato di OpenRouter: lui lo traduce nel parametro
	 * proprio di ogni fornitore. I valori sono «low», «medium», «high».
	 *
	 * Sui modelli che non ragionano viene ignorato, non produce un errore.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public record Reasoning(String effort) {
	}

	public static LlmRichiesta di(String modello, int maxTokens, String istruzioni,
			String testoUtente, String effort) {
		return new LlmRichiesta(modello, maxTokens,
				// L'ordine conta: prima le istruzioni, poi il turno dell'utente.
				List.of(Turno.sistema(istruzioni), Turno.utente(testoUtente)),
				effort == null ? null : new Reasoning(effort));
	}
}
