package com.example.demo.controller;

import com.example.demo.dto.FollowRequest;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.UtenteAutenticato;
import com.example.demo.service.IscrizioneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/iscrizioni")
public class IscrizioneController {

    private final IscrizioneService iscrizioneService;

    public IscrizioneController(IscrizioneService iscrizioneService) {
        this.iscrizioneService = iscrizioneService;
    }

    /** Richiede JWT. L'utente e' quello del token, nel body arriva solo il canale. */
    @PostMapping
    public ResponseEntity<Void> follow(@Valid @RequestBody FollowRequest richiesta,
                                       @CurrentUser UtenteAutenticato utente) {
        iscrizioneService.follow(utente.id(), richiesta.idCanale());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** Richiede JWT. */
    @DeleteMapping("/{idCanale}")
    public ResponseEntity<Void> unfollow(@PathVariable UUID idCanale,
                                         @CurrentUser UtenteAutenticato utente) {
        iscrizioneService.unfollow(utente.id(), idCanale);
        return ResponseEntity.noContent().build();
    }
}
