package it.epicode.solution.segnalazioni.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

/** La regola della cache simulata: dentro i 30 giorni si riusa, oltre si richiede alle API. */
class GeocodeCacheEntryTest {

	private static final Duration TTL = Duration.ofDays(30);
	private static final BigDecimal ROMA_LAT = new BigDecimal("41.902800");
	private static final BigDecimal ROMA_LNG = new BigDecimal("12.496400");

	private GeocodeCacheEntry entry() {
		return new GeocodeCacheEntry(GeocodeKind.FORWARD, "via roma 1, milano",
				ROMA_LAT, ROMA_LNG, "Via Roma, 1, Milano MI, Italia");
	}

	@Test
	void appenaGeocodificataEFresca() {
		assertTrue(entry().isFresh(TTL));
	}

	@Test
	void restaFrescaFinoAlTrentesimoGiorno() {
		GeocodeCacheEntry entry = entry();
		OffsetDateTime fraVentinoveGiorni = entry.getFetchedAt().plusDays(29);

		assertTrue(entry.isFresh(TTL, fraVentinoveGiorni));
	}

	@Test
	void scadeOltreIlTtl() {
		GeocodeCacheEntry entry = entry();

		// Esattamente a 30 giorni il limite e' escluso: la entry e' gia' vecchia.
		assertFalse(entry.isFresh(TTL, entry.getFetchedAt().plus(TTL)));
		assertFalse(entry.isFresh(TTL, entry.getFetchedAt().plusDays(31)));
	}

	@Test
	void refreshRiscriveLaEntryEFaRipartireIlTtl() {
		GeocodeCacheEntry entry = entry();
		OffsetDateTime primaGeocodifica = entry.getFetchedAt();

		entry.refresh(new BigDecimal("45.464211"), new BigDecimal("9.191383"), "Piazza Duomo, Milano MI, Italia");

		assertEquals(new BigDecimal("45.464211"), entry.getLatitude());
		assertEquals(new BigDecimal("9.191383"), entry.getLongitude());
		assertEquals("Piazza Duomo, Milano MI, Italia", entry.getFormattedAddress());
		// Il punto del refresh: il TTL riparte, quindi la entry scaduta torna
		// riusabile senza inserire una riga nuova.
		assertFalse(entry.getFetchedAt().isBefore(primaGeocodifica));
		assertTrue(entry.isFresh(TTL));
	}
}
