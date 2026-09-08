package it.epicode.demo.notifiche.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Una notifica cosi' come viene salvata sul database.
 *
 * @Entity dice a Hibernate: "di questa classe tieni traccia sul database".
 * Ogni campo qui sotto diventa una colonna della tabella "notifications",
 * e ogni oggetto Notification diventa una riga.
 */
@Entity
@Table(
		name = "notifications",
		// Un indice e' un "indice analitico" del database: senza, per contare le notifiche
		// non lette di mario il database dovrebbe leggere tutta la tabella riga per riga.
		// Le nostre due query filtrano sempre per destinatario e stato di lettura: indicizziamo quelli.
		indexes = @Index(name = "idx_notifications_recipient_read", columnList = "recipient, read_at"))
public class Notification {

	// L'id non lo scegliamo noi: IDENTITY significa "lo genera il database
	// al momento dell'INSERT, contando 1, 2, 3...".
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// A chi e' destinata la notifica. Per ora e' solo un nome ("mario"):
	// in un'app vera qui ci sarebbe l'utente autenticato.
	@Column(nullable = false, length = 100)
	private String recipient;

	// EnumType.STRING salva sul database la parola "ORDER_SHIPPED".
	// L'alternativa (ORDINAL) salverebbe il numero 0, cioe' la posizione nell'enum:
	// basterebbe che qualcuno riordinasse le costanti e tutte le righe gia' salvate
	// cambierebbero significato. Con STRING questo non puo' succedere.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private NotificationType type;

	// L'id della cosa a cui la notifica si riferisce (per esempio l'ordine numero 42).
	// Puo' essere null: non tutte le notifiche parlano di qualcosa di preciso.
	@Column(name = "resource_id")
	private Long resourceId;

	// Il testo che l'utente legge nella campanella.
	@Column(nullable = false, length = 200)
	private String title;

	// updatable = false: la data di creazione si scrive una volta e poi non si tocca piu'.
	// Se per errore provassimo a cambiarla, Hibernate ignorerebbe la modifica.
	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	// Qui non usiamo un boolean "letta si'/no" ma la data in cui e' stata letta.
	// null significa "non ancora letta". Costa uguale e ci dice una cosa in piu':
	// non solo SE e' stata letta, ma anche QUANDO.
	@Column(name = "read_at")
	private Instant readAt;

	// Hibernate ha bisogno di un costruttore vuoto per ricostruire l'oggetto
	// quando legge una riga dal database. E' protected perche' serve a lui, non a noi.
	protected Notification() {
	}

	// Questo e' il costruttore che usiamo noi. Chiede tutto quello che serve
	// per avere una notifica valida: cosi' non e' possibile crearne una a meta'.
	public Notification(String recipient, NotificationType type, Long resourceId, String title) {
		this.recipient = recipient;
		this.type = type;
		this.resourceId = resourceId;
		this.title = title;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getRecipient() {
		return recipient;
	}

	public NotificationType getType() {
		return type;
	}

	public Long getResourceId() {
		return resourceId;
	}

	public String getTitle() {
		return title;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getReadAt() {
		return readAt;
	}

	// Non esiste un setReadAt pubblico: l'unico modo di modificare la notifica
	// e' questo metodo, che ha un nome che dice cosa sta succedendo.
	public void markRead(Instant when) {
		this.readAt = when;
	}
}
