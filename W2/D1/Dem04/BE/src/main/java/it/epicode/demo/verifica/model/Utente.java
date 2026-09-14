package it.epicode.demo.verifica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "utenti")
public class Utente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// L'unicita' la garantisce il vincolo della tabella, non un controllo nel
	// servizio: fra la lettura e la scrittura ci puo' stare un'altra richiesta.
	@Column(nullable = false, unique = true)
	private String email;

	// La demo di oggi e' sul token, non sulle credenziali: qui c'e' un digest
	// con sale, non la password in chiaro. In un progetto vero si usa
	// PasswordEncoder di Spring Security, che arriva piu' avanti nel corso.
	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String sale;

	@Column(nullable = false)
	private boolean verificato;

	@Column(name = "creato_il", nullable = false)
	private Instant creatoIl;

	protected Utente() {
	}

	public Utente(String email, String passwordHash, String sale) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.sale = sale;
		this.verificato = false;
		this.creatoIl = Instant.now();
	}

	public void verifica() {
		this.verificato = true;
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getSale() {
		return sale;
	}

	public boolean isVerificato() {
		return verificato;
	}

	public Instant getCreatoIl() {
		return creatoIl;
	}
}
