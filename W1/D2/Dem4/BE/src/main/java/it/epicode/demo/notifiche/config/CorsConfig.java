package it.epicode.demo.notifiche.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Il frontend React gira su localhost:5173, il backend Spring su localhost:8080.
 * Per il browser sono due siti diversi, e per sicurezza il browser blocca di default
 * le chiamate fatte da un sito verso un altro.
 *
 * Questa classe dice al browser: "le richieste che arrivano da :5173 sono autorizzate".
 * Senza queste righe la pagina React riceverebbe un errore CORS e non vedrebbe mai le notifiche.
 *
 * ATTENZIONE, e' una fonte di confusione ricorrente: questa autorizzazione vale solo
 * per le chiamate REST. L'endpoint STOMP ha una lista sua, dichiarata in WebSocketConfig
 * con setAllowedOrigins. Sono due elenchi separati anche quando contengono lo stesso
 * indirizzo: dimenticarne uno dei due rompe meta' applicazione.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	// L'indirizzo del frontend non e' scritto qui dentro: lo leggiamo da application.yml
	// (chiave app.cors.allowed-origin), cosi' in produzione si cambia senza toccare il codice.
	private final String allowedOrigin;

	public CorsConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
		this.allowedOrigin = allowedOrigin;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		// Solo gli indirizzi che iniziano per /api, solo dal frontend, solo questi tre metodi.
		// Tutto il resto resta chiuso: si apre il minimo che serve, non tutto.
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigin)
				.allowedMethods("GET", "POST", "PATCH");
	}
}
