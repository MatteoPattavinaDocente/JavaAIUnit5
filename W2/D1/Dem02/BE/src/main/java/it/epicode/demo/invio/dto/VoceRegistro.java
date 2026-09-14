package it.epicode.demo.invio.dto;

import java.time.Instant;

/**
 * Il registro della slide 23: esito e momento dell'invio, altrimenti non
 * sappiamo che cosa e' partito. Qui sta in memoria; in produzione su tabella.
 */
public record VoceRegistro(
		Instant momento,
		String destinatario,
		String modo,
		String thread,
		boolean accettato,
		long millisInvio,
		String errore) {
}
