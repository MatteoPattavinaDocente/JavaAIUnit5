package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreaCanaleRequest(
        @NotBlank(message = "nome obbligatorio")
        @Size(max = 100, message = "nome max 100 caratteri")
        String nome,

        @Size(max = 2000, message = "descrizione max 2000 caratteri")
        String descrizione
) {}
