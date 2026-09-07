package it.epicode.demo.mappe.repository;

import it.epicode.demo.mappe.model.Post;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

	// Nome derivato: "Location" e' il campo @Embedded, "Latitude" il campo dentro
	// GeoPoint. Il BETWEEN scarta da solo i post senza coordinate, perche' un
	// confronto con NULL non e' mai vero. Ed e' la query che usa idx_posts_lat_lng.
	//
	// Il CreatedAtAfter esclude i post scaduti: durano 30 giorni, oltre non si vedono.
	List<Post> findByLocationLatitudeBetweenAndLocationLongitudeBetweenAndCreatedAtAfter(
			BigDecimal latMin, BigDecimal latMax, BigDecimal lngMin, BigDecimal lngMax, Instant scadenza);

	// Il riuso della geocodifica: cerca il post piu' recente ancora valido che abbia
	// lo stesso indirizzo normalizzato. Se c'e', le sue coordinate sono buone e non
	// serve chiamare Google.
	//
	// findFirst + OrderBy CreatedAtDesc: di post con lo stesso indirizzo ce ne possono
	// essere molti (nessun unique constraint, e non lo vogliamo: due persone possono
	// scrivere due post diversi sullo stesso posto). Prendiamo il piu' fresco.
	//
	// CreatedAtAfter: un post scaduto non e' una fonte affidabile. Passati 30 giorni
	// ri-geocodifichiamo, cosi' un indirizzo che nel frattempo e' cambiato viene
	// risolto di nuovo invece di restare sbagliato per sempre.
	//
	// addressKey non e' mai null quando chiamiamo questo metodo (lo passiamo solo
	// nel ramo con indirizzo), quindi il confronto = non incrocia le righe NULL
	// dei post nati da click sulla mappa.
	Optional<Post> findFirstByAddressKeyAndCreatedAtAfterOrderByCreatedAtDesc(
			String addressKey, Instant scadenza);
}
