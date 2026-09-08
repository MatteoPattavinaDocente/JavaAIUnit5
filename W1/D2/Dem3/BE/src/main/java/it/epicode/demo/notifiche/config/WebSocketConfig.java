package it.epicode.demo.notifiche.config;

import it.epicode.demo.notifiche.web.NotificationWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Stesso WebSocket nudo della Dem 2: nessun broker, nessun protocollo sopra il tubo.
 *
 * Le novita' rispetto alla Dem 2 sono due, ed entrambe stanno in una riga sola:
 *  - l'handler adesso e' NotificationWebSocketHandler, che conosce il dominio;
 *  - c'e' un interceptor che, durante l'handshake, cattura il nome dell'utente.
 *
 * Tutto il resto (chi e' collegato, chi segue cosa, a chi consegnare) e' codice nostro,
 * e si legge dentro l'handler. Nella Dem 4 quel codice sparisce e questa classe
 * cambia completamente forma.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final String allowedOrigin;
	private final NotificationWebSocketHandler handler;
	private final UtenteHandshakeInterceptor utenteInterceptor;

	public WebSocketConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin,
			NotificationWebSocketHandler handler,
			UtenteHandshakeInterceptor utenteInterceptor) {
		this.allowedOrigin = allowedOrigin;
		this.handler = handler;
		this.utenteInterceptor = utenteInterceptor;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(handler, "/ws")
				// L'interceptor entra in mezzo all'handshake per leggere ?utente=.
				.addInterceptors(utenteInterceptor)
				// Autorizzazione separata da quella REST di CorsConfig: sono due elenchi
				// diversi. Se manca, il browser mostra un generico errore di connessione
				// e sembra un problema di rete.
				.setAllowedOrigins(allowedOrigin);
	}
}
