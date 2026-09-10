package com.example.demo.controller;

import com.example.demo.dto.ConteggioNotificheResponse;
import com.example.demo.dto.CreaNotificaRequest;
import com.example.demo.dto.CreazioneNotificaResponse;
import com.example.demo.dto.NotificaResponse;
import com.example.demo.dto.PageResponse;
import com.example.demo.security.CurrentUser;
import com.example.demo.security.UtenteAutenticato;
import com.example.demo.service.NotificaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Tutti gli endpoint richiedono JWT: il destinatario e' sempre l'utente del token. */
@RestController
@RequestMapping("/api/notifiche")
public class NotificaController {

    private final NotificaService notificaService;

    public NotificaController(NotificaService notificaService) {
        this.notificaService = notificaService;
    }

    /** Non lette per prime, poi le altre, dalla piu' recente. 15 per pagina. */
    @GetMapping
    public PageResponse<NotificaResponse> elenca(@CurrentUser UtenteAutenticato utente,
                                                 @RequestParam(defaultValue = "0") int page) {
        return notificaService.elenca(utente.id(), page);
    }

    /** Numero di notifiche non lette dell'utente loggato. */
    @GetMapping("/count")
    public ConteggioNotificheResponse conta(@CurrentUser UtenteAutenticato utente) {
        return new ConteggioNotificheResponse(notificaService.contaNonLette(utente.id()));
    }

    /** Valorizza read_at con l'istante corrente. */
    @PatchMapping("/{idNotifica}/read")
    public NotificaResponse marcaLetta(@PathVariable UUID idNotifica,
                                       @CurrentUser UtenteAutenticato utente) {
        return notificaService.marcaLetta(idNotifica, utente.id());
    }

    /** Valorizza read_at su tutte le notifiche non lette dell'utente loggato. */
    @PostMapping("/read-all")
    public ConteggioNotificheResponse marcaTutteLette(@CurrentUser UtenteAutenticato utente) {
        notificaService.marcaTutteLette(utente.id());
        return new ConteggioNotificheResponse(0);
    }

    /** Crea la notifica e la recapita via WebSocket ai destinatari connessi. */
    @PostMapping
    public ResponseEntity<CreazioneNotificaResponse> crea(@Valid @RequestBody CreaNotificaRequest richiesta,
                                                          @CurrentUser UtenteAutenticato utente) {
        return ResponseEntity.status(HttpStatus.CREATED).body(notificaService.crea(richiesta));
    }
}
