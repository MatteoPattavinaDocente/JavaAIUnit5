package it.epicode.demo.chat.model;

/**
 * Tre momenti distinti (slide 23):
 *
 *   INVIATO    il server ha salvato il messaggio e lo ha pubblicato
 *   CONSEGNATO il client del destinatario ha ricevuto il frame e lo ha detto
 *   LETTO      il destinatario ha mostrato il messaggio sullo schermo
 *
 * Il salto da CONSEGNATO a LETTO lo dichiara il CLIENT: il server non puo'
 * sapere che cosa e' visibile su uno schermo.
 */
public enum StatoMessaggio {
	INVIATO,
	CONSEGNATO,
	LETTO
}
