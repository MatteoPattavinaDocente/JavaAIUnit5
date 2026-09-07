package it.epicode.demo.mappe.geocoding;

// Gerarchia di eccezioni per il geocoding.
// Una sottoclasse per ogni esito possibile: il TYPE dell'eccezione comunica l'errore,
// non una stringa dentro Exception.message().
//
// Benefici:
// 1. Il @RestControllerAdvice fa uno switch esaustivo sulle sottoclassi (sealed).
// 2. Ogni eccezione mapizza a un diverso status HTTP e messaggio client.
// 3. Se aggiungiamo una sottoclasse e dimentichiamo il mapping, il compilatore
//    ci ferma (sealed class): non rischiamo che un utente veda "unknown error".
//
// Eccezioni mappate da GeocodingService.mapError().
public abstract sealed class GeocodingException extends RuntimeException {

	protected GeocodingException(String message) {
		super(message);
	}

	// Google ha risposto 200 OK, ma results è vuoto: indirizzo non trovato (non è un errore API).
	// Mappizza a 404 Not Found nel controller.
	public static final class AddressNotFound extends GeocodingException {
		public AddressNotFound(String address) {
			super("nessun risultato per l'indirizzo: " + address);
		}
	}

	// Google ha risposto 400 INVALID_ARGUMENT: indirizzo malformato o non riconosciuto.
	// Mappizza a 400 Bad Request nel controller.
	public static final class InvalidAddress extends GeocodingException {
		public InvalidAddress(String message) {
			super("indirizzo non valido per il servizio di geocodifica: " + message);
		}
	}

	// Google ha risposto 429 RESOURCE_EXHAUSTED: quota giornaliera superata.
	// Mappizza a 429 Too Many Requests nel controller.
	public static final class QuotaExceeded extends GeocodingException {
		public QuotaExceeded(String message) {
			super("quota del servizio di geocodifica superata: " + message);
		}
	}

	// Google ha risposto 403 PERMISSION_DENIED: chiave API non valida/disabilitata/scaduta.
	// Errore nostro, non dell'utente. Mappizza a 500 Internal Server Error nel controller.
	public static final class ApiKeyRejected extends GeocodingException {
		public ApiKeyRejected(String message) {
			super("chiave API rifiutata dal servizio di geocodifica: " + message);
		}
	}

	// Tutto il resto: errori di rete (timeout, DNS down), 5xx di Google, connessione rifiutata, ecc.
	// Mappizza a 503 Service Unavailable nel controller.
	public static final class Unavailable extends GeocodingException {
		public Unavailable(String message) {
			super("servizio di geocodifica non raggiungibile: " + message);
		}
	}
}
