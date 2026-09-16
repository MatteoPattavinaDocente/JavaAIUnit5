package it.epicode.demo.chiavi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Che cosa registriamo (slide 43). Senza questi dati non e' possibile
 * rispondere alla domanda «perche' la fattura e' cresciuta».
 *
 * Il testo dell'utente NON c'e': si registra solo se serve e se e' consentito.
 */
@Entity
@Table(name = "chiamate_llm",
		indexes = @Index(name = "ix_chiamate_utente_giorno", columnList = "utente, giorno"))
public class ChiamataLlm {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** Chi ha chiesto la generazione: per attribuire consumo e responsabilita'. */
	@Column(nullable = false, length = 60)
	private String utente;

	/** Il giorno, per il conteggio giornaliero: indicizzato insieme all'utente. */
	@Column(nullable = false)
	private LocalDate giorno;

	/** Il costo per token dipende dal modello scelto: va registrato anche quello. */
	@Column(nullable = false, length = 60)
	private String modello;

	@Column(name = "token_ingresso", nullable = false)
	private int tokenIngresso;

	@Column(name = "token_uscita", nullable = false)
	private int tokenUscita;

	/** Il motivo di arresto, oppure il codice di errore restituito dal servizio. */
	@Column(nullable = false, length = 40)
	private String esito;

	/** Il tempo della chiamata ESTERNA, separato dal tempo della nostra richiesta. */
	@Column(name = "durata_ms", nullable = false)
	private long durataMs;

	@Column(nullable = false)
	private Instant istante;

	protected ChiamataLlm() {
	}

	public ChiamataLlm(String utente, String modello, int tokenIngresso, int tokenUscita,
			String esito, long durataMs) {
		this.utente = utente;
		this.modello = modello;
		this.tokenIngresso = tokenIngresso;
		this.tokenUscita = tokenUscita;
		this.esito = esito;
		this.durataMs = durataMs;
		this.istante = Instant.now();
		this.giorno = LocalDate.now(ZoneId.systemDefault());
	}

	public Long getId() {
		return id;
	}

	public String getUtente() {
		return utente;
	}

	public LocalDate getGiorno() {
		return giorno;
	}

	public String getModello() {
		return modello;
	}

	public int getTokenIngresso() {
		return tokenIngresso;
	}

	public int getTokenUscita() {
		return tokenUscita;
	}

	public int getTokenTotali() {
		return tokenIngresso + tokenUscita;
	}

	public String getEsito() {
		return esito;
	}

	public long getDurataMs() {
		return durataMs;
	}

	public Instant getIstante() {
		return istante;
	}
}
