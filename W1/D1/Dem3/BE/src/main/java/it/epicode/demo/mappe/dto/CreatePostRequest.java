package it.epicode.demo.mappe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// Due modi di indicare il posto, mai insieme: o le coordinate dal click sulla mappa,
// o l'indirizzo da geocodificare.
public record CreatePostRequest(
		@NotBlank(message = "il titolo e' obbligatorio")
		@Size(max = 200, message = "il titolo non puo' superare 200 caratteri")
		String title,

		BigDecimal latitude,
		BigDecimal longitude,
		String address
) {

	private static final BigDecimal LAT_MIN = new BigDecimal("-90");
	private static final BigDecimal LAT_MAX = new BigDecimal("90");
	private static final BigDecimal LNG_MIN = new BigDecimal("-180");
	private static final BigDecimal LNG_MAX = new BigDecimal("180");

	public CreatePostRequest {
		if ((latitude == null) != (longitude == null)) {
			throw new IllegalArgumentException("latitudine e longitudine devono essere entrambe presenti o entrambe assenti");
		}

		boolean haCoordinate = latitude != null;
		boolean haIndirizzo = address != null && !address.isBlank();

		// Esclusivo in entrambi i sensi: senza nessuno dei due non sappiamo dove mettere
		// il post, con tutti e due non sappiamo a quale dei due credere.
		if (haCoordinate == haIndirizzo) {
			throw new IllegalArgumentException("indicare le coordinate oppure l'indirizzo, non entrambi e non nessuno dei due");
		}

		if (haCoordinate) {
			// compareTo, non equals: 44.10 e 44.100000 sono lo stesso numero ma
			// BigDecimal.equals li considera diversi perche' confronta anche la scala.
			if (latitude.compareTo(LAT_MIN) < 0 || latitude.compareTo(LAT_MAX) > 0) {
				throw new IllegalArgumentException("latitudine fuori range: deve stare tra -90 e 90");
			}
			if (longitude.compareTo(LNG_MIN) < 0 || longitude.compareTo(LNG_MAX) > 0) {
				throw new IllegalArgumentException("longitudine fuori range: deve stare tra -180 e 180");
			}
		}
	}

	public boolean haIndirizzo() {
		return address != null && !address.isBlank();
	}
}
