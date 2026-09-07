package it.epicode.solution.segnalazioni.dto;

import java.math.BigDecimal;

/**
 * Rettangolo visibile sulla mappa (angolo sud-ovest / nord-est).
 * Le coordinate sono validate nel costruttore compatto: fuori range → 400.
 */
public record Viewport(BigDecimal swLat, BigDecimal swLng, BigDecimal neLat, BigDecimal neLng) {

	public Viewport {
		Coordinates.requireValid(swLat, swLng);
		Coordinates.requireValid(neLat, neLng);
		if (swLat.compareTo(neLat) > 0) {
			throw new IllegalArgumentException("swLat (" + swLat + ") non può essere maggiore di neLat (" + neLat + ")");
		}
	}

	/**
	 * true quando il viewport attraversa l'antimeridiano (es. swLng=170, neLng=-170):
	 * in quel caso il filtro sulla longitudine è una OR, non un BETWEEN.
	 */
	public boolean crossesAntimeridian() {
		return swLng.compareTo(neLng) > 0;
	}
}
