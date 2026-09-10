package com.example.demo.exception;

/** 401: token assente, scaduto, revocato o non valido. */
public class NonAutenticatoException extends RuntimeException {
    public NonAutenticatoException(String messaggio) {
        super(messaggio);
    }
}
