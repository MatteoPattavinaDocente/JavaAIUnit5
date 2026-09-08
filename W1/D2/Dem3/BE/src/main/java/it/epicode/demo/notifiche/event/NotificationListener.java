package it.epicode.demo.notifiche.event;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.repository.NotificationRepository;
import it.epicode.demo.notifiche.web.NotificationPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Il pezzo che collega il dominio al canale.
 *
 * Il service salva la notifica e annuncia "e' nata la numero 7".
 * Questa classe sente l'annuncio, rilegge la notifica e la spedisce al suo destinatario.
 *
 * Cosi' il service resta pulito: non sa niente di WebSocket, di sessioni, di buste.
 * Se domani volessimo mandare anche una mail, basterebbe un altro listener come questo,
 * senza toccare una riga del service.
 */
@Component
public class NotificationListener {

	private final NotificationRepository repository;
	private final NotificationPublisher publisher;

	public NotificationListener(NotificationRepository repository, NotificationPublisher publisher) {
		this.repository = repository;
		this.publisher = publisher;
	}

	/**
	 * IL DETTAGLIO PIU' IMPORTANTE DI QUESTA CLASSE: phase = AFTER_COMMIT.
	 *
	 * Vuol dire "eseguimi solo DOPO che la transazione e' andata a buon fine".
	 *
	 * Con un normale @EventListener questo metodo partirebbe subito, mentre la
	 * transazione e' ancora aperta. Se poco dopo qualcosa fallisse, il database
	 * tornerebbe indietro (rollback) ma il messaggio sarebbe gia' partito:
	 * l'utente vedrebbe comparire una notifica che sul database non esiste.
	 *
	 * Regola generale: quello che esce dall'applicazione (messaggi, mail, chiamate
	 * ad altri servizi) va fatto partire dopo il commit, non prima.
	 */
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onNotificationCreated(NotificationCreated event) {
		repository.findById(event.notificationId())
				.map(NotificationDto::from)
				// ifPresent: se nel frattempo la riga fosse sparita, semplicemente non spediamo.
				.ifPresent(dto -> publisher.personale(dto.recipient(), dto));
	}
}
