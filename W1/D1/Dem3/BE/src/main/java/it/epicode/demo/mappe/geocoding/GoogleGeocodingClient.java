package it.epicode.demo.mappe.geocoding;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

// Client HTTP dichiarativo per la Google Geocoding API v4.
// Spring legge questa interfaccia e genera il proxy HTTP al runtime.
// L'implementazione viene registrata da GeocodingClientConfig con la base-url
// da spring.http.serviceclient.geocoding.base-url.
//
// Vantaggi: no RestTemplate boilerplate, no string concatenazione, type-safe.
// Spring gestisce serializzazione, status HTTP, errori di rete.
@HttpExchange("/v4/geocode")
public interface GoogleGeocodingClient {

	// Endpoint: GET /v4/geocode/address/{address}?regionCode=...&languageCode=...
	//
	// Parametri:
	// - address: indirizzo da geocodificare (in path, non query: richiesta API v4)
	// - regionCode: codice ISO della regione (es. "IT" per Italia) per geo-bilanciamento
	// - languageCode: lingua della risposta (es. "it" per italiano)
	// - apiKey: credenziale (NELL'HEADER, non in query per non finire negli access log dei proxy)
	//
	// Nota: il routing è in header X-Goog-Api-Key perché se la chiave andasse in query,
	// passerebbe attraverso ogni proxy della catena: DNS, load balancer, CDN.
	// Finire negli access log di una macchina sconosciuta = compromissione della credenziale.
	@GetExchange("/address/{address}")
	GeocodeApiResponse geocode(
			@PathVariable("address") String address,
			@RequestParam("regionCode") String regionCode,
			@RequestParam("languageCode") String languageCode,
			@RequestHeader("X-Goog-Api-Key") String apiKey);
}
