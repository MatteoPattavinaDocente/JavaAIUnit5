package it.epicode.demo.messaggi.model;

/**
 * Tre momenti distinti, non tre sinonimi (slide 23).
 *
 *   INVIATO    il server ha salvato il messaggio e lo ha pubblicato
 *   CONSEGNATO il client del destinatario lo ha ricevuto dal canale
 *   LETTO      il destinatario lo ha visto sullo schermo
 *
 * Qui usiamo i primi due: la conferma di lettura arriva nella demo 4.
 */
public enum StatoMessaggio {
	INVIATO,
	CONSEGNATO,
	LETTO
}
