package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "username obbligatorio") String username,
        @NotBlank(message = "password obbligatoria") String password
) {}
