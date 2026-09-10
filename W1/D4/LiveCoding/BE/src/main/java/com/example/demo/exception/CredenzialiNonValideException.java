package com.example.demo.exception;

/**
 * 401 con messaggio volutamente generico: non deve trapelare se a non
 * corrispondere sia lo username o la password.
 */
public class CredenzialiNonValideException extends RuntimeException {
    public CredenzialiNonValideException() {
        super("Credenziali non valide");
    }
}
