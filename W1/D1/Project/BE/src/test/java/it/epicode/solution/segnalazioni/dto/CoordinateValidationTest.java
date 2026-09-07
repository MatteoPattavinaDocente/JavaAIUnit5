package it.epicode.solution.segnalazioni.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import it.epicode.solution.segnalazioni.domain.Category;

class CoordinateValidationTest {

	private static final BigDecimal MILANO_LAT = new BigDecimal("45.464211");
	private static final BigDecimal MILANO_LNG = new BigDecimal("9.191383");

	@Test
	void accettaCoordinateValideENormalizzaLaScala() {
		CreateReportRequest request = new CreateReportRequest(
				Category.BUCA, "  Buca profonda  ", new BigDecimal("45.4"), new BigDecimal("9.2"), null);

		assertEquals(6, request.latitude().scale());
		assertEquals(6, request.longitude().scale());
		assertEquals("Buca profonda", request.description());
	}

	@Test
	void rifiutaLatitudineFuoriRange() {
		assertThrows(IllegalArgumentException.class, () -> new CreateReportRequest(
				Category.BUCA, "test", new BigDecimal("91"), MILANO_LNG, null));
	}

	@Test
	void rifiutaLongitudineFuoriRange() {
		assertThrows(IllegalArgumentException.class, () -> new CreateReportRequest(
				Category.RIFIUTI, "test", MILANO_LAT, new BigDecimal("-180.1"), null));
	}

	@Test
	void rifiutaCoordinateNulle() {
		assertThrows(IllegalArgumentException.class, () -> new CreateReportRequest(
				Category.ALTRO, "test", null, MILANO_LNG, null));
	}

	@Test
	void viewportRifiutaSudSopraNord() {
		assertThrows(IllegalArgumentException.class, () -> new Viewport(
				new BigDecimal("46"), new BigDecimal("9"), new BigDecimal("45"), new BigDecimal("10")));
	}

	@Test
	void viewportRilevaAntimeridiano() {
		Viewport normale = new Viewport(
				new BigDecimal("45"), new BigDecimal("9"), new BigDecimal("46"), new BigDecimal("10"));
		Viewport aCavallo = new Viewport(
				new BigDecimal("-10"), new BigDecimal("170"), new BigDecimal("10"), new BigDecimal("-170"));

		assertEquals(false, normale.crossesAntimeridian());
		assertEquals(true, aCavallo.crossesAntimeridian());
	}
}
