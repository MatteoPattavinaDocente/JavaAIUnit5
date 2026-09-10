package com.example.demo.repository;

import com.example.demo.entity.Iscrizione;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IscrizioneRepository extends JpaRepository<Iscrizione, UUID> {

    Optional<Iscrizione> findByUtenteIdAndCanaleId(UUID idUtente, UUID idCanale);

    boolean existsByUtenteIdAndCanaleId(UUID idUtente, UUID idCanale);

    /** Iscritti a un canale, usati come destinatari nel fan-out delle notifiche. */
    @Query("SELECT i.utente.id FROM Iscrizione i WHERE i.canale.id = :idCanale")
    List<UUID> findIdUtentiIscritti(@Param("idCanale") UUID idCanale);
}
