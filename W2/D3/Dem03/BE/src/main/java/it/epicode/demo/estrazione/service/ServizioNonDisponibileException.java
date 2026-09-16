package it.epicode.demo.estrazione.service;

/** Un guasto del servizio esterno: si puo' ritentare (slide 9). */
public class ServizioNonDisponibileException extends RuntimeException {

	public ServizioNonDisponibileException(String messaggio) {
		super(messaggio);
	}
}
