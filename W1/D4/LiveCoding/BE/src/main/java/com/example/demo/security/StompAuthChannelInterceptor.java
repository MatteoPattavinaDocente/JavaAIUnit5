package com.example.demo.security;

import io.jsonwebtoken.Claims;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Autentica la sessione WebSocket leggendo il JWT dall'header STOMP del frame CONNECT.
 * Il principal associato alla sessione ha come nome l'id utente: e' cosi' che il
 * broker recapita i messaggi su /user/queue/** e che il registro sessioni sa
 * dire se un destinatario e' online.
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final TokenBlacklist blacklist;

    public StompAuthChannelInterceptor(JwtService jwtService, TokenBlacklist blacklist) {
        this.jwtService = jwtService;
        this.blacklist = blacklist;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = jwtService.estraiDaHeader(accessor.getFirstNativeHeader("Authorization"));
            Claims claims = jwtService.leggiClaims(token);

            if (claims == null || blacklist.isRevocato(claims.getId())) {
                throw new IllegalArgumentException("JWT mancante o non valido nel frame CONNECT");
            }

            accessor.setUser(new UtenteAutenticato(
                    UUID.fromString(claims.getSubject()),
                    claims.get("username", String.class)
            ));
        }

        return message;
    }
}
