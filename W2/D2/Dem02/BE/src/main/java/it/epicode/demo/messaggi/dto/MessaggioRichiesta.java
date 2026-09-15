package it.epicode.demo.messaggi.dto;

/**
 * Un record separato dall'entita' tiene FUORI i campi decisi dal server
 * (slide 16). Solo due campi:
 *
 *   - il mittente arriva dal Principal
 *   - l'istante lo assegna il server al salvataggio
 *   - l'id lo genera il database
 */
public record MessaggioRichiesta(String destinatario, String testo) {

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
