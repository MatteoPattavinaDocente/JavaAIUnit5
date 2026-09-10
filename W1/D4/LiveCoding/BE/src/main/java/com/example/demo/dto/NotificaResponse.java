package com.example.demo.dto;

import com.example.demo.entity.Notifica;
import com.example.demo.entity.TipoNotifica;

import java.time.Instant;
import java.util.UUID;

public record NotificaResponse(
        UUID id,
        TipoNotifica tipo,
        UUID idCanale,
        String message,
        Instant createdAt,
        Instant readAt
) {

    public static NotificaResponse from(Notifica notifica) {
        return new NotificaResponse(
                notifica.getId(),
                notifica.getTipo(),
                notifica.getCanale() == null ? null : notifica.getCanale().getId(),
                notifica.getMessage(),
                notifica.getCreatedAt(),
                notifica.getReadAt()
        );
    }
}
