package it.epicode.solution.segnalazioni.geocoding;

import java.math.BigDecimal;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mappatura minimale della risposta della Google Geocoding API v4.
 *
 * v4 usa camelCase (formattedAddress, granularity), quindi non serve nessun
 * @JsonProperty; v3 usava snake_case (formatted_address).
 *
 * Attenzione: nel corpo NON c'e' nessun campo "status" (v3 ce l'aveva).
 * L'esito sta nello status HTTP. In particolare "indirizzo non trovato" e' un
 * 200 OK con corpo {} - results resta null, non una lista vuota: per questo
 * hasResults() controlla anche il null.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GeocodingApiResponse(List<Result> results) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Result(Location location, String formattedAddress, String granularity) {
	}

	/** In v4 i campi si chiamano latitude/longitude; in v3 erano lat/lng. */
	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Location(BigDecimal latitude, BigDecimal longitude) {
	}

	public boolean hasResults() {
		return results != null && !results.isEmpty();
	}
}
