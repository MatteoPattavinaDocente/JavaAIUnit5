package it.epicode.demo.mappe.service;

import it.epicode.demo.mappe.dto.CreatePostRequest;
import it.epicode.demo.mappe.dto.PostResponse;
import it.epicode.demo.mappe.model.GeoPoint;
import it.epicode.demo.mappe.model.Post;
import it.epicode.demo.mappe.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostService {

	private final PostRepository repository;

	public PostService(PostRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public PostResponse create(CreatePostRequest request) {
		// Arrivati qui la richiesta e' gia' valida: il compact constructor ha gia'
		// rifiutato tutto il resto.
		GeoPoint location = new GeoPoint(request.latitude(), request.longitude());
		Post saved = repository.save(new Post(request.title(), location));
		return PostResponse.from(saved);
	}
}
