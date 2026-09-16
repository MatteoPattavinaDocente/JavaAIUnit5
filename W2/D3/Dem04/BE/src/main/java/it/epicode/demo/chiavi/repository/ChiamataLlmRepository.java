package it.epicode.demo.chiavi.repository;

import it.epicode.demo.chiavi.model.ChiamataLlm;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChiamataLlmRepository extends JpaRepository<ChiamataLlm, Long> {

	/**
	 * I token consumati oggi da un utente. Il conteggio lo fa il DATABASE:
	 * sommare in memoria richiederebbe di caricare tutte le righe.
	 *
	 * coalesce perche' senza righe la somma e' null, non zero.
	 */
	@Query("""
			select coalesce(sum(c.tokenIngresso + c.tokenUscita), 0)
			from ChiamataLlm c
			where c.utente = :utente and c.giorno = :giorno
			""")
	int tokenDelGiorno(String utente, LocalDate giorno);

	List<ChiamataLlm> findByUtenteOrderByIdDesc(String utente, Limit limite);

	List<ChiamataLlm> findAllByOrderByIdDesc(Limit limite);
}
