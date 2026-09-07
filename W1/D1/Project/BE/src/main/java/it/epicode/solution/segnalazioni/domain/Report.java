package it.epicode.solution.segnalazioni.domain;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "reports")
public class Report {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false, length = 32)
	private Category category;

	@Column(name = "description", nullable = false, length = 500)
	private String description;

	/** -90..90 con 6 decimali: precisione 8, scala 6 (~11 cm di risoluzione). */
	@Column(name = "latitude", nullable = false, precision = 8, scale = 6)
	private BigDecimal latitude;

	/** -180..180 con 6 decimali: precisione 9, scala 6. */
	@Column(name = "longitude", nullable = false, precision = 9, scale = 6)
	private BigDecimal longitude;

	/** Indirizzo testuale ricavato dal reverse geocoding: opzionale. */
	@Column(name = "address", length = 255)
	private String address;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	protected Report() {
		// richiesto da JPA
	}

	public Report(Category category, String description, BigDecimal latitude, BigDecimal longitude, String address) {
		this.category = category;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
		this.address = address;
		this.createdAt = OffsetDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public Category getCategory() {
		return category;
	}

	public String getDescription() {
		return description;
	}

	public BigDecimal getLatitude() {
		return latitude;
	}

	public BigDecimal getLongitude() {
		return longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
