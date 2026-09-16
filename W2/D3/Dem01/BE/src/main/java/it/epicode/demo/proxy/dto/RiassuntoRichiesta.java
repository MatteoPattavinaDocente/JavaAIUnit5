package it.epicode.demo.proxy.dto;

/** Quello che il frontend manda: solo il testo. Tutto il resto lo decide il server. */
public record RiassuntoRichiesta(String testo) {

	public RiassuntoRichiesta {
		if (testo == null || testo.isBlank()) {
			throw new IllegalArgumentException("testo: non puo' essere vuoto");
		}
		if (testo.length() > 20_000) {
			throw new IllegalArgumentException("testo: al massimo 20000 caratteri");
		}
	}
}
