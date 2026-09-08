# Mappa dei componenti — Dem 1 → Dem 4

Le quattro demo della giornata sono **lo stesso programma**, con un pezzo in più ogni volta.
Il dominio (le notifiche) non cambia mai: cambia solo **come la notizia arriva al browser**.

Questa mappa risponde a tre domande:

- quali file esistono in ogni demo,
- chi chiama chi,
- **cosa cambia** rispetto alla demo precedente.

Le spiegazioni a parole sono in `DemN/spiegazione.md`, il flusso passo a passo in
`DemN/flusso.md`, i comandi per avviare e provare in `DemN/help.md`.

---

## 0. La progressione in una figura

```mermaid
flowchart LR
    D1["<b>Dem 1</b><br/>solo HTTP<br/>la notifica esiste,<br/>nessuno la spedisce"]
    D2["<b>Dem 2</b><br/>WebSocket nudo<br/>il tubo esiste,<br/>ma non porta niente di vero"]
    D3["<b>Dem 3</b><br/>WebSocket nudo + dominio<br/>consegna a un utente solo,<br/>circa 140 righe scritte a mano"]
    D4["<b>Dem 4</b><br/>STOMP + SockJS<br/>quelle 140 righe diventano<br/>un broker e tre destinazioni"]

    D1 -->|"aggiungi il canale"| D2
    D2 -->|"metti il dominio dentro il canale"| D3
    D3 -->|"sostituisci il codice con il protocollo"| D4

    subgraph invariato["Questo nucleo non cambia in nessuna delle quattro"]
        N["Notification (entity)<br/>NotificationType<br/>NotificationRepository<br/>NotificationService<br/>NotificationDto<br/>NotificationController<br/>DemoOrderController<br/>PostgreSQL"]
    end

    D1 -.-> N
    D2 -.-> N
    D3 -.-> N
    D4 -.-> N
```

**I colori dei diagrammi**

```mermaid
flowchart LR
    A["nuovo in questa demo"]:::nuovo
    B["modificato"]:::mod
    C["uguale alla demo precedente"]:::inv
    D["cancellato qui"]:::del
    E["ce lo dà Spring o una libreria"]:::infra

    classDef nuovo fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef mod fill:#fef9c3,stroke:#ca8a04,color:#713f12
    classDef inv fill:#f1f5f9,stroke:#94a3b8,color:#334155
    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
    classDef infra fill:#e0e7ff,stroke:#4f46e5,color:#312e81
```

---

## 1. Dem 1 — solo HTTP

Il dato c'è, il canale no. Tutto quello che si vede in pagina arriva da due GET,
e parte sempre dal browser.

```mermaid
flowchart TB
    subgraph FE["FE — React + Vite :5173, solo fetch"]
        direction TB
        main1["main.tsx<br/>monta App"]:::inv
        app1["App.tsx<br/>scelta utente + pulsante Spedisci"]:::inv
        camp1["Campanella.tsx<br/>badge, pannello,<br/>paginazione, segna letta"]:::inv
        hook1["useNotifiche.ts<br/>stato della pagina,<br/>ricarica solo se glielo chiedi"]:::inv
        api1["api.ts<br/>l unico file che conosce l URL"]:::inv
        css1["index.css"]:::inv
        main1 --> app1 --> camp1
        app1 --> hook1
        camp1 --> hook1
        hook1 --> api1
    end

    subgraph BE["BE — Spring Boot :8080"]
        direction TB
        boot1["NotificheApplication"]:::inv
        cors1["config/CorsConfig<br/>autorizza le fetch da :5173"]:::inv
        nc1["web/NotificationController<br/>GET /api/notifications<br/>GET /unread-count<br/>PATCH /-id-/read<br/>POST /read-all"]:::inv
        doc1["web/DemoOrderController<br/>POST /api/demo/orders/-id-/ship<br/>il telecomando della lezione"]:::inv
        svc1["service/NotificationService<br/>notify, list, unreadCount,<br/>markRead, markAllRead"]:::inv
        repo1["repository/NotificationRepository<br/>i nomi dei metodi sono le query"]:::inv
        ent1["model/Notification<br/>readAt null = non letta"]:::inv
        enum1["model/NotificationType<br/>ORDER_SHIPPED / ORDER_CANCELLED / MESSAGE"]:::inv
        dto1["dto/NotificationDto<br/>from(entity): readAt diventa read"]:::inv
        yml1["application.yml<br/>database, ddl-auto update, log SQL"]:::inv

        boot1 --> cors1
        nc1 --> svc1
        doc1 --> svc1
        svc1 --> repo1
        svc1 --> dto1
        nc1 --> dto1
        repo1 --> ent1
        ent1 --> enum1
        dto1 --> ent1
    end

    DB[("PostgreSQL :5432<br/>tabella notifications<br/>creata da Hibernate all avvio")]:::infra

    api1 -->|"HTTP / JSON"| nc1
    api1 -->|"HTTP"| doc1
    repo1 -->|"Hibernate / JDBC"| DB

    BUCO["Il buco della Dem 1:<br/>nessuno avvisa il browser.<br/>La riga esiste, ma la pagina cambia<br/>solo se premi Aggiorna"]:::del
    doc1 -.-> BUCO

    classDef nuovo fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef mod fill:#fef9c3,stroke:#ca8a04,color:#713f12
    classDef inv fill:#f1f5f9,stroke:#94a3b8,color:#334155
    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
    classDef infra fill:#e0e7ff,stroke:#4f46e5,color:#312e81
```

