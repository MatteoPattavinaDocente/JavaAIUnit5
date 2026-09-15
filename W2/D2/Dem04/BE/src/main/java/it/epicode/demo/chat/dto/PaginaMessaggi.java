package it.epicode.demo.chat.dto;

import java.util.List;

/**
 * Una pagina di cronologia. `ultimoId` serve al client per chiedere, dopo una
 * riconnessione, solo quello che e' arrivato dopo (slide 38).
 */
public record PaginaMessaggi(Long conversazione, List<MessaggioRisposta> messaggi, Long ultimoId, long nonLetti) {
}
