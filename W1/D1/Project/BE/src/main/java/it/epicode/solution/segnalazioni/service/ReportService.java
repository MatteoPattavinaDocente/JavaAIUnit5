package it.epicode.solution.segnalazioni.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.epicode.solution.segnalazioni.domain.Category;
import it.epicode.solution.segnalazioni.domain.Report;
import it.epicode.solution.segnalazioni.dto.CreateReportRequest;
import it.epicode.solution.segnalazioni.dto.ReportResponse;
import it.epicode.solution.segnalazioni.dto.Viewport;
import it.epicode.solution.segnalazioni.geocoding.GeocodingException;
import it.epicode.solution.segnalazioni.geocoding.GeocodingService;
import it.epicode.solution.segnalazioni.repository.ReportRepository;

@Service
public class ReportService {

	private static final Logger log = LoggerFactory.getLogger(ReportService.class);

	private final ReportRepository repository;
	private final GeocodingService geocodingService;

	public ReportService(ReportRepository repository, GeocodingService geocodingService) {
		this.repository = repository;
		this.geocodingService = geocodingService;
	}

	/** Solo le segnalazioni nel riquadro visibile: non "tutti i post". */
	@Transactional(readOnly = true)
	public List<ReportResponse> findInViewport(Viewport viewport, Category category) {
		List<Report> found = viewport.crossesAntimeridian()
				? repository.findInViewportCrossingAntimeridian(
						viewport.swLat(), viewport.swLng(), viewport.neLat(), viewport.neLng(), category)
				: repository.findInViewport(
						viewport.swLat(), viewport.swLng(), viewport.neLat(), viewport.neLng(), category);
		return found.stream().map(ReportResponse::from).toList();
	}

	@Transactional
	public ReportResponse create(CreateReportRequest request) {
		Report report = new Report(
				request.category(),
				request.description(),
				request.latitude(),
				request.longitude(),
				request.address());

		if (report.getAddress() == null) {
			report.setAddress(resolveAddress(request));
		}

		return ReportResponse.from(repository.save(report));
	}

	/**
	 * Reverse geocoding del punto inserito: dalle coordinate ricaviamo l'indirizzo
	 * testuale da mostrare nella lista e nell'InfoWindow.
	 *
	 * Passa dalla cache simulata (tabella geocoding_cache, TTL 30 giorni) con
	 * chiave "lat,lng" a scala 6: dieci segnalazioni sullo stesso incrocio costano
	 * una sola chiamata a Google, non dieci.
	 *
	 * Best effort - se Google non risponde la segnalazione viene salvata comunque,
	 * con address null.
	 */
	private String resolveAddress(CreateReportRequest request) {
		try {
			return geocodingService.reverseGeocode(request.latitude(), request.longitude()).formattedAddress();
		}
		catch (GeocodingException ex) {
			log.warn("Reverse geocoding non riuscito ({}), salvo senza indirizzo", ex.getMessage());
			return null;
		}
	}
}
