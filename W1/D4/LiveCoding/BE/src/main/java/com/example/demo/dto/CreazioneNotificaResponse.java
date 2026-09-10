package com.example.demo.dto;

import com.example.demo.entity.TipoNotifica;

/** Esito della creazione: quante notifiche sono state generate dal fan-out. */
public record CreazioneNotificaResponse(TipoNotifica tipo, int destinatari, int inviateViaWebSocket) {}
