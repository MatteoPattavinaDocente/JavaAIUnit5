package it.epicode.demo.canale.dto;

import java.util.List;

/**
 * Chi e' collegato adesso, secondo SimpUserRegistry (slide 12). Il registro
 * vale per il singolo nodo: con piu' istanze ognuna conosce solo i propri
 * collegati.
 */
public record Presenza(int utenti, int sessioni, List<UtenteCollegato> collegati) {

	public record UtenteCollegato(String nome, int sessioni) {
	}
}
