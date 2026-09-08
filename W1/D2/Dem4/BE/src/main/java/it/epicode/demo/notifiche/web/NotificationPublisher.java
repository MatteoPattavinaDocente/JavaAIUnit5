package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.NotificationDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * Le stesse tre destinazioni della Dem 3. Confrontate i due file: e' il riassunto
 * di tutta la giornata.
 *
 * Nella Dem 3 questa classe si appoggiava a un handler nostro da 140 righe, che
 * teneva a mano elenchi di sessioni. Qui si appoggia a SimpMessagingTemplate,
 * un oggetto che Spring ci mette a disposizione: gli si dice "manda questo li'",
 * e chi tiene il conto di sessioni e iscrizioni e' il broker.
 *
 * Tre metodi, tre righe. Niente elenchi, niente cicli, niente lock.
 */
@Component
public class NotificationPublisher {

	private final SimpMessagingTemplate messaging;

	public NotificationPublisher(SimpMessagingTemplate messaging) {
		this.messaging = messaging;
	}

	/** A tutti quelli iscritti a /topic/notifications. Nessun filtro, nessun destinatario. */
	public void broadcast(NotificationDto dto) {
		messaging.convertAndSend("/topic/notifications", dto);
	}

	/**
	 * A chi si interessa di un ordine.
	 *
	 * La destinazione non va dichiarata da nessuna parte: sul broker semplice
	 * /topic/ordini/42 nasce nel momento in cui qualcuno ci si iscrive. Se non c'e'
	 * nessuno, il messaggio semplicemente non arriva a nessuno.
	 *
	 * Nella Dem 3 era un comando inventato da noi piu' un insieme di id salvato su
	 * ogni sessione. Qui il client fa una SUBSCRIBE e noi non ne sappiamo niente.
	 */
	public void perOrdine(Long ordineId, NotificationDto dto) {
		messaging.convertAndSend("/topic/ordini/" + ordineId, dto);
	}

	/**
	 * A un utente solo.
	 *
	 * Il primo parametro e' il getName() del Principal della sessione (il "login"
	 * letto dal CONNECT), non l'id sul database. Spring cerca le sessioni con quel
	 * nome e consegna a ognuna; se non ce n'e' nessuna, il messaggio viene scartato
	 * senza errori e la notifica resta solo sul database.
	 *
	 * Nota il percorso: qui scriviamo "/queue/notifications", ma il client si
	 * iscrive a "/user/queue/notifications". Il prefisso /user lo aggiunge Spring,
	 * ed e' la parte che rende quella coda privata.
	 */
	public void personale(String username, NotificationDto dto) {
		messaging.convertAndSendToUser(username, "/queue/notifications", dto);
	}
}
