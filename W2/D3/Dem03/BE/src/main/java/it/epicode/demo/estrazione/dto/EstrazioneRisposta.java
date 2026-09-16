package it.epicode.demo.estrazione.dto;

/**
 * Il nostro formato. Include il JSON grezzo perche' in aula serve vederlo:
 * prima di correggere il prompt conviene salvare nel log la risposta grezza,
 * spesso l'errore e' gia' visibile (slide 34).
 */
public record EstrazioneRisposta(
		Contatto contatto,
		boolean riuscita,
		String avviso,
		String jsonGrezzo,
		String motivoArresto,
		boolean schemaUsato,
		int tokenIngresso,
		int tokenUscita,
		long millisEsterni) {
}
