import { Client } from '@stomp/stompjs'
import { useCallback, useEffect, useRef, useState } from 'react'
import SockJS from 'sockjs-client'
import {
  caricaStorico,
  contaNonLette,
  inviaConPush,
  inviaSenzaPush,
  richiesteHttp,
  segnaTutteLette,
} from './api'
import { opzioniSockJs } from './trasporto'

/**
 * Il cervello del frontend della Dem 4.
 *
 * Confrontatelo con la Dem 3 e guardate cosa NON c'e' piu':
 *  - nessun setTimeout per la riconnessione: la fa il client STOMP;
 *  - nessun controllo su readyState prima di mandare: il client accoda;
 *  - nessun campo "canale" da leggere dentro il contenuto: la destinazione
 *    sta negli header del frame MESSAGE.
 *
 * In cambio ci sono due librerie:
 *  - SockJS: sceglie il trasporto (WebSocket se puo', altrimenti HTTP);
 *  - @stomp/stompjs: parla STOMP dentro quel trasporto.
 */

// Con SockJS l'indirizzo e' http://, non ws://: il primo contatto e' una richiesta
// HTTP normale, ed e' li' che si decide quale trasporto usare.
const SOCKJS_URL = 'http://localhost:8080/ws'

// La coda personale. /user e' un prefisso virtuale: il client si iscrive a questo
// indirizzo, e Spring lo traduce nella coda privata di QUESTA sessione.
// Nessun altro puo' iscriversi alla nostra.
export const DESTINAZIONE_PERSONALE = '/user/queue/notifications'

// La coda dei rifiuti, personale come l'altra. Restiamo sempre iscritti, anche in
// modo polling: un permesso negato non e' una notifica, e' la risposta a una cosa
// che abbiamo appena fatto noi e vogliamo saperlo subito.
export const DESTINAZIONE_ERRORI = '/user/queue/errors'

const POLLING_MS = 5000

// La versione TSX dichiarava qui sette tipi. Senza TypeScript restano un commento,
// ma vanno letti comunque, perche' sono la mappa di quello che l'hook restituisce:
//
//   StatoConnessione  'connesso' | 'in riconnessione' | 'offline'
//
//   ModoRicezione     'stomp' | 'polling'
//                     come questa pagina viene a sapere delle notifiche: e'
//                     l'interruttore del confronto.
//
//   Arrivo            { id, destinazione, corpo, ora }
//                     un messaggio arrivato su una destinazione qualunque, come lo
//                     mostriamo in pagina.
//
//   Riga              { id, testo, verso }  con verso 'in' | 'out' | 'nota'
//                     una riga del log di stompjs: i frame grezzi, cosi' come
//                     passano sul canale.
//
//   Scoperta          { titolo, ritardoMs, modo }
//                     il cronometro: quanto tempo e' passato fra la nascita della
//                     notifica e il momento in cui questa pagina se n'e' accorta.
//
//   Errore            { messaggio, destinazione, ora }
//                     un rifiuto arrivato dal server su /user/queue/errors.
//
//   Opzioni           { trasporto, modoRicezione, senzaLogin, destinazioni }
//                     quello che il componente passa all'hook:
//                      - senzaLogin: se vero, manda il CONNECT senza header login,
//                        la sessione resta senza Principal e i messaggi su
//                        /user/queue vengono buttati via in silenzio. Serve a far
//                        vedere proprio quel silenzio.
//                      - destinazioni: le iscrizioni oltre a quella personale.
//                        Cambiarle NON riapre la connessione: manda solo un frame
//                        SUBSCRIBE o UNSUBSCRIBE.

const MAX_RIGHE = 250

// Un contatore per dare a ogni riga di log e a ogni arrivo una chiave unica.
let sequenza = 0
const prossimoId = () => ++sequenza

