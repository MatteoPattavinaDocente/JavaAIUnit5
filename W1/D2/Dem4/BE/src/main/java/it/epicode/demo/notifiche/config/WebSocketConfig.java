package it.epicode.demo.notifiche.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * QUESTO E' IL FILE PIU' IMPORTANTE DELLA DEM 4.
 *
 * Confrontatelo mentalmente con la Dem 3: la' avevamo un handler di 140 righe che
 * teneva a mano l'elenco delle sessioni per utente, l'insieme degli ordini seguiti
 * da ciascuna, e una "busta" con dentro un campo canale per dire al client perche'
 * gli fosse arrivato un messaggio.
 *
 * Tutto quel codice qui NON ESISTE. Al suo posto c'e' questa configurazione, e il
 * lavoro lo fa un broker: un componente che tiene lui l'elenco di chi e' iscritto
 * a cosa e consegna di conseguenza.
 *
 * COS'E' STOMP
 * E' un protocollo di messaggi che gira DENTRO la WebSocket. La WebSocket resta un
 * tubo che trasporta stringhe; STOMP e' l'accordo su come sono fatte quelle stringhe.
 * Ogni messaggio diventa un "frame" con un comando e delle intestazioni:
 *   CONNECT     mi collego, e mi presento
 *   SUBSCRIBE   voglio ricevere quello che passa da /topic/ordini/42
 *   SEND        mando questo contenuto a questa destinazione
 *   MESSAGE     ecco qualcosa per te, arrivato da questa destinazione
 *   UNSUBSCRIBE non mi interessa piu'
 *
 * Il "canale" che nella Dem 3 ci eravamo messi dentro il contenuto, qui e'
 * un'intestazione vera del protocollo, che il client legge senza che noi facciamo nulla.
 *
 * Notate anche l'annotazione: @EnableWebSocketMessageBroker, non @EnableWebSocket.
 * Non registriamo piu' un handler nostro: registriamo un endpoint STOMP.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final String allowedOrigin;
	private final StompLoginInterceptor loginInterceptor;

	public WebSocketConfig(@Value("${app.cors.allowed-origin}") String allowedOrigin,
			StompLoginInterceptor loginInterceptor) {
		this.allowedOrigin = allowedOrigin;
		this.loginInterceptor = loginInterceptor;
	}

	/**
	 * Dove il client si collega.
	 *
	 * withSockJS() aggiunge, accanto al WebSocket vero, dei trasporti di ripiego
	 * costruiti sopra HTTP normale. Servono quando la WebSocket non passa: proxy
	 * aziendali, reti che bloccano l'upgrade, browser molto vecchi. Il client non
	 * se ne accorge: prova il WebSocket e, se fallisce, ripiega da solo.
	 *
	 * Conseguenza pratica: dal lato client l'indirizzo diventa http://localhost:8080/ws
	 * e non piu' ws://localhost:8080/ws. Sembra un dettaglio ma e' un errore classico.
	 */
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigin).withSockJS();
	}

	/**
	 * Le tre righe che sostituiscono l'handler della Dem 3.
	 */
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// (1) Il broker. "Simple" vuol dire: in memoria, dentro questo processo.
		// Tutto quello che va a una destinazione che inizia per /topic o /queue lo
		// smista lui, senza che noi scriviamo una riga.
		//
		// Per convenzione /topic = a molti (chi si iscrive riceve), /queue = personale.
		// In produzione, con piu' server, qui si mette un broker vero (RabbitMQ,
		// ActiveMQ) e questa riga diventa enableStompBrokerRelay.
		registry.enableSimpleBroker("/topic", "/queue");

		// (2) Le destinazioni che invece devono arrivare a CODICE NOSTRO.
		// Quando il client manda a /app/messaggi, il messaggio non va al broker:
		// va al metodo annotato @MessageMapping("/messaggi").
		// E' la porta d'ingresso per la logica applicativa.
		registry.setApplicationDestinationPrefixes("/app");

		// (3) Il prefisso "magico" dei messaggi personali.
		// Il server pubblica per l'utente mario su /queue/notifications, e Spring
		// riscrive quella destinazione in una privata, unica per la sua sessione.
		// Il client si iscrive a /user/queue/notifications e riceve solo la sua roba:
		// nessun altro puo' iscriversi alla coda di mario.
		//
		// Nella Dem 3 questo era la mappa perUtente piu' il metodo aUtente().
		registry.setUserDestinationPrefix("/user");
	}

	/**
	 * Tutto quello che arriva dai client passa da questo canale, e qui ci infiliamo
	 * il nostro interceptor per capire CHI e' la sessione.
	 *
	 * Nella Dem 3 il nome arrivava nella query string dell'handshake e finiva negli
	 * attributi della sessione. Qui arriva in un'intestazione del frame CONNECT,
	 * e diventa il Principal della sessione: lo stesso oggetto che userebbe
	 * Spring Security se ci fosse.
	 */
	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		registration.interceptors(loginInterceptor);
	}
}
