package it.epicode.demo.mappe.dto;

import it.epicode.demo.mappe.model.Post;
import java.math.BigDecimal;
import java.time.Instant;

public record PostResponse(
		Long id,
		String title,
		BigDecimal latitude,
		BigDecimal longitude,
		Instant createdAt
) {

	// La conversione entity -> DTO sta qui: il controller riceve gia' un PostResponse
	// e non vede mai un Post. Cambiare l'entity non cambia il JSON del contratto.
	public static PostResponse from(Post post) {
		return new PostResponse(
				post.getId(),
				post.getTitle(),
				post.getLocation().getLatitude(),
				post.getLocation().getLongitude(),
				post.getCreatedAt()
		);
	}
}
