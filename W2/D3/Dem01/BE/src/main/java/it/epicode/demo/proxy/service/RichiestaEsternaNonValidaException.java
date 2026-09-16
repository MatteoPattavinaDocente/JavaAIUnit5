package it.epicode.demo.proxy.service;

/**
 * Un 4xx del servizio esterno e' un errore NOSTRO, non dell'utente (slide 9):
 * abbiamo costruito male la richiesta, o la chiave non va.
 */
public class RichiestaEsternaNonValidaException extends RuntimeException {

	public RichiestaEsternaNonValidaException(String messaggio) {
		super(messaggio);
	}
}