**Il percorso di una notifica**

```mermaid
sequenceDiagram
    participant U as Utente / curl
    participant DOC as DemoOrderController
    participant S as NotificationService
    participant R as Repository
    participant DB as PostgreSQL
    participant FE as Browser

    U->>DOC: POST /api/demo/orders/42/ship?recipient=mario
    DOC->>S: notify(...)
    S->>R: save(new Notification)
    R->>DB: INSERT
    S-->>DOC: la notifica salvata
    DOC-->>U: 201 + il JSON
    Note over FE: in pagina non cambia NIENTE
    FE->>FE: l utente preme «Aggiorna»
    FE->>DB: GET /api/notifications + /unread-count
    Note over FE: solo adesso il badge cambia
```

---

## 2. Dem 2 — il tubo, vuoto

Il database c'è ma non lo tocchiamo. Si costruisce solo il canale, e il canale è
**WebSocket nudo**: trasporta stringhe, e il significato glielo diamo noi.

```mermaid
flowchart TB
    subgraph FE2["FE — nessuna libreria aggiunta: WebSocket è già nel browser"]
        main2["main.tsx"]:::inv
        app2["App.tsx<br/>stato della connessione + invio"]:::nuovo
        area2["AreaFrame.tsx<br/>una riga per messaggio:<br/>ora, testo, stringa grezza"]:::nuovo
        hook2["useCanale.ts<br/>new WebSocket dentro useEffect,<br/>onopen / onmessage / onclose,<br/>riconnessione a 5s scritta da noi"]:::nuovo
        main2 --> app2 --> area2
        app2 --> hook2
    end

    subgraph BE2["BE — Spring Boot :8080"]
        wsc2["config/WebSocketConfig<br/>@EnableWebSocket<br/>l handler risponde su /ws"]:::nuovo
        echo2["web/EchoWebSocketHandler<br/>un Set di sessioni tenuto a mano<br/>apertura / chiusura / messaggio<br/>aTutti() = un ciclo for"]:::nuovo
        tm2["dto/TestMessage<br/>un solo campo: text"]:::nuovo
        tc2["web/TestController<br/>POST /api/demo/broadcast?text=<br/>GET /api/demo/sessions"]:::nuovo
        pom2["pom.xml<br/>+ spring-boot-starter-websocket"]:::mod
        yml2["application.yml<br/>+ log web.socket"]:::mod
        core2["model/ repository/ service/<br/>NotificationController<br/>DemoOrderController<br/>ci sono, ma qui nessuno li usa"]:::inv

        wsc2 --> echo2
        tc2 --> echo2
        echo2 --> tm2
        yml2 -.-> wsc2
    end

    CURL["curl -X POST /api/demo/broadcast"]:::infra

    hook2 <-->|"ws://localhost:8080/ws<br/>101 Switching Protocols,<br/>poi solo stringhe"| echo2
    CURL --> tc2

    NOTE["Da notare: chi pubblica NON deve<br/>essere dentro una sessione WebSocket.<br/>L handler è un @Component come gli altri,<br/>quindi lo si può chiamare da qualunque punto."]:::nuovo
    tc2 -.-> NOTE

    classDef nuovo fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef mod fill:#fef9c3,stroke:#ca8a04,color:#713f12
    classDef inv fill:#f1f5f9,stroke:#94a3b8,color:#334155
    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
    classDef infra fill:#e0e7ff,stroke:#4f46e5,color:#312e81
```

