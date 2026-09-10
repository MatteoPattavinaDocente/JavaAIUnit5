package com.example.demo.service;

import com.example.demo.dto.CanaleResponse;
import com.example.demo.dto.CreaCanaleRequest;
import com.example.demo.dto.PageResponse;
import com.example.demo.entity.Canale;
import com.example.demo.entity.Utente;
import com.example.demo.exception.RisorsaNonTrovataException;
import com.example.demo.repository.CanaleRepository;
import com.example.demo.repository.UtenteRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CanaleService {

    /** Dimensione di pagina fissata dai requisiti. */
    public static final int DIMENSIONE_PAGINA = 15;

    private final CanaleRepository canaleRepository;
    private final UtenteRepository utenteRepository;

    public CanaleService(CanaleRepository canaleRepository, UtenteRepository utenteRepository) {
        this.canaleRepository = canaleRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    public CanaleResponse crea(CreaCanaleRequest richiesta, UUID idUtente) {
        Utente proprietario = utenteRepository.findById(idUtente)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));

        Canale canale = new Canale(richiesta.nome(), richiesta.descrizione(), proprietario);
        canaleRepository.save(canale);

        return CanaleResponse.from(canale);
    }

    /** Dettaglio di un singolo canale, usato dalla pagina del canale. */
    @Transactional(readOnly = true)
    public CanaleResponse dettaglio(UUID idCanale) {
        return canaleRepository.findById(idCanale)
                .map(CanaleResponse::from)
                .orElseThrow(() -> new RisorsaNonTrovataException("Canale non trovato"));
    }

    /** Tutti i canali, dal piu' recente, 15 per pagina. */
    @Transactional(readOnly = true)
    public PageResponse<CanaleResponse> elencaTutti(int pagina) {
        Pageable pageable = PageRequest.of(pagina, DIMENSIONE_PAGINA, Sort.by(Sort.Direction.DESC, "createdAt"));
        return PageResponse.from(canaleRepository.findAll(pageable), CanaleResponse::from);
    }

    /** Solo i canali a cui l'utente del token e' iscritto, dal piu' recente, 15 per pagina. */
    @Transactional(readOnly = true)
    public PageResponse<CanaleResponse> elencaIscritto(UUID idUtente, int pagina) {
        // l'ordinamento e' gia' nella query: qui il Pageable porta solo la paginazione
        Pageable pageable = PageRequest.of(pagina, DIMENSIONE_PAGINA);
        return PageResponse.from(canaleRepository.findIscrittoBy(idUtente, pageable), CanaleResponse::from);
    }
}
