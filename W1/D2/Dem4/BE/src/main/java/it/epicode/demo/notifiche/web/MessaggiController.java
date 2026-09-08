package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.ErroreStomp;
import it.epicode.demo.notifiche.dto.MessaggioPubblico;
import it.epicode.demo.notifiche.dto.MessaggioTopic;
import it.epicode.demo.notifiche.dto.MessaggioUtente;
import it.epicode.demo.notifiche.dto.NotificationDto;
import it.epicode.demo.notifiche.event.SubscriptionRegistry;
import it.epicode.demo.notifiche.model.NotificationType;
import it.epicode.demo.notifiche.service.NotificationService;
import java.security.Principal;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * LO STESSO IDENTICO MESSAGGIO, MANDATO IN QUATTRO MODI DIVERSI.
 *
 * E' la classe da usare per il confronto in aula: il contenuto e la riga sul
 * database sono uguali, cambia solo come parte e come arriva.
 *
 *   1. POST /api/demo/messaggi/senza-push
 *      HTTP all'andata, niente al ritorno. Il destinatario lo scopre solo
 *      quando ricarica la pagina. E' come funzionava la Dem 1.
 *
 *   2. POST /api/demo/messaggi/con-push
 *      HTTP all'andata, STOMP al ritorno. E' il caso normale di un'applicazione
 *      vera: il messaggio nasce da una richiesta (o da un lavoro schedulato,
 *      o da un altro servizio) e la consegna la fa il broker.
 *
 *   3. @MessageMapping("/messaggi")
 *      STOMP sia all'andata sia al ritorno. Nessuna richiesta HTTP: il canale
 *      e' gia' aperto e funziona nelle due direzioni.
 *
 *   4. @MessageMapping("/topic-messaggi")
 *      Come sopra, ma verso molti destinatari, con una regola in piu':
 *      si scrive solo sui topic a cui questa sessione e' iscritta.
 *
 * Un metodo @MessageMapping non e' un endpoint HTTP: non ha un URL, non risponde
 * con un codice di stato, non lo si chiama con curl. Lo raggiunge un frame SEND
 * mandato dal client sul canale gia' aperto.
 */
@RestController
@RequestMapping("/api/demo/messaggi")
public class MessaggiController {

	private static final Logger log = LoggerFactory.getLogger(MessaggiController.class);

	private final NotificationService service;
	private final SubscriptionRegistry sottoscrizioni;
	private final SimpMessagingTemplate messaging;

	public MessaggiController(NotificationService service, SubscriptionRegistry sottoscrizioni,
			SimpMessagingTemplate messaging) {
		this.service = service;
		this.sottoscrizioni = sottoscrizioni;
		this.messaging = messaging;
	}

	/**
	 * (1) La notifica "vecchio stile": salvata e lasciata li'.
	 *
	 * Usa notifySenzaPush, che NON annuncia l'evento. Quindi NotificationListener
	 * non parte e nessuno spedisce niente sul canale. La notifica esiste solo
	 * sul database, e il destinatario la scoprira' quando ricarica.
	 *
	 * Da provare: mandala con le due finestre aperte e guarda che non succede niente.
	 */
	@PostMapping("/senza-push")
	@ResponseStatus(HttpStatus.CREATED)
	public NotificationDto senzaPush(@RequestParam String from, @RequestParam String to,
			@RequestParam String text) {
		log.info("messaggio SENZA push   da={} a={}", from, to);
		return NotificationDto.from(service.notifySenzaPush(to, NotificationType.MESSAGE, null, titolo(from, text)));
	}

	/**
	 * (2) Stessa identica scrittura, ma con l'evento.
	 *
	 * Al commit della transazione, NotificationListener pubblica su
	 * /user/{to}/queue/notifications e il destinatario la vede comparire
	 * senza aver chiesto niente. Unica differenza col metodo qui sopra:
	 * notify() invece di notifySenzaPush().
	 */
	@PostMapping("/con-push")
	@ResponseStatus(HttpStatus.CREATED)
	public NotificationDto conPush(@RequestParam String from, @RequestParam String to,
			@RequestParam String text) {
		log.info("messaggio CON push     da={} a={}", from, to);
		return NotificationDto.from(service.notify(to, NotificationType.MESSAGE, null, titolo(from, text)));
	}