**Cosa manca ancora, e in quale demo arriva**

```mermaid
flowchart LR
    M1["sapere CHI è la sessione"] --> D3a["Dem 3: ?utente= nell handshake"]
    M2["mandare a UN destinatario"] --> D3b["Dem 3: una mappa scritta a mano"]
    M3["dire PERCHÉ è arrivato"] --> D3c["Dem 3: un campo dentro il messaggio"]
    M4["iscriversi a un argomento"] --> D4a["Dem 4: SUBSCRIBE di STOMP"]
    M5["riconnettersi dopo una caduta"] --> D4b["Dem 4: una riga di opzione"]
    M6["funzionare senza WebSocket"] --> D4c["Dem 4: SockJS"]
```

---

## 3. Dem 3 — il dominio dentro il tubo, tutto a mano

Le due metà si incontrano: la notifica della Dem 1 viaggia sul canale della Dem 2,
e arriva a **un utente solo**. Ma il canale è ancora nudo, quindi decidere
**a chi consegnare è codice nostro**.

```mermaid
flowchart TB
    subgraph FE3["FE — ancora nessuna libreria aggiunta"]
        app3["App.tsx<br/>cambiare utente chiude la connessione<br/>e ne apre un altra"]:::mod
        hook3["useNotifiche.ts<br/>storico e contatore via HTTP,<br/>WebSocket con ?utente=,<br/>smista guardando busta.canale,<br/>comandi segui / smetti / pubblica,<br/>riconnessione scritta da noi"]:::mod
        camp3["Campanella.tsx<br/>badge + segna tutte lette"]:::mod
        lista3["ListaNotifiche.tsx<br/>storico paginato"]:::nuovo
        canali3["CanaliPubblici.tsx<br/>segui un ordine, scrivici dentro<br/>con STOMP non si scriverebbe"]:::nuovo
        form3["FormInvio.tsx<br/>manda una notifica all altro utente"]:::nuovo
        tipi3["tipi.ts<br/>icone, etichette, date"]:::nuovo
        api3["api.ts<br/>+ tipo Busta, + inviaNotifica"]:::mod

        app3 --> camp3
        app3 --> lista3
        app3 --> canali3
        app3 --> form3
        app3 --> hook3
        hook3 --> api3
        lista3 --> tipi3
    end

    subgraph BE3["BE — Spring Boot"]
        wsc3["config/WebSocketConfig<br/>+ l interceptor dell handshake"]:::mod
        hsi3["config/UtenteHandshakeInterceptor<br/>legge ?utente= e lo salva<br/>negli attributi della sessione"]:::nuovo
        cors3["config/CorsConfig<br/>serve di nuovo: torna il REST"]:::nuovo
        handler3["web/NotificationWebSocketHandler<br/>mappa utente -> sue sessioni<br/>ordini seguiti, per sessione<br/>aUtente / aTutti / aChiSegue<br/><b>il file che nella Dem 4 sparisce</b>"]:::nuovo
        pub3["web/NotificationPublisher<br/>personale / broadcast / perOrdine"]:::nuovo
        evt3["event/NotificationCreated<br/>l annuncio: contiene solo l id"]:::nuovo
        lst3["event/NotificationListener<br/>parte DOPO il commit,<br/>rilegge e pubblica"]:::nuovo
        busta3["dto/Busta<br/>canale + notifica:<br/>la destinazione dentro il messaggio"]:::nuovo
        cmd3["dto/ComandoWs<br/>azione + ordine + testo:<br/>una SUBSCRIBE fatta in casa"]:::nuovo
        nreq3["dto/NotifyRequest"]:::nuovo
        dnc3["web/DemoNotifyController<br/>POST /api/demo/notify"]:::nuovo
        dpc3["web/DemoPublisherController<br/>ripubblica la stessa notifica<br/>sulle altre due destinazioni"]:::nuovo
        svc3["service/NotificationService<br/>+ annuncia l evento<br/>+ find(id)"]:::mod
        dto3["dto/NotificationDto<br/>+ recipient<br/>+ diCanale(): id null = non salvata"]:::mod
        rest3["NotificationController<br/>DemoOrderController<br/>model/ repository/"]:::inv
        rip3["web/TestController<br/>dto/TestMessage<br/>via: il canale ora ha contenuto vero"]:::del

        svc3 --> evt3
        evt3 --> lst3
        lst3 --> pub3
        pub3 --> handler3
        handler3 --> busta3
        handler3 --> cmd3
        wsc3 --> hsi3
        wsc3 --> handler3
        dnc3 --> svc3
        dpc3 --> pub3
        dpc3 --> svc3
        nreq3 --> dnc3
        rest3 --> svc3
        svc3 --> dto3
    end

    DB3[("PostgreSQL")]:::infra
    svc3 --> DB3

    hook3 <-->|"ws://.../ws?utente=mario"| handler3
    api3 -->|"HTTP: storico, contatore, invio"| rest3

    classDef nuovo fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef mod fill:#fef9c3,stroke:#ca8a04,color:#713f12
    classDef inv fill:#f1f5f9,stroke:#94a3b8,color:#334155
    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
    classDef infra fill:#e0e7ff,stroke:#4f46e5,color:#312e81
```

