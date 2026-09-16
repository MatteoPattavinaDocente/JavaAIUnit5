package it.epicode.demo.estrazione.web;

import it.epicode.demo.estrazione.service.LimiteRaggiuntoException;
import it.epicode.demo.estrazione.service.RichiestaEsternaNonValidaException;
import it.epicode.demo.estrazione.service.ServizioNonDisponibileException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * La regola di traduzione (slide 9): l'utente riceve un messaggio
 * comprensibile, il log riceve il dettaglio tecnico completo.
 *
 * Nessuno di questi messaggi contiene il testo dell'errore esterno: rimandarlo
 * al browser e' il modo piu' rapido per esporre la chiave.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

	/** 502: il servizio a monte ha ceduto. Non e' colpa dell'utente. */
	@ExceptionHandler(ServizioNonDisponibileException.class)
	public ResponseEntity<ApiError> servizioGiu(ServizioNonDisponibileException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(ApiError.di(502, "Bad Gateway",
						"il servizio di generazione non e' raggiungibile in questo momento, riprova"));
	}

	/**
	 * Anche questo e' 502, non 400: un 4xx esterno significa che NOI abbiamo
	 * costruito male la richiesta, non che l'utente ha sbagliato (slide 9).
	 */
	@ExceptionHandler(RichiestaEsternaNonValidaException.class)
	public ResponseEntity<ApiError> richiestaEsterna(RichiestaEsternaNonValidaException ex) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
				.body(ApiError.di(502, "Bad Gateway",
						"la richiesta al servizio di generazione non e' stata accettata: e' un problema di configurazione, non tuo"));
	}

	/** 429, con l'indicazione di quando riprovare. */
	@ExceptionHandler(LimiteRaggiuntoException.class)
	public ResponseEntity<ApiError> limite(LimiteRaggiuntoException ex) {
		var intestazioni = new HttpHeaders();
		if (ex.riprovaDopo() != null) {
			intestazioni.add("Retry-After", ex.riprovaDopo());
		}
		return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
				.headers(intestazioni)
				.body(ApiError.di(429, "Too Many Requests",
						"troppe richieste in questo momento"
								+ (ex.riprovaDopo() == null ? "" : ": riprova fra " + ex.riprovaDopo() + " secondi")));
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
