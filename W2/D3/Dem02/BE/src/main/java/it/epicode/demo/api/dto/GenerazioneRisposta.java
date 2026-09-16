package it.epicode.demo.api.dto;

import java.util.List;

/**
 * Il nostro formato. Include quello che serve a far vedere in aula com'e' fatta
 * la risposta del servizio: le scelte ricevute, il motivo di arresto, il
 * consumo.
 */
public record GenerazioneRisposta(
		String testo,
		String testoIngenuo,
		boolean utilizzabile,
		String avviso,
		String modello,
		/** Chi ha servito la richiesta a valle di OpenRouter. */
		String fornitore,
		String motivoArresto,
		String motivoArrestoNativo,
		List<SceltaVista> scelte,
		int tokenIngresso,
		int tokenUscita,
		int maxTokensRichiesti,
		long millisEsterni) {

	/** Solo per la lezione: cosa e' arrivato in ogni voce di choices. */
	public record SceltaVista(
			int indice,
			String motivoArresto,
			String motivoArrestoNativo,
			int caratteri,
			boolean conRifiuto,
			boolean conRagionamento) {
	}
}
