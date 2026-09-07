package it.epicode.demo.mappe.web;

import it.epicode.demo.mappe.dto.CreatePostRequest;
import it.epicode.demo.mappe.dto.PostResponse;
import it.epicode.demo.mappe.service.PostService;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final PostService service;

	public PostController(PostService service) {
		this.service = service;
	}

	// In firma non compare mai Post: solo i due record. L'entity resta dietro al service.
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PostResponse create(@Valid @RequestBody CreatePostRequest request) {
		return service.create(request);
	}

	// I quattro bordi del riquadro visibile arrivano dalla mappa. Chiediamo al database
	// solo i post di quel rettangolo, non tutti quelli esistenti.
	@GetMapping
	public List<PostResponse> findInBounds(
			@RequestParam BigDecimal south,
			@RequestParam BigDecimal north,
			@RequestParam BigDecimal west,
			@RequestParam BigDecimal east) {
		return service.findInBounds(south, north, west, east);
	}
}
