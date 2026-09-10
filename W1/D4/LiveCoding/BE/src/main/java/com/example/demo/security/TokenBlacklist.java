package com.example.demo.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro in memoria dei token invalidati dal logout.
 *
 * Un JWT resta valido fino alla scadenza: per fare logout lato server serve
 * ricordare quali token rifiutare. In memoria significa che il riavvio
 * dell'applicazione rende di nuovo validi i token non ancora scaduti.
 */
@Component
public class TokenBlacklist {

    /** jti del token -> istante di scadenza, usato per la pulizia periodica. */
    private final Map<String, Instant> revocati = new ConcurrentHashMap<>();

    public void revoca(String jti, Instant scadenza) {
        if (jti != null && scadenza != null) {
            revocati.put(jti, scadenza);
        }
    }

    public boolean isRevocato(String jti) {
        return jti != null && revocati.containsKey(jti);
    }

    /** Rimuove le voci ormai scadute: dopo la scadenza il token e' gia' rifiutato dalla firma. */
    @Scheduled(fixedDelay = 600_000)
    public void pulisci() {
        Instant adesso = Instant.now();
        revocati.entrySet().removeIf(voce -> voce.getValue().isBefore(adesso));
    }
}
