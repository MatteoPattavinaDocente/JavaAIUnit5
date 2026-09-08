package it.epicode.demo.notifiche.dto;

/**
 * Il contenuto del frame SEND quando un utente scrive a un altro utente.
 *
 * GUARDATE COSA MANCA: il mittente.
 *
 * Non e' una dimenticanza. Il mittente lo mette il server, prendendolo dal Principal
 * della sessione STOMP (quello messo da StompLoginInterceptor sul CONNECT).
 * Se fosse un campo di questo record, chiunque potrebbe firmarsi con il nome di
 * un altro semplicemente cambiando il JSON che manda.
 *
 * Vale sempre: l'identita' non si accetta dal client, si ricava dalla sessione.
 */
public record MessaggioUtente(String destinatario, String testo) {
}
