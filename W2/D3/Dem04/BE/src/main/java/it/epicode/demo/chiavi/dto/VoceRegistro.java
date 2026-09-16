package it.epicode.demo.chiavi.dto;

import it.epicode.demo.chiavi.model.ChiamataLlm;
import java.time.Instant;

/** Una riga del registro, come la mostra la pagina. */
public record VoceRegistro(
		Long id,
		Instant istante,
		String utente,
		String modello,
		int tokenIngresso,
		int tokenUscita,
		int tokenTotali,
		String esito,
		long durataMs) {

	public static VoceRegistro da(ChiamataLlm c) {
		return new VoceRegistro(c.getId(), c.getIstante(), c.getUtente(), c.getModello(),
				c.getTokenIngresso(), c.getTokenUscita(), c.getTokenTotali(), c.getEsito(), c.getDurataMs());
	}
}
