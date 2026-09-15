package it.epicode.demo.canale.dto;

/**
 * Il payload in entrata contiene destinatario e testo. Il mittente NON c'e':
 * lo decide il server leggendo il Principal della sessione (slide 11 e 16).
 * Il campo "da" dichiarato dal client non va creduto.
 */
public record MessaggioPrivato(String destinatario, String testo) {

	public MessaggioPrivato {
		if (destinatario == null || destinatario.isBlank()) {
			throw new IllegalArgumentException("destinatario: non puo' essere vuoto");
		}
		if (testo == null || testo.isBlank()) {
			throw new IllegalArgumentException("testo: non puo' essere vuoto");
		}
	}
}
