package com.example.demo.service;

import com.example.demo.entity.Canale;
import com.example.demo.entity.Iscrizione;
import com.example.demo.entity.Utente;
import com.example.demo.exception.ConflittoException;
import com.example.demo.exception.RisorsaNonTrovataException;
import com.example.demo.repository.CanaleRepository;
import com.example.demo.repository.IscrizioneRepository;
import com.example.demo.repository.UtenteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class IscrizioneService {

    private final IscrizioneRepository iscrizioneRepository;
    private final CanaleRepository canaleRepository;
    private final UtenteRepository utenteRepository;

    public IscrizioneService(IscrizioneRepository iscrizioneRepository,
                             CanaleRepository canaleRepository,
                             UtenteRepository utenteRepository) {
        this.iscrizioneRepository = iscrizioneRepository;
        this.canaleRepository = canaleRepository;
        this.utenteRepository = utenteRepository;
    }

    @Transactional
    public void follow(UUID idUtente, UUID idCanale) {
        if (iscrizioneRepository.existsByUtenteIdAndCanaleId(idUtente, idCanale)) {
            throw new ConflittoException("Iscrizione gia esistente");
        }

        Utente utente = utenteRepository.findById(idUtente)
                .orElseThrow(() -> new RisorsaNonTrovataException("Utente non trovato"));
        Canale canale = canaleRepository.findById(idCanale)
                .orElseThrow(() -> new RisorsaNonTrovataException("Canale non trovato"));

        // il proprietario riceve gia' il canale tra i suoi: iscriversi non avrebbe senso
        if (canale.getUtente().getId().equals(idUtente)) {
            throw new ConflittoException("Non puoi iscriverti a un canale che hai creato");
        }

        try {
            iscrizioneRepository.saveAndFlush(new Iscrizione(utente, canale));
        } catch (DataIntegrityViolationException e) {
            // due follow simultanei sullo stesso canale: decide il vincolo UNIQUE
            throw new ConflittoException("Iscrizione gia esistente");
        }
    }

    @Transactional
    public void unfollow(UUID idUtente, UUID idCanale) {
        Iscrizione iscrizione = iscrizioneRepository.findByUtenteIdAndCanaleId(idUtente, idCanale)
                .orElseThrow(() -> new RisorsaNonTrovataException("Iscrizione non trovata"));

        iscrizioneRepository.delete(iscrizione);
    }
}
