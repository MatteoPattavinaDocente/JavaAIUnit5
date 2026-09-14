package it.epicode.demo.verifica.dto;

public record RegistrazioneRequest(String email, String password) {

	public RegistrazioneRequest {
		if (email == null || !email.contains("@")) {
			throw new IllegalArgumentException("email: serve un indirizzo con la chiocciola");
		}
		if (password == null || password.length() < 8) {
			throw new IllegalArgumentException("password: almeno 8 caratteri");
		}
	}
}
