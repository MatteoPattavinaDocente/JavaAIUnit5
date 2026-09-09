package it.epicode.demo.ocr.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Il frontend gira su un'altra porta: senza questa configurazione il browser
// blocca le fetch verso :8080. Vale solo per /api/**.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final String allowedOrigin;

	public CorsConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin) {
		this.allowedOrigin = allowedOrigin;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigin)
				.allowedMethods("GET", "POST", "PATCH", "DELETE")
				.allowedHeaders("*");
	}
}
