package it.epicode.demo.mappe.web;

import it.epicode.demo.mappe.geocoding.GeocodingException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	// Le annotazioni sui singoli campi (@NotBlank sul titolo) falliscono qui.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleValidation(MethodArgumentNotValidException ex) {
		List<String> messages = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage())
				.toList();
		return ApiError.badRequest(messages);
	}

	// Quando il compact constructor lancia, l'eccezione avviene DENTRO Jackson, che
	// non e' ancora riuscito a costruire l'oggetto: Spring la consegna avvolta in una
	// HttpMessageNotReadableException. Senza scavare nelle cause il messaggio
	// dell'invariante andrebbe perso e il frontend leggerebbe solo "JSON non valido".
	@ExceptionHandler(HttpMessageNotReadableException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleUnreadable(HttpMessageNotReadableException ex) {
		Throwable cause = ex;
		while (cause != null) {
			if (cause instanceof IllegalArgumentException) {
				return ApiError.badRequest(List.of(cause.getMessage()));
			}
			cause = cause.getCause();
		}
		return ApiError.badRequest(List.of("corpo della richiesta non leggibile"));
	}

	// Qui si vede il senso di avere cinque eccezioni invece di una: lo switch e'
	// esaustivo perche' GeocodingException e' sealed. Aggiungerne una sesta senza
	// mapparla non compila.
	//
	// La scelta degli status: 404 l'indirizzo non c'e'; 400 la richiesta e' malfatta;
	// 429 riporta al chiamante il "rallenta" di Google; 502 dice "il servizio a monte
	// non collabora" — chiave sbagliata compresa, perche' e' un problema nostro di
	// configurazione, non un errore di chi ha chiamato.
	@ExceptionHandler(GeocodingException.class)
	public ResponseEntity<ApiError> handleGeocoding(GeocodingException ex) {
		HttpStatus status = switch (ex) {
			case GeocodingException.AddressNotFound ignored -> HttpStatus.NOT_FOUND;
			case GeocodingException.InvalidAddress ignored -> HttpStatus.BAD_REQUEST;
			case GeocodingException.QuotaExceeded ignored -> HttpStatus.TOO_MANY_REQUESTS;
			case GeocodingException.ApiKeyRejected ignored -> HttpStatus.BAD_GATEWAY;
			case GeocodingException.Unavailable ignored -> HttpStatus.BAD_GATEWAY;
		};
		return ResponseEntity.status(status).body(ApiError.of(status, List.of(ex.getMessage())));
	}

	// L'invariante di GeoPoint, se qualcuno costruisce un punto a meta' nel service.
	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleIllegalArgument(IllegalArgumentException ex) {
		return ApiError.badRequest(List.of(ex.getMessage()));
	}
}
