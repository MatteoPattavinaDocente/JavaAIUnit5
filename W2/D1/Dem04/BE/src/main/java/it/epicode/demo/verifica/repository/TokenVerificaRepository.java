package it.epicode.demo.verifica.repository;

import it.epicode.demo.verifica.model.TokenVerifica;
import it.epicode.demo.verifica.model.Utente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TokenVerificaRepository extends JpaRepository<TokenVerifica, Long> {

	// La ricerca avviene sempre sul valore, che e' indicizzato e unico.
	// La JOIN FETCH evita la seconda query per l'utente.
	@Query("select t from TokenVerifica t join fetch t.utente where t.valore = :valore")
	Optional<TokenVerifica> trovaPerValore(String valore);

	List<TokenVerifica> findByUtenteAndUsatoIlIsNull(Utente utente);

	List<TokenVerifica> findByUtenteOrderByIdDesc(Utente utente);
}
