package it.epicode.solution.segnalazioni.geocoding;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import it.epicode.solution.segnalazioni.domain.GeocodeKind;
import it.epicode.solution.segnalazioni.dto.Coordinates;
import it.epicode.solution.segnalazioni.dto.GeocodeResponse;

/**
 * Geocoding lato server sulla Google Geocoding API v4: la chiave Google resta qui
 * e non viene mai esposta al frontend.
 *
 * Prima di spendere una chiamata si guarda in geocoding_cache (vedi
 * {@link GeocodingCache}): la stessa chiave viene chiesta a Google al massimo una
 * volta ogni 30 giorni. Scaduto il TTL la si richiede, cosi' un indirizzo che nel
 * frattempo e' cambiato non resta sbagliato per sempre.
 *
 * La cache viene scritta solo dopo una risposta valida: un fallimento non
 * "avvelena" la chiave, e la richiesta successiva riprova.
 *
 * Nota su v4: non c'e' nessun campo "status" nel corpo. L'esito riuscito sta
 * nello status HTTP, e "nessun risultato" e' un 200 con corpo {}. Gli errori
 * arrivano invece come 4xx/5xx e vanno letti dal corpo dell'eccezione.
 */
@Service
public class GeocodingService {

	private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

	private final GoogleGeocodingClient client;
	private final GeocodingCache cache;
	private final String apiKey;
	private final String language;
	private final String region;

	public GeocodingService(GoogleGeocodingClient client, GeocodingCache cache,
			@Value("${app.google.geocoding.api-key}") String apiKey,
			@Value("${app.google.geocoding.language:it}") String language,
			@Value("${app.google.geocoding.region:IT}") String region) {
		// Meglio accorgersene all'avvio che ricevere un 403 "unregistered callers"
		// alla prima richiesta di un utente.
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(
					"Manca GOOGLE_GEOCODING_API_KEY (BE/secrets.properties o variabile d'ambiente): "
							+ "il geocoding non puo' funzionare.");
		}
		this.client = client;
		this.cache = cache;
		this.apiKey = apiKey;
		this.language = language;
		this.region = region;
	}

	public GeocodeResponse geocode(String address) {
		String query = address == null ? "" : address.strip();
		if (query.isEmpty()) {
			throw new IllegalArgumentException("address e' obbligatorio");
		}

		String key = cacheKey(query);
		Optional<GeocodeResponse> cached = cache.find(GeocodeKind.FORWARD, key);
		if (cached.isPresent()) {
			return cached.get();
		}

		log.info("Geocoding: cache MISS, chiamata a Google per [{}]", query);
		GeocodingApiResponse response = call(() -> client.geocode(query, region, language, apiKey), query);
		GeocodeResponse fresh = toResponse(response, query);
		cache.store(GeocodeKind.FORWARD, key, fresh);
		return fresh;
	}

	public GeocodeResponse reverseGeocode(BigDecimal latitude, BigDecimal longitude) {
		Coordinates.requireValid(latitude, longitude);
		String latLng = cacheKey(latitude, longitude);

		Optional<GeocodeResponse> cached = cache.find(GeocodeKind.REVERSE, latLng);
		if (cached.isPresent()) {
			return cached.get();
		}

		log.info("Reverse geocoding: cache MISS, chiamata a Google per [{}]", latLng);
		GeocodingApiResponse response = call(() -> client.reverseGeocode(latLng, language, apiKey), latLng);
		GeocodeResponse fresh = toResponse(response, latLng);
		cache.store(GeocodeKind.REVERSE, latLng, fresh);
		return fresh;
	}

	/**
	 * Chiave di cache dell'indirizzo: trim, minuscolo, spazi compattati. Cosi'
	 * "Via Roma 1, Milano" e "via  roma 1,  milano" condividono la stessa riga.
	 */
	public String cacheKey(String address) {
		if (address == null) {
			return "";
		}
		return address.strip().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
	}

	/**
	 * Chiave di cache delle coordinate, che e' anche il formato che v4 vuole nel
	 * path: "41.902800,12.496400".
	 *
	 * La scala 6 non serve solo al database: taglia la chiave a ~11 cm, quindi due
	 * click praticamente nello stesso punto riusano la stessa geocodifica invece di
	 * generare una riga di cache a testa.
	 */
	public String cacheKey(BigDecimal latitude, BigDecimal longitude) {
		return Coordinates.normalize(latitude).toPlainString() + "," + Coordinates.normalize(longitude).toPlainString();
	}

	private GeocodingApiResponse call(Supplier<GeocodingApiResponse> invocation, String query) {
		try {
			return invocation.get();
		}
		catch (RestClientResponseException ex) {
			// Google ha risposto con 4xx/5xx: lo status testuale nel corpo dice
			// quale errore sia davvero.
			throw mapError(ex, query);
		}
		catch (RestClientException ex) {
			// Nessuna risposta HTTP: timeout, DNS, connessione rifiutata,
			// corpo non deserializzabile.
			log.warn("Geocoding non raggiungibile per [{}]: {}", query, ex.getMessage());
			throw new GeocodingException.Unavailable("Servizio di geocoding non raggiungibile per: " + query, ex);
		}
	}

	private GeocodeResponse toResponse(GeocodingApiResponse response, String query) {
		// Il caso silenzioso: 200 OK con corpo {}. Nessun errore HTTP, ma anche
		// nessun risultato: va controllato PRIMA di leggere results.
		if (response == null || !response.hasResults()) {
			throw new GeocodingException.AddressNotFound(query);
		}

		GeocodingApiResponse.Result first = response.results().getFirst();
		GeocodingApiResponse.Location location = first.location();
		if (location == null || location.latitude() == null || location.longitude() == null) {
			throw new GeocodingException.UpstreamRejected("GEOMETRIA_MANCANTE", null);
		}

		Coordinates.requireValid(location.latitude(), location.longitude());
		return GeocodeResponse.fromApi(
				Coordinates.normalize(location.latitude()),
				Coordinates.normalize(location.longitude()),
				first.formattedAddress());
	}

	/**
	 * Traduce l'errore di Google in una delle nostre eccezioni.
	 *
	 * La discriminante e' lo status testuale nel corpo, non il codice HTTP: un
	 * 400 INVALID_ARGUMENT puo' voler dire "indirizzo malformato" oppure "chiave
	 * API non valida", e solo il messaggio distingue i due casi.
	 */
	private GeocodingException mapError(RestClientResponseException ex, String query) {
		GeocodingApiError body = ex.getResponseBodyAs(GeocodingApiError.class);
		String status = body == null ? null : body.status();
		String message = body == null || body.message() == null ? ex.getMessage() : body.message();

		log.warn("Geocoding ha risposto {} ({}) per [{}]: {}", ex.getStatusCode(), status, query, message);

		return switch (status) {
			// Chiave non abilitata su questa API, oppure API non attiva sul progetto.
			case "PERMISSION_DENIED", "UNAUTHENTICATED" -> new GeocodingException.UpstreamRejected(status, message);
			// Quota superata.
			case "RESOURCE_EXHAUSTED" -> new GeocodingException.QuotaExceeded(status, message);
			// Indirizzo illeggibile, oppure chiave malformata: il messaggio decide.
			case "INVALID_ARGUMENT" -> message != null && message.contains("API key")
					? new GeocodingException.UpstreamRejected(status, message)
					: new GeocodingException.InvalidAddress(query, message);
			case "NOT_FOUND" -> new GeocodingException.AddressNotFound(query);
			case null, default -> new GeocodingException.UpstreamRejected(
					status == null ? "SCONOSCIUTO" : status, message);
		};
	}
}
