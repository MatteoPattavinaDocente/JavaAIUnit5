package it.epicode.demo.mappe.service;

import it.epicode.demo.mappe.dto.CreatePostRequest;
import it.epicode.demo.mappe.dto.PostResponse;
import it.epicode.demo.mappe.geocoding.GeocodingService;
import it.epicode.demo.mappe.model.GeoPoint;
import it.epicode.demo.mappe.model.Post;
import it.epicode.demo.mappe.repository.PostRepository;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

	private static final Logger log = LoggerFactory.getLogger(PostService.class);

	// Un post vive 30 giorni. Lo stesso valore decide due cose:
	// - quali post la mappa mostra (i piu' vecchi sono scaduti)
	// - quanto a lungo una geocodifica gia' fatta resta riusabile
	// Non e' una coincidenza: la tabella posts E' la cache della geocodifica, e la
	// scadenza del post e' la scadenza della cache. Nessuno store separato da tenere
	// sincronizzato, nessun processo di eviction: quando il post non e' piu' valido
	// non e' nemmeno piu' una fonte per le coordinate.
	private static final Duration DURATA_POST = Duration.ofDays(30);

	private final PostRepository repository;
	private final GeocodingService geocoding;

	public PostService(PostRepository repository, GeocodingService geocoding) {
		this.repository = repository;
		this.geocoding = geocoding;
	}

	@Transactional
	public PostResponse create(CreatePostRequest request) {
		Post post;

		if (request.haIndirizzo()) {
			post = creaDaIndirizzo(request);
		} else {
			// Coordinate dal click sulla mappa: nessun indirizzo, nessuna geocodifica.
			post = new Post(request.title(), new GeoPoint(request.latitude(), request.longitude()), null, null);
		}

		return PostResponse.from(repository.save(post));
	}

	// Nessuna chiamata a Google: legge solo quello che e' gia' in tabella,
	// scartando i post scaduti.
	@Transactional(readOnly = true)
	public List<PostResponse> findInBounds(BigDecimal south, BigDecimal north, BigDecimal west, BigDecimal east) {
		return repository
				.findByLocationLatitudeBetweenAndLocationLongitudeBetweenAndCreatedAtAfter(
						south, north, west, east, scadenza())
				.stream()
				.map(PostResponse::from)
				.toList();
	}

	// Il punto della faccenda: prima di spendere una chiamata Google, guardiamo se
	// quell'indirizzo lo abbiamo gia' risolto in un post ancora valido.
	private Post creaDaIndirizzo(CreatePostRequest request) {
		String chiave = GeocodingService.normalizeAddress(request.address());

		var esistente = repository.findFirstByAddressKeyAndCreatedAtAfterOrderByCreatedAtDesc(chiave, scadenza());

		if (esistente.isPresent()) {
			Post fonte = esistente.get();
			log.debug("Geocodifica riusata dal post {} per '{}': nessuna chiamata a Google",
					fonte.getId(), request.address());

			// Copiamo il GeoPoint invece di passare quello del post esistente.
			// GeoPoint e' un @Embeddable con campi mutabili: condividere la stessa
			// istanza fra due entity gestite significa che Hibernate ha due righe che
			// puntano allo stesso oggetto in memoria. Oggi funzionerebbe, ma il giorno
			// che qualcuno aggiunge un setter la modifica di un post ne cambierebbe
			// due. Copiare costa due getter.
			GeoPoint copia = new GeoPoint(fonte.getLocation().getLatitude(), fonte.getLocation().getLongitude());
			return new Post(request.title(), copia, fonte.getFormattedAddress(), chiave);
		}

		// Nessun post valido con questo indirizzo: tocca chiamare Google.
		// Se l'indirizzo non esiste o l'API e' giu', geocode solleva e la @Transactional
		// fa rollback: non salviamo un post con coordinate inventate.
		GeocodingService.GeocodeResult risultato = geocoding.geocode(request.address());
		return new Post(request.title(), risultato.location(), risultato.formattedAddress(), chiave);
	}

	// Istante prima del quale un post e' scaduto. Calcolato a ogni chiamata, non
	// tenuto in un campo: un valore congelato all'avvio invecchierebbe con il processo.
	private Instant scadenza() {
		return Instant.now().minus(DURATA_POST);
	}
}
