package it.epicode.demo.benvenuto.dto;

public record EsitoInvio(
		boolean accettato,
		long millis,
		String messaggio,
		String causa) {
}
