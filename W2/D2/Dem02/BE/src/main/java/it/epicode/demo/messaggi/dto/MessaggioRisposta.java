package it.epicode.demo.messaggi.dto;

import it.epicode.demo.messaggi.model.Messaggio;
import java.time.Instant;

/**
 * Quello che viaggia verso i client: contiene l'id generato dal database e
 * l'istante assegnato dal server, che il payload in entrata non aveva.
 */
public record MessaggioRisposta(
		Long id,
		String mittente,
		String destinatario,
		String testo,
		Instant istante,
		String stato,
		String idTemporaneo) {

	public static MessaggioRisposta da(Messaggio m) {
		return new MessaggioRisposta(m.getId(), m.getMittente(), m.getDestinatario(),
				m.getTesto(), m.getIstante(), m.getStato().name(), null);
	}

	/**
	 * Solo per il modo difettoso: un messaggio mai salvato, quindi senza id.
	 * Serve a far vedere che cosa succede quando si consegna prima di salvare.
	 */
	public static MessaggioRisposta finto(String mittente, String destinatario, String testo) {
		return new MessaggioRisposta(null, mittente, destinatario, testo,
				Instant.now(), "MAI SALVATO", null);
	}
}
