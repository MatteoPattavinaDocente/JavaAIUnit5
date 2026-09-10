package com.example.demo.repository;

import com.example.demo.entity.Notifica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface NotificaRepository extends JpaRepository<Notifica, UUID> {

    /** Notifiche dell'utente: prima le non lette, poi le altre, sempre dalla piu' recente. */
    @Query("""
            SELECT n FROM Notifica n
            LEFT JOIN FETCH n.canale
            WHERE n.destinatario.id = :idUtente
            ORDER BY CASE WHEN n.readAt IS NULL THEN 0 ELSE 1 END, n.createdAt DESC
            """)
    Page<Notifica> findByDestinatario(@Param("idUtente") UUID idUtente, Pageable pageable);

    Optional<Notifica> findByIdAndDestinatarioId(UUID id, UUID idDestinatario);

    long countByDestinatarioIdAndReadAtIsNull(UUID idDestinatario);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Notifica n SET n.readAt = :istante WHERE n.destinatario.id = :idUtente AND n.readAt IS NULL")
    int marcaTutteLette(@Param("idUtente") UUID idUtente, @Param("istante") Instant istante);
}