// stompjs scrive nel log ">>>" per i frame in uscita e "<<<" per quelli in entrata.
// Restituisce 'out', 'in' oppure 'nota'.
function verso(testo) {
  if (testo.startsWith('>>')) return 'out'
  if (testo.startsWith('<<')) return 'in'
  return 'nota'
}

export function useNotifiche(utente, opzioni) {
  const { trasporto, modoRicezione, senzaLogin, destinazioni } = opzioni

  const [notifiche, setNotifiche] = useState([])
  const [nonLette, setNonLette] = useState(0)
  const [stato, setStato] = useState('offline')
  const [trasportoNegoziato, setTrasportoNegoziato] = useState(null)
  const [arrivi, setArrivi] = useState([])
  const [righe, setRighe] = useState([])
  const [richieste, setRichieste] = useState(0)
  const [scoperta, setScoperta] = useState(null)
  const [errore, setErrore] = useState(null)

  /**
   * Un contatore che aumenta a ogni CONNECT riuscito.
   *
   * Serve come segnale: quando la connessione riparte, le vecchie iscrizioni sono
   * morte con la sessione precedente e vanno rifatte. Mettendolo fra le dipendenze
   * dell'effetto delle SUBSCRIBE, quell'effetto riparte da solo.
   */
  const [connessione, setConnessione] = useState(0)

  const clientRef = useRef(null)

  // destinazione -> oggetto sottoscrizione, per poter fare unsubscribe quando serve.
  const sottoscrizioniRef = useRef(new Map())

  // L'id piu' alto che questa pagina ha gia' visto: serve a capire quali notifiche
  // sono davvero nuove, e quindi quando ha senso far partire il cronometro.
  const idVistoRef = useRef(0)
  const utenteVistoRef = useRef(null)

  const registraRiga = useCallback((testo) => {
    setRighe((precedenti) =>
      [{ id: prossimoId(), testo, verso: verso(testo) }, ...precedenti].slice(0, MAX_RIGHE),
    )
  }, [])

  const registraArrivo = useCallback((destinazione, corpo) => {
    setArrivi((precedenti) =>
      [
        { id: prossimoId(), destinazione, corpo, ora: new Date().toLocaleTimeString() },
        ...precedenti,
      ].slice(0, 50),
    )
  }, [])

  // Il modo di ricezione lo teniamo anche in una ref, non solo nello stato.
  // Motivo: la funzione misura() deve sapere qual e' il modo ATTUALE, ma non deve
  // essere ricreata quando cambia, altrimenti gli effetti che dipendono da lei
  // ripartirebbero e perderemmo la misura appena fatta.
  const modoRef = useRef(modoRicezione)
  useEffect(() => {
    modoRef.current = modoRicezione
  }, [modoRicezione])

  /**
   * IL CRONOMETRO DEL CONFRONTO.
   *
   * createdAt e' il momento in cui il server ha scritto la riga sul database.
   * Date.now() e' il momento in cui questa pagina l'ha saputo.
   * La differenza fra i due e' il ritardo, ed e' il numero che rende visibile
   * la differenza fra ricevere (decine di millisecondi) e chiedere (fino a 5 secondi).
   *
   * Misuriamo solo se l'id e' piu' alto di quello gia' visto: rileggere lo storico
   * non e' una scoperta.
   */
  const misura = useCallback((dto) => {
    if (dto.id <= idVistoRef.current) return
    idVistoRef.current = dto.id
    setScoperta({
      titolo: dto.title,
      ritardoMs: Date.now() - new Date(dto.createdAt).getTime(),
      modo: modoRef.current,
    })
  }, [])

  // --- 1. La connessione ----------------------------------------------------
  //
  // Si rifa' solo se cambia l'utente, il trasporto o la scelta sul login.
  // Le iscrizioni NO: quelle stanno nell'effetto piu' sotto, ed e' esattamente
  // la differenza fra un frame CONNECT e un frame SUBSCRIBE.

  useEffect(() => {
    let attivo = true
    const vive = sottoscrizioniRef.current

    const client = new Client({
      /**
       * Al posto di brokerURL passiamo una fabbrica: cosi' il tubo lo apre SockJS,
       * che sceglie il trasporto (WebSocket se puo', altrimenti HTTP).
       * Da qui in su, per STOMP, non cambia assolutamente niente.
       */
      webSocketFactory: () => {
        const sock = new SockJS(SOCKJS_URL, null, opzioniSockJs(trasporto))

        // sock.transport contiene il nome del trasporto negoziato ("websocket",
        // "xhr-streaming"...). In TSX questa riga era piena di conversioni, perche'
        // quel campo non compare nei tipi della libreria, che si presenta come un
        // WebSocket. Qui si legge e basta: e' l'unico modo di sapere dal codice
        // quello che si vede nel pannello Network del browser.
        sock.addEventListener('open', () => {
          const nome = sock.transport ?? '?'
          setTrasportoNegoziato(nome)
          registraRiga(`nota: SockJS ha negoziato il trasporto ${nome}`)
        })
        return sock
      },

      /**
       * Il nome dell'utente in un'intestazione del frame CONNECT, non nella query
       * string come nella Dem 3. Vantaggi concreti: non finisce nei log del proxy,
       * e regge il cambio di trasporto perche' e' un header STOMP, non HTTP.
       */
      connectHeaders: senzaLogin ? {} : { login: utente },

      // Un'opzione al posto di tutto il codice di riconnessione della Dem 3.
      reconnectDelay: 5000,

      debug: (riga) => {
        console.log('[STOMP]', riga)
        registraRiga(riga)
      },

      onConnect: () => {
        if (!attivo) return
        setStato('connesso')
        // Il segnale per rifare le iscrizioni: vedi l'effetto piu' sotto.
        setConnessione((n) => n + 1)
      },

      // Il tubo e' caduto, ma reconnectDelay fara' riprovare fra 5 secondi.
      // Le iscrizioni di prima sono morte con la sessione: via dalla mappa,
      // altrimenti al ritorno crederemmo di essere ancora iscritti.
      onWebSocketClose: () => {
        vive.clear()
        setTrasportoNegoziato(null)
        setStato((s) => (s === 'offline' ? s : 'in riconnessione'))
      },

      // Errore di protocollo (per esempio una destinazione rifiutata dal broker):
      // qui il client non riprova da solo.
      onStompError: (frame) => {
        console.error('[STOMP] errore dal broker:', frame.headers.message)
        setStato('offline')
      },
    })

    client.activate()
    clientRef.current = client

    // Solo per le prove dalla console del browser: window.stomp.subscribe(...)
    // In TSX serviva una conversione per poter appendere una proprieta' a window.
    window.stomp = client

    return () => {
      attivo = false
      vive.clear()
      void client.deactivate()
    }
  }, [utente, trasporto, senzaLogin, registraRiga])

  // --- 2. Storico e conteggio dal database ----------------------------------

  /**
   * Le chiamate HTTP. Servono in tre momenti:
   *  - all'apertura della pagina;
   *  - dopo ogni CONNECT (l'iscrizione vale da adesso, quello di prima sta in tabella);
   *  - ogni 5 secondi, se siamo in modo polling.
   *
   * E' anche il motivo per cui una notifica arrivata mentre il canale era caduto
   * non si perde: la verita' definitiva sta sul database, non sul canale.
   */
  const ricarica = useCallback(async () => {
    // Cambiando utente si riparte da zero: lo storico dell'altro non e' "nuovo".
    if (utenteVistoRef.current !== utente) {
      utenteVistoRef.current = utente
      idVistoRef.current = 0
    }

    // idVisto a zero significa che questa pagina non ha ancora visto niente per
    // questo utente: lo storico che arriva adesso e' roba vecchia, non una scoperta,
    // e cronometrarlo darebbe ritardi assurdi (ore, giorni).
    const primoGiro = idVistoRef.current === 0

    const [storico, conteggio] = await Promise.all([caricaStorico(utente), contaNonLette(utente)])
    setNotifiche(storico)
    setNonLette(conteggio)
    setRichieste(richiesteHttp())

    if (storico.length === 0) return

    // La piu' recente e' la prima: lo storico arriva ordinato per createdAt desc.
    if (primoGiro) {
      idVistoRef.current = storico[0].id
      setScoperta(null)
      return
    }
    misura(storico[0])
  }, [utente, misura])

  // --- 3. Le iscrizioni -----------------------------------------------------
  //
  // Effetto separato da quello della connessione, e la separazione E' il punto:
  // cambiare destinazione non riapre niente, manda solo un frame.
  // Nella Dem 3 "iscriversi" voleva dire inventarsi un comando e sperare.

  useEffect(() => {
    const client = clientRef.current
    if (!client?.connected) return

    const vive = sottoscrizioniRef.current

    // Le destinazioni che vogliamo adesso. Nota: in modo polling la coda personale
    // NON e' nell'elenco, quindi viene disiscritta davvero. Nel log si vede la
    // UNSUBSCRIBE, e quel frame MESSAGE non arriva piu': non stiamo fingendo
    // lato client di non aver ricevuto niente.
    const volute = [
      DESTINAZIONE_ERRORI,
      ...(modoRicezione === 'stomp' ? [DESTINAZIONE_PERSONALE] : []),
      ...destinazioni,
    ]

    // Iscriviamo quelle nuove.
    for (const destinazione of volute) {
      if (vive.has(destinazione)) continue
      vive.set(
        destinazione,
        client.subscribe(destinazione, (frame) => {
          // La destinazione la leggiamo dagli HEADER del frame, non dal contenuto.
          // E' la differenza piu' concreta con la "busta" della Dem 3.
          registraArrivo(frame.headers.destination ?? destinazione, frame.body)

          if (destinazione === DESTINAZIONE_ERRORI) {
            // In TSX ogni JSON.parse portava dietro un "as" per dire al compilatore
            // cosa ci aspettavamo. Qui arriva quello che arriva.
            const rifiuto = JSON.parse(frame.body)
            setErrore({ ...rifiuto, ora: new Date().toLocaleTimeString() })
            return
          }

          // Gli altri topic li mostriamo solo nel riquadro degli arrivi.
          if (destinazione !== DESTINAZIONE_PERSONALE) return

          // Il contenuto e' il DTO e basta: niente involucro, niente campo canale.
          const notifica = JSON.parse(frame.body)
          setNotifiche((precedenti) => [notifica, ...precedenti])
          setNonLette((n) => n + 1)
          misura(notifica)
        }),
      )
    }

    // Disiscriviamo quelle che non vogliamo piu'.
    for (const [destinazione, sottoscrizione] of vive) {
      if (volute.includes(destinazione)) continue
      sottoscrizione.unsubscribe()
      vive.delete(destinazione)
    }
  }, [destinazioni, modoRicezione, connessione, registraArrivo, misura])

  // All'apertura, a ogni cambio utente e dopo ogni CONNECT.
  useEffect(() => {
    void ricarica()
  }, [ricarica, connessione])

  /**
   * IL POLLING: l'unico modo di sapere qualcosa senza un canale aperto.
   *
   * Una richiesta ogni 5 secondi, sempre, anche quando non e' cambiato niente.
   * E comunque in ritardo: una notifica nata subito dopo un controllo aspetta
   * fino a 5 secondi prima di essere vista.
   *
   * Provate a immaginare questo moltiplicato per diecimila utenti collegati.
   */
  useEffect(() => {
    if (modoRicezione !== 'polling') return
    const timer = setInterval(() => void ricarica(), POLLING_MS)
    return () => clearInterval(timer)
  }, [modoRicezione, ricarica])

  const segnaLette = useCallback(async () => {
    await segnaTutteLette(utente)
    // Aggiorniamo subito la pagina invece di rileggere tutto: sappiamo gia'
    // qual e' il risultato, e ci risparmiamo due richieste.
    setNotifiche((precedenti) => precedenti.map((n) => ({ ...n, read: true })))
    setNonLette(0)
    setRichieste(richiesteHttp())
  }, [utente])

  // --- 4. I modi di mandare un messaggio ------------------------------------

  /** (1) HTTP, e nessuno avvisa il destinatario: lo scoprira' chiedendo. */
  const inviaHttpSenzaPush = useCallback(
    async (a, testo) => {
      await inviaSenzaPush(utente, a, testo)
      setRichieste(richiesteHttp())
    },
    [utente],
  )

  /** (2) HTTP all'andata, STOMP in consegna: il caso normale di un'applicazione vera. */
  const inviaHttpConPush = useCallback(
    async (a, testo) => {
      await inviaConPush(utente, a, testo)
      setRichieste(richiesteHttp())
    },
    [utente],
  )

  /**
   * (3) STOMP all'andata e in consegna: nessuna richiesta HTTP.
   *
   * Guardate il contatore delle richieste mentre premete: sta fermo.
   * Il canale e' gia' aperto e funziona nelle due direzioni.
   *
   * Il mittente non e' nel contenuto: lo mette il server, prendendolo dal
   * Principal della sessione. Nessuno puo' firmarsi come un altro.
   */
  const inviaViaStomp = useCallback((a, testo) => {
    clientRef.current?.publish({
      destination: '/app/messaggi',
      body: JSON.stringify({ destinatario: a, testo }),
    })
  }, [])

  /**
   * (4) Su un topic, e non su uno qualunque: il server accetta solo i topic a cui
   * questa sessione e' iscritta, e se non lo siamo risponde su /user/queue/errors.
   *
   * Mandiamo il NOME del topic, non la destinazione completa: il prefisso /topic
   * lo mette il server, cosi' nessuno prova a scrivere nelle code personali altrui.
   */
  const inviaSuTopic = useCallback((topic, testo) => {
    setErrore(null)
    clientRef.current?.publish({
      destination: '/app/topic-messaggi',
      body: JSON.stringify({ topic, testo }),
    })
  }, [])

  /**
   * Il giro di prova: SEND su /app/ping.
   *
   * /app non e' un prefisso del broker: porta a un @MessageMapping, cioe' a codice
   * nostro. Quello che il metodo restituisce, @SendTo lo rimette su /topic/test.
   * Quindi il ping torna indietro SOLO se siamo iscritti a /topic/test:
   * e' un ottimo modo per far vedere che senza SUBSCRIBE non arriva niente.
   */
  const inviaPing = useCallback((testo) => {
    clientRef.current?.publish({
      destination: '/app/ping',
      body: JSON.stringify({ text: testo }),
    })
  }, [])

  /**
   * Stacca il tubo senza chiudere il client: la caduta di rete in un tasto.
   *
   * reconnectDelay riprova dopo 5 secondi e onConnect fa ripartire l'effetto che
   * rifa' tutte le iscrizioni. Non c'e' una riga di codice nostro per gestire tutto
   * questo: nella Dem 3 era un setTimeout piu' un ciclo di comandi da rimandare.
   */
  const simulaCaduta = useCallback(() => {
    registraRiga('nota: forceDisconnect() -- il client riprovera fra 5s')
    clientRef.current?.forceDisconnect()
  }, [registraRiga])

  const pulisciLog = useCallback(() => {
    setRighe([])
    setArrivi([])
    setErrore(null)
  }, [])

  return {
    notifiche,
    nonLette,
    stato,
    trasportoNegoziato,
    arrivi,
    righe,
    richieste,
    scoperta,
    errore,
    segnaLette,
    ricarica,
    inviaHttpSenzaPush,
    inviaHttpConPush,
    inviaViaStomp,
    inviaSuTopic,
    inviaPing,
    simulaCaduta,
    pulisciLog,
  }
}
