package it.epicode.demo.notifiche.service;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.event.NotificationCreated;
import it.epicode.demo.notifiche.model.Notification;
import it.epicode.demo.notifiche.model.NotificationType;
import it.epicode.demo.notifiche.repository.NotificationRepository;
import java.time.Instant;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Lo stesso service della Dem 3, parola per parola, con un metodo in piu' usato solo
 * per il confronto in aula (notifySenzaPush).
 *
 * Notate cosa NON c'e' in questa classe: nessun import di STOMP, nessun broker,
 * nessuna destinazione. Il service si occupa solo del dominio: salva e annuncia.
 * Il collegamento col canale sta tutto in NotificationListener, che ascolta l'annuncio.
 *
 * E' il motivo per cui esistono gli eventi di Spring: chi produce un fatto non deve
 * conoscere chi lo consuma. Domani possiamo aggiungere una mail, una notifica push
 * o un log su file senza toccare una riga di qui.
 */
@Service
public class NotificationService {

	private final NotificationRepository repository;

	// ApplicationEventPublisher e' l'oggetto con cui si annunciano gli eventi dentro
	// Spring. Ce lo fornisce lui, non va creato.
	private final ApplicationEventPublisher events;

	public NotificationService(NotificationRepository repository, ApplicationEventPublisher events) {
		this.repository = repository;
		this.events = events;
	}

	/**
	 * Salva la notifica e annuncia che e' nata.
	 *
	 * L'annuncio parte da qui, dentro la transazione, ma chi lo ascolta e' segnato
	 * come AFTER_COMMIT: quindi il messaggio uscira' solo se il salvataggio va
	 * davvero a buon fine. Se dopo questa riga qualcosa fallisse, il database
	 * tornerebbe indietro e nessuno spedirebbe niente.
	 */
	@Transactional
	public Notification notify(String recipient, NotificationType type, Long resourceId, String title) {
		Notification salvata = repository.save(new Notification(recipient, type, resourceId, title));
		events.publishEvent(new NotificationCreated(salvata.getId()));
		return salvata;
	}

	/**
	 * La stessa scrittura di notify(), ma SENZA l'evento.
	 *
	 * La riga finisce in tabella e nessuno la spedisce a nessuno: e' la notifica
	 * "normale", quella che il destinatario scopre solo quando ricarica la pagina.
	 *
	 * Esiste solo per avere il termine di paragone a lezione. Senza questo metodo
	 * ogni notifica del progetto arriverebbe spinta dal server, e la differenza fra
	 * "chiedere" e "ricevere" non si vedrebbe. Lo usa MessaggiController.senzaPush().
	 */
	@Transactional
	public Notification notifySenzaPush(String recipient, NotificationType type, Long resourceId, String title) {
		return repository.save(new Notification(recipient, type, resourceId, title));
	}

	/** Una pagina di notifiche di un utente, gia' tradotte in DTO. */
	@Transactional(readOnly = true)
	public Page<NotificationDto> list(String recipient, Pageable pageable) {
		return repository.findByRecipient(recipient, pageable).map(NotificationDto::from);
	}

	/** Una singola notifica per id. Serve a DemoPublisherController per ripubblicarla. */
	@Transactional(readOnly = true)
	public NotificationDto find(Long id) {
		return repository.findById(id)
				.map(NotificationDto::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notifica " + id + " inesistente"));
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
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "notifica " + id + " inesistente"));
		notification.markRead(Instant.now());
	}

	/** Segna lette tutte le notifiche dell'utente. Restituisce quante ne ha toccate. */
	@Transactional
	public int markAllRead(String recipient) {
		return repository.markAllRead(recipient, Instant.now());
	}
}
