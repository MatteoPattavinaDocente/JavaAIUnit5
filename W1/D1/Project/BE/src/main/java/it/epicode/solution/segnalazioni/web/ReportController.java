package it.epicode.solution.segnalazioni.web;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.solution.segnalazioni.domain.Category;
import it.epicode.solution.segnalazioni.dto.CreateReportRequest;
import it.epicode.solution.segnalazioni.dto.ReportResponse;
import it.epicode.solution.segnalazioni.dto.Viewport;
import it.epicode.solution.segnalazioni.service.ReportService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

	private final ReportService service;

	public ReportController(ReportService service) {
		this.service = service;
	}

	/**
	 * GET /api/reports?swLat=&swLng=&neLat=&neLng=[&category=]
	 * Risponde per viewport. I parametri sono obbligatori: il record Viewport li valida.
	 */
	@GetMapping
	public List<ReportResponse> byViewport(
			@RequestParam BigDecimal swLat,
			@RequestParam BigDecimal swLng,
			@RequestParam BigDecimal neLat,
			@RequestParam BigDecimal neLng,
			@RequestParam(required = false) Category category) {
		return service.findInViewport(new Viewport(swLat, swLng, neLat, neLng), category);
	}

	@PostMapping
	public ResponseEntity<ReportResponse> create(@Valid @RequestBody CreateReportRequest request) {
		ReportResponse created = service.create(request);
		return ResponseEntity.created(URI.create("/api/reports/" + created.id())).body(created);
	}
}
