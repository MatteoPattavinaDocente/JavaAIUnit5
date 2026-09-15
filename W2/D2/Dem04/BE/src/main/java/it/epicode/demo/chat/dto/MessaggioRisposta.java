package it.epicode.demo.chat.dto;

import it.epicode.demo.chat.model.Messaggio;
import java.time.Instant;

public record MessaggioRisposta(
		Long id,
		Long conversazione,
		String mittente,
		String destinatario,
		String testo,
		Instant istante,
		String stato,
		String idTemporaneo) {

	public static MessaggioRisposta da(Messaggio m) {
		return con(m, null);
	}

	/** Con l'id temporaneo, per il solo frame che torna a chi ha scritto. */
	public static MessaggioRisposta con(Messaggio m, String idTemporaneo) {
		return new MessaggioRisposta(m.getId(), m.getConversazione().getId(), m.getMittente(),
				m.getDestinatario(), m.getTesto(), m.getIstante(), m.getStato().name(), idTemporaneo);
	}
}
