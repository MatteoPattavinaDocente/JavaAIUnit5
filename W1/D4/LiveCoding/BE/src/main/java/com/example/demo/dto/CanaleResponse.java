package com.example.demo.dto;

import com.example.demo.entity.Canale;

import java.util.UUID;

/** idUtente serve al client per riconoscere i canali di cui l'utente e' proprietario. */
public record CanaleResponse(UUID id, String nome, String descrizione, UUID idUtente) {

    public static CanaleResponse from(Canale canale) {
        // getId() sul proxy pigro non forza il caricamento dell'utente
        return new CanaleResponse(canale.getId(), canale.getNome(), canale.getDescrizione(), canale.getUtente().getId());
    }
}
