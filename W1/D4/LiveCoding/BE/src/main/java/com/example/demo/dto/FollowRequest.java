package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** L'id utente non viene mai letto dal body: arriva sempre dal token JWT. */
public record FollowRequest(@NotNull(message = "idCanale obbligatorio") UUID idCanale) {}
