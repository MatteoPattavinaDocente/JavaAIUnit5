package it.epicode.solution.segnalazioni.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corpo di errore della Geocoding API v4:
 * { "error": { "code": 400, "status": "INVALID_ARGUMENT", "message": "..." } }
 *
 * Lo status testuale e' piu' preciso del codice HTTP: Google usa 400 sia per un
 * indirizzo illeggibile sia per una chiave non valida, e solo il messaggio nel
 * corpo distingue i due casi.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingApiError(Detail error) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Detail(int code, String status, String message) {
	}

	public String status() {
		return error == null ? null : error.status();
	}

	public String message() {
		return error == null ? null : error.message();
	}
}
