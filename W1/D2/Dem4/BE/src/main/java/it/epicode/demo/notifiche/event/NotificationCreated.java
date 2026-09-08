package it.epicode.demo.notifiche.event;

/**
 * L'annuncio interno "e' nata una notifica".
 *
 * Non e' un messaggio che esce dall'applicazione: resta dentro Spring, e serve
 * a disaccoppiare chi crea la notifica (il service) da chi la spedisce sul canale
 * (il listener). Il service non sa nemmeno che esiste un WebSocket.
 *
 * Perche' dentro c'e' solo l'id e non tutta la notifica?
 * Perche' il listener parte dopo che la transazione e' stata chiusa. A quel punto
 * un oggetto Hibernate caricato prima potrebbe non essere piu' utilizzabile, quindi
 * tanto vale portarsi dietro l'id e rileggere la riga.
 */
public record NotificationCreated(Long notificationId) {
}
