package it.epicode.demo.notifiche.dto;

/**
 * Il contenuto del frame SEND quando si scrive su un topic.
 *
 * Notate che arriva il nome nudo ("ordini/42") e non la destinazione completa
 * ("/topic/ordini/42"): il prefisso lo mette il server. E' voluto. Se il client
 * potesse indicare la destinazione per intero, potrebbe provare a scrivere su
 * /queue o su /user, cioe' nelle code personali di qualcun altro.
 *
 * Regola generale: mai lasciar scegliere al client la destinazione completa.
 */
public record MessaggioTopic(String topic, String testo) {
}
