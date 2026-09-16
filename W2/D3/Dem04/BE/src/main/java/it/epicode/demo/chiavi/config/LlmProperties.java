package it.epicode.demo.chiavi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Un prefisso, un record, tutti i valori del servizio esterno in un punto
 * (slide 38 e 39).
 *
 * Perche' non @Value sparso fra le classi: con @Value i valori si sparpagliano
 * e nessuno sa piu' quali proprieta' esistono. Qui l'elenco e' questo, e basta
 * aprire il file per leggerlo.
 *
 * Il record e' immutabile: i valori arrivano dal costruttore e restano tali per
 * tutta la vita dell'applicazione.
 *
 * app.llm.api-key nel file diventa apiKey qui: il legame fra il trattino e la
 * maiuscola lo fa Spring.
 *
 * Il prefisso e' «app.llm» e non «app.openrouter»: il fornitore e' un dettaglio
 * di configurazione, e cambiarlo non deve costare un rinomina in tutto il
 * progetto - come e' successo passando da Anthropic a OpenRouter.
 */
@ConfigurationProperties("app.llm")
public record LlmProperties(
		String baseUrl,
		String apiKey,
		String model,
		Integer maxTokens,
		String effort,
		Integer limiteGiornalieroToken,
		/** Facoltative: OpenRouter le usa per le sue classifiche pubbliche. */
		String siteUrl,
		String siteName) {

	/** La chiave non si mostra mai per intero, nemmeno nei log (slide 37). */
	public boolean chiavePresente() {
		return apiKey != null && !apiKey.isBlank();
	}
}