	/**
	 * (3) Niente HTTP: il client manda un frame SEND a /app/messaggi.
	 *
	 * Il prefisso /app (dichiarato in WebSocketConfig) fa arrivare il messaggio qui
	 * e non al broker.
	 *
	 * IL PARAMETRO IMPORTANTE E' IL SECONDO. Il Principal lo ha messo
	 * StompLoginInterceptor quando e' arrivato il CONNECT, e Spring ce lo passa qui.
	 * Il mittente quindi non e' un campo del contenuto, e nessuno puo' firmarsi come
	 * un altro. E' la differenza fra una prova come /app/ping e un messaggio vero.
	 *
	 * Non c'e' nessun @SendTo perche' la risposta non va a tutti, va a una persona
	 * sola: la consegna la fa il solito NotificationListener con convertAndSendToUser.
	 */
	@MessageMapping("/messaggi")
	public void viaStomp(MessaggioUtente messaggio, Principal mittente) {
		String da = mittente == null ? "ANONIMO" : mittente.getName();
		log.info("messaggio via STOMP    da={} a={}", da, messaggio.destinatario());
		service.notify(messaggio.destinatario(), NotificationType.MESSAGE, null,
				titolo(da, messaggio.testo()));
	}

	/**
	 * (4) Un messaggio su un topic, ma solo su quelli a cui QUESTA sessione e' iscritta.
	 *
	 * PERCHE' QUESTO CONTROLLO ESISTE
	 * Sul broker semplice le destinazioni non hanno un proprietario: /topic/ordini/42
	 * nasce quando qualcuno ci si iscrive, e chiunque puo' scriverci. Senza un
	 * controllo qui, una sessione potrebbe scrivere su un topic che non guarda nemmeno.
	 *
	 * Il controllo e' sulla SESSIONE (non sull'utente) e sta sul SERVER. Il pannello
	 * del frontend che nasconde i topic non sottoscritti e' solo una cortesia verso
	 * chi lo usa: chiunque puo' mandare un frame a mano dalla console del browser.
	 * La regola vale solo se e' scritta qui.
	 */
	@MessageMapping("/topic-messaggi")
	public void suTopic(MessaggioTopic messaggio, Principal mittente, SimpMessageHeaderAccessor headers) {
		String da = mittente == null ? "ANONIMO" : mittente.getName();
		String sessione = headers.getSessionId();

		// Il prefisso lo mette il server: al client chiediamo il nome del topic,
		// non la destinazione completa, cosi' nessuno prova a scrivere su /queue o /user.
		String destinazione = "/topic/" + messaggio.topic();

		if (sessione == null || !sottoscrizioni.eSottoscritta(sessione, destinazione)) {
			log.info("messaggio su topic RIFIUTATO   da={} destinazione={} (non sottoscritto)", da, destinazione);

			// Il rifiuto va al solo mittente, su una coda personale: la connessione
			// resta in piedi e nessun altro vede niente. Vedi ErroreStomp per il perche'
			// non usiamo il frame ERROR del protocollo.
			messaging.convertAndSendToUser(da, "/queue/errors",
					new ErroreStomp("non sei sottoscritto a " + destinazione + ": SUBSCRIBE prima di scrivere",
							destinazione));
			return;
		}

		log.info("messaggio su topic             da={} destinazione={}", da, destinazione);

		// convertAndSend, non convertAndSendToUser: qui non c'e' un destinatario preciso.
		// Lo riceve chi e' iscritto in questo momento, mittente compreso, e nessun altro.
		// Niente database: un topic non ha storico, chi non c'era ha perso il messaggio.
		messaging.convertAndSend(destinazione,
				new MessaggioPubblico(messaggio.topic(), da, messaggio.testo(), Instant.now()));
	}

	private String titolo(String mittente, String testo) {
		return mittente + ": " + testo;
	}
}
