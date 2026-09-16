package it.epicode.demo.estrazione.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Il record rispecchia lo schema che abbiamo dichiarato (slide 32).
 *
 * I nomi devono corrispondere ESATTAMENTE a quelli dello schema: un campo
 * scritto diversamente resta vuoto senza segnalare niente (slide 34). E' il
 * difetto piu' silenzioso della giornata.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Contatto(
		String nome,
		String email,
		String telefono,
		String azienda,
		String citta) {
}
