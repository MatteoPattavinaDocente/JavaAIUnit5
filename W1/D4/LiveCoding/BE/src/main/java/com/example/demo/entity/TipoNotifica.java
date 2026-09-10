package com.example.demo.entity;

/**
 * Tipologia di notifica.
 *
 * PERSONAL : notifica di sistema indirizzata a un solo utente (nessun canale)
 * CANALE   : notifica indirizzata agli iscritti di un canale (canale valorizzato)
 * ALL      : notifica di sistema indirizzata a tutti gli utenti (nessun canale)
 */
public enum TipoNotifica {
    PERSONAL,
    CANALE,
    ALL
}
