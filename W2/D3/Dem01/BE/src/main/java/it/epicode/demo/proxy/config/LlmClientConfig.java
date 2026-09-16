package it.epicode.demo.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Un client per servizio esterno, costruito una volta e riusato (slide 7).
 *
 * Il builder lo fornisce Spring Boot gia' preconfigurato: eredita i timeout
 * dichiarati sotto spring.http.clients, quindi non li impostiamo a mano qui.
 *
 * Il servizio e' OpenRouter, che espone il protocollo di OpenAI: un solo
 * endpoint /chat/completions, autenticazione con «Authorization: Bearer».
 * Nessuna intestazione di versione da indicare.
 */
@Configuration
public class LlmClientConfig {

	private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

	@Bean
	RestClient llmClient(RestClient.Builder builder,
			@Value("${app.llm.base-url}") String baseUrl,
			@Value("${app.llm.api-key:}") String apiKey,
			@Value("${app.llm.site-url:}") String siteUrl,
			@Value("${app.llm.site-name:}") String siteName) {

		// Nella demo 4 questo controllo diventa un errore di avvio: meglio un
		// errore all'avvio che un 401 al primo utente (slide 40). Qui avvisa,
		// perche' vogliamo poter mostrare anche il 401.
		if (apiKey == null || apiKey.isBlank()) {
			log.warn("app.llm.api-key non impostata: le chiamate riceveranno 401");
		}

		log.info("client LLM verso {}", baseUrl);

		var costruttore = builder
				.baseUrl(baseUrl)
				// L'intestazione si imposta una volta, non a ogni chiamata: meno
				// occasioni in cui la chiave compare nel codice (slide 40).
				//
				// Lo schema e' «Bearer <chiave>»: la parola Bearer fa parte del
				// valore, non e' il nome dell'intestazione.
				.defaultHeader("Authorization", "Bearer " + apiKey);

		// Facoltative: OpenRouter le usa per attribuire il traffico nelle sue
		// classifiche pubbliche. Non cambiano la risposta, e se sono vuote non
		// vanno inviate vuote.
		if (siteUrl != null && !siteUrl.isBlank()) {
			costruttore.defaultHeader("HTTP-Referer", siteUrl);
		}
		if (siteName != null && !siteName.isBlank()) {
			costruttore.defaultHeader("X-Title", siteName);
		}

		return costruttore.build();
	}
}
