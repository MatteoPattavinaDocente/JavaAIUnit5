package it.epicode.demo.mappe.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

// L'indice composito e' dichiarato qui: con ddl-auto=update lo crea Hibernate.
@Entity
@Table(name = "posts", indexes = @Index(name = "idx_posts_lat_lng", columnList = "latitude, longitude"))
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String title;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	// @Embedded: le due colonne di GeoPoint finiscono nella tabella posts,
	// non in una tabella separata.
	@Embedded
	private GeoPoint location;

	protected Post() {
	}

	public Post(String title, GeoPoint location) {
		this.title = title;
		this.location = location;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public GeoPoint getLocation() {
		return location;
	}
}
