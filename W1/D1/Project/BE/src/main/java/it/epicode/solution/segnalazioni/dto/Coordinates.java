package it.epicode.solution.segnalazioni.dto;

import java.math.BigDecimal;

/**
 * Validazione condivisa delle coordinate.
 * Lancia IllegalArgumentException: il GlobalExceptionHandler la traduce in 400.
 */
public final class Coordinates {

	public static final BigDecimal MIN_LAT = new BigDecimal("-90");
	public static final BigDecimal MAX_LAT = new BigDecimal("90");
	public static final BigDecimal MIN_LNG = new BigDecimal("-180");
	public static final BigDecimal MAX_LNG = new BigDecimal("180");

	private Coordinates() {
	}

	public static void requireValidLatitude(BigDecimal latitude) {
		if (latitude == null) {
			throw new IllegalArgumentException("latitude è obbligatoria");
		}
		if (latitude.compareTo(MIN_LAT) < 0 || latitude.compareTo(MAX_LAT) > 0) {
			throw new IllegalArgumentException("latitude deve essere compresa fra -90 e 90, ricevuto: " + latitude);
		}
	}

	public static void requireValidLongitude(BigDecimal longitude) {
		if (longitude == null) {
			throw new IllegalArgumentException("longitude è obbligatoria");
		}
		if (longitude.compareTo(MIN_LNG) < 0 || longitude.compareTo(MAX_LNG) > 0) {
			throw new IllegalArgumentException("longitude deve essere compresa fra -180 e 180, ricevuto: " + longitude);
		}
	}

	public static void requireValid(BigDecimal latitude, BigDecimal longitude) {
		requireValidLatitude(latitude);
		requireValidLongitude(longitude);
	}

	/** Normalizza a scala 6 per allinearsi a NUMERIC(x,6) sul database. */
	public static BigDecimal normalize(BigDecimal value) {
		return value.setScale(6, java.math.RoundingMode.HALF_UP);
	}
}
