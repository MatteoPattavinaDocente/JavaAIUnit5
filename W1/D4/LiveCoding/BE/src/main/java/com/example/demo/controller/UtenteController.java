package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegistrazioneRequest;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.JwtAuthFilter;
import com.example.demo.security.UtenteAutenticato;
import com.example.demo.service.UtenteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    /** 201 se creato, 400 se lo username e' gia' in uso. Restituisce gia' il token. */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registra(@Valid @RequestBody RegistrazioneRequest richiesta) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utenteService.registra(richiesta));
    }

    /** 200 con il token, 401 generico se le credenziali non corrispondono. */
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest richiesta) {
        return utenteService.login(richiesta);
    }

    /** Richiede JWT: invalida il token usato per la chiamata. */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser UtenteAutenticato utente, HttpServletRequest request) {
        String jti = (String) request.getAttribute(JwtAuthFilter.ATTRIBUTO_JTI);
        Instant scadenza = (Instant) request.getAttribute("jwtScadenza");
        utenteService.logout(jti, scadenza);
        return ResponseEntity.noContent().build();
    }
}
