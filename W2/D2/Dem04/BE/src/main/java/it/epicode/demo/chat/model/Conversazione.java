package it.epicode.demo.chat.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Una conversazione fra due utenti.
 *
 * I due nomi sono conservati in ordine alfabetico (uno &lt; altro): cosi' la
 * coppia (anna, bruno) e la coppia (bruno, anna) sono la STESSA riga, e il
 * vincolo di unicita' del database lo garantisce senza bisogno di controlli
 * nel servizio.
 */
@Entity
@Table(name = "conversazioni",
		uniqueConstraints = @UniqueConstraint(name = "uq_conversazione", columnNames = { "uno", "altro" }))
public class Conversazione {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 60)
	private String uno;

	@Column(nullable = false, length = 60)
	private String altro;

	protected Conversazione() {
	}

	private Conversazione(String uno, String altro) {
		this.uno = uno;
		this.altro = altro;
	}

	/** Costruisce la coppia sempre nello stesso ordine, qualunque sia l'ordine dei parametri. */
	public static Conversazione fra(String a, String b) {
		return a.compareTo(b) <= 0 ? new Conversazione(a, b) : new Conversazione(b, a);
	}

	public static String primo(String a, String b) {
		return a.compareTo(b) <= 0 ? a : b;
	}

	public static String secondo(String a, String b) {
		return a.compareTo(b) <= 0 ? b : a;
	}

	/** L'altro partecipante, visto da chi guarda. */
	public String controparteDi(String utente) {
		return uno.equals(utente) ? altro : uno;
	}

	public boolean partecipa(String utente) {
		return uno.equals(utente) || altro.equals(utente);
	}

	public Long getId() {
		return id;
	}

	public String getUno() {
		return uno;
	}

	public String getAltro() {
		return altro;
	}
}
