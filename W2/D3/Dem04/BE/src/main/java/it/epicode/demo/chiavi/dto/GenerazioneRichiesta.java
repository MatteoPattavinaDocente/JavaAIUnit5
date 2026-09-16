package it.epicode.demo.chiavi.dto;

/**
 * L'utente arriva come campo perche' in questo corso Spring Security non c'e'
 * ancora: in un progetto vero verrebbe dal Principal della richiesta, e il
 * client non potrebbe dichiararlo. Da dire in aula.
 */
public record GenerazioneRichiesta(String utente, String testo) {

	public GenerazioneRichiesta {
		if (utente == null || utente.isBlank()) {
			throw new IllegalArgumentException("utente: non puo' essere vuoto");
		}
		if (testo == null || testo.isBlank()) {
			throw new IllegalArgumentException("testo: non puo' essere vuoto");
		}
	}
}
