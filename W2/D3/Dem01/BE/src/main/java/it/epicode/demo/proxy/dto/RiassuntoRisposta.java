package it.epicode.demo.proxy.dto;

/**
 * Il NOSTRO contratto verso il frontend, che non somiglia a quello del servizio
 * esterno (slide 6). Se lo ricopiassimo, ogni loro modifica diventerebbe una
 * modifica anche del nostro client.
 *
 * Da notare cosa NON c'e': la chiave, il modello per intero, gli identificativi
 * interni del servizio, il corpo grezzo della loro risposta.
 */
public record RiassuntoRisposta(
		String riassunto,
		String modello,
		int tokenIngresso,
		int tokenUscita,
		String motivoArresto,
		long millisEsterni) {
}
