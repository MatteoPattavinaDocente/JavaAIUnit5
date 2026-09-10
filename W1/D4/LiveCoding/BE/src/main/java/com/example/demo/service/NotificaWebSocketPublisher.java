package com.example.demo.service;

import com.example.demo.dto.NotificaResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Consegna immediata delle notifiche via WebSocket.
 *
 * Il push avviene solo se il destinatario ha una sessione STOMP aperta; se e'
 * offline la notifica resta comunque a database e verra' letta dalla GET.
 */
@Service
public class NotificaWebSocketPublisher {

    /** Coda personale del destinatario: /user/queue/notifiche */
    private static final String CODA_PERSONALE = "/queue/notifiche";
    /** Topic di canale: /topic/canale/{idCanale} */
    private static final String TOPIC_CANALE = "/topic/canale/";

    private final SimpMessagingTemplate messagingTemplate;
    private final SimpUserRegistry userRegistry;

    public NotificaWebSocketPublisher(SimpMessagingTemplate messagingTemplate, SimpUserRegistry userRegistry) {
        this.messagingTemplate = messagingTemplate;
        this.userRegistry = userRegistry;
    }

    /**
     * @return true se il destinatario era connesso e il messaggio e' stato inoltrato.
     */
    public boolean inviaSeConnesso(UUID idDestinatario, NotificaResponse notifica) {
        if (!isConnesso(idDestinatario)) {
            return false;
        }
        messagingTemplate.convertAndSendToUser(idDestinatario.toString(), CODA_PERSONALE, notifica);
        return true;
    }

    /** Broadcast sul topic del canale, per i client iscritti a /topic/canale/{id}. */
    public void inviaSulTopicCanale(UUID idCanale, NotificaResponse notifica) {
        messagingTemplate.convertAndSend(TOPIC_CANALE + idCanale, notifica);
    }

    /** Aggiorna il contatore delle non lette sulla coda personale. */
    public void inviaConteggio(UUID idDestinatario, long nonLette) {
        if (isConnesso(idDestinatario)) {
            messagingTemplate.convertAndSendToUser(idDestinatario.toString(), "/queue/conteggio", nonLette);
        }
    }

    /** Il nome del principal della sessione STOMP e' l'id utente. */
    public boolean isConnesso(UUID idUtente) {
        return userRegistry.getUser(idUtente.toString()) != null;
    }
}
