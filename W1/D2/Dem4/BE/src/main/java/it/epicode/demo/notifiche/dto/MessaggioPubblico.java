package it.epicode.demo.notifiche.dto;

import java.time.Instant;

/**
 * Quello che esce su /topic/{nome}.
 *
 * Non e' un NotificationDto e non passa dal database. E' la differenza da dire ad
 * alta voce a lezione: UN TOPIC NON HA STORICO.
 *
 * Chi non e' collegato in quel momento non lo riceve e non ha nessun modo di
 * recuperarlo dopo. La coda personale invece e' appoggiata alla tabella notifications:
 * quella si puo' sempre rileggere.
 *
 * Quando scegliete fra topic e coda personale, la domanda giusta e':
 * "se il destinatario non c'era, deve poterlo ritrovare?"
 */
public record MessaggioPubblico(String topic, String mittente, String testo, Instant inviatoIl) {
}
