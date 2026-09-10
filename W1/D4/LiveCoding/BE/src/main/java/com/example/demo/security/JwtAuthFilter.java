package com.example.demo.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Legge il JWT dall'header Authorization e, se valido, espone l'utente come
 * attributo di richiesta. Non blocca nulla: sono i controller, tramite il
 * parametro annotato con @CurrentUser, a pretendere l'autenticazione.
 */
@Component
@Order(1)
public class JwtAuthFilter extends OncePerRequestFilter {

    public static final String ATTRIBUTO_UTENTE = "utenteAutenticato";
    public static final String ATTRIBUTO_JTI = "jwtJti";

    private final JwtService jwtService;
    private final TokenBlacklist blacklist;

    public JwtAuthFilter(JwtService jwtService, TokenBlacklist blacklist) {
        this.jwtService = jwtService;
        this.blacklist = blacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String token = jwtService.estraiDaHeader(request.getHeader("Authorization"));
        Claims claims = jwtService.leggiClaims(token);

        if (claims != null && !blacklist.isRevocato(claims.getId())) {
            UtenteAutenticato utente = new UtenteAutenticato(
                    UUID.fromString(claims.getSubject()),
                    claims.get("username", String.class)
            );
            request.setAttribute(ATTRIBUTO_UTENTE, utente);
            request.setAttribute(ATTRIBUTO_JTI, claims.getId());
            request.setAttribute("jwtScadenza", jwtService.scadenza(claims));
        }

        chain.doFilter(request, response);
    }
}
