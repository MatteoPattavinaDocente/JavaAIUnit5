package it.epicode.demo.mappe.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// Rappresenta un errore dalla Google Geocoding API v4.
// Struttura: { "error": { "code": 400, "status": "INVALID_ARGUMENT", "message": "..." } }
//
// Lo status testuale (INVALID_ARGUMENT, PERMISSION_DENIED, etc.) è più preciso del codice HTTP.
// Google usa 400 sia per indirizzi illeggibili che per chiavi non valide: solo lo status
// nel body ci dice di quale errore si tratta. GeocodingService.mapError() usa questo status.
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodeApiError(Detail error) {

	// Dettaglio dell'errore: codice HTTP, status testuale (la parte che conta), messaggio.
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Detail(int code, String status, String message) {
	}
}
