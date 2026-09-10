package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.example.demo.security.StompAuthChannelInterceptor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Broker STOMP over WebSocket.
 *
 * Endpoint di handshake : ws://localhost:8080/ws
 * Topic di canale       : /topic/canale/{idCanale}
 * Coda personale        : /user/queue/notifiche
 * Prefisso invii client : /app/**
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOrigins;
    private final StompAuthChannelInterceptor authInterceptor;

    public WebSocketConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins,
                           StompAuthChannelInterceptor authInterceptor) {
        this.allowedOrigins = allowedOrigins;
        this.authInterceptor = authInterceptor;
    }

    /** Autentica la sessione STOMP leggendo il JWT dal frame CONNECT. */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // broker in-memory: destinazioni broadcast (/topic) e per singolo utente (/queue)
        registry.enableSimpleBroker("/topic", "/queue");
        // destinazioni gestite dai @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");
        // prefisso per i messaggi indirizzati al singolo utente (/user/queue/...)
        registry.setUserDestinationPrefix("/user");
    }
}
