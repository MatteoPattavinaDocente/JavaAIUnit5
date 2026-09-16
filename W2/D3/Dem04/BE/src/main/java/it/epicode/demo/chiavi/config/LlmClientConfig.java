package it.epicode.demo.chiavi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmClientConfig {

	private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

	@Bean
	RestClient llmClient(RestClient.Builder builder, LlmProperties p) {
		// L'applicazione NON parte se la chiave manca: meglio un errore
		// all'avvio che un 401 al primo utente (slide 40).
		//
		// Il messaggio dice quale proprieta' manca e come impostarla: un
		// «IllegalStateException» senza spiegazione costa mezz'ora a chi lo
		// incontra la prima volta.
		if (!p.chiavePresente()) {
			throw new IllegalStateException("""
					app.llm.api-key non impostata.

					La chiave arriva da una variabile d'ambiente, non dal file di properties:

					    LLM_API_KEY=sk-or-v1-...  ./mvnw spring-boot:run

					La chiave si crea su https://openrouter.ai/keys
					In aula: copiare chiave.esempio.cmd in chiave.cmd e scriverci la chiave.
					""");
		}
		if (p.model() == null || p.model().isBlank()) {
			throw new IllegalStateException("app.llm.model non impostata");
		}

		// Si dice DA DOVE arriva e QUANTO e' lunga, mai il valore.
		log.info("client LLM verso {} - modello {} - chiave presente ({} caratteri)",
				p.baseUrl(), p.model(), p.apiKey().length());

		var costruttore = builder
				.baseUrl(p.baseUrl())
				// Lo schema e' «Bearer <chiave>»: la parola Bearer fa parte del
				// valore, non e' il nome dell'intestazione.
				.defaultHeader("Authorization", "Bearer " + p.apiKey());

		if (p.siteUrl() != null && !p.siteUrl().isBlank()) {
			costruttore.defaultHeader("HTTP-Referer", p.siteUrl());
		}
		if (p.siteName() != null && !p.siteName().isBlank()) {
			costruttore.defaultHeader("X-Title", p.siteName());
		}

		return costruttore.build();
	}
}
