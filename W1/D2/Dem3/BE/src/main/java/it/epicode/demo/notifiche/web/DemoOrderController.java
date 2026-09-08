package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.model.NotificationType;
import it.epicode.demo.notifiche.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Endpoint di comodo per generare una notifica a lezione: qui non esiste
// nessun ordine, solo il suo id.
@RestController
@RequestMapping("/api/demo")
public class DemoOrderController {

	private final NotificationService service;

	public DemoOrderController(NotificationService service) {
		this.service = service;
	}

	@PostMapping("/orders/{id}/ship")
	@ResponseStatus(HttpStatus.CREATED)
	public NotificationDto ship(@PathVariable Long id, @RequestParam String recipient) {
		return NotificationDto.from(
				service.notify(recipient, NotificationType.ORDER_SHIPPED, id, "Ordine " + id + " spedito"));
	}
}
