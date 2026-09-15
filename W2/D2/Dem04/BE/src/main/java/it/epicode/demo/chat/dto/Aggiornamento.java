package it.epicode.demo.chat.dto;

import java.util.List;

/**
 * Quello che viaggia sulla destinazione utente oltre ai messaggi: cambi di
 * stato e indicatore di scrittura.
 *
 * Un solo record con un campo `tipo` tiene il client semplice: una
 * sottoscrizione, uno switch.
 */
public record Aggiornamento(String tipo, Long conversazione, String utente, List<Long> messaggi) {

	public static Aggiornamento letti(Long conversazione, String daChi, List<Long> messaggi) {
		return new Aggiornamento("LETTI", conversazione, daChi, messaggi);
	}

	public static Aggiornamento scrive(Long conversazione, String chi) {
		return new Aggiornamento("SCRIVE", conversazione, chi, List.of());
	}
}
