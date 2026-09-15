package it.epicode.demo.client.dto;

import it.epicode.demo.client.model.Messaggio;
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
}
