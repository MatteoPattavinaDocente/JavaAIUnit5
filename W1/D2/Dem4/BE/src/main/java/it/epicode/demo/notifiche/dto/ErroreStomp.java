package it.epicode.demo.notifiche.dto;

/**
 * Un rifiuto comunicato al solo mittente, su /user/queue/errors.
 *
 * PERCHE' NON USIAMO IL FRAME ERROR DI STOMP
 * Perche' quello, per specifica del protocollo, CHIUDE la connessione. Per un
 * permesso mancante e' una reazione sproporzionata: l'utente perderebbe anche
 * tutte le altre iscrizioni e dovrebbe ricollegarsi.
 *
 * Un normale messaggio su una coda personale invece lascia tutto in piedi, e lo
 * legge solo chi ha sbagliato.
 */
public record ErroreStomp(String messaggio, String destinazione) {
}
