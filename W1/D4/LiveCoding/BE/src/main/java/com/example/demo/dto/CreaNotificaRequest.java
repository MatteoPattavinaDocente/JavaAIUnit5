package com.example.demo.dto;

import com.example.demo.entity.TipoNotifica;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Creazione di una notifica.
 *
 * PERSONAL : idDestinatario obbligatorio, idCanale deve essere null
 * ALL      : idDestinatario e idCanale devono essere null (fan-out su tutti gli utenti)
 * CANALE   : idCanale obbligatorio, idDestinatario deve essere null (fan-out sugli iscritti)
 */
public record CreaNotificaRequest(
        @NotNull(message = "tipo obbligatorio") TipoNotifica tipo,
        @NotBlank(message = "message obbligatorio") String message,
        UUID idDestinatario,
        UUID idCanale
) {}
