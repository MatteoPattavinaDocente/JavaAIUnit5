package it.epicode.solution.segnalazioni.web;

import java.math.BigDecimal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.epicode.solution.segnalazioni.dto.GeocodeResponse;
import it.epicode.solution.segnalazioni.geocoding.GeocodingService;

/** Il geocoding passa dal server: il frontend non conosce la chiave usata qui. */
@RestController
@RequestMapping("/api/geocode")
public class GeocodeController {

	private final GeocodingService geocodingService;

	public GeocodeController(GeocodingService geocodingService) {
		this.geocodingService = geocodingService;
	}

	/** GET /api/geocode?address=Via Roma 1, Milano */
	@GetMapping
	public GeocodeResponse geocode(@RequestParam String address) {
		return geocodingService.geocode(address);
	}

	/** GET /api/geocode/reverse?latitude=&longitude= */
	@GetMapping("/reverse")
	public GeocodeResponse reverse(@RequestParam BigDecimal latitude, @RequestParam BigDecimal longitude) {
		return geocodingService.reverseGeocode(latitude, longitude);
	}
}
