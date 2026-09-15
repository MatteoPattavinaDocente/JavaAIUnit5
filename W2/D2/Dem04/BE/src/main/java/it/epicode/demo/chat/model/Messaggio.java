package it.epicode.demo.chat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "messaggi",
		indexes = {
				// La query piu' frequente: i messaggi di una conversazione in
				// ordine. L'indice copre entrambe le colonne dell'ORDER BY.
				@Index(name = "ix_messaggi_conv", columnList = "conversazione_id, id"),
				@Index(name = "ix_messaggi_non_letti", columnList = "conversazione_id, destinatario, stato")
		})
public class Messaggio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "conversazione_id", nullable = false)
	private Conversazione conversazione;

	@Column(nullable = false, length = 60)
	private String mittente;

	@Column(nullable = false, length = 60)
	private String destinatario;

	@Column(nullable = false, length = 2000)
	private String testo;

	// Assegnato dal server: l'orologio del client puo' essere sbagliato (slide 20).
	@Column(nullable = false)
	private Instant istante;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatoMessaggio stato;

	protected Messaggio() {
	}

	public Messaggio(Conversazione conversazione, String mittente, String destinatario, String testo) {
		this.conversazione = conversazione;
		this.mittente = mittente;
		this.destinatario = destinatario;
		this.testo = testo;
		this.istante = Instant.now();
		this.stato = StatoMessaggio.INVIATO;
	}

	public void consegnato() {
		if (stato == StatoMessaggio.INVIATO) {
			stato = StatoMessaggio.CONSEGNATO;
		}
	}

	/** Lo stato non torna indietro: letto resta letto. */
	public void letto() {
		stato = StatoMessaggio.LETTO;
	}

	public Long getId() {
		return id;
	}

	public Conversazione getConversazione() {
		return conversazione;
	}

	public String getMittente() {
		return mittente;
	}

	public String getDestinatario() {
		return destinatario;
	}

	public String getTesto() {
		return testo;
	}

	public Instant getIstante() {
		return istante;
	}

	public StatoMessaggio getStato() {
		return stato;
	}
}
