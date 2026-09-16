package it.epicode.demo.proxy.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/**
 * La risposta di /chat/completions (slide 19).
 *
 * @JsonIgnoreProperties: i campi che non conosciamo non devono far fallire la
 * lettura (slide 20). Il servizio ne aggiunge nel tempo, e il nostro codice non
 * si deve rompere per questo. Con OpenRouter conta il doppio: ogni fornitore a
 * valle aggiunge campi propri.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmRisposta(
		String id,
		String model,
		List<Scelta> choices,
		Uso usage) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Scelta(int index, Messaggio message, String finish_reason) {
	}

	/**
	 * «reasoning» e' un campo in piu' di OpenRouter, presente solo per i modelli
	 * di ragionamento: contiene i passaggi intermedi. Non e' la risposta, e non
	 * va mostrato all'utente - ma sapere se c'e' aiuta a capire dove sono finiti
	 * i token di uscita.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Messaggio(String role, String content, String refusal, String reasoning) {
	}

	/**
	 * I nomi sono quelli di OpenAI: prompt_tokens per l'ingresso,
	 * completion_tokens per l'uscita.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Uso(int prompt_tokens, int completion_tokens, int total_tokens) {
	}

	/**
	 * Il contenuto non sta in cima: sta dentro choices, che e' un ELENCO.
	 * Ne arriva una sola voce se non si chiede altrimenti, ma resta un elenco -
	 * e un elenco puo' essere vuoto (slide 19).
	 *
	 * Prendere choices[0].message.content senza controllare che l'elenco ci sia
	 * funziona finche' non arriva una risposta vuota - e arriva.
	 */
	public String primoTesto() {
		if (choices == null || choices.isEmpty()) {
			return "";
		}
		var messaggio = choices.getFirst().message();
		if (messaggio == null || messaggio.content() == null) {
			return "";
		}
		return messaggio.content();
	}

	/**
	 * Il motivo di arresto sta nella singola scelta, non in cima alla risposta.
	 * I valori sono «stop» (finito), «length» (tagliato dal tetto dei token),
	 * «content_filter» (bloccato), «tool_calls».
	 */
	public String motivoArresto() {
		if (choices == null || choices.isEmpty()) {
			return null;
		}
		return choices.getFirst().finish_reason();
	}

	/** Quanti caratteri di ragionamento sono arrivati: 0 se il campo manca. */
	public int lunghezzaRagionamento() {
		if (choices == null || choices.isEmpty()) {
			return 0;
		}
		var messaggio = choices.getFirst().message();
		if (messaggio == null || messaggio.reasoning() == null) {
			return 0;
		}
		return messaggio.reasoning().length();
	}

	public int tokenIngresso() {
		return usage == null ? 0 : usage.prompt_tokens();
	}

	public int tokenUscita() {
		return usage == null ? 0 : usage.completion_tokens();
	}
}
