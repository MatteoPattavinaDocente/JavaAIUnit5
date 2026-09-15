package it.epicode.demo.chat.repository;

import it.epicode.demo.chat.model.Messaggio;
import it.epicode.demo.chat.model.StatoMessaggio;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MessaggioRepository extends JpaRepository<Messaggio, Long> {

	/**
	 * L'ultima pagina della conversazione: si legge una pagina per volta, non
	 * migliaia di righe (slide 43). Ordinata al contrario per prendere i piu'
	 * recenti, poi il servizio la rovescia.
	 */
	@Query("select m from Messaggio m where m.conversazione.id = :conversazione order by m.id desc")
	List<Messaggio> ultimi(Long conversazione, Limit limite);

	/**
	 * Il recupero dopo una riconnessione: solo quello che e' arrivato mentre la
	 * connessione era chiusa. Il broker non lo ripete (slide 38).
	 */
	@Query("select m from Messaggio m where m.conversazione.id = :conversazione and m.id > :dopo order by m.id asc")
	List<Messaggio> dopo(Long conversazione, Long dopo);

	/**
	 * Il conteggio dei non letti si calcola sul SERVER, perche' deve essere lo
	 * stesso su tutti i dispositivi dell'utente (slide 42).
	 */
	@Query("""
			select count(m) from Messaggio m
			where m.conversazione.id = :conversazione
			  and m.destinatario = :utente
			  and m.stato <> it.epicode.demo.chat.model.StatoMessaggio.LETTO
			""")
	long nonLetti(Long conversazione, String utente);

	@Query("select max(m.id) from Messaggio m where m.conversazione.id = :conversazione")
	Long ultimoId(Long conversazione);

	/**
	 * Segna letti in una sola istruzione: con cinquanta messaggi non letti,
	 * caricarli tutti per cambiare un campo sarebbe uno spreco.
	 *
	 * Restituisce gli id aggiornati? No: JPQL non lo permette. Per informare il
	 * mittente il servizio legge prima quali sono.
	 */
	@Modifying
	@Query("""
			update Messaggio m set m.stato = :stato
			where m.conversazione.id = :conversazione
			  and m.destinatario = :utente
			  and m.stato <> :stato
			""")
	int segnaLetti(Long conversazione, String utente, StatoMessaggio stato);

	@Query("""
			select m.id from Messaggio m
			where m.conversazione.id = :conversazione
			  and m.destinatario = :utente
			  and m.stato <> it.epicode.demo.chat.model.StatoMessaggio.LETTO
			""")
	List<Long> idNonLetti(Long conversazione, String utente);
}
