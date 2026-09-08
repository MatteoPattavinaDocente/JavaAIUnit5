package it.epicode.demo.notifiche.config;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * IL PROBLEMA: come fa il server a sapere chi c'e' dall'altra parte di una WebSocket?
 *
 * In HTTP ogni richiesta si porta dietro i suoi header e Spring Security ci dice
 * chi e' l'utente. Una WebSocket invece si apre una volta e poi resta li': quello
 * che sappiamo di chi si e' collegato, lo dobbiamo catturare in quel momento.
 *
 * L'unico momento utile e' l'handshake, cioe' la richiesta HTTP iniziale con cui
 * il browser chiede di passare a WebSocket. Un HandshakeInterceptor ci fa entrare
 * proprio li' in mezzo.
 *
 * COSA FACCIAMO QUI
 * Il frontend si collega a  ws://localhost:8080/ws?utente=mario
 * Noi leggiamo "mario" dall'indirizzo e lo scriviamo negli attributi della sessione,
 * che e' una piccola mappa che resta attaccata alla sessione per tutta la sua vita.
 * Da li' l'handler potra' rileggerla ogni volta che serve.
 *
 * ATTENZIONE, NON FATELO IN PRODUZIONE
 * Il nome nella query string e' un ripiego didattico: finisce nei log del server e
 * del proxy, e soprattutto chiunque puo' scrivere ?utente=lucia e ricevere le notifiche
 * di lucia. Non c'e' nessuna verifica. In un'applicazione vera qui ci sarebbe un token
 * da validare. Nella Dem 4 il nome arrivera' in un'intestazione del frame CONNECT,
 * che e' gia' un posto piu' sensato.
 */
@Component
public class UtenteHandshakeInterceptor implements HandshakeInterceptor {

	// La chiave con cui salviamo e rileggiamo il nome. E' una costante perche'
	// la scrive questa classe e la legge l'handler: se fosse una stringa scritta
	// due volte, basterebbe un refuso per rompere tutto in silenzio.
	public static final String ATTRIBUTO_UTENTE = "utente";

	private static final Logger log = LoggerFactory.getLogger(UtenteHandshakeInterceptor.class);

	/**
	 * Chiamato PRIMA che la connessione venga accettata.
	 * Restituire false qui rifiuterebbe la connessione.
	 */
	@Override
	public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler handler, Map<String, Object> attributes) {
		// Estrae il valore di ?utente= dall'indirizzo della richiesta.
		String utente = UriComponentsBuilder.fromUri(request.getURI())
				.build()
				.getQueryParams()
				.getFirst(ATTRIBUTO_UTENTE);

		if (utente == null || utente.isBlank()) {
			// Scelta didattica: lasciamo passare comunque. La sessione resta senza nome,
			// riceve gli annunci a tutti ma non le notifiche personali, e nessuno se ne
			// accorge finche' non ci si chiede perche' non arriva niente.
			// E' esattamente il tipo di errore muto che rende difficile il WebSocket nudo.
			log.warn("handshake senza ?utente=: sessione anonima, le notifiche personali non arriveranno");
			return true;
		}

		// La mappa "attributes" diventa session.getAttributes() dentro l'handler.
		attributes.put(ATTRIBUTO_UTENTE, utente);
		return true;
	}

	/** Chiamato dopo l'handshake. A noi non serve fare niente: da qui in poi comanda l'handler. */
	@Override
	public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
			WebSocketHandler handler, Exception exception) {
	}
}
