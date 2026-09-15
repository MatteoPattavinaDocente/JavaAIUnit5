package it.epicode.demo.chat.dto;

public record SegnaLetti(Long conversazione) {

	public SegnaLetti {
		if (conversazione == null) {
			throw new IllegalArgumentException("conversazione: obbligatoria");
		}
	}
}
