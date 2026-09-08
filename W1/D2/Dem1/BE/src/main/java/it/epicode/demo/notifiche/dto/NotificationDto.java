package it.epicode.demo.notifiche.dto;

import it.epicode.demo.notifiche.model.Notification;
import it.epicode.demo.notifiche.model.NotificationType;
import java.time.Instant;

/**
 * La notifica come la vede il frontend.
 *
 * Perche' non mandiamo direttamente l'entity Notification? Perche' l'entity e' legata
 * al database (e a Hibernate) e contiene cose che al browser non servono.
 * Il DTO e' una copia "da esporre": scegliamo noi quali campi escono e in che forma.
 *
 * E' un record: Java genera da solo costruttore, getter, equals e toString.
 * Un record e' immutabile, quindi una volta creato nessuno puo' modificarlo per sbaglio.
 */
public record NotificationDto(
		Long id,
		NotificationType type,
		Long resourceId,
		String title,
		Instant createdAt,
		boolean read) {

	// Traduce l'entity in DTO. Nota il campo readAt: sul database e' una data,
	// ma al frontend serve solo sapere si'/no, quindi qui diventa un booleano.
	// Il "quando" resta un dettaglio interno del backend.
	public static NotificationDto from(Notification n) {
		return new NotificationDto(
				n.getId(),
				n.getType(),
				n.getResourceId(),
				n.getTitle(),
				n.getCreatedAt(),
				n.getReadAt() != null);
	}
}
