package it.epicode.solution.segnalazioni.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CORS per il dev server Vite. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	private final String[] allowedOrigins;

	public CorsConfig(@Value("${app.cors.allowed-origins}") String[] allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(allowedOrigins)
				.allowedMethods("GET", "POST", "OPTIONS")
				.allowedHeaders("*")
				.maxAge(3600);
	}
}
