package it.epicode.demo.mappe.geocoding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;

// Rappresenta la risposta della Google Geocoding API v4.
// Un array di Result, uno per ogni indirizzo trovato.
//
// API v4 usa camelCase (formattedAddress, granularity): nessun @JsonProperty necessario.
// API v3 usava snake_case (formatted_address, location_type).
//
// Attenzione: non c'è nessun campo "status" nel corpo (v3 lo aveva).
// In v4 l'esito sta nello status HTTP (200, 400, 403, 429...).
// "Nessun risultato trovato" = 200 OK con results vuoto: è un successo,
// ma isEmpty() lo distingue dall'errore. Controllare isEmpty() PRIMA di usare results.
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodeApiResponse(List<Result> results) {

	// Un singolo risultato: coordinate e indirizzo formattato nel locale richiesto.
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result(Location location, String formattedAddress, String granularity) {
	}

	// Coordinate: latitudine e longitudine in precisione alta (BigDecimal evita errori di arrotondamento).
	// v3 li chiamava lat/lng.
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Location(BigDecimal latitude, BigDecimal longitude) {
	}

	// Verifica se la risposta è vuota (nessun risultato trovato).
	public boolean isEmpty() {
		return results == null || results.isEmpty();
	}
}
