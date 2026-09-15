package it.epicode.demo.chat.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final String allowedOrigin;
	private final StompAuthInterceptor authInterceptor;

	public WebSocketConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin,
			StompAuthInterceptor authInterceptor) {
		this.allowedOrigin = allowedOrigin;
		this.authInterceptor = authInterceptor;
	}

	/**
	 * Nessun withSockJS() qui: i browser attuali parlano WebSocket, quindi
	 * l'indirizzo lato client e' ws:// e non http:// (slide 26). Con SockJS
	 * l'endpoint accetterebbe anche i trasporti di ripiego su HTTP.
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigin);
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// Le destinazioni che iniziano con questi prefissi le smista il broker
		// semplice, in memoria, dentro questo processo (slide 5).
		//   /topic -> pubblica: chi si iscrive riceve tutto
		//   /queue -> riservata a una sessione
		registry.enableSimpleBroker("/topic", "/queue");

		// Quello che il client manda con questo prefisso NON va al broker: va a
		// un @MessageMapping, cioe' a codice nostro. E' l'unica direzione in cui
		// il client puo' far eseguire qualcosa al server.
		registry.setApplicationDestinationPrefixes("/app");

		// Il prefisso virtuale. Il valore predefinito e' proprio "/user", ma
		// dichiararlo serve a ricordare che esiste: senza la traduzione, due
		// utenti iscritti a /queue/messaggi leggerebbero la posta l'uno
		// dell'altro (slide 6 e 7).
		registry.setUserDestinationPrefix("/user");
	}

	// Il canale in entrata passa da qui: e' il punto in cui la sessione riceve
	// il suo Principal (slide 8).
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(authInterceptor);
	}
}
