package it.epicode.demo.smtp.dto;

/**
 * L'esito del solo passaggio all'MSA: send() senza eccezione significa che il
 * server ha accettato il messaggio, non che sia stato consegnato (slide 23).
 */
public record EsitoInvio(
		boolean accettato,
		long millis,
		String messaggio,
		String eccezione,
		String causa) {

	public static EsitoInvio ok(long millis, String messaggio) {
		return new EsitoInvio(true, millis, messaggio, null, null);
	}

	public static EsitoInvio errore(long millis, Throwable ex) {
		// Il testo utile quasi mai sta nel messaggio esterno: sta nella causa
		// annidata (porta chiusa, host sbagliato, TLS non concordato) - slide 12.
		Throwable radice = ex;
		while (radice.getCause() != null) {
			radice = radice.getCause();
		}
		return new EsitoInvio(false, millis, "invio fallito",
				ex.getClass().getSimpleName() + ": " + ex.getMessage(),
				radice.getClass().getName() + ": " + radice.getMessage());
	}
}
