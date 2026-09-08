package it.epicode.demo.notifiche.dto;

import it.epicode.demo.notifiche.model.Notification;
import it.epicode.demo.notifiche.model.NotificationType;
import java.time.Instant;

/**
 * La notifica come la vede il frontend.
 *
 * Rispetto alla Dem 1 c'e' un campo in piu', recipient, perche' adesso la notifica
 * viaggia anche sul canale WebSocket: chi la riceve deve poter capire di chi e'.
 */
public record NotificationDto(
		Long id,
		String recipient,
		NotificationType type,
		Long resourceId,
		String title,
		Instant createdAt,
		boolean read) {

	/** Traduce una notifica salvata sul database in DTO. */
	public static NotificationDto from(Notification n) {
		return new NotificationDto(
				n.getId(),
				n.getRecipient(),
				n.getType(),
				n.getResourceId(),
				n.getTitle(),
				n.getCreatedAt(),
				n.getReadAt() != null);
	}

	/**
	 * Un messaggio nato SUL CANALE, non sul database.
	 *
	 * Quando un utente scrive nella chat di un ordine, quel messaggio non viene salvato
	 * da nessuna parte: nasce, attraversa il canale, e finisce li'. Per questo l'id e' null.
	 *
	 * Conseguenza da mostrare a lezione: chi non e' collegato in quel momento lo perde
	 * per sempre, e ricaricare la pagina non lo fa ricomparire. Solo le notifiche
	 * personali passano dal database e quindi hanno uno storico.
	 */
	public static NotificationDto diCanale(String mittente, Long ordineId, String testo) {
		return new NotificationDto(
				null,
				mittente == null ? "anonimo" : mittente,
				NotificationType.MESSAGE,
				ordineId,
				testo,
				Instant.now(),
				false);
	}
}
