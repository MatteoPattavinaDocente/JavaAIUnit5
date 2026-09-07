package it.epicode.solution.segnalazioni;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
// La cache del geocoding e' una tabella (geocoding_cache), non una cache di
// libreria: @EnableScheduling serve solo alla pulizia periodica delle entry
// che nessuno richiede piu'. Vedi GeocodingCache.purgeStale().
@EnableScheduling
public class SegnalazioniApplication {

	public static void main(String[] args) {
		SpringApplication.run(SegnalazioniApplication.class, args);
	}

}
