package it.epicode.demo.chiavi.service;

/** Il limite giornaliero dell'utente, non quello del servizio esterno. */
public class LimiteSuperatoException extends RuntimeException {

	private final int limite;
	private final int usati;

	public LimiteSuperatoException(int limite, int usati) {
		super("limite giornaliero superato");
		this.limite = limite;
		this.usati = usati;
	}

	public int limite() {
		return limite;
	}

	public int usati() {
		return usati;
	}
}
