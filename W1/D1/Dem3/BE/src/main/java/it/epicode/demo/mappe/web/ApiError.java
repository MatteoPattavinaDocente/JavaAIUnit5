package it.epicode.demo.mappe.web;

import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

// Un solo formato di errore per tutta l'API: qualunque cosa vada storta, il
// frontend legge sempre le stesse chiavi.
public record ApiError(Instant timestamp, int status, String error, List<String> messages) {

	public static ApiError of(HttpStatus status, List<String> messages) {
		return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(), messages);
	}

	public static ApiError badRequest(List<String> messages) {
		return of(HttpStatus.BAD_REQUEST, messages);
	}
}
