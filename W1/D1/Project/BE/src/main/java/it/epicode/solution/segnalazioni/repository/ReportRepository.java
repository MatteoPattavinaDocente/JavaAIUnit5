package it.epicode.solution.segnalazioni.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import it.epicode.solution.segnalazioni.domain.Category;
import it.epicode.solution.segnalazioni.domain.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

	/**
	 * Segnalazioni dentro il viewport. La categoria e' opzionale (null = tutte).
	 * Caso normale: la longitudine sta fra swLng e neLng.
	 */
	@Query("""
			SELECT r FROM Report r
			WHERE r.latitude BETWEEN :swLat AND :neLat
			  AND r.longitude BETWEEN :swLng AND :neLng
			  AND (:category IS NULL OR r.category = :category)
			ORDER BY r.createdAt DESC
			""")
	List<Report> findInViewport(
			@Param("swLat") BigDecimal swLat,
			@Param("swLng") BigDecimal swLng,
			@Param("neLat") BigDecimal neLat,
			@Param("neLng") BigDecimal neLng,
			@Param("category") Category category);

	/**
	 * Variante per viewport che attraversa l'antimeridiano: il filtro sulla longitudine
	 * diventa una OR fra i due spicchi.
	 */
	@Query("""
			SELECT r FROM Report r
			WHERE r.latitude BETWEEN :swLat AND :neLat
			  AND (r.longitude >= :swLng OR r.longitude <= :neLng)
			  AND (:category IS NULL OR r.category = :category)
			ORDER BY r.createdAt DESC
			""")
	List<Report> findInViewportCrossingAntimeridian(
			@Param("swLat") BigDecimal swLat,
			@Param("swLng") BigDecimal swLng,
			@Param("neLat") BigDecimal neLat,
			@Param("neLng") BigDecimal neLng,
			@Param("category") Category category);
}
