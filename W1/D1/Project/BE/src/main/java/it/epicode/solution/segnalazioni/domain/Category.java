package it.epicode.solution.segnalazioni.domain;

/**
 * Categorie ammesse per una segnalazione.
 * I nomi coincidono con i valori accettati dal CHECK constraint su reports.category.
 */
public enum Category {
	BUCA,
	ILLUMINAZIONE,
	RIFIUTI,
	ALTRO
}
