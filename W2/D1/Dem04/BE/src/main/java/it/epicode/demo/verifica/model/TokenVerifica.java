package it.epicode.demo.verifica.model;

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
import java.time.Instant;

/**
 * I campi sono quattro: valore, utente, scadenza e momento dell'uso (slide 38).
 * Un utente puo' avere piu' token nel tempo, quindi la relazione e' molti a uno
 * verso l'utente.
 */
@Entity
@Table(name = "token_verifica",
		indexes = @Index(name = "ix_token_valore", columnList = "valore", unique = true))
public class TokenVerifica {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// La ricerca avviene sempre su questo campo: indicizzato e unico (slide 38).
	@Column(nullable = false, unique = true, length = 64)
	private String valore;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "utente_id", nullable = false)
	private Utente utente;

	@Column(nullable = false)
	private Instant scadenza;

	// Null finche' il token non e' stato consumato: il secondo clic non deve
	// riattivare nulla (slide 41).
	@Column(name = "usato_il")
	private Instant usatoIl;

	protected TokenVerifica() {
	}

	public TokenVerifica(String valore, Utente utente, Instant scadenza) {
		this.valore = valore;
		this.utente = utente;
		this.scadenza = scadenza;
	}

	public boolean scaduto(Instant adesso) {
		return scadenza.isBefore(adesso);
	}

	public boolean usato() {
		return usatoIl != null;
	}

	public void consuma(Instant adesso) {
		this.usatoIl = adesso;
	}

	/** Il rinvio invalida i token precedenti dello stesso utente (slide 41). */
	public void invalida(Instant adesso) {
		if (this.usatoIl == null) {
			this.usatoIl = adesso;
		}
	}

	public Long getId() {
		return id;
	}

	public String getValore() {
		return valore;
	}

	public Utente getUtente() {
		return utente;
	}

	public Instant getScadenza() {
		return scadenza;
	}

	public Instant getUsatoIl() {
		return usatoIl;
	}
}
