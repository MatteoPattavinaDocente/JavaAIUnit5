package it.epicode.demo.client.dto;

public record LoginRequest(String utente) {

	public LoginRequest {
		if (utente == null || utente.isBlank()) {
			throw new IllegalArgumentException("utente: non puo' essere vuoto");
		}
	}
}
