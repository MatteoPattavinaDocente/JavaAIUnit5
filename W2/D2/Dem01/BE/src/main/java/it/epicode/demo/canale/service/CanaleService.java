package it.epicode.demo.canale.service;

import it.epicode.demo.canale.dto.FrameLog;
import it.epicode.demo.canale.dto.Presenza;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

@Service
public class CanaleService {

	private static final Logger log = LoggerFactory.getLogger(CanaleService.class);

	/** La destinazione a cui tutti i client sono iscritti, uguale per tutti. */
	public static final String DESTINAZIONE = "/queue/messaggi";

	private final SimpMessagingTemplate template;
	private final SimpUserRegistry registro;

	public CanaleService(SimpMessagingTemplate template, SimpUserRegistry registro) {
		this.template = template;
		this.registro = registro;
	}

	/**
	 * Il metodo aggiunge il prefisso /user da solo: la destinazione si passa
	 * senza (slide 10). Il primo argomento e' il NOME dell'utente, non la
	 * sessione: se ne ha due aperte, ricevono entrambe.
	 *
	 * Se l'utente non e' collegato non succede niente e non c'e' nessun errore:
	 * il messaggio si perde. In questa demo non c'e' database, quindi si perde
	 * davvero - dalla demo 2 in poi resta salvato.
	 */
	public boolean inviaA(String destinatario, String mittente, String testo) {
		FrameLog frame = new FrameLog(Instant.now(), mittente, "/user" + DESTINAZIONE, testo);
		template.convertAndSendToUser(destinatario, DESTINAZIONE, frame);

		boolean collegato = registro.getUser(destinatario) != null;
		log.info("inviato a {} (collegato={}) da {}: {}", destinatario, collegato, mittente, testo);
		return collegato;
	}

	/** Chi e' collegato adesso, con il numero di sessioni per utente. */
	public Presenza presenza() {
		List<Presenza.UtenteCollegato> collegati = registro.getUsers().stream()
				.map(u -> new Presenza.UtenteCollegato(u.getName(), u.getSessions().size()))
				.sorted(Comparator.comparing(Presenza.UtenteCollegato::nome))
				.toList();
		int sessioni = collegati.stream().mapToInt(Presenza.UtenteCollegato::sessioni).sum();
		return new Presenza(collegati.size(), sessioni, collegati);
	}
}
