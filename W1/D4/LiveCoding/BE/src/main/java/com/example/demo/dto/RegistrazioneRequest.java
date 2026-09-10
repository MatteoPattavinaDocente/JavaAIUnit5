package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrazioneRequest(
        @NotBlank(message = "username obbligatorio")
        @Size(min = 3, max = 50, message = "username tra 3 e 50 caratteri")
        String username,

        @NotBlank(message = "password obbligatoria")
        @Size(min = 4, max = 100, message = "password tra 4 e 100 caratteri")
        String password
) {}
