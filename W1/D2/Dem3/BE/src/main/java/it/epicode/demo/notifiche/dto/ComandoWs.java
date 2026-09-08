package it.epicode.demo.notifiche.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Quello che il CLIENT puo' mandare al server sul canale. Tre azioni:
 *
 *   {"azione":"segui",    "ordine":42}                    -> voglio ricevere i messaggi dell'ordine 42
 *   {"azione":"smetti",   "ordine":42}                    -> non li voglio piu'
 *   {"azione":"pubblica", "ordine":42, "testo":"ciao"}    -> scrivo nell'ordine 42
 *
 * QUESTO E' UN PROTOCOLLO INVENTATO DA NOI.
 * Le prime due azioni sono una SUBSCRIBE fatta a mano, la terza e' una SEND.
 * Nessuno lo conosce se non lo documentiamo, e se il frontend scrive "seguii"
 * invece di "segui" il server ignora il comando senza dire niente.
 *
 * Guardate il campo testo: serve solo all'azione "pubblica", per le altre due e' null.
 * Un record con un campo che a volte non serve e' il primo segnale che il protocollo
 * fatto in casa sta crescendo male. In STOMP sarebbero due frame diversi
 * (SUBSCRIBE e SEND), ognuno con le sue intestazioni.
 *
 * @JsonIgnoreProperties(ignoreUnknown = true): se il client manda un campo in piu'
 * che non conosciamo, lo ignoriamo invece di far esplodere la lettura del JSON.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ComandoWs(String azione, Long ordine, String testo) {
}
