package it.epicode.demo.allegati.service;

import java.util.List;

/** Un file non valido fa rifiutare l'intero invio: la scelta e' spiegata nella GUIDA. */
public class RejectedFileException extends RuntimeException {

    private final List<String> reasons;

    public RejectedFileException(List<String> reasons) {
        super("Invio rifiutato");
        this.reasons = reasons;
    }

    public List<String> getReasons() {
        return reasons;
    }
}
