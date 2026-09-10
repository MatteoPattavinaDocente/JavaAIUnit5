package com.example.demo.controller;

import com.example.demo.dto.CanaleResponse;
import com.example.demo.dto.CreaCanaleRequest;
import com.example.demo.dto.PageResponse;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.UtenteAutenticato;
import com.example.demo.service.CanaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/canali")
public class CanaleController {

    private final CanaleService canaleService;

    public CanaleController(CanaleService canaleService) {
        this.canaleService = canaleService;
    }

    /** Richiede JWT: il proprietario del canale e' l'utente del token. */
    @PostMapping
    public ResponseEntity<CanaleResponse> crea(@Valid @RequestBody CreaCanaleRequest richiesta,
                                               @CurrentUser UtenteAutenticato utente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(canaleService.crea(richiesta, utente.id()));
    }

    /** Tutti i canali, 15 per pagina, dal piu' recente. Endpoint pubblico. */
    @GetMapping
    public PageResponse<CanaleResponse> elencaTutti(@RequestParam(defaultValue = "0") int page) {
        return canaleService.elencaTutti(page);
    }

    /** Dettaglio di un canale. Endpoint pubblico come l'elenco. */
    @GetMapping("/{idCanale}")
    public CanaleResponse dettaglio(@PathVariable UUID idCanale) {
        return canaleService.dettaglio(idCanale);
    }

    /** Richiede JWT: solo i canali a cui l'utente del token e' iscritto. */
    @GetMapping("/iscritto")
    public PageResponse<CanaleResponse> elencaIscritto(@CurrentUser UtenteAutenticato utente,
                                                       @RequestParam(defaultValue = "0") int page) {
        return canaleService.elencaIscritto(utente.id(), page);
    }
}
