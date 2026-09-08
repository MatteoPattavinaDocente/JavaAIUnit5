package it.epicode.demo.notifiche.service;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.model.Notification;
import it.epicode.demo.notifiche.model.NotificationType;
import it.epicode.demo.notifiche.repository.NotificationRepository;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Il service e' il livello di mezzo fra il controller (che parla HTTP)
 * e il repository (che parla col database). Qui sta la logica del dominio:
 * cosa vuol dire creare una notifica, leggerla, contarne le non lette.
 *
 * Il controller non tocca mai il repository direttamente: passa sempre da qui.
 * Cosi' se domani la regola cambia, si cambia in un posto solo.
 *
 * @Transactional avvolge il metodo in una transazione: o va tutto a buon fine,
 * o il database torna com'era prima. Se il metodo lancia un'eccezione, viene annullato tutto.
 */
@Service
public class NotificationService {

	private final NotificationRepository repository;

	// Spring vede questo costruttore e passa lui il repository quando crea il service.
	// Si chiama dependency injection: la classe dichiara di cosa ha bisogno,
	// non se lo va a cercare da sola.
	public NotificationService(NotificationRepository repository) {
		this.repository = repository;
	}

	/**
	 * L'unico punto di tutta l'applicazione in cui nasce una notifica.
	 *
	 * Restituisce l'oggetto salvato (non void) perche' serve a chi l'ha chiesto,
	 * e nelle demo successive servira' a chi dovra' spedirlo sul WebSocket.
	 */
	@Transactional
	public Notification notify(String recipient, NotificationType type, Long resourceId, String title) {
		return repository.save(new Notification(recipient, type, resourceId, title));
	}

	/** Una pagina di notifiche di un utente, gia' tradotte in DTO per il frontend. */
	@Transactional(readOnly = true) // readOnly: stiamo solo leggendo, il database puo' ottimizzare
	public Page<NotificationDto> list(String recipient, Pageable pageable) {
		return repository.findByRecipient(recipient, pageable).map(NotificationDto::from);
	}

	/** Il numero che finisce nel pallino rosso della campanella. */
	@Transactional(readOnly = true)
	public long unreadCount(String recipient) {
		return repository.countByRecipientAndReadAtIsNull(recipient);
	}

	/**
	 * Segna letta una singola notifica.
	 *
	 * Attenzione a cosa NON c'e': non chiamiamo repository.save().
	 * Dentro una transazione Hibernate tiene d'occhio gli oggetti che ha caricato lui;
	 * quando vede che readAt e' cambiato, scrive da solo l'UPDATE alla fine del metodo.
	 */
	@Transactional
	public void markRead(Long id) {
		Notification notification = repository.findById(id)
				// findById restituisce un Optional: la notifica potrebbe non esistere.
				// In quel caso rispondiamo 404 invece di lasciar esplodere un NullPointerException.
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notifica " + id + " inesistente"));
		notification.markRead(Instant.now());
	}

	/** Segna lette tutte le notifiche dell'utente. Restituisce quante ne ha toccate. */
	@Transactional
	public int markAllRead(String recipient) {
		return repository.markAllRead(recipient, Instant.now());
	}
}
