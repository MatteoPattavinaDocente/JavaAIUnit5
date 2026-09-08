package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Prende una notifica gia' esistente e la rimanda sul canale nelle altre due modalita'.
 *
 * A cosa serve: a far vedere in classe la differenza fra le tre destinazioni con lo
 * stesso identico contenuto. Stessa notifica, tre percorsi diversi, tre gruppi diversi
 * di persone che la ricevono.
 *
 * Non crea niente e non salva niente: pubblica soltanto.
 */
@RestController
@RequestMapping("/api/demo/notifications/{id}")
public class DemoPublisherController {

	private final NotificationService service;
	private final NotificationPublisher publisher;

	public DemoPublisherController(NotificationService service, NotificationPublisher publisher) {
		this.service = service;
		this.publisher = publisher;
	}

	/** POST /api/demo/notifications/7/broadcast -> la ricevono TUTTI i collegati. */
	@PostMapping("/broadcast")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void broadcast(@PathVariable Long id) {
		publisher.broadcast(service.find(id));
	}

	/**
	 * POST /api/demo/notifications/7/to-order -> la ricevono solo quelli che seguono
	 * l'ordine a cui la notifica si riferisce (il campo resourceId).
	 */
	@PostMapping("/to-order")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void perOrdine(@PathVariable Long id) {
		NotificationDto dto = service.find(id);
		publisher.perOrdine(dto.resourceId(), dto);
	}
}
