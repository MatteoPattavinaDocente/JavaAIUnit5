package com.example.demo.exception;

import java.time.Instant;
import java.util.List;

public record ErroreResponse(int status, String errore, String messaggio, List<String> dettagli, Instant timestamp) {

    public static ErroreResponse di(int status, String errore, String messaggio) {
        return new ErroreResponse(status, errore, messaggio, List.of(), Instant.now());
    }

    public static ErroreResponse di(int status, String errore, String messaggio, List<String> dettagli) {
        return new ErroreResponse(status, errore, messaggio, dettagli, Instant.now());
    }
}
