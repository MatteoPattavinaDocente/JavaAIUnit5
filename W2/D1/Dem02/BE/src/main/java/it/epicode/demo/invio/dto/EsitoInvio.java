package it.epicode.demo.invio.dto;

/**
 * millisRichiesta e' il tempo che l'utente aspetta davanti allo schermo;
 * millisInvio e' il tempo speso a parlare con il server di posta. Con @Async
 * i due numeri si separano: e' tutto il senso della slide 22.
 */
public record EsitoInvio(
		String modo,
		boolean asincrono,
		boolean accettato,
		long millisRichiesta,
		long millisInvio,
		String messaggio,
		String eccezione,
		String causa) {
}
