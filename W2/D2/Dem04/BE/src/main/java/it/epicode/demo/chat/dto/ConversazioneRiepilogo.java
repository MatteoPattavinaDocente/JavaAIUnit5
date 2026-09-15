package it.epicode.demo.chat.dto;

import java.time.Instant;

/** Una riga dell'elenco delle conversazioni (slide 42). */
public record ConversazioneRiepilogo(
		Long id,
		String controparte,
		long nonLetti,
		String ultimoTesto,
		Instant ultimoIstante,
		boolean controparteCollegata) {
}