**Il registro tenuto a mano — il cuore della Dem 3**

```mermaid
flowchart LR
    subgraph H["NotificationWebSocketHandler"]
        MAP["perUtente<br/>nome utente -> insieme delle sue sessioni"]
        M1["mario -> sessione a1b2 (scheda), sessione c3d4 (telefono)"]
        M2["lucia -> sessione e5f6"]
        MAP --- M1
        MAP --- M2
    end
    CALL["aUtente('mario', busta)"] --> MAP
    M1 --> SEND["per ognuna delle sue sessioni:<br/>sendMessage(frame)"]

    R1["1. un utente ha PIÙ sessioni: due schede, due dispositivi"]
    R2["2. alla chiusura va tolta la sessione E, se era l ultima,<br/>anche la voce dalla mappa"]
    R3["3. thread diversi aprono, chiudono e spediscono insieme:<br/>servono le collezioni concorrenti"]
    MAP -.-> R1
    MAP -.-> R2
    MAP -.-> R3
```

**Le tre destinazioni, e come sono fatte**

```mermaid
flowchart LR
    P["NotificationPublisher"]
    P --> B1["broadcast(dto)<br/>{canale: tutti}"] --> S1["tutte le sessioni aperte"]
    P --> B2["perOrdine(42, dto)<br/>{canale: ordine:42}"] --> S2["chi ha mandato<br/>{azione: segui, ordine: 42}"]
    P --> B3["personale(nome, dto)<br/>{canale: personale}"] --> S3["le sessioni di quel nome"]
    NOTA["Il campo canale NON è un header del protocollo:<br/>è un campo JSON inventato da noi.<br/>Serve perché il client riceve tre messaggi<br/>identici sulla stessa connessione e deve<br/>poter capire quale è quale."]
    P -.-> NOTA
```

**Il percorso completo**

```mermaid
sequenceDiagram
    participant C as curl
    participant DOC as DemoOrderController
    participant S as NotificationService
    participant DB as PostgreSQL
    participant L as NotificationListener
    participant P as NotificationPublisher
    participant H as NotificationWebSocketHandler
    participant B as Browser mario
    participant B2 as Browser lucia

    C->>DOC: POST /orders/42/ship?recipient=mario
    DOC->>S: notify(...)
    activate S
    Note over S: transazione APERTA
    S->>DB: save(...)
    S->>S: annuncia NotificationCreated(id)
    deactivate S
    Note over S,DB: COMMIT — solo ora l annuncio viene consegnato
    S->>L: NotificationCreated
    L->>DB: findById(id): rilettura sicura
    L->>P: personale(recipient, dto)
    P->>H: aUtente('mario', ...)
    H->>H: cerca 'mario' nella mappa
    H->>B: canale personale + notifica
    Note over B2: non riceve niente:<br/>il suo nome non è in quella chiave
    B->>B: il badge passa da 0 a 1, senza ricaricare
```

---

## 4. Dem 4 — STOMP sopra, SockJS sotto

Cambiano **due livelli**, e sono tutti e due sotto la nostra logica. Il pezzo in mezzo —
entity, service, evento dopo il commit, listener, storico REST — è la Dem 3 parola per parola.

