package it.epicode.demo.benvenuto.dto;

public record BenvenutoRequest(String destinatario, String nome, String lingua) {

	public BenvenutoRequest {
		if (destinatario == null || !destinatario.contains("@")) {
			throw new IllegalArgumentException("destinatario: serve un indirizzo con la chiocciola");
		}
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("nome: non puo' essere vuoto");
		}
		// La lingua dell'email dipende dall'utente, non dalla richiesta che ha
		// innescato l'invio: qui arriva dal profilo, non dall'header
		// Accept-Language del browser (slide 33).
		if (lingua == null || !(lingua.equals("it") || lingua.equals("en"))) {
			throw new IllegalArgumentException("lingua: it oppure en");
		}
	}
}
