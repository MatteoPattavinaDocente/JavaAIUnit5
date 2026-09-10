package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NonAutenticatoException.class)
    public ResponseEntity<ErroreResponse> nonAutenticato(NonAutenticatoException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErroreResponse.di(401, "Unauthorized", e.getMessage()));
    }

    @ExceptionHandler(CredenzialiNonValideException.class)
    public ResponseEntity<ErroreResponse> credenziali(CredenzialiNonValideException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErroreResponse.di(401, "Unauthorized", e.getMessage()));
    }

    @ExceptionHandler(RisorsaNonTrovataException.class)
    public ResponseEntity<ErroreResponse> nonTrovata(RisorsaNonTrovataException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErroreResponse.di(404, "Not Found", e.getMessage()));
    }

    @ExceptionHandler(ConflittoException.class)
    public ResponseEntity<ErroreResponse> conflitto(ConflittoException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErroreResponse.di(400, "Bad Request", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErroreResponse> argomentoNonValido(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(ErroreResponse.di(400, "Bad Request", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroreResponse> validazione(MethodArgumentNotValidException e) {
        List<String> dettagli = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();
        return ResponseEntity.badRequest()
                .body(ErroreResponse.di(400, "Bad Request", "Richiesta non valida", dettagli));
    }
}