```mermaid
flowchart TB
    subgraph FE4["FE — + @stomp/stompjs, sockjs-client"]
        app4["App.tsx<br/>tre stati: connesso /<br/>in riconnessione / offline"]:::mod
        hook4["useNotifiche.ts<br/>Client STOMP, il tubo lo apre SockJS,<br/>il nome nel frame CONNECT,<br/>subscribe alla coda personale,<br/>riconnessione = una opzione"]:::mod
        camp4["Campanella.tsx"]:::inv
        pstomp["PannelloStomp.tsx<br/>i frame in entrata e uscita, a schermo"]:::nuovo
        ptrasp["PannelloTrasporto.tsx<br/>cosa succede senza WebSocket"]:::nuovo
        pconf["PannelloConfronto.tsx<br/>senza push, con push, e il cronometro"]:::nuovo
        trasp4["trasporto.ts<br/>le due leve del ripiego"]:::nuovo
        api4["api.ts<br/>via il tipo Busta,<br/>+ invii senza e con push,<br/>+ contatore richieste HTTP"]:::mod
        vite4["vite.config.ts<br/>define global -> globalThis:<br/>senza, sockjs-client non parte"]:::mod
        canc4["CanaliPubblici.tsx<br/>cancellato: una subscribe<br/>dalla console fa lo stesso"]:::del

        app4 --> camp4
        app4 --> pstomp
        app4 --> ptrasp
        app4 --> pconf
        app4 --> hook4
        hook4 --> api4
        hook4 --> trasp4
    end

    subgraph BE4["BE — Spring Boot"]
        wsc4["config/WebSocketConfig<br/>@EnableWebSocketMessageBroker<br/>endpoint /ws con SockJS<br/>broker su /topic e /queue<br/>codice nostro su /app<br/>code personali su /user"]:::mod
        sli4["config/StompLoginInterceptor<br/>sul CONNECT legge l header login<br/>e lo rende il Principal della sessione"]:::nuovo
        pub4["web/NotificationPublisher<br/>tre metodi, tre righe:<br/>convertAndSend / convertAndSendToUser"]:::mod
        sev4["event/SessionEventsListener<br/>scrive nel log chi si collega<br/>e chi se ne va"]:::nuovo
        sreg4["event/SubscriptionRegistry<br/>quale sessione è iscritta a cosa<br/><b>serve ad AUTORIZZARE, non a consegnare</b>"]:::nuovo
        msgc4["web/MessaggiController<br/>lo stesso messaggio in 4 modi"]:::nuovo
        tc4["web/TestController<br/>/app/ping con ritorno su /topic/test"]:::nuovo
        dtos4["dto/MessaggioUtente, MessaggioTopic,<br/>MessaggioPubblico, ErroreStomp, TestMessage"]:::nuovo
        inv4["NotificationCreated, NotificationListener<br/>service/ model/ repository/<br/>NotificationController<br/>DemoOrderController<br/>DemoPublisherController, CorsConfig<br/>nessuna riga cambiata"]:::inv
        canc4b["web/NotificationWebSocketHandler<br/>config/UtenteHandshakeInterceptor<br/>dto/Busta, dto/ComandoWs<br/>cancellati"]:::del

        wsc4 --> sli4
        inv4 --> pub4
        msgc4 --> sreg4
        msgc4 --> dtos4
        tc4 --> dtos4
    end

    subgraph SPRING["Quello che ci dà Spring, senza scrivere niente"]
        broker["il broker in memoria<br/>tiene lui l elenco delle iscrizioni:<br/>è la mappa della Dem 3, ma non è nostra"]:::infra
        tmpl["SimpMessagingTemplate<br/>per pubblicare da qualunque punto del codice"]:::infra
        sockjs["il servizio SockJS<br/>i trasporti di ripiego sotto /ws"]:::infra
    end

    pub4 --> tmpl
    tmpl --> broker
    msgc4 --> tmpl
    wsc4 --> broker
    wsc4 --> sockjs
    broker --> sockjs
    sockjs <-->|"WebSocket 101, oppure<br/>richieste HTTP se il WebSocket non passa"| hook4
    sli4 --> sev4
    sli4 --> broker

    classDef nuovo fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef mod fill:#fef9c3,stroke:#ca8a04,color:#713f12
    classDef inv fill:#f1f5f9,stroke:#94a3b8,color:#334155
    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
    classDef infra fill:#e0e7ff,stroke:#4f46e5,color:#312e81
```

