package it.epicode.solution.segnalazioni.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import it.epicode.solution.segnalazioni.dto.ApiError;
import it.epicode.solution.segnalazioni.geocoding.GeocodingException;

/**
 * Traduzione degli errori in status HTTP.
 * Punto chiave: coordinate non valide arrivano come IllegalArgumentException dal costruttore
 * compatto del record - anche quando Jackson la incapsula durante la deserializzazione -
 * e diventano 400, non 500.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> onBeanValidation(MethodArgumentNotValidException ex) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
				.toList();
		return ResponseEntity.badRequest()
				.body(ApiError.of(HttpStatus.BAD_REQUEST, "Payload non valido", details));
	}

	/**
	 * Body JSON illeggibile OPPURE costruttore del record che ha rifiutato i valori
	 * (es. latitude 200): in entrambi i casi la richiesta e' malformata -> 400.
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiError> onUnreadableBody(HttpMessageNotReadableException ex) {
		String message = rootIllegalArgumentMessage(ex);
		return ResponseEntity.badRequest()
				.body(ApiError.of(HttpStatus.BAD_REQUEST, message != null ? message : "Body della richiesta non valido"));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ApiError> onIllegalArgument(IllegalArgumentException ex) {
		return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiError> onMissingParam(MissingServletRequestParameterException ex) {
		return ResponseEntity.badRequest()
				.body(ApiError.of(HttpStatus.BAD_REQUEST, "Parametro obbligatorio mancante: " + ex.getParameterName()));
	}

	/** Parametro non convertibile: swLat=abc, category=PIPPO. */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiError> onTypeMismatch(MethodArgumentTypeMismatchException ex) {
		String message = "Valore non valido per il parametro '" + ex.getName() + "': " + ex.getValue();
		return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, message));
	}

	@ExceptionHandler(GeocodingException.AddressNotFound.class)
	public ResponseEntity<ApiError> onAddressNotFound(GeocodingException.AddressNotFound ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body(ApiError.of(HttpStatus.NOT_FOUND, ex.getMessage()));
	}

	@ExceptionHandler(GeocodingException.InvalidAddress.class)
	public ResponseEntity<ApiError> onInvalidAddress(GeocodingException.InvalidAddress ex) {
		return ResponseEntity.badRequest().body(ApiError.of(HttpStatus.BAD_REQUEST, ex.getMessage()));
	}

	@ExceptionHandler(GeocodingException.QuotaExceeded.class)
	public ResponseEntity<ApiError> onQuotaExceeded(GeocodingException.QuotaExceeded ex) {
		log.error("Quota geocoding esaurita: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.header("Retry-After", "60")
				.body(ApiError.of(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage()));
	}

	@ExceptionHandler(GeocodingException.UpstreamRejected.class)
	public ResponseEntity<ApiError> onUpstreamRejected(GeocodingException.UpstreamRejected ex) {
		log.error("Geocoding rifiutato a monte: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(ApiError.of(HttpStatus.BAD_GATEWAY, ex.getMessage()));
	}

	@ExceptionHandler(GeocodingException.Unavailable.class)
	public ResponseEntity<ApiError> onUnavailable(GeocodingException.Unavailable ex) {
		log.error("Geocoding non raggiungibile: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.header("Retry-After", "30")
				.body(ApiError.of(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> onUnexpected(Exception ex) {
		log.error("Errore non gestito", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ApiError.of(HttpStatus.INTERNAL_SERVER_ERROR, "Errore interno"));
	}

	/** Cerca nella catena delle cause il messaggio dell'IllegalArgumentException originale. */
	private String rootIllegalArgumentMessage(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			if (current instanceof IllegalArgumentException && current.getMessage() != null) {
				return current.getMessage();
			}
			current = current.getCause() == current ? null : current.getCause();
		}
		return null;
	}
}
