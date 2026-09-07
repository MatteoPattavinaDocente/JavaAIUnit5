package it.epicode.solution.segnalazioni.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

import it.epicode.solution.segnalazioni.geocoding.GoogleGeocodingClient;

/**
 * Registra il proxy del client HTTP dichiarativo nel gruppo "geocoding".
 * Base-url e timeout sono configurati in application.yml sotto
 * spring.http.serviceclient.geocoding.
 */
@Configuration
@ImportHttpServices(group = "geocoding", types = GoogleGeocodingClient.class)
public class GeocodingClientConfig {
}
