package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.dto.NotifyRequest;
import it.epicode.demo.notifiche.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Versione generica del "telecomando": qui il tipo e il titolo li sceglie chi chiama.
 *
 * Serve per non doversi inventare un endpoint nuovo per ogni scenario da mostrare
 * a lezione. Crea la notifica e basta: la consegna sul canale parte da sola, perche'
 * il service annuncia l'evento e NotificationListener lo raccoglie.
 *
 * @Valid attiva i controlli dichiarati su NotifyRequest: se manca il destinatario,
 * Spring risponde 400 senza nemmeno entrare nel metodo.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoNotifyController {

	private final NotificationService service;

	public DemoNotifyController(NotificationService service) {
		this.service = service;
	}

	@PostMapping("/notify")
	@ResponseStatus(HttpStatus.CREATED)
	public NotificationDto notify(@Valid @RequestBody NotifyRequest richiesta) {
		return NotificationDto.from(service.notify(
				richiesta.recipient(), richiesta.type(), richiesta.resourceId(), richiesta.title()));
	}
}
