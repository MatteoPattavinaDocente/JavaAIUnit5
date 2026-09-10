package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
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
import org.hibernate.annotations.Check;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "notifiche",
        indexes = {
                // supporta la GetAll: non lette prima, poi le altre in ordine di creazione DESC
                @Index(name = "idx_notifiche_destinatario_read_created", columnList = "id_destinatario, read_at, created_at DESC"),
                @Index(name = "idx_notifiche_canale", columnList = "id_canale")
        }
)
// Se il canale non e' valorizzato la notifica e' di sistema: il tipo puo' essere solo PERSONAL o ALL.
// Specularmente, se il canale e' valorizzato il tipo deve essere CANALE.
@Check(
        name = "ck_notifiche_canale_tipo",
        constraints = "(id_canale IS NULL AND tipo IN ('PERSONAL', 'ALL')) OR (id_canale IS NOT NULL AND tipo = 'CANALE')"
)
@Getter
@Setter
@NoArgsConstructor
public class Notifica {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /** Utente che riceve la notifica. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_destinatario", nullable = false, foreignKey = @ForeignKey(name = "fk_notifiche_destinatario"))
    private Utente destinatario;

    /** Valorizzato solo per le notifiche di canale; null per quelle di sistema. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_canale", foreignKey = @ForeignKey(name = "fk_notifiche_canale"))
    private Canale canale;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNotifica tipo;

    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Null finche' la notifica non viene letta. */
    @Column(name = "read_at")
    private Instant readAt;

    /** Notifica di sistema: PERSONAL verso un singolo utente oppure ALL. */
    public Notifica(Utente destinatario, TipoNotifica tipo, String message) {
        this.destinatario = destinatario;
        this.tipo = tipo;
        this.message = message;
    }

    /** Notifica di canale: il tipo e' implicitamente CANALE. */
    public Notifica(Utente destinatario, Canale canale, String message) {
        this.destinatario = destinatario;
        this.canale = canale;
        this.tipo = TipoNotifica.CANALE;
        this.message = message;
    }

    public boolean isLetta() {
        return readAt != null;
    }
}
