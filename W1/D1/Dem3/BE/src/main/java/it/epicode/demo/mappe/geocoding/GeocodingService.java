package it.epicode.demo.mappe.geocoding;

import it.epicode.demo.mappe.model.GeoPoint;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

// Servizio di geocodifica: converte indirizzi in coordinate (lat/lon).
// Usa la Google Geocoding API v4. Nessuna cache in questa classe: il riuso di una
// geocodifica già fatta è responsabilità di PostService, che prima di chiamare questo
// servizio cerca in tabella un post ancora valido con lo stesso indirizzo.
@Service
public class GeocodingService {

	private static final Logger log = LoggerFactory.getLogger(GeocodingService.class);

	private final GoogleGeocodingClient client;
	private final String apiKey;

	// Per la lezione: conta le chiamate HTTP che escono davvero.
	// Serve a dimostrare che il secondo post sullo stesso indirizzo non fa salire il
	// contatore, perché PostService non arriva nemmeno a chiamare questo metodo.
	private final AtomicLong chiamateDiRete = new AtomicLong();

	// Legge la chiave API dalla variabile di ambiente via @Value.
	// Fallisce al bootstrap se manca: meglio sapere subito che il deployment è rotto,
	// piuttosto che lasciarlo partire e poi avere 502 "unregistered callers" dal cloud
	// 30 minuti dopo, quando il problema è diventato difficile da debuggare.
	public GeocodingService(GoogleGeocodingClient client,
			@Value("${app.google.geocoding.api-key}") String apiKey) {
		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(
					"Manca la variabile d'ambiente GOOGLE_GEOCODING_API_KEY: il geocoding non puo' funzionare.");
		}
		this.client = client;
		this.apiKey = apiKey;
	}

	// Normalizza un indirizzo per usarlo come chiave di confronto:
	// "Via del Prione 1 " e "via del prione 1" devono dare la stessa chiave.
	//
	// Locale.ROOT e non toLowerCase() senza argomenti: il default usa il locale della
	// JVM, e in locale turco 'I' diventa una 'i' senza punto. Due server con locale
	// diverso produrrebbero chiavi diverse per lo stesso indirizzo, e il riuso non
	// scatterebbe mai. Con ROOT la regola è la stessa su ogni macchina.
	//
	// Sta qui e non in PostService perché è la definizione di "stesso indirizzo" per
	// questo servizio: chi scrive la chiave in tabella e chi la cerca devono usare la
	// stessa funzione, altrimenti scrivono e leggono chiavi che non si incontrano.
	public static String normalizeAddress(String address) {
		return address == null ? null : address.strip().toLowerCase(Locale.ROOT);
	}

	// Geocodifica un indirizzo (testo) in coordinate (lat/lon).
	// Ogni invocazione è una chiamata di rete: il filtro sta a monte, in PostService,
	// che cerca in tabella un post valido con lo stesso indirizzo prima di arrivare qui.
	public GeocodeResult geocode(String address) {
		log.info("Chiamata di rete a Google per '{}' (uscite in rete finora: {})",
				address, chiamateDiRete.incrementAndGet());

		GeocodeApiResponse response;
		try {
			// client è il proxy HTTP generato da Spring. Chiama Google con il nostro indirizzo.
			response = client.geocode(address, "IT", "it", apiKey);
		} catch (RestClientResponseException e) {
			// Google ha risposto con un codice di errore (4xx, 5xx).
			// Parsiamo il corpo della risposta e solleviamo un'eccezione specifica.
			throw mapError(e);
		} catch (RestClientException e) {
			// Errori di rete: timeout, DNS down, connessione rifiutata.
			// Non c'è nessuna risposta HTTP da leggere, solo un'eccezione client-side.
			throw new GeocodingException.Unavailable(e.getMessage());
		}

		// Il caso sleeper: Google ha risposto 200 OK (no HTTP error), ma results è vuoto.
		// Significa che l'indirizzo non è stato trovato (non è un errore).
		// Se controllassimo solo is2xxSuccessful(), qui potremmo fare NullPointerException
		// perché results potrebbe essere null o una lista vuota. Controlliamo isEmpty().
		if (response == null || response.isEmpty()) {
			throw new GeocodingException.AddressNotFound(address);
		}

		// Prendiamo il primo risultato (ne basta uno).
		GeocodeApiResponse.Result primo = response.results().getFirst();
		return new GeocodeResult(
				new GeoPoint(primo.location().latitude(), primo.location().longitude()),
				primo.formattedAddress());
	}

	// Mappa la risposta di errore HTTP di Google in un'eccezione GeocodingException.
	//
	// La discriminante è lo status testuale nel corpo (es. INVALID_ARGUMENT, PERMISSION_DENIED),
	// NON il codice HTTP. Esempio: 400 INVALID_ARGUMENT può significare "indirizzo malformato"
	// oppure "chiave API non valida". Il messaggio nel body ci dice quale. Solo lì possiamo
	// discriminare tra InvalidAddress e ApiKeyRejected.
	private GeocodingException mapError(RestClientResponseException e) {
		GeocodeApiError body = e.getResponseBodyAs(GeocodeApiError.class);
		String status = body != null && body.error() != null ? body.error().status() : null;
		String message = body != null && body.error() != null ? body.error().message() : e.getMessage();

		return switch (status) {
			// Chiave API non abilitata o scaduta.
			case "PERMISSION_DENIED" -> new GeocodingException.ApiKeyRejected(message);
			// Quota giornaliera superata.
			case "RESOURCE_EXHAUSTED" -> new GeocodingException.QuotaExceeded(message);
			// Indirizzo malformato o chiave API invalida (il messaggio ci distingue i due).
			case "INVALID_ARGUMENT" -> message != null && message.contains("API key")
					? new GeocodingException.ApiKeyRejected(message)
					: new GeocodingException.InvalidAddress(message);
			// Errori HTTP inaspettati (5xx, network error, deserialize error).
			case null, default -> new GeocodingException.Unavailable(message);
		};
	}

	// Risultato di una geocodifica: coordinate + indirizzo normalizzato.
	public record GeocodeResult(GeoPoint location, String formattedAddress) {
	}
}
