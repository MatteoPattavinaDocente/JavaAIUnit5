package it.epicode.demo.mappe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class GeoPoint {

	// precision 9, scale 6: 3 cifre intere + 6 decimali. Copre -180.000000/180.000000
	// e in PostgreSQL diventa NUMERIC(9,6), non un double: nessun errore di arrotondamento.
	@Column(name = "latitude", precision = 9, scale = 6)
	private BigDecimal latitude;

	@Column(name = "longitude", precision = 9, scale = 6)
	private BigDecimal longitude;

	// Richiesto da JPA per ricostruire l'oggetto dal database. Protected: non e' un
	// costruttore che il nostro codice deve usare.
	protected GeoPoint() {
	}

	public GeoPoint(BigDecimal latitude, BigDecimal longitude) {
		// L'invariante vive qui dentro: una coordinata sola non e' un punto.
		// Nessun chiamante puo' creare un GeoPoint a meta'.
		if ((latitude == null) != (longitude == null)) {
			throw new IllegalArgumentException("latitudine e longitudine devono essere entrambe presenti o entrambe assenti");
		}
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public boolean isEmpty() {
		return latitude == null;
	}
}
