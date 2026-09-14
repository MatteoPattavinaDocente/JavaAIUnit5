package it.epicode.demo.verifica.dto;

import java.time.Instant;

/**
 * Solo per la pagina della demo. In produzione il valore del token non si
 * mostra da nessuna parte se non nell'email: chi lo vede puo' attivare
 * l'account di un altro.
 */
public record TokenView(
		Long id,
		String valore,
		Instant scadenza,
		Instant usatoIl,
		boolean valido) {
}
