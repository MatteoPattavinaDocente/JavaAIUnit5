package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegistrazioneRequest;
import com.example.demo.entity.Utente;
import com.example.demo.exception.ConflittoException;
import com.example.demo.exception.CredenzialiNonValideException;
import com.example.demo.repository.UtenteRepository;
import com.example.demo.security.JwtService;
import com.example.demo.security.TokenBlacklist;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final JwtService jwtService;
    private final TokenBlacklist blacklist;

    public UtenteService(UtenteRepository utenteRepository, JwtService jwtService, TokenBlacklist blacklist) {
        this.utenteRepository = utenteRepository;
        this.jwtService = jwtService;
        this.blacklist = blacklist;
    }

    /**
     * Registra un nuovo utente e lo autentica subito, restituendo il token:
     * evita al client una seconda chiamata di login dopo la registrazione.
     */
    @Transactional
    public AuthResponse registra(RegistrazioneRequest richiesta) {
        if (utenteRepository.existsByUsername(richiesta.username())) {
            throw new ConflittoException("Username gia in uso");
        }

        // La password viene salvata in chiaro: scelta esplicita per questa esercitazione.
        Utente utente = new Utente(richiesta.username(), richiesta.password());

        try {
            utenteRepository.saveAndFlush(utente);
        } catch (DataIntegrityViolationException e) {
            // due registrazioni simultanee con lo stesso username: decide il vincolo UNIQUE
            throw new ConflittoException("Username gia in uso");
        }

        return new AuthResponse(utente.getId(), utente.getUsername(), jwtService.genera(utente.getId(), utente.getUsername()));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest richiesta) {
        Utente utente = utenteRepository.findByUsername(richiesta.username())
                .orElseThrow(CredenzialiNonValideException::new);

        // Confronto diretto: nessun hashing attivo.
        if (!utente.getPassword().equals(richiesta.password())) {
            throw new CredenzialiNonValideException();
        }

        return new AuthResponse(utente.getId(), utente.getUsername(), jwtService.genera(utente.getId(), utente.getUsername()));
    }

    /** Invalida il token corrente inserendolo nella blacklist fino alla sua scadenza naturale. */
    public void logout(String jti, Instant scadenza) {
        blacklist.revoca(jti, scadenza);
    }
}
