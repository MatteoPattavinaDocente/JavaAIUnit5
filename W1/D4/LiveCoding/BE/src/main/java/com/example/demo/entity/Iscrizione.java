package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "iscrizioni",
        // impedisce a livello DB la doppia iscrizione dello stesso utente allo stesso canale
        uniqueConstraints = @UniqueConstraint(name = "uk_iscrizioni_utente_canale", columnNames = {"id_utente", "id_canale"}),
        indexes = {
                @Index(name = "idx_iscrizioni_utente", columnList = "id_utente"),
                @Index(name = "idx_iscrizioni_canale", columnList = "id_canale")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Iscrizione {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utente", nullable = false, foreignKey = @ForeignKey(name = "fk_iscrizioni_utente"))
    private Utente utente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_canale", nullable = false, foreignKey = @ForeignKey(name = "fk_iscrizioni_canale"))
    private Canale canale;

    public Iscrizione(Utente utente, Canale canale) {
        this.utente = utente;
        this.canale = canale;
    }
}
