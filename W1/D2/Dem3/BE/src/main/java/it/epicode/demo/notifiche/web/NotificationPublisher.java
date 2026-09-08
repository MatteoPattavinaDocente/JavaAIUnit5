package it.epicode.demo.notifiche.web;

import it.epicode.demo.notifiche.dto.Busta;
import it.epicode.demo.notifiche.dto.NotificationDto;
import org.springframework.stereotype.Component;

/**
 * Le tre destinazioni, con un nome leggibile davanti.
 *
 * Questa classe non fa niente di suo: gira le chiamate all'handler. Esiste per
 * dare un nome alle tre possibilita' e per essere l'unico punto che costruisce
 * le buste, cosi' chi pubblica (listener, controller) non deve sapere come sono fatte.
 *
 * Confronto con la Dem 4: la' al posto dell'handler c'e' SimpMessagingTemplate,
 * un oggetto che Spring ci mette a disposizione gia' pronto, e il "dove" non e'
 * piu' un campo dentro il messaggio ma una destinazione vera del protocollo.
 */
@Component
public class NotificationPublisher {

	private final NotificationWebSocketHandler canale;

	public NotificationPublisher(NotificationWebSocketHandler canale) {
		this.canale = canale;
	}

	/** A tutti i collegati, chiunque siano. */
	public void broadcast(NotificationDto dto) {
		canale.aTutti(Busta.tutti(dto));
	}

	/**
	 * A chi ha chiesto di seguire un ordine, cioe' a chi ha mandato
	 * {"azione":"segui","ordine":42} e di cui ci siamo segnati la richiesta.
	 */
	public void perOrdine(Long ordineId, NotificationDto dto) {
		canale.aChiSegue(ordineId, Busta.ordine(ordineId, dto));
	}

	/**
	 * A un utente solo.
	 *
	 * Il nome e' quello arrivato nella query dell'handshake. Se non corrisponde a
	 * nessuna sessione aperta non c'e' niente da consegnare e la notifica resta
	 * soltanto sul database: la ritrovera' nello storico quando si ricollega.
	 */
	public void personale(String username, NotificationDto dto) {
		canale.aUtente(username, Busta.personale(dto));
	}
}
