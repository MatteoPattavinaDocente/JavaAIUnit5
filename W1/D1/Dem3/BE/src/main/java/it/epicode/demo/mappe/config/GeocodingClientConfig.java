package it.epicode.demo.mappe.config;

import it.epicode.demo.mappe.geocoding.GoogleGeocodingClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

// Registra il proxy del client nel gruppo "geocoding": base-url e timeout stanno
// in application.yml sotto spring.http.serviceclient.geocoding.
@Configuration
@ImportHttpServices(group = "geocoding", types = GoogleGeocodingClient.class)
public class GeocodingClientConfig {
}
