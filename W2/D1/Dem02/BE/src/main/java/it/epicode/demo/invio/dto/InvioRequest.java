package it.epicode.demo.invio.dto;

public record InvioRequest(
		String destinatario,
		String nome,
		Modo modo,
		boolean asincrono,
		int ritardoMs) {

	public InvioRequest {
		if (destinatario == null || !destinatario.contains("@")) {
			throw new IllegalArgumentException("destinatario: serve un indirizzo con la chiocciola");
		}
		if (nome == null || nome.isBlank()) {
			throw new IllegalArgumentException("nome: non puo' essere vuoto");
		}
		if (modo == null) {
			throw new IllegalArgumentException("modo: uno fra TESTO, HTML, ALLEGATO, INLINE");
		}
		if (ritardoMs < 0 || ritardoMs > 30_000) {
			throw new IllegalArgumentException("ritardoMs: fra 0 e 30000");
		}
	}
}