**Le tre famiglie di destinazioni — la cosa da non confondere**

```mermaid
flowchart LR
    CL["Client STOMP"]
    subgraph SRV["Server"]
        APP["/app<br/>CODICE NOSTRO<br/>@MessageMapping"]:::nuovo
        BRK["/topic e /queue<br/>IL BROKER"]:::infra
        USR["/user<br/>prefisso VIRTUALE<br/>lo riscrive Spring"]:::infra
    end

    CL -->|"SEND: solo verso /app/..."| APP
    CL -->|"SUBSCRIBE: solo verso /topic, /queue, /user"| BRK
    APP -->|"@SendTo oppure SimpMessagingTemplate"| BRK

    ERR["Scambiarsi i due non dà nessun errore,<br/>e semplicemente non funziona:<br/>è l inghippo classico da mostrare"]:::del
    CL -.-> ERR

    classDef nuovo fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
    classDef infra fill:#e0e7ff,stroke:#4f46e5,color:#312e81
```

**Perché `/user` è privato davvero**

```mermaid
flowchart TB
    S["SERVER<br/>convertAndSendToUser('mario', '/queue/notifications')"]
    C["CLIENT<br/>subscribe('/user/queue/notifications')"]
    R["la destinazione VERA nel broker:<br/>/queue/notifications-user{idSessione}<br/>una diversa per ogni sessione"]
    S -->|"Spring la riscrive"| R
    C -->|"Spring la riscrive"| R
    N["Nessuna delle due stringhe che scriviamo nel codice<br/>esiste davvero dentro il broker.<br/>Il client non potrebbe nemmeno indovinarla:<br/>non conosce l id della propria sessione.<br/>La privatezza viene da QUI, non da un<br/>controllo di autorizzazione."]
    R -.-> N
```

**L'apertura del canale, frame per frame**

```mermaid
sequenceDiagram
    participant H as useNotifiche.ts
    participant SJ as SockJS
    participant SRV as Spring
    participant I as StompLoginInterceptor
    participant BRK as il broker

    H->>H: new Client({...}) — non apre ancora niente
    H->>SJ: client.activate()
    SJ->>SRV: GET /ws/info — cosa sai fare?
    SRV-->>SJ: websocket sì, origini ammesse
    SJ->>SRV: GET /ws/.../websocket
    SRV-->>SJ: 101 Switching Protocols
    Note over SJ,SRV: se il WebSocket è bloccato SockJS ripiega:<br/>una richiesta HTTP che non si chiude mai<br/>+ una richiesta per ogni frame in uscita
    H->>SRV: CONNECT — login:mario, heart-beat
    SRV->>I: preSend, comando CONNECT
    I->>I: setUser(mario)
    SRV-->>H: CONNECTED
    Note over H: solo ORA scatta onConnect
    H->>H: ricarica() — lo storico via HTTP
    H->>BRK: SUBSCRIBE /user/queue/notifications
    Note over H,BRK: poi silenzio, con un battito ogni 10 secondi
```

**Il percorso di una notifica (Dem 4)**

```mermaid
sequenceDiagram
    participant C as curl
    participant S as NotificationService
    participant L as NotificationListener
    participant P as NotificationPublisher
    participant T as SimpMessagingTemplate
    participant BRK as il broker
    participant B as Browser mario

    C->>S: POST /orders/42/ship — identico alla Dem 3
    S->>S: salva + annuncia, poi COMMIT
    S->>L: dopo il commit — identico alla Dem 3
    L->>P: personale(recipient, dto)
    Note over P: QUI cambia quello che c è sotto
    P->>T: convertAndSendToUser('mario', '/queue/notifications', dto)
    T->>BRK: destinazione riscritta con l id di sessione
    BRK->>B: MESSAGE, e dentro solo il DTO
    Note over B: contenuto PIÙ MAGRO della Dem 3:<br/>niente busta, il perché sta negli header
```

**Le quattro strade dello stesso messaggio**

