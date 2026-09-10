package com.example.demo.security;

import java.security.Principal;
import java.util.UUID;

/** Identita' ricavata dal JWT, usata sia dalle richieste HTTP sia dalle sessioni WebSocket. */
public record UtenteAutenticato(UUID id, String username) implements Principal {

    /** Il nome del principal e' l'id utente: e' la chiave delle destinazioni /user/**. */
    @Override
    public String getName() {
        return id.toString();
    }
}
