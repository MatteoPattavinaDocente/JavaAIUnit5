package it.epicode.demo.verifica.repository;

import it.epicode.demo.verifica.model.Utente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtenteRepository extends JpaRepository<Utente, Long> {

	Optional<Utente> findByEmail(String email);

	List<Utente> findAllByOrderByCreatoIlDesc();
}
