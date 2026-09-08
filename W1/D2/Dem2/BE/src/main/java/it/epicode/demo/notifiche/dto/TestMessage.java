package it.epicode.demo.notifiche.dto;

/**
 * Il messaggio che viaggia sul canale in questa demo: un campo di testo e nient'altro.
 *
 * E' volutamente povero. Serve solo a far vedere che qualcosa attraversa il tubo:
 * il dominio vero (le notifiche) entra nel canale solo dalla Dem 3.
 */
public record TestMessage(String text) {
}
