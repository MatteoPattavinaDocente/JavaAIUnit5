package it.epicode.demo.chiavi.dto;

public record GenerazioneRisposta(
		String testo,
		boolean completa,
		String modello,
		/** Chi ha servito la richiesta a valle di OpenRouter: va nel registro. */
		String fornitore,
		String motivoArresto,
		int tokenIngresso,
		int tokenUscita,
		long millisEsterni,
		int usatiOggi,
		int limiteGiornaliero) {
}
