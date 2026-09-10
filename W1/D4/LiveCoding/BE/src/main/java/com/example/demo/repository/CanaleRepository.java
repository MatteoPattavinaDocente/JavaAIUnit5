package com.example.demo.repository;

import com.example.demo.entity.Canale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CanaleRepository extends JpaRepository<Canale, UUID> {

    /** Canali a cui l'utente e' iscritto, dal piu' recente. */
    @Query("""
            SELECT c FROM Canale c
            JOIN Iscrizione i ON i.canale = c
            WHERE i.utente.id = :idUtente
            ORDER BY c.createdAt DESC
            """)
    Page<Canale> findIscrittoBy(@Param("idUtente") UUID idUtente, Pageable pageable);
}
