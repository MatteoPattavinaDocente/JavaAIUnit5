package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.service.NotificationService;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Le quattro chiamate HTTP che il frontend puo' fare sulle notifiche.
 *
 * @RestController: ogni metodo restituisce direttamente il corpo della risposta,
 * che Spring converte in JSON in automatico.
 * @RequestMapping("/api/notifications"): tutti gli indirizzi qui sotto partono da li'.
 *
 * Identiche dalla Dem 1: quattro demo, quattro trasporti diversi, e questo controller
 * non e' mai cambiato di una riga. E' il messaggio piu' importante della giornata:
 * STOMP e il broker non sostituiscono il REST, gli si affiancano. Il canale porta le
 * novita' a chi e' collegato adesso; queste chiamate servono per lo storico e per
 * tutto quello che un utente si e' perso mentre era via.
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

	private final NotificationService service;

	public NotificationController(NotificationService service) {
		this.service = service;
	}

	/**
	 * GET /api/notifications?recipient=mario&page=0&size=5
	 *
	 * Il destinatario arriva come parametro nell'URL. In un'app vera lo prenderemmo
	 * dal token di autenticazione: qui lo passiamo a mano per poter cambiare utente
	 * al volo durante la lezione.
	 *
	 * @PageableDefault: se il frontend non dice niente, Spring usa questi valori
	 * (20 per pagina, ordinate per data decrescente, cioe' le piu' recenti in cima).
	 */
	@GetMapping
	public Page<NotificationDto> list(
			@RequestParam String recipient,
			@PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
		return service.list(recipient, pageable);
	}

	/**
	 * GET /api/notifications/unread-count?recipient=mario
	 * Risponde { "count": 3 }. Restituiamo un oggetto e non il numero nudo
	 * perche' un JSON con un nome dentro e' piu' facile da estendere domani.
	 */
	@GetMapping("/unread-count")
	public Map<String, Long> unreadCount(@RequestParam String recipient) {
		return Map.of("count", service.unreadCount(recipient));
	}

	/**
	 * PATCH /api/notifications/7/read
	 *
	 * PATCH e non PUT: stiamo cambiando un solo campo della notifica, non sostituendola tutta.
	 * @PathVariable prende il 7 dall'indirizzo.
	 * 204 No Content: e' andato tutto bene e non c'e' niente da restituire.
	 */
	@PatchMapping("/{id}/read")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void markRead(@PathVariable Long id) {
		service.markRead(id);
	}

	/** POST /api/notifications/read-all?recipient=mario — segna lette tutte in un colpo. */
	@PostMapping("/read-all")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void markAllRead(@RequestParam String recipient) {
		service.markAllRead(recipient);
	}
}
