package it.epicode.demo.api.service;

/** Il 429: limite di richieste o di token al minuto superato (slide 11 e 23). */
public class LimiteRaggiuntoException extends RuntimeException {

	private final String riprovaDopo;

	public LimiteRaggiuntoException(String riprovaDopo) {
		super("limite di frequenza raggiunto");
		this.riprovaDopo = riprovaDopo;
	}

	public String riprovaDopo() {
		return riprovaDopo;
	}
}
