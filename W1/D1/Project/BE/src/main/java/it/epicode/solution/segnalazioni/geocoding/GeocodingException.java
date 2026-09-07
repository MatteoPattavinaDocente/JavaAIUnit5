package it.epicode.solution.segnalazioni.geocoding;

/** Radice degli errori di geocoding: la sottoclasse determina lo status HTTP restituito. */
public abstract class GeocodingException extends RuntimeException {

	protected GeocodingException(String message) {
		super(message);
	}

	protected GeocodingException(String message, Throwable cause) {
		super(message, cause);
	}

	/** Nessun risultato (in v4: 200 con corpo vuoto) -> 404. */
	public static class AddressNotFound extends GeocodingException {
		public AddressNotFound(String query) {
			super("Nessun risultato di geocoding per: " + query);
		}
	}

	/** INVALID_ARGUMENT su un indirizzo illeggibile: colpa dell'input -> 400. */
	public static class InvalidAddress extends GeocodingException {
		public InvalidAddress(String query, String detail) {
			super("Indirizzo non interpretabile: " + query + (detail == null ? "" : " (" + detail + ")"));
		}
	}

	/** RESOURCE_EXHAUSTED: quota superata -> 429. */
	public static class QuotaExceeded extends GeocodingException {
		public QuotaExceeded(String status, String detail) {
			super("Quota Geocoding superata (" + status + ")" + (detail == null ? "" : ": " + detail));
		}
	}

	/** PERMISSION_DENIED / UNAUTHENTICATED / errori inattesi a monte -> 502. */
	public static class UpstreamRejected extends GeocodingException {
		public UpstreamRejected(String status, String detail) {
			super("Geocoding ha risposto " + status + (detail == null ? "" : ": " + detail));
		}
	}

	/** Errore di rete, timeout o HTTP non 2xx -> 503. */
	public static class Unavailable extends GeocodingException {
		public Unavailable(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
