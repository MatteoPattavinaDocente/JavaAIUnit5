package it.epicode.demo.chiavi.dto;

/**
 * Un turno della conversazione: ruolo e contenuto. Riusato sia in entrata sia
 * in uscita (slide 20).
 *
 * I ruoli sono tre: «system» per le istruzioni permanenti, «user» per l'utente,
 * «assistant» per le risposte precedenti. Nel protocollo di OpenAI - quello che
 * usa OpenRouter - le istruzioni sono un turno come gli altri, e non un campo
 * separato del corpo (slide 17).
 */
public record Turno(String role, String content) {

	public static Turno sistema(String contenuto) {
		return new Turno("system", contenuto);
	}

	public static Turno utente(String contenuto) {
		return new Turno("user", contenuto);
	}

	public static Turno assistente(String contenuto) {
		return new Turno("assistant", contenuto);
	}
}
