package it.epicode.demo.mappe.geocoding;

import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Controller STUB: finge di essere la Google Geocoding API per scopi didattici.
// Attivo SOLO quando il profilo Spring è "stub" (application-stub.yml).
//
// Scopo: testare il codice che gestisce l'errore 429 RESOURCE_EXHAUSTED (quota superata)
// senza doverla superare davvero (che costerebbe e metterebbe in pausa l'account vero).
//
// Setup:
// - Lanciare l'app con --spring.profiles.active=stub
// - application-stub.yml configura spring.http.serviceclient.geocoding.base-url
//   per puntare a http://localhost:8080 invece del vero endpoint Google
// - Il client chiama questo controller invece di Google
// - Vediamo il flusso di errore: GeocodingService.mapError() -> GeocodingException.QuotaExceeded
//
// Il stub risponde sempre 429 RESOURCE_EXHAUSTED, indipendentemente dall'indirizzo.
@RestController
@Profile("stub")
public class GeocodingStubController {

	// Endpoint finto: GET /v4/geocode/address/{address}
	// Simula la risposta di Google quando la quota è superata.
	@GetMapping("/v4/geocode/address/{address}")
	@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
	public Map<String, Object> quotaSuperata(@PathVariable String address) {
		return Map.of("error", Map.of(
				"code", 429,
				"status", "RESOURCE_EXHAUSTED",
				"message", "Quota exceeded for quota metric 'Geocode requests'"));
	}
}
