package it.epicode.solution.segnalazioni.geocoding;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import it.epicode.solution.segnalazioni.domain.GeocodeCacheEntry;
import it.epicode.solution.segnalazioni.domain.GeocodeKind;
import it.epicode.solution.segnalazioni.dto.GeocodeResponse;
import it.epicode.solution.segnalazioni.repository.GeocodeCacheRepository;

/**
 * Cache simulata delle geocodifiche: una tabella di database al posto di una
 * cache di libreria.
 *
 * Regola unica: una entry vale per app.geocoding.cache.ttl (default 30 giorni).
 * Dentro il TTL la si riusa e Google non viene chiamato; oltre il TTL e' come se
 * non ci fosse, si richiama l'API e la riga viene riscritta con la risposta nuova.
 *
 * Perche' non Caffeine: qui la cache deve sopravvivere al riavvio. Con una cache
 * in memoria ogni deploy azzerava il risparmio e le prime richieste di ogni
 * mattina tornavano a costare. Ed essendo una tabella, "cosa c'e' in cache" e'
 * una SELECT invece di un mistero dentro il processo.
 *
 * Gli errori non finiscono mai in cache: store() viene chiamato solo su una
 * risposta valida, quindi un guasto di Google non resta appiccicato alla chiave.
 */
@Service
public class GeocodingCache {

	private static final Logger log = LoggerFactory.getLogger(GeocodingCache.class);

	private final GeocodeCacheRepository repository;
	private final Duration ttl;

	public GeocodingCache(GeocodeCacheRepository repository,
			@Value("${app.geocoding.cache.ttl:30d}") Duration ttl) {
		this.repository = repository;
		this.ttl = ttl;
	}

	public Duration ttl() {
		return ttl;
	}

	/**
	 * Lettura: Optional vuoto sia quando la chiave non c'e' sia quando la entry e'
	 * scaduta. Per chi chiama sono lo stesso caso - "tocca chiamare l'API" - e la
	 * differenza la gestisce store(), che aggiorna la riga vecchia invece di
	 * inserirne una seconda.
	 */
	@Transactional(readOnly = true)
	public Optional<GeocodeResponse> find(GeocodeKind kind, String cacheKey) {
		Optional<GeocodeCacheEntry> entry = repository.findByKindAndCacheKey(kind, cacheKey);

		if (entry.isEmpty()) {
			return Optional.empty();
		}

		GeocodeCacheEntry found = entry.get();
		if (!found.isFresh(ttl)) {
			log.info("Cache {} SCADUTA per [{}] (geocodificata il {}): richiamo le API",
					kind, cacheKey, found.getFetchedAt());
			return Optional.empty();
		}

		log.debug("Cache {} HIT per [{}] (geocodificata il {})", kind, cacheKey, found.getFetchedAt());
		return Optional.of(GeocodeResponse.fromCache(
				found.getLatitude(), found.getLongitude(), found.getFormattedAddress(), found.getFetchedAt()));
	}

	/**
	 * Scrittura: aggiorna la entry esistente o ne crea una nuova.
	 *
	 * REQUIRES_NEW perche' la cache non deve dipendere dall'esito di chi la
	 * popola: il reverse geocoding parte dentro la transazione di
	 * ReportService.create, e se quel salvataggio fallisce non ha senso buttare
	 * via una geocodifica gia' pagata a Google.
	 *
	 * Il catch sul vincolo unico copre la corsa fra due richieste identiche
	 * simultanee: entrambe leggono "assente", entrambe inseriscono, una perde. La
	 * riga dell'altra e' identica, quindi non c'e' niente da recuperare - la cache
	 * e' comunque popolata e la risposta all'utente non cambia.
	 */
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void store(GeocodeKind kind, String cacheKey, GeocodeResponse response) {
		try {
			repository.findByKindAndCacheKey(kind, cacheKey).ifPresentOrElse(
					entry -> entry.refresh(response.latitude(), response.longitude(), response.formattedAddress()),
					() -> repository.save(new GeocodeCacheEntry(kind, cacheKey,
							response.latitude(), response.longitude(), response.formattedAddress())));
			// flush esplicito: senza, il vincolo unico salterebbe al commit, fuori
			// dal try, e diventerebbe un 500 invece di una corsa persa e ignorata.
			repository.flush();
		}
		catch (DataIntegrityViolationException ex) {
			log.debug("Cache {} per [{}] scritta in parallelo da un'altra richiesta: la lascio com'e'",
					kind, cacheKey);
		}
	}

	/**
	 * Eviction: le entry ancora richieste vengono riscritte da store() e restano
	 * sempre sotto soglia, quindi qui cade solo cio' che nessuno ha piu' chiesto
	 * per il doppio del TTL (60 giorni con il default).
	 *
	 * Il doppio e non il TTL secco: una entry appena scaduta e' ancora utile come
	 * riga da aggiornare, cancellarla costringerebbe a una INSERT al posto di una
	 * UPDATE senza risparmiare nulla.
	 */
	@Scheduled(cron = "${app.geocoding.cache.purge-cron:0 30 3 * * *}")
	@Transactional
	public void purgeStale() {
		OffsetDateTime threshold = OffsetDateTime.now().minus(ttl.multipliedBy(2));
		long removed = repository.deleteByFetchedAtBefore(threshold);
		if (removed > 0) {
			log.info("Cache geocoding: rimosse {} entry non piu' richieste da prima del {}", removed, threshold);
		}
	}
}
