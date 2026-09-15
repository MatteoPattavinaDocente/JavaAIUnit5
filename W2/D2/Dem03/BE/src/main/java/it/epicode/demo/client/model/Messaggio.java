package it.epicode.demo.client.model;

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
 * Il modello della slide 15. Il payload che arriva dal client contiene testo e
 * destinatario: tutto il resto lo decide il server.
 */
@Entity
@Table(name = "messaggi",
		indexes = {
				@Index(name = "ix_messaggi_destinatario", columnList = "destinatario, istante"),
				@Index(name = "ix_messaggi_mittente", columnList = "mittente, istante")
		})
public class Messaggio {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// Ricavato dal Principal della sessione, non dal contenuto inviato dal
	// client (slide 15).
	@Column(nullable = false, length = 60)
	private String mittente;

	@Column(nullable = false, length = 60)
	private String destinatario;

	// La lunghezza massima la decide il vincolo della colonna, non il client.
	@Column(nullable = false, length = 2000)
	private String testo;

	// Assegnato dal server: l'orologio del client puo' essere sbagliato, e con
	// due dispositivi non concordano fra loro (slide 15 e 20).
	@Column(nullable = false)
	private Instant istante;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StatoMessaggio stato;

	protected Messaggio() {
	}

	public Messaggio(String mittente, String destinatario, String testo) {
		this.mittente = mittente;
		this.destinatario = destinatario;
		this.testo = testo;
		this.istante = Instant.now();
		this.stato = StatoMessaggio.INVIATO;
	}

	public void consegnato() {
		this.stato = StatoMessaggio.CONSEGNATO;
	}

	public Long getId() {
		return id;
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
