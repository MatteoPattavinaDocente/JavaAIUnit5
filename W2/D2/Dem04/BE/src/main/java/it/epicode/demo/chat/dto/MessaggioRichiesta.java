package it.epicode.demo.chat.dto;

/**
 * Solo i campi che il client decide. L'idTemporaneo e' l'eccezione utile:
 * non finisce nel database, torna indietro nel frame di risposta perche' il
 * client possa sostituire la riga mostrata in anticipo (slide 21).
 */
public record MessaggioRichiesta(String destinatario, String testo, String idTemporaneo) {

	public MessaggioRichiesta {
		if (destinatario == null || destinatario.isBlank()) {
			throw new IllegalArgumentException("destinatario: non puo' essere vuoto");
		}
		if (testo == null || testo.isBlank()) {
			throw new IllegalArgumentException("testo: non puo' essere vuoto");
		}
		if (testo.length() > 2000) {
			throw new IllegalArgumentException("testo: al massimo 2000 caratteri");
		}
	}
}
