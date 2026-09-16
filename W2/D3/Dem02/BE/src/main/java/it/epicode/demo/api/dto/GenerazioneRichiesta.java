package it.epicode.demo.api.dto;

/**
 * Quello che il frontend manda a NOI. Il tetto dei token e' esposto solo perche'
 * la demo lo fa variare: in un'applicazione vera lo decide il server.
 */
public record GenerazioneRichiesta(String testo, Integer maxTokens) {

	public GenerazioneRichiesta {
		if (testo == null || testo.isBlank()) {
			throw new IllegalArgumentException("testo: non puo' essere vuoto");
		}
		if (maxTokens != null && (maxTokens < 1 || maxTokens > 8192)) {
			throw new IllegalArgumentException("maxTokens: fra 1 e 8192");
		}
	}
}
