package it.epicode.demo.chiavi.web;

import it.epicode.demo.chiavi.service.LimiteRaggiuntoException;
import it.epicode.demo.chiavi.service.LimiteSuperatoException;
import it.epicode.demo.chiavi.service.RichiestaEsternaNonValidaException;
import it.epicode.demo.chiavi.service.ServizioNonDisponibileException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	/**
	 * Il limite NOSTRO, non quello del servizio. Il messaggio deve essere
	 * comprensibile: l'utente non sa cosa sia un token, quindi gli si dice
	 * quanto ha usato, quanto poteva usare e quando ricomincia (slide 45).
	 */
	@ExceptionHandler(LimiteSuperatoException.class)
	public ResponseEntity<ApiError> limiteUtente(LimiteSuperatoException ex) {
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.body(ApiError.di(429, "Too Many Requests",
						"hai raggiunto il limite di utilizzo giornaliero (%d token su %d): riprende domani"
								.formatted(ex.usati(), ex.limite())));
	}

	/** Il 429 del servizio esterno: causa diversa, messaggio diverso. */
	@ExceptionHandler(LimiteRaggiuntoException.class)
	public ResponseEntity<ApiError> limiteServizio(LimiteRaggiuntoException ex) {
		var intestazioni = new HttpHeaders();
		if (ex.riprovaDopo() != null) {
			intestazioni.add("Retry-After", ex.riprovaDopo());
		}
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.headers(intestazioni)
				.body(ApiError.di(429, "Too Many Requests",
						"il servizio e' momentaneamente occupato"
								+ (ex.riprovaDopo() == null ? "" : ": riprova fra " + ex.riprovaDopo() + " secondi")));
	}

	@ExceptionHandler(ServizioNonDisponibileException.class)
	public ResponseEntity<ApiError> servizioGiu(ServizioNonDisponibileException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(ApiError.di(502, "Bad Gateway",
						"il servizio di generazione non e' raggiungibile in questo momento, riprova"));
	}

	@ExceptionHandler(RichiestaEsternaNonValidaException.class)
	public ResponseEntity<ApiError> richiestaEsterna(RichiestaEsternaNonValidaException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(ApiError.di(502, "Bad Gateway",
						"la richiesta al servizio di generazione non e' stata accettata: e' un problema di configurazione, non tuo"));
	}

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

	@ExceptionHandler(IllegalArgumentException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public ApiError handleIllegalArgument(IllegalArgumentException ex) {
		return ApiError.badRequest(List.of(ex.getMessage()));
	}
}
