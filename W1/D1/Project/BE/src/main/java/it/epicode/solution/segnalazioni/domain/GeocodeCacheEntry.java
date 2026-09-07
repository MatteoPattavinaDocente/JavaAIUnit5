package it.epicode.solution.segnalazioni.domain;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Una geocodifica gia' pagata, tenuta in tabella: la "cache simulata".
 *
 * Non e' una cache di libreria: e' una riga di database che gestiamo a mano.
 * Il vantaggio rispetto a Caffeine e' che sopravvive al riavvio del processo -
 * dopo un deploy non ricominciamo a pagare Google da zero - e che la si puo'
 * ispezionare con una SELECT.
 *
 * Il prezzo e' che la scadenza tocca a noi: nessuno rimuove le righe vecchie,
 * quindi fetched_at va confrontato con il TTL a ogni lettura (isFresh) e la riga
 * scaduta viene riscritta in place da refresh(), non duplicata.
 *
 * Perche' 30 giorni e non "per sempre": un indirizzo cambia (numeri civici
 * rinumerati, vie rinominate, POI che si spostano). Oltre il mese la risposta
 * memorizzata smette di essere un risparmio e diventa un dato potenzialmente
 * sbagliato tenuto in vita a tempo indeterminato.
 */
@Entity
@Table(name = "geocoding_cache",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_geocoding_cache_kind_key",
				columnNames = { "kind", "cache_key" }))
public class GeocodeCacheEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "kind", nullable = false, length = 16)
	private GeocodeKind kind;

	/**
	 * Chiave normalizzata: l'indirizzo in minuscolo con gli spazi compattati per
	 * FORWARD, "lat,lng" a scala 6 per REVERSE. La normalizzazione sta in
	 * GeocodingService.cacheKey(...), qui arriva gia' fatta.
	 *
	 * 500 caratteri: la stessa lunghezza che accettiamo come indirizzo di ricerca,
	 * non i 255 della colonna address di reports (quella e' l'uscita, questa
	 * l'ingresso).
	 */
	@Column(name = "cache_key", nullable = false, length = 500)
	private String cacheKey;

	@Column(name = "latitude", nullable = false, precision = 8, scale = 6)
	private BigDecimal latitude;

	@Column(name = "longitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	/** L'indirizzo come lo ha normalizzato Google: puo' mancare. */
	@Column(name = "formatted_address", length = 500)
	private String formattedAddress;

	/**
	 * Quando la risposta e' stata chiesta a Google, non quando la riga e' stata
	 * creata: refresh() lo riporta a "adesso", ed e' l'unico dato che decide se la
	 * entry vale ancora.
	 */
	@Column(name = "fetched_at", nullable = false)
	private OffsetDateTime fetchedAt;

	protected GeocodeCacheEntry() {
		// richiesto da JPA
	}

	public GeocodeCacheEntry(GeocodeKind kind, String cacheKey, BigDecimal latitude, BigDecimal longitude,
			String formattedAddress) {
		this.kind = kind;
		this.cacheKey = cacheKey;
		this.latitude = latitude;
		this.longitude = longitude;
		this.formattedAddress = formattedAddress;
		this.fetchedAt = OffsetDateTime.now();
	}

	/**
	 * true finche' la entry e' dentro il TTL. L'istante viene letto a ogni
	 * chiamata: un "adesso" calcolato una volta e tenuto in un campo invecchierebbe
	 * insieme al processo.
	 */
	public boolean isFresh(Duration ttl) {
		return isFresh(ttl, OffsetDateTime.now());
	}

	/**
	 * Variante con l'istante di riferimento esplicito: serve ai test, che devono
	 * poter guardare la entry da 31 giorni nel futuro senza aspettarli.
	 *
	 * Il limite e' escluso (isAfter): a TTL esattamente scaduto la entry e' vecchia.
	 */
	public boolean isFresh(Duration ttl, OffsetDateTime now) {
		return fetchedAt.isAfter(now.minus(ttl));
	}

	/**
	 * Riscrive la entry con la risposta appena arrivata da Google.
	 *
	 * In place e non con una INSERT nuova: la coppia (kind, cache_key) e' unica, e
	 * tenere lo storico delle versioni di un indirizzo non serve a nessuno.
	 */
	public void refresh(BigDecimal latitude, BigDecimal longitude, String formattedAddress) {
		this.latitude = latitude;
		this.longitude = longitude;
		this.formattedAddress = formattedAddress;
		this.fetchedAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public GeocodeKind getKind() {
		return kind;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}

	public OffsetDateTime getFetchedAt() {
		return fetchedAt;
	}
}
