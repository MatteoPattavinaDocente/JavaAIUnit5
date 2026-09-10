package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "canali",
        indexes = @Index(name = "idx_canali_created_at", columnList = "created_at DESC")
)
@Getter
@Setter
@NoArgsConstructor
public class Canale {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "descrizione", columnDefinition = "text")
    private String descrizione;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Utente proprietario/creatore del canale. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_utente", nullable = false, foreignKey = @jakarta.persistence.ForeignKey(name = "fk_canali_utente"))
    private Utente utente;

    public Canale(String nome, String descrizione, Utente utente) {
        this.nome = nome;
        this.descrizione = descrizione;
        this.utente = utente;
    }
}
