package com.example.demo.service;

import com.example.demo.dto.CreaNotificaRequest;
import com.example.demo.dto.CreazioneNotificaResponse;
import com.example.demo.dto.NotificaResponse;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Canale;
import com.example.demo.entity.Notifica;
import com.example.demo.entity.TipoNotifica;
import com.example.demo.entity.Utente;
import com.example.demo.exception.RisorsaNonTrovataException;
import com.example.demo.repository.CanaleRepository;
import com.example.demo.repository.IscrizioneRepository;
import com.example.demo.repository.NotificaRepository;
import com.example.demo.repository.UtenteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class NotificaService {

    public static final int DIMENSIONE_PAGINA = 15;

    private final NotificaRepository notificaRepository;
    private final UtenteRepository utenteRepository;
    private final CanaleRepository canaleRepository;
    private final IscrizioneRepository iscrizioneRepository;
    private final NotificaWebSocketPublisher publisher;

    public NotificaService(NotificaRepository notificaRepository,
                           UtenteRepository utenteRepository,
                           CanaleRepository canaleRepository,
                           IscrizioneRepository iscrizioneRepository,
                           NotificaWebSocketPublisher publisher) {
        this.notificaRepository = notificaRepository;
        this.utenteRepository = utenteRepository;
        this.canaleRepository = canaleRepository;
        this.iscrizioneRepository = iscrizioneRepository;
        this.publisher = publisher;
    }

    /** Notifiche dell'utente: prima le non lette, poi le altre, sempre dalla piu' recente. */
    @Transactional(readOnly = true)
    public PageResponse<NotificaResponse> elenca(UUID idUtente, int pagina) {
        Pageable pageable = PageRequest.of(pagina, DIMENSIONE_PAGINA);
        return PageResponse.from(notificaRepository.findByDestinatario(idUtente, pageable), NotificaResponse::from);
    }

    @Transactional(readOnly = true)
    public long contaNonLette(UUID idUtente) {
        return notificaRepository.countByDestinatarioIdAndReadAtIsNull(idUtente);
    }

    /**
     * Marca come letta una singola notifica. La ricerca include il destinatario:
     * un utente non puo' segnare come lette le notifiche altrui.
     */
    @Transactional
    public NotificaResponse marcaLetta(UUID idNotifica, UUID idUtente) {
        Notifica notifica = notificaRepository.findByIdAndDestinatarioId(idNotifica, idUtente)
                .orElseThrow(() -> new RisorsaNonTrovataException("Notifica non trovata"));

        // idempotente: se era gia' letta si conserva il primo istante di lettura
        if (notifica.getReadAt() == null) {
            notifica.setReadAt(Instant.now());
        }

        publisher.inviaConteggio(idUtente, contaNonLette(idUtente));
        return NotificaResponse.from(notifica);
    }

    /** @return quante notifiche sono state marcate come lette. */
    @Transactional
    public int marcaTutteLette(UUID idUtente) {
        int aggiornate = notificaRepository.marcaTutteLette(idUtente, Instant.now());
        publisher.inviaConteggio(idUtente, 0);
        return aggiornate;
    }

    /**
     * Crea la notifica e la recapita subito ai destinatari connessi via WebSocket.
     *
     * PERSONAL : un solo record verso l'utente indicato
     * ALL      : un record per ogni utente registrato
     * CANALE   : un record per ogni iscritto al canale, piu' il broadcast sul topic del canale
     */
    @Transactional
    public CreazioneNotificaResponse crea(CreaNotificaRequest richiesta) {
        List<UUID> destinatari = risolviDestinatari(richiesta);
        Canale canale = richiesta.tipo() == TipoNotifica.CANALE
                ? canaleRepository.findById(richiesta.idCanale())
                    .orElseThrow(() -> new RisorsaNonTrovataException("Canale non trovato"))
                : null;

        List<Notifica> create = new ArrayList<>(destinatari.size());
        for (UUID idDestinatario : destinatari) {
            Utente destinatario = utenteRepository.getReferenceById(idDestinatario);
            Notifica notifica = canale != null
                    ? new Notifica(destinatario, canale, richiesta.message())
                    : new Notifica(destinatario, richiesta.tipo(), richiesta.message());
            create.add(notifica);
        }
        notificaRepository.saveAll(create);
        // forza l'assegnazione di id e created_at prima di serializzare il payload WebSocket
        notificaRepository.flush();

        int inviate = 0;
        for (int i = 0; i < create.size(); i++) {
            NotificaResponse payload = NotificaResponse.from(create.get(i));
            if (publisher.inviaSeConnesso(destinatari.get(i), payload)) {
                inviate++;
            }
        }

        if (canale != null && !create.isEmpty()) {
            publisher.inviaSulTopicCanale(canale.getId(), NotificaResponse.from(create.getFirst()));
        }

        return new CreazioneNotificaResponse(richiesta.tipo(), create.size(), inviate);
    }

    /** Applica le regole del tipo di notifica e restituisce gli id dei destinatari. */
    private List<UUID> risolviDestinatari(CreaNotificaRequest richiesta) {
        return switch (richiesta.tipo()) {
            case PERSONAL -> {
                if (richiesta.idDestinatario() == null) {
                    throw new IllegalArgumentException("idDestinatario obbligatorio per le notifiche PERSONAL");
                }
                if (richiesta.idCanale() != null) {
                    throw new IllegalArgumentException("idCanale non ammesso per le notifiche PERSONAL");
                }
                if (!utenteRepository.existsById(richiesta.idDestinatario())) {
                    throw new RisorsaNonTrovataException("Destinatario non trovato");
                }
                yield List.of(richiesta.idDestinatario());
            }
            case ALL -> {
                if (richiesta.idCanale() != null || richiesta.idDestinatario() != null) {
                    throw new IllegalArgumentException("idCanale e idDestinatario non ammessi per le notifiche ALL");
                }
                yield utenteRepository.findAll().stream().map(Utente::getId).toList();
            }
            case CANALE -> {
                if (richiesta.idCanale() == null) {
                    throw new IllegalArgumentException("idCanale obbligatorio per le notifiche CANALE");
                }
                if (richiesta.idDestinatario() != null) {
                    throw new IllegalArgumentException("idDestinatario non ammesso per le notifiche CANALE");
                }
                if (!canaleRepository.existsById(richiesta.idCanale())) {
                    throw new RisorsaNonTrovataException("Canale non trovato");
                }
                yield iscrizioneRepository.findIdUtentiIscritti(richiesta.idCanale());
            }
        };
    }
}
