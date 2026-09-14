package it.epicode.demo.verifica.dto;

import java.time.Instant;
import java.util.List;

/** Solo per la pagina della demo: in produzione questo elenco non esiste. */
public record UtenteView(
		Long id,
		String email,
		boolean verificato,
		Instant creatoIl,
		List<TokenView> token) {
}
