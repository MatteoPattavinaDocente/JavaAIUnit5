package it.epicode.demo.canale.dto;

public record LoginRequest(String utente) {

	public LoginRequest {
		if (utente == null || utente.isBlank()) {
			throw new IllegalArgumentException("utente: non puo' essere vuoto");
		}
	}
}
