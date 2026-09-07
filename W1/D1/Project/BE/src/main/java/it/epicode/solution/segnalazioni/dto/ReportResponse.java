package it.epicode.solution.segnalazioni.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import it.epicode.solution.segnalazioni.domain.Category;
import it.epicode.solution.segnalazioni.domain.Report;

public record ReportResponse(
		Long id,
		Category category,
		String description,
		BigDecimal latitude,
		BigDecimal longitude,
		String address,
		OffsetDateTime createdAt
) {

	public static ReportResponse from(Report report) {
		return new ReportResponse(
				report.getId(),
				report.getCategory(),
				report.getDescription(),
				report.getLatitude(),
				report.getLongitude(),
				report.getAddress(),
				report.getCreatedAt());
	}
}
