package it.epicode.demo.notifiche.dto;

/**
 * La "busta" che avvolge la notifica prima di infilarla nel canale.
 *
 * PERCHE' SERVE
 * Su una WebSocket nuda il messaggio e' una stringa e basta: niente mittente,
 * niente destinazione, niente intestazioni. Ma il frontend, quando riceve qualcosa,
 * deve poter capire PERCHE' gli e' arrivato: e' una notifica mia? un annuncio a tutti?
 * un messaggio della chat dell'ordine 42?
 *
 * Non potendo metterlo fuori, quel "perche'" lo mettiamo dentro, in un campo:
 * canale vale "personale", "tutti" oppure "ordine:42".
 *
 * Questo campo e' una destinazione scritta a mano. In STOMP (Dem 4) sparisce,
 * perche' la destinazione viaggia nell'intestazione del frame ed e' il protocollo
 * a gestirla: /user/queue/notifications, /topic/notifications, /topic/orders/42.
 *
 * I tre metodi statici esistono per evitare che qualcuno scriva "personle" per sbaglio:
 * la stringa e' scritta in un posto solo.
 */
public record Busta(String canale, NotificationDto notifica) {

	public static Busta personale(NotificationDto notifica) {
		return new Busta("personale", notifica);
	}

	public static Busta tutti(NotificationDto notifica) {
		return new Busta("tutti", notifica);
	}

	public static Busta ordine(Long ordineId, NotificationDto notifica) {
		return new Busta("ordine:" + ordineId, notifica);
	}
}
