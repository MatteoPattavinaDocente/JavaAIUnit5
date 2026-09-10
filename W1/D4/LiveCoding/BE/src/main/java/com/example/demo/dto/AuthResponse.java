package com.example.demo.dto;

import java.util.UUID;

/** Esito del login (e della registrazione, che effettua il login automatico). */
public record AuthResponse(UUID id, String username, String token) {}
