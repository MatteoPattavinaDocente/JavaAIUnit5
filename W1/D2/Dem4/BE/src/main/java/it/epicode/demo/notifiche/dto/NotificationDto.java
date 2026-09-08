package it.epicode.demo.notifiche.dto;

import it.epicode.demo.notifiche.model.Notification;
import it.epicode.demo.notifiche.model.NotificationType;
import java.time.Instant;

/**
 * La notifica come la vedono il frontend e il broker.
 *
 * Rispetto alla Dem 3 manca il metodo diCanale(): qui non serve piu'.
 * I messaggi che nascono sul canale e non sul database adesso hanno un tipo tutto
 * loro, MessaggioPubblico, invece di essere finte notifiche con l'id a null.
 * Avere destinazioni distinte ci ha permesso di avere anche tipi distinti.
 */
public record NotificationDto(
		Long id,
		String recipient,
		NotificationType type,
		Long resourceId,
		String title,
		Instant createdAt,
		boolean read) {

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
}
