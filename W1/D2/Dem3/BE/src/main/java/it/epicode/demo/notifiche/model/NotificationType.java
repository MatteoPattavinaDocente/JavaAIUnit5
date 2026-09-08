package it.epicode.demo.notifiche.model;

/**
 * I tipi di notifica che il sistema sa produrre.
 * Sono un enum e non delle semplici stringhe perche' cosi' il compilatore
 * ci ferma subito se scriviamo un tipo che non esiste.
 */
public enum NotificationType {
	ORDER_SHIPPED,
	ORDER_CANCELLED,
	MESSAGE
}
