package it.epicode.demo.client.repository;

import it.epicode.demo.client.model.Messaggio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessaggioRepository extends JpaRepository<Messaggio, Long> {

	/**
	 * La conversazione fra due utenti, nei due versi. L'ordine e' per istante
	 * del SERVER e, a pari istante, per id: con due messaggi nello stesso
	 * millisecondo l'istante da solo non basta a decidere chi viene prima
	 * (slide 20).
	 */
	@Query("""
			select m from Messaggio m
			where (m.mittente = :uno and m.destinatario = :altro)
			   or (m.mittente = :altro and m.destinatario = :uno)
			order by m.istante asc, m.id asc
			""")
	List<Messaggio> conversazione(String uno, String altro);
}