```mermaid
flowchart TB
    M["web/MessaggiController"]
    M --> V1["1. POST /messaggi/senza-push<br/>salva e basta, nessun annuncio<br/>-> il destinatario lo scopre quando chiede"]
    M --> V2["2. POST /messaggi/con-push<br/>salva e annuncia<br/>-> HTTP in andata, STOMP in consegna"]
    M --> V3["3. SEND a /app/messaggi<br/>-> STOMP in andata E in consegna,<br/>il mittente lo mette il server"]
    M --> V4["4. SEND a /app/topic-messaggi<br/>-> a molti, ma solo sui topic a cui<br/>QUESTA sessione è iscritta"]
    V4 --> REG["SubscriptionRegistry: sei iscritto?"]
    REG -->|"no"| ERRO["rifiuto sulla coda personale del mittente,<br/>la connessione resta in piedi"]
    REG -->|"sì"| OK["lo ricevono tutti gli iscritti<br/>in questo momento, mittente compreso"]
```

**I due trasporti, affiancati**

```mermaid
flowchart LR
    subgraph WS["WebSocket"]
        W1["una sola richiesta di apertura"]
        W2["nel Network: una riga, filtro WS, stato 101"]
        W3["andata e ritorno sullo stesso tubo"]
        W4["ritardo minimo"]
    end
    subgraph XHR["Ripiego su HTTP"]
        X1["una richiesta che resta aperta per ricevere<br/>+ una richiesta per ogni messaggio in uscita"]
        X2["nel Network: tutte righe Fetch/XHR, stato 200"]
        X3["più traffico e più ritardo"]
        X4["ma funziona anche dove il WebSocket è bloccato"]
    end
    CODE["Il nostro codice è IDENTICO nei due casi,<br/>STOMP compreso. Cambia solo cosa si vede<br/>nel pannello Network del browser."]
    WS -.-> CODE
    XHR -.-> CODE
```

---

## 5. Il salto Dem 3 → Dem 4, in una figura

```mermaid
flowchart TB
    subgraph L1["Dem 3 — scritto a mano, circa 140 righe"]
        A1["registro delle sessioni per utente<br/>NotificationWebSocketHandler · ~40 righe"]:::del
        A2["ordini seguiti + comandi inventati<br/>stesso file + ComandoWs · ~30 righe"]:::del
        A3["campo canale dentro il messaggio<br/>Busta + smistamento nel client · ~20 righe"]:::del
        A4["nome utente nella query string<br/>UtenteHandshakeInterceptor · ~30 righe"]:::del
        A5["riconnessione + comandi da rimandare<br/>useNotifiche.ts · ~20 righe"]:::del
    end

    subgraph L2["Dem 4 — quasi tutto configurazione"]
        B1["il registro lo tiene il broker"]:::infra
        B2["client.subscribe('/topic/ordini/42', cb)<br/>una riga, e ti restituisce come disiscriverti"]:::nuovo
        B3["la destinazione sta negli HEADER del frame"]:::infra
        B4["il nome in un header del CONNECT<br/>+ StompLoginInterceptor · ~15 righe"]:::nuovo
        B5["reconnectDelay: una riga"]:::infra
    end

    A1 --> B1
    A2 --> B2
    A3 --> B3
    A4 --> B4
    A5 --> B5

    classDef nuovo fill:#dcfce7,stroke:#16a34a,color:#14532d
    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
    classDef infra fill:#e0e7ff,stroke:#4f46e5,color:#312e81
```

---

## 6. Cosa NON fa nessuna delle quattro

```mermaid
flowchart LR
    X1["nessuna autenticazione vera:<br/>?utente=lucia (Dem 3) e login:mario (Dem 4)<br/>li dichiara il client e nessuno li verifica"]:::del
    X2["nessun broker esterno: quello di Spring vive in memoria.<br/>Con due server dietro un bilanciatore ci sarebbero<br/>due elenchi separati, e i messaggi non passerebbero<br/>dall uno all altro"]:::del
    X3["nessuna migrazione di schema: le tabelle le crea<br/>Hibernate con ddl-auto update.<br/>In produzione si usa Flyway o Liquibase"]:::del
    X4["lo storico non arriva MAI dal canale:<br/>il canale porta solo quello che succede da adesso.<br/>Se nessuno è collegato con quel nome il messaggio si perde,<br/>ed è lo storico HTTP a rimediare"]:::del

    classDef del fill:#fee2e2,stroke:#dc2626,color:#7f1d1d
```
