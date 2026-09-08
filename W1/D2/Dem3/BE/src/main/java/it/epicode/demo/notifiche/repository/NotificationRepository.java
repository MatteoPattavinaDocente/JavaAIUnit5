package it.epicode.demo.notifiche.repository;

import it.epicode.demo.notifiche.model.Notification;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Il repository e' il punto in cui si parla con il database.
 *
 * E' un'interfaccia senza implementazione: non la scriviamo noi.
 * Spring Data legge i nomi dei metodi all'avvio e genera lui il codice SQL.
 * Estendendo JpaRepository otteniamo gratis save, findById, delete, findAll...
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	// Il nome del metodo E' la query: "find By Recipient" diventa
	// SELECT * FROM notifications WHERE recipient = ?
	// Il parametro Pageable aggiunge da solo LIMIT e OFFSET, cosi' non carichiamo
	// in memoria mille notifiche per mostrarne cinque.
	Page<Notification> findByRecipient(String recipient, Pageable pageable);

	// Stessa regola: "count By Recipient And ReadAt Is Null" diventa
	// SELECT count(*) FROM notifications WHERE recipient = ? AND read_at IS NULL
	//
	// Il numero di non lette lo CHIEDIAMO al database ogni volta, invece di tenere
	// una colonna contatore. Un contatore andrebbe aggiornato a mano a ogni lettura,
	// e prima o poi qualcuno se ne dimentica: da quel momento il badge mente.
	long countByRecipientAndReadAtIsNull(String recipient);

	// Qui il nome non basta piu' e la query la scriviamo noi.
	// E' scritta in JPQL, che assomiglia a SQL ma lavora sulle classi Java
	// (Notification, n.readAt) invece che sulle tabelle.
	//
	// Il punto: "segna tutte lette" e' UNA sola UPDATE che tocca N righe,
	// non N cicli di lettura + salvataggio. Su cento notifiche e' cento volte piu' veloce.
	//
	// @Modifying avvisa Spring Data che questa query scrive: senza, verrebbe eseguita
	// come se fosse una SELECT e fallirebbe.
	@Modifying
	@Query("UPDATE Notification n SET n.readAt = :now WHERE n.recipient = :recipient AND n.readAt IS NULL")
	int markAllRead(String recipient, Instant now);
}
