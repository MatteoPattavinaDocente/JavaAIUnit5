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

/**
 * Il "telecomando" della lezione: serve solo a far nascere una notifica quando vogliamo noi.
 *
 * In un'applicazione vera questa notifica nascerebbe dentro la logica degli ordini,
 * nel momento in cui la spedizione parte davvero. Qui non esiste nessun ordine:
 * esiste solo il suo numero, che passiamo a mano.
 *
 * Questa classe non fa parte del dominio: e' un attrezzo da demo.
 */
@RestController
@RequestMapping("/api/demo")
public class DemoOrderController {

	private final NotificationService service;

	public DemoOrderController(NotificationService service) {
		this.service = service;
	}

	/**
	 * POST /api/demo/orders/42/ship?recipient=mario
	 *
	 * Crea la notifica e restituisce 201 Created con la notifica appena nata.
	 *
	 * Il metodo e' identico a quello della Dem 1: crea e basta. Cambia solo cosa
	 * succede dopo, dentro il service, e da li' in poi il messaggio arriva al
	 * destinatario collegato senza che questa classe ne sappia niente.
	 */
	@PostMapping("/orders/{id}/ship")
	@ResponseStatus(HttpStatus.CREATED)
	public NotificationDto ship(@PathVariable Long id, @RequestParam String recipient) {
		return NotificationDto.from(
				service.notify(recipient, NotificationType.ORDER_SHIPPED, id, "Ordine " + id + " spedito"));
	}
}
