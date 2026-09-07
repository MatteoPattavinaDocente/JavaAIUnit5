package it.epicode.solution.segnalazioni.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(int status, String error, String message, List<String> details, OffsetDateTime timestamp) {

	public static ApiError of(org.springframework.http.HttpStatus status, String message) {
		return new ApiError(status.value(), status.getReasonPhrase(), message, List.of(), OffsetDateTime.now());
	}

	public static ApiError of(org.springframework.http.HttpStatus status, String message, List<String> details) {
		return new ApiError(status.value(), status.getReasonPhrase(), message, details, OffsetDateTime.now());
	}
}
