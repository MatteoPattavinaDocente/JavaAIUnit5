package it.epicode.demo.smtp.dto;

// Le annotazioni di validazione arrivano piu' avanti nel corso: i controlli
// minimi li scrive il compact constructor.
public record InvioRequest(String destinatario, String oggetto, String corpo) {

	public InvioRequest {
		if (destinatario == null || !destinatario.contains("@")) {
			throw new IllegalArgumentException("destinatario: serve un indirizzo con la chiocciola");
		}
		if (oggetto == null || oggetto.isBlank()) {
			throw new IllegalArgumentException("oggetto: non puo' essere vuoto");
		}
		if (corpo == null || corpo.isBlank()) {
			throw new IllegalArgumentException("corpo: non puo' essere vuoto");
		}
	}
}
