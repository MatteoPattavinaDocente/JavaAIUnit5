package it.epicode.demo.client.service;

import it.epicode.demo.client.dto.MessaggioRisposta;
import it.epicode.demo.client.model.Messaggio;
import it.epicode.demo.client.repository.MessaggioRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);

	/** Tutti i client sono iscritti a /user + questa stringa. */
	public static final String DESTINAZIONE = "/queue/messaggi";

	private final MessaggioRepository repository;
	private final SimpMessagingTemplate template;
	private final SimpUserRegistry registro;

	public ChatService(MessaggioRepository repository,
			SimpMessagingTemplate template,
			SimpUserRegistry registro) {
		this.repository = repository;
		this.template = template;
		this.registro = registro;
	}

	/**
	 * L'ordine delle operazioni e' il punto della slide 18: PRIMA il salvataggio,
	 * POI l'invio.
	 *
	 * Il metodo NON e' annotato con @Transactional, ed e' voluto: i metodi di
	 * JpaRepository sono gia' transazionali per conto loro, quindi quando save()
	 * ritorna la riga e' committata. Se avvolgessimo tutto il metodo in una
	 * transazione, la pubblicazione avverrebbe PRIMA del commit e un client che
	 * ricarica subito la cronologia via REST potrebbe non trovare il messaggio.
	 */
	public MessaggioRisposta inoltra(String mittente, String destinatario, String testo) {
		Messaggio salvato = repository.save(new Messaggio(mittente, destinatario, testo));

		// "Consegnato" qui e' un'approssimazione: sappiamo che il destinatario ha
		// una sessione aperta, non che il suo client abbia davvero ricevuto il
		// frame. La conferma vera la manda il client (slide 23, demo 4).
		if (registro.getUser(destinatario) != null) {
			salvato.consegnato();
			repository.save(salvato);
		}

		MessaggioRisposta risposta = MessaggioRisposta.da(salvato);

		// Al destinatario e ANCHE al mittente: chi scrive puo' avere altre schede
		// aperte, e deve vederci comparire il messaggio (slide 19).
		template.convertAndSendToUser(destinatario, DESTINAZIONE, risposta);
		template.convertAndSendToUser(mittente, DESTINAZIONE, risposta);

		log.info("messaggio {} da {} a {} ({})", salvato.getId(), mittente, destinatario, salvato.getStato());
		return risposta;
	}

	/** Solo per la lezione: azzera l'archivio fra una prova e l'altra. */
	public void azzera() {
		repository.deleteAll();
	}

	/** La cronologia: quello che il canale non consegna si recupera da qui (slide 22). */
	public List<MessaggioRisposta> cronologia(String utente, String conChi) {
		return repository.conversazione(utente, conChi).stream()
				.map(MessaggioRisposta::da)
				.toList();
	}
}
