package it.epicode.demo.notifiche.config;

import it.epicode.demo.notifiche.web.EchoWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Qui accendiamo il WebSocket. Sono nove righe di configurazione, ed e' tutto.
 *
 * DIFFERENZA CON HTTP
 * Una richiesta HTTP e' come una telefonata: il browser chiama, il server risponde,
 * si riattacca. Il server non puo' richiamare.
 * Una WebSocket e' come lasciare la linea aperta: dopo il collegamento iniziale,
 * tutti e due possono parlare quando vogliono, finche' qualcuno non chiude.
 *
 * COSA C'E' E COSA NON C'E' IN QUESTA DEM
 * C'e' il tubo, e basta. Nessun broker, nessun protocollo sopra il WebSocket.
 * Quello che passa nel tubo sono stringhe: significato zero finche' non glielo diamo noi.
 * Chi tiene l'elenco di chi e' collegato? Codice nostro (vedi EchoWebSocketHandler).
 * Dalla Dem 4 quel lavoro lo fara' STOMP.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	private final String allowedOrigin;
	private final EchoWebSocketHandler handler;

	public WebSocketConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin,
			EchoWebSocketHandler handler) {
		this.allowedOrigin = allowedOrigin;
		this.handler = handler;
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		// "Le connessioni che arrivano su /ws le gestisce questo handler,
		//  e le accetto solo se partono dal frontend."
		//
		// Se ci si dimentica setAllowedOrigins, il collegamento iniziale (handshake)
		// viene rifiutato e nel browser si vede un errore di connessione, non un errore
		// CORS: sembra un problema di rete e si perde mezz'ora a cercarlo nel posto sbagliato.
		registry.addHandler(handler, "/ws").setAllowedOrigins(allowedOrigin);
	}
}
