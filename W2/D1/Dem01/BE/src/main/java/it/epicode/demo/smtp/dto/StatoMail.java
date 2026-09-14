package it.epicode.demo.smtp.dto;

/** Quello che l'applicazione sa del server di posta, letto dal bean e non dal file. */
public record StatoMail(
		String host,
		int porta,
		String utenza,
		String connectionTimeout,
		String readTimeout,
		String writeTimeout,
		boolean raggiungibile,
		String errore) {
}
