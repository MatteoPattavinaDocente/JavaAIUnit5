package it.epicode.demo.chiavi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import java.util.Optional;

/**
 * La risposta di /chat/completions (slide 19 e 20).
 *
 * @JsonIgnoreProperties(ignoreUnknown = true): i campi ignoti non devono far
 * fallire la lettura. Senza questa annotazione una nuova chiave nella loro
 * risposta rompe il nostro codice. Con OpenRouter serve piu' che altrove: la
 * risposta porta anche i campi propri del fornitore che ha servito la
 * richiesta, e quel fornitore puo' cambiare da una chiamata all'altra.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record LlmRisposta(
		String id,
		String model,
		/** Chi ha servito la richiesta a valle di OpenRouter. Utile nel log. */
		String provider,
		List<Scelta> choices,
		Uso usage,
		Errore error) {

	/**
	 * Una risposta possibile. choices e' un ELENCO: ne arriva una sola voce se
	 * non si chiede altrimenti, ma resta un elenco - e un elenco puo' essere
	 * vuoto (slide 19).
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Scelta(
			int index,
			Messaggio message,
			/** Normalizzato da OpenRouter: stop, length, content_filter, tool_calls, error. */
			String finish_reason,
			/** Quello grezzo del fornitore, senza normalizzazione. Serve a capire i casi strani. */
			String native_finish_reason) {
	}

	/**
	 * Il messaggio dell'assistente. content e' una STRINGA, non un elenco di
	 * blocchi: e' la differenza piu' pratica rispetto al protocollo di
	 * Anthropic.
	 *
	 * Puo' essere null: quando il modello rifiuta, e quando la risposta e'
	 * tutta ragionamento e niente testo.
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Messaggio(
			String role,
			String content,
			/** Popolato solo quando il modello declina in modo esplicito. */
			String refusal,
			/** Il ragionamento, se il modello lo espone e se e' stato chiesto. */
			String reasoning) {
	}

	/** I nomi sono quelli di OpenAI: prompt per l'ingresso, completion per l'uscita. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Uso(int prompt_tokens, int completion_tokens, int total_tokens) {
	}

	/**
	 * OpenRouter puo' rispondere 200 con un errore nel corpo, quando il guasto
	 * arriva dal fornitore a valle: il codice HTTP non basta a decidere se la
	 * chiamata e' andata bene (slide 33).
	 */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Errore(Integer code, String message) {
	}

	/** La scelta che ci interessa, se c'e'. */
	public Optional<Scelta> primaScelta() {
		if (choices == null || choices.isEmpty()) {
			return Optional.empty();
		}
		return Optional.ofNullable(choices.getFirst());
	}

	/**
	 * Il modo GIUSTO: si controlla che l'elenco ci sia, che il messaggio ci sia
	 * e che il testo non sia vuoto, prima di restituirlo (slide 19).
	 */
	public Optional<String> testo() {
		return primaScelta()
				.map(Scelta::message)
				.map(Messaggio::content)
				.filter(t -> !t.isBlank());
	}

	/**
	 * Il modo SBAGLIATO, tenuto qui per poterlo mostrare in aula: prendere
	 * choices[0].message.content senza controllare niente funziona finche' non
	 * arriva una risposta rifiutata o vuota - e arriva (slide 19 e 34).
	 */
	public String testoIngenuo() {
		if (choices == null || choices.isEmpty()) {
			return null;
		}
		var messaggio = choices.getFirst().message();
		return messaggio == null ? null : messaggio.content();
	}

	/**
	 * Il motivo di arresto sta nella singola scelta, non in cima alla risposta:
	 * ogni scelta puo' essersi fermata per un motivo diverso.
	 */
	public String motivoArresto() {
		return primaScelta().map(Scelta::finish_reason).orElse(null);
	}

	public String motivoArrestoNativo() {
		return primaScelta().map(Scelta::native_finish_reason).orElse(null);
	}

	/**
	 * La risposta e' finita, oppure e' stata tagliata dal tetto dei token?
	 * Un 200 non significa che la risposta sia utilizzabile (slide 33).
	 */
	public boolean completa() {
		return "stop".equals(motivoArresto());
	}

	/** «length», non «max_tokens»: il nome cambia con il protocollo. */
	public boolean tagliata() {
		return "length".equals(motivoArresto());
	}

	/**
	 * Il rifiuto ha due forme: il motivo di arresto «content_filter», e il
	 * campo refusal dentro il messaggio. Vanno controllate entrambe, perche'
	 * quale delle due arriva dipende dal fornitore a valle.
	 */
	public boolean rifiutata() {
		if ("content_filter".equals(motivoArresto())) {
			return true;
		}
		return primaScelta()
				.map(Scelta::message)
				.map(Messaggio::refusal)
				.filter(r -> !r.isBlank())
				.isPresent();
	}

	/** Il testo del rifiuto, quando il fornitore lo dichiara. */
	public Optional<String> motivoRifiuto() {
		return primaScelta()
				.map(Scelta::message)
				.map(Messaggio::refusal)
				.filter(r -> !r.isBlank());
	}

	/** L'errore nel corpo di un 200: va guardato prima di usare il contenuto. */
	public boolean inErrore() {
		return error != null && error.message() != null && !error.message().isBlank();
	}

	public int tokenIngresso() {
		return usage == null ? 0 : usage.prompt_tokens();
	}

	public int tokenUscita() {
		return usage == null ? 0 : usage.completion_tokens();
	}
}
