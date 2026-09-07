package it.epicode.solution.segnalazioni.dto;

import java.math.BigDecimal;

import it.epicode.solution.segnalazioni.domain.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload di creazione. La validazione delle coordinate sta nel costruttore compatto:
 * nessuna istanza con coordinate fuori range può esistere.
 */
public record CreateReportRequest(

		@NotNull(message = "category è obbligatoria")
		Category category,

		@NotBlank(message = "description è obbligatoria")
		@Size(max = 500, message = "description non può superare 500 caratteri")
		String description,

		BigDecimal latitude,

		BigDecimal longitude,

		@Size(max = 255, message = "address non può superare 255 caratteri")
		String address
) {

	public CreateReportRequest {
		Coordinates.requireValid(latitude, longitude);
		latitude = Coordinates.normalize(latitude);
		longitude = Coordinates.normalize(longitude);
		if (description != null) {
			description = description.strip();
		}
		if (address != null && address.isBlank()) {
			address = null;
		}
	}
}
