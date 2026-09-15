package it.epicode.demo.chat.dto;

/** Il payload dell'indicatore di scrittura: non viene salvato (slide 41). */
public record Scrittura(String destinatario) {

	public Scrittura {
		if (destinatario == null || destinatario.isBlank()) {
			throw new IllegalArgumentException("destinatario: non puo' essere vuoto");
		}
	}
}
