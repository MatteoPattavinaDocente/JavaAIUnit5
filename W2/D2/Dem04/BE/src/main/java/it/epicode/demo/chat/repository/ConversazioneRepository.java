package it.epicode.demo.chat.repository;

import it.epicode.demo.chat.model.Conversazione;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConversazioneRepository extends JpaRepository<Conversazione, Long> {

	Optional<Conversazione> findByUnoAndAltro(String uno, String altro);

	@Query("select c from Conversazione c where c.uno = :utente or c.altro = :utente order by c.id")
	List<Conversazione> diUtente(String utente);
}
