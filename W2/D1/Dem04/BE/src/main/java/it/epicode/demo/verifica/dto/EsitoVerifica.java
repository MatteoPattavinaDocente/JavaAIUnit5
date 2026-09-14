package it.epicode.demo.verifica.dto;

/**
 * Le quattro strade possibili quando qualcuno apre il collegamento. Sono
 * distinte perche' il messaggio all'utente cambia, non perche' cambi la
 * sicurezza: un token sconosciuto e uno gia' usato finiscono comunque in un
 * rifiuto.
 */
public record EsitoVerifica(String stato, String messaggio, String email) {

	public static EsitoVerifica verificato(String email) {
		return new EsitoVerifica("VERIFICATO", "indirizzo confermato: ora puoi accedere", email);
	}

	public static EsitoVerifica giaUsato() {
		return new EsitoVerifica("GIA_USATO", "questo collegamento e' gia' stato usato", null);
	}

	public static EsitoVerifica scaduto() {
		return new EsitoVerifica("SCADUTO", "il collegamento e' scaduto: chiedine uno nuovo", null);
	}

	public static EsitoVerifica nonTrovato() {
		return new EsitoVerifica("NON_TROVATO", "collegamento non valido", null);
	}
}
