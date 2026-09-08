package it.epicode.demo.notifiche.dto;

import it.epicode.demo.notifiche.model.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Il corpo JSON del POST /api/demo/notify: chi la riceve, di che tipo e',
 * su quale risorsa e cosa dice.
 *
 * Le annotazioni @NotBlank e @NotNull sono controlli di validazione: insieme
 * al @Valid nel controller fanno rispondere 400 Bad Request se un campo manca,
 * prima ancora che il metodo venga eseguito. Cosi' non serve scrivere if a mano.
 */
public record NotifyRequest(
		@NotBlank String recipient,
		@NotNull NotificationType type,
		Long resourceId,
		@NotBlank String title) {
}
