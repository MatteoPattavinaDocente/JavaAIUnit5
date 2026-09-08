package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Ripubblica una notifica gia' esistente nelle altre due modalita': serve a mostrare
// a lezione la differenza fra le tre destinazioni, con lo stesso identico payload.
@RestController
@RequestMapping("/api/demo/notifications/{id}")
public class DemoPublisherController {

	private final NotificationService service;
	private final NotificationPublisher publisher;

	public DemoPublisherController(NotificationService service, NotificationPublisher publisher) {
		this.service = service;
		this.publisher = publisher;
	}

	@PostMapping("/broadcast")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void broadcast(@PathVariable Long id) {
		publisher.broadcast(service.find(id));
	}

	@PostMapping("/to-order")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void perOrdine(@PathVariable Long id) {
		NotificationDto dto = service.find(id);
		publisher.perOrdine(dto.resourceId(), dto);
	}
}
