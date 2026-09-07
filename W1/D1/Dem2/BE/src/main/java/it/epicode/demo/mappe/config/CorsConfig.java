package it.epicode.demo.mappe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Vite gira su :5173, Spring su :8080: per il browser sono due origini diverse.
// Senza questa configurazione la fetch dal frontend viene bloccata dal CORS.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins("http://localhost:5173")
				.allowedMethods("GET", "POST");
	}
}
