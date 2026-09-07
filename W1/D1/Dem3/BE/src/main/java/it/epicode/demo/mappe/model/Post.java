package it.epicode.demo.mappe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

// Gli indici sono dichiarati qui: con ddl-auto=update li crea Hibernate.
// idx_posts_address_key serve al riuso della geocodifica: la ricerca per indirizzo
// normalizzato deve costare un index scan, non un sequential scan della tabella.
@Entity
@Table(name = "posts", indexes = {
		@Index(name = "idx_posts_lat_lng", columnList = "latitude, longitude"),
		@Index(name = "idx_posts_address_key", columnList = "address_key, created_at")
})
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	// @Embedded: le due colonne di GeoPoint finiscono nella tabella posts,
	// non in una tabella separata.
	@Embedded
	private GeoPoint location;

	// L'indirizzo cosi' come Google lo ha normalizzato. Null quando il punto e' stato
	// scelto cliccando sulla mappa: in quel caso non c'e' nessun indirizzo da salvare.
	@Column(name = "formatted_address", length = 500)
	private String formattedAddress;

	// L'indirizzo come lo ha scritto l'utente, normalizzato (trim + minuscolo).
	// E' la chiave con cui cerchiamo se abbiamo gia' geocodificato quell'indirizzo.
	//
	// Perche' una colonna separata e non riusare formatted_address? Perche' le due
	// stringhe non coincidono: l'utente scrive "via prione 1", Google risponde
	// "Via del Prione, 1, 19121 La Spezia SP, Italia". Cercare l'input dell'utente
	// dentro l'output di Google non matcherebbe mai. Serve la chiave grezza.
	//
	// Null quando il post nasce da un click sulla mappa: nessun indirizzo, nessuna
	// geocodifica, niente da riusare.
	@Column(name = "address_key", length = 500)
	private String addressKey;

	protected Post() {
	}

	public Post(String title, GeoPoint location, String formattedAddress, String addressKey) {
		this.title = title;
		this.location = location;
		this.formattedAddress = formattedAddress;
		this.addressKey = addressKey;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}

	public String getAddressKey() {
		return addressKey;
	}
}
