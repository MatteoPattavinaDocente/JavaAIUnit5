package it.epicode.solution.segnalazioni.geocoding;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

/**
 * Client HTTP dichiarativo per la Google Geocoding API v4 (Spring 7).
 * Il proxy viene registrato da GeocodingClientConfig tramite @ImportHttpServices;
 * la base-url arriva da spring.http.serviceclient.geocoding.base-url e vale
 * https://geocode.googleapis.com.
 *
 * Perche' v4 e non la vecchia /maps/api/geocode/json: l'API legacy risponde
 * REQUEST_DENIED ("You must enable Billing") sui progetti in cui e' abilitata
 * soltanto la Geocoding API v4. Stessa chiave, endpoint diverso.
 *
 * Differenze rispetto a v3:
 * - l'indirizzo sta nel PATH, non in query
 * - la chiave sta nell'header X-Goog-Api-Key, non nel parametro key: cosi' non
 *   finisce negli access log dei proxy attraversati dalla richiesta
 * - i parametri sono regionCode/languageCode, non region/language
 * - la risposta e' camelCase e non ha nessun campo "status": l'esito sta nello
 *   status HTTP
 */
@HttpExchange("/v4/geocode")
public interface GoogleGeocodingClient {

	/** GET /v4/geocode/address/{address}?regionCode=IT&languageCode=it */
	@GetExchange("/address/{address}")
	GeocodingApiResponse geocode(
			@PathVariable("address") String address,
			@RequestParam("regionCode") String regionCode,
			@RequestParam("languageCode") String languageCode,
			@RequestHeader("X-Goog-Api-Key") String apiKey);

	/**
	 * GET /v4/geocode/location/{latitude},{longitude}?languageCode=it
	 *
	 * Le due coordinate sono un unico segmento di path separato da una virgola:
	 * per questo il parametro e' gia' formattato come "41.9028,12.4964".
	 */
	@GetExchange("/location/{latLng}")
	GeocodingApiResponse reverseGeocode(
			@PathVariable("latLng") String latLng,
			@RequestParam("languageCode") String languageCode,
			@RequestHeader("X-Goog-Api-Key") String apiKey);
}
