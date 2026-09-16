package it.epicode.demo.proxy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Il corpo minimo della richiesta a /chat/completions (slide 15 e 17).
 *
 * OpenRouter parla il protocollo di OpenAI: i campi obbligatori sono model e
 * messages. max_tokens e' facoltativo, ma metterlo e' l'unico modo per non
 * scoprire il costo a fattura arrivata.
 *
 * Le istruzioni permanenti NON stanno in un campo a parte: sono il primo turno
 * dell'elenco, con ruolo «system». E' la differenza piu' visibile rispetto al
 * protocollo di Anthropic, dove system e' un campo di primo livello.
 *
 * @JsonInclude(NON_NULL): i campi facoltativi che restano a null non vanno
 * inviati. Mandare «"reasoning": null» non e' la stessa cosa che non mandarlo:
 * alcuni fornitori a valle lo rifiutano.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LlmCorpo(
		String model,
		int max_tokens,
		List<Turno> messages,
		Ragionamento reasoning) {

	public record Turno(String role, String content) {

		public static Turno sistema(String contenuto) {
			return new Turno("system", contenuto);
		}

		public static Turno utente(String contenuto) {
			return new Turno("user", contenuto);
		}
	}

	/**
	 * Il campo «reasoning» e' un'estensione di OpenRouter, non del protocollo di
	 * OpenAI: e' un oggetto, non un booleano, perche' accetta anche altre voci
	 * (lo sforzo, se nascondere i passaggi). A noi serve solo l'interruttore.
	 *
	 * I modelli che non ragionano ignorano il campo.
	 */
	public record Ragionamento(boolean enabled) {

		private static final Ragionamento ACCESO = new Ragionamento(true);

		/** null quando e' spento: cosi' il campo non viene inviato affatto. */
		public static Ragionamento se(boolean attivo) {
			return attivo ? ACCESO : null;
		}
	}

	/** L'ordine conta: prima le istruzioni, poi il turno dell'utente. */
	public static LlmCorpo di(String modello, int maxTokens, boolean ragionamento,
			String istruzioni, String testoUtente) {
		return new LlmCorpo(modello, maxTokens,
				List.of(Turno.sistema(istruzioni), Turno.utente(testoUtente)),
				Ragionamento.se(ragionamento));
	}
}
