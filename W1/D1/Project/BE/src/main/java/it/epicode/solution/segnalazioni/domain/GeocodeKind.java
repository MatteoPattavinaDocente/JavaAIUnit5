package it.epicode.solution.segnalazioni.domain;

/**
 * Direzione della geocodifica memorizzata in cache.
 *
 * Fa parte della chiave insieme a cache_key: "41.9028,12.4964" e' una chiave
 * valida in entrambe le direzioni (un utente puo' cercare quella stringa come
 * indirizzo), e senza il kind le due entry si sovrascriverebbero a vicenda.
 */
public enum GeocodeKind {

	/** Indirizzo -> coordinate. */
	FORWARD,

	/** Coordinate -> indirizzo. */
	REVERSE
}
