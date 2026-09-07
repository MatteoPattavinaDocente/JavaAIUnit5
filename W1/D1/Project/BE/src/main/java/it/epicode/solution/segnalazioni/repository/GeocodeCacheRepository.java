package it.epicode.solution.segnalazioni.repository;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.epicode.solution.segnalazioni.domain.GeocodeCacheEntry;
import it.epicode.solution.segnalazioni.domain.GeocodeKind;

public interface GeocodeCacheRepository extends JpaRepository<GeocodeCacheEntry, Long> {

	/**
	 * La lettura della cache: (kind, cache_key) e' la chiave unica, quindi al
	 * massimo una riga. La freschezza NON e' filtrata qui di proposito - la entry
	 * scaduta serve comunque, perche' refresh() la riscrive invece di inserirne
	 * un'altra.
	 */
	Optional<GeocodeCacheEntry> findByKindAndCacheKey(GeocodeKind kind, String cacheKey);

	/**
	 * Pulizia delle entry che nessuno ha piu' chiesto: quelle ancora richieste
	 * vengono riscritte da refresh() e non superano mai la soglia.
	 */
	long deleteByFetchedAtBefore(OffsetDateTime threshold);
}
