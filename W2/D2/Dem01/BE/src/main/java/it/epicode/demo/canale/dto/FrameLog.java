package it.epicode.demo.canale.dto;

import java.time.Instant;

/** Quello che il client mostra nel riquadro dei frame ricevuti. */
public record FrameLog(Instant istante, String mittente, String destinazione, String testo) {
}
