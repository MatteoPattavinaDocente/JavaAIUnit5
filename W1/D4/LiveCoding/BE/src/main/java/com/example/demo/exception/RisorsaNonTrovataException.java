package com.example.demo.exception;

/** 404: entita' inesistente o non accessibile all'utente corrente. */
public class RisorsaNonTrovataException extends RuntimeException {
    public RisorsaNonTrovataException(String messaggio) {
        super(messaggio);
    }
}
