package it.epicode.solution.segnalazioni.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * Risultato del geocoding esposto al frontend: la chiave Google non compare mai.
 *
 * cached/fetchedAt raccontano la provenienza del dato: senza di essi una cache
 * che funziona e una cache che non viene mai usata sono indistinguibili dal
 * client, e fetchedAt dice anche quanto manca alla scadenza (TTL 30 giorni).
 */
public record GeocodeResponse(
		BigDecimal latitude,
		BigDecimal longitude,
		String formattedAddress,
		boolean cached,
		OffsetDateTime fetchedAt) {

	/** Risposta appena arrivata dalle API: fetchedAt e' adesso. */
	public static GeocodeResponse fromApi(BigDecimal latitude, BigDecimal longitude, String formattedAddress) {
		return new GeocodeResponse(latitude, longitude, formattedAddress, false, OffsetDateTime.now());
	}

	/** Risposta riusata dalla tabella geocoding_cache: fetchedAt e' la chiamata originale. */
	public static GeocodeResponse fromCache(BigDecimal latitude, BigDecimal longitude, String formattedAddress,
			OffsetDateTime fetchedAt) {
		return new GeocodeResponse(latitude, longitude, formattedAddress, true, fetchedAt);
	}
}
