package com.example.demo.exception;

/** 400: la richiesta e' incoerente con lo stato attuale (username occupato, iscrizione gia' presente...). */
public class ConflittoException extends RuntimeException {
    public ConflittoException(String messaggio) {
        super(messaggio);
    }
}
