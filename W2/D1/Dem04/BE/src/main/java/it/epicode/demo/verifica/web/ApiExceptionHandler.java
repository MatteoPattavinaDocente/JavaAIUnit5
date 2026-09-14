package it.epicode.demo.verifica.web;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

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
