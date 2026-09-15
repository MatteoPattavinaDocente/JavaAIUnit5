package it.epicode.demo.chat.service;

import it.epicode.demo.chat.dto.Aggiornamento;
import it.epicode.demo.chat.dto.ConversazioneRiepilogo;
import it.epicode.demo.chat.dto.MessaggioRisposta;
import it.epicode.demo.chat.dto.PaginaMessaggi;
import it.epicode.demo.chat.model.Conversazione;
import it.epicode.demo.chat.model.Messaggio;
import it.epicode.demo.chat.model.StatoMessaggio;
import it.epicode.demo.chat.repository.ConversazioneRepository;
import it.epicode.demo.chat.repository.MessaggioRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Limit;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);

	/** Dove arrivano i messaggi: tutti i client sono iscritti a /user + questa. */
	public static final String MESSAGGI = "/queue/messaggi";

	/** Dove arrivano i cambi di stato e l'indicatore di scrittura. */
	public static final String AGGIORNAMENTI = "/queue/aggiornamenti";

	/** Quanti messaggi per pagina di cronologia (slide 43). */
	private static final int PAGINA = 30;

	/**
	 * Una chat aperta ma mai usata: nessuna riga in archivio, quindi nessun id.
	 * Il client la mostra vuota e non chiede conferme di lettura per una
	 * conversazione che non esiste.
	 */
	private static final PaginaMessaggi VUOTA = new PaginaMessaggi(null, List.of(), null, 0);

	private final MessaggioRepository messaggi;
	private final ConversazioneRepository conversazioni;
	private final SimpMessagingTemplate template;
	private final SimpUserRegistry registro;

	public ChatService(MessaggioRepository messaggi,
			ConversazioneRepository conversazioni,
			SimpMessagingTemplate template,
			SimpUserRegistry registro) {
		this.messaggi = messaggi;
		this.conversazioni = conversazioni;
		this.template = template;
		this.registro = registro;
	}

	// --- invio ------------------------------------------------------------------

	/**
	 * Salva e poi consegna, in quest'ordine (slide 18). Il metodo non e'
	 * @Transactional: i metodi di JpaRepository lo sono per conto loro, quindi
	 * quando save() ritorna la riga e' committata e un client che ricarica la
	 * cronologia la trova.
	 */
	public MessaggioRisposta inoltra(String mittente, String destinatario, String testo, String idTemporaneo) {
		Conversazione conversazione = trovaOCrea(mittente, destinatario);
		Messaggio salvato = messaggi.save(new Messaggio(conversazione, mittente, destinatario, testo));

		if (registro.getUser(destinatario) != null) {
			salvato.consegnato();
			salvato = messaggi.save(salvato);
		}

		// Al destinatario senza l'id temporaneo: non e' suo, non gli serve.
		template.convertAndSendToUser(destinatario, MESSAGGI, MessaggioRisposta.da(salvato));

		// Al mittente CON l'id temporaneo, cosi' il suo client sostituisce la
		// riga mostrata in anticipo invece di aggiungerne una seconda
		// (slide 21 e 46, riga «Due copie»).
		template.convertAndSendToUser(mittente, MESSAGGI, MessaggioRisposta.con(salvato, idTemporaneo));

		log.info("messaggio {} conv={} {} -> {} ({})", salvato.getId(), conversazione.getId(),
				mittente, destinatario, salvato.getStato());
		return MessaggioRisposta.da(salvato);
	}

	/**
	 * L'indicatore di scrittura non viene salvato: e' informazione temporanea
	 * (slide 41). Se l'utente chiude la scheda mentre scrive, il messaggio di
	 * fine non parte mai: per questo lato client l'indicatore scade da solo.
	 */
	public void scrive(String chi, String aChi) {
		// Prima del primo messaggio non c'e' una conversazione a cui legare
		// l'indicatore, e il client lo scarterebbe comunque: non si crea una
		// riga solo perche' qualcuno ha toccato la tastiera.
		trova(chi, aChi).ifPresent(conversazione -> template.convertAndSendToUser(
				aChi, AGGIORNAMENTI, Aggiornamento.scrive(conversazione.getId(), chi)));
	}

	// --- lettura ----------------------------------------------------------------

	/**
	 * Il conteggio scende quando il client dichiara di aver MOSTRATO i messaggi,
	 * non quando li riceve (slide 42).
	 */
	@Transactional
	public void segnaLetti(Long idConversazione, String utente) {
		Conversazione conversazione = conversazioni.findById(idConversazione)
				.orElseThrow(() -> new IllegalArgumentException("conversazione: non esiste"));
		if (!conversazione.partecipa(utente)) {
			// Va verificato che i due utenti abbiano una conversazione in comune
			// (slide 11): senza, chiunque potrebbe segnare letti i messaggi di
			// un altro.
			throw new IllegalArgumentException("conversazione: non sei un partecipante");
		}

		// Prima quali sono, poi l'aggiornamento in massa: il mittente vuole
		// sapere quali righe sono diventate lette.
		List<Long> id = messaggi.idNonLetti(idConversazione, utente);
		if (id.isEmpty()) {
			return;
		}
		messaggi.segnaLetti(idConversazione, utente, StatoMessaggio.LETTO);

		// Il server aggiorna lo stato e informa il MITTENTE sulla sua
		// destinazione utente (slide 23).
		String controparte = conversazione.controparteDi(utente);
		template.convertAndSendToUser(controparte, AGGIORNAMENTI,
				Aggiornamento.letti(idConversazione, utente, id));
		// E anche a chi ha letto, che puo' avere un secondo dispositivo aperto.
		template.convertAndSendToUser(utente, AGGIORNAMENTI,
				Aggiornamento.letti(idConversazione, utente, id));

		log.info("letti {} messaggi conv={} da {}", id.size(), idConversazione, utente);
	}

	// --- lettura della cronologia -----------------------------------------------

	/**
	 * L'ultima pagina, dalla più vecchia alla più recente.
	 *
	 * Qui la conversazione si CERCA soltanto. Aprire una chat non è ancora una
	 * conversazione: la riga nasce con il primo messaggio, dentro inoltra().
	 * Crearla in lettura riempirebbe l'elenco di righe vuote - e le farebbe
	 * comparire anche sullo schermo dell'altro, che non ha ancora ricevuto
	 * niente. In più, creare dentro un metodo readOnly non funziona: la
	 * @Transactional di trovaOCrea non entra in gioco, perché la chiamata parte
	 * da dentro la stessa classe e non passa dal proxy di Spring, e PostgreSQL
	 * rifiuta l'INSERT nella transazione di sola lettura aperta qui.
	 */
	@Transactional(readOnly = true)
	public PaginaMessaggi cronologia(String utente, String conChi) {
		Conversazione conversazione = trova(utente, conChi).orElse(null);
		if (conversazione == null) {
			return VUOTA;
		}
		List<MessaggioRisposta> pagina = new ArrayList<>(
				messaggi.ultimi(conversazione.getId(), Limit.of(PAGINA)).stream()
						.map(MessaggioRisposta::da)
						.toList());
		// La query prende i più recenti in ordine decrescente: qui si rovescia.
		pagina.sort(Comparator.comparing(MessaggioRisposta::id));
		return new PaginaMessaggi(conversazione.getId(), pagina,
				messaggi.ultimoId(conversazione.getId()),
				messaggi.nonLetti(conversazione.getId(), utente));
	}

	/**
	 * Il recupero dopo una riconnessione: il buco temporale. I messaggi arrivati
	 * mentre la connessione era chiusa non vengono ripetuti dal broker, quindi
	 * il client chiede quelli successivi all'ultimo che ha in memoria (slide 38).
	 */
	@Transactional(readOnly = true)
	public PaginaMessaggi dopo(String utente, String conChi, Long dopoId) {
		Conversazione conversazione = trova(utente, conChi).orElse(null);
		if (conversazione == null) {
			return VUOTA;
		}
		List<MessaggioRisposta> nuovi = messaggi.dopo(conversazione.getId(), dopoId).stream()
				.map(MessaggioRisposta::da)
				.toList();
		return new PaginaMessaggi(conversazione.getId(), nuovi,
				messaggi.ultimoId(conversazione.getId()),
				messaggi.nonLetti(conversazione.getId(), utente));
	}

	/** L'elenco delle conversazioni con il conteggio dei non letti (slide 42). */
	@Transactional(readOnly = true)
	public List<ConversazioneRiepilogo> riepilogo(String utente) {
		return conversazioni.diUtente(utente).stream()
				.map(c -> {
					List<Messaggio> ultimo = messaggi.ultimi(c.getId(), Limit.of(1));
					String controparte = c.controparteDi(utente);
					return new ConversazioneRiepilogo(
							c.getId(),
							controparte,
							messaggi.nonLetti(c.getId(), utente),
							ultimo.isEmpty() ? null : ultimo.getFirst().getTesto(),
							ultimo.isEmpty() ? null : ultimo.getFirst().getIstante(),
							registro.getUser(controparte) != null);
				})
				.sorted(Comparator.comparing(ConversazioneRiepilogo::controparte))
				.toList();
	}

	// --- utilità ----------------------------------------------------------------

	/**
	 * La coppia e' conservata in ordine alfabetico, quindi (anna, bruno) e
	 * (bruno, anna) trovano la stessa riga.
	 */
	/** Cerca la coppia senza crearla: vuoto se i due non si sono mai scritti. */
	public Optional<Conversazione> trova(String a, String b) {
		if (a.equals(b)) {
			throw new IllegalArgumentException("destinatario: non puoi scrivere a te stesso");
		}
		return conversazioni.findByUnoAndAltro(Conversazione.primo(a, b), Conversazione.secondo(a, b));
	}

	@Transactional
	public Conversazione trovaOCrea(String a, String b) {
		return trova(a, b).orElseGet(() -> conversazioni.save(Conversazione.fra(a, b)));
	}

	/** Solo per la lezione. */
	@Transactional
	public void azzera() {
		messaggi.deleteAll();
		conversazioni.deleteAll();
	}

	/** Solo per la lezione: riempie una conversazione per far vedere la paginazione. */
	@Transactional
	public int riempi(String a, String b, int quanti) {
		Conversazione conversazione = trovaOCrea(a, b);
		for (int i = 1; i <= quanti; i++) {
			boolean pari = i % 2 == 0;
			messaggi.save(new Messaggio(conversazione,
					pari ? a : b, pari ? b : a, "messaggio di prova numero " + i));
		}
		return quanti;
	}
}
