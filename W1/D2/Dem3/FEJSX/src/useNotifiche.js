import { useCallback, useEffect, useRef, useState } from 'react'
import {
  caricaPagina,
  contaNonLette,
  segnaLetta,
  segnaTutteLette,
  PAGINA_VUOTA,
} from './api'

// Un solo indirizzo, nessun broker, nessun protocollo sopra il tubo.
// Il nome dell'utente viaggia nella query string dell'handshake: ?utente=mario
const WS_URL = 'ws://localhost:8080/ws'
const RITARDO_RICONNESSIONE = 5000

// In TSX qui c'era un tipo in piu':
//   export type BustaRicevuta = Busta & { ricevutaAlle: string }
// una Busta con l'aggiunta dell'ora in cui l'abbiamo vista arrivare. Senza
// TypeScript quell'aggiunta si vede solo dove la scriviamo, dentro onmessage.

/**
 * Il cervello del frontend: mette insieme le due fonti di dati.
 *
 *   1. HTTP  -> lo storico e il conteggio. E' la verita' definitiva, sta sul database.
 *   2. WEBSOCKET -> le novita' mentre siamo qui. E' veloce, ma solo per chi e' collegato.
 *
 * Le due cose non si sostituiscono: si aiutano. Il canale dice "e' successo qualcosa",
 * e poi si torna a chiedere al server la versione ufficiale.
 */
export function useNotifiche(utente) {
  // --- Stato che arriva dalle chiamate HTTP --------------------------------
  // PAGINA_VUOTA come valore iniziale: cosi' pagina.content esiste da subito e i
  // componenti non devono difendersi da un null.
  const [pagina, setPagina] = useState(PAGINA_VUOTA)
  const [indice, setIndice] = useState(0) // quale pagina stiamo guardando (0 = la prima)
  const [dimensione, setDimensione] = useState(5) // quante notifiche per pagina
  const [nonLette, setNonLette] = useState(0)
  const [caricamento, setCaricamento] = useState(false)
  const [errore, setErrore] = useState(null)

  // --- Stato che arriva dal canale WebSocket -------------------------------
  const [connesso, setConnesso] = useState(false)

  /**
   * Notifiche arrivate mentre l'utente sta guardando una pagina diversa dalla prima.
   * Non le infiliamo a forza nella sua vista (gli sposterebbe le righe sotto il dito):
   * gli mostriamo un avviso e decide lui se andare a vederle.
   */
  const [arretrate, setArretrate] = useState(0)

  /** Quello che arriva sui canali non personali: serve a vedere in pagina la differenza
   *  fra "a tutti", "a chi segue l'ordine 42" e "a te". Dentro ci finiscono delle
   *  buste con il campo ricevutaAlle aggiunto da noi. */
  const [pubbliche, setPubbliche] = useState([])

  /** Gli ordini che questa sessione ha chiesto di seguire: un array di numeri. */
  const [ordiniSeguiti, setOrdiniSeguiti] = useState([])

  const socketRef = useRef(null)

  /**
   * DUE REF CHE SEMBRANO STRANE MA HANNO UNA RAGIONE PRECISA.
   *
   * Il problema: le funzioni dentro l'useEffect della WebSocket (per esempio onmessage)
   * "fotografano" i valori del momento in cui sono state create. Se avessero bisogno
   * dei valori aggiornati, dovremmo metterli fra le dipendenze dell'effetto...
   * ma allora l'effetto ripartirebbe, e riaprire la WebSocket ogni volta che si cambia
   * pagina sarebbe assurdo.
   *
   * La soluzione: una ref e' una scatola sempre aggiornata che chiunque puo' aprire
   * al momento giusto, senza comparire fra le dipendenze.
   */
  const vistaRef = useRef({ indice, dimensione })
  vistaRef.current = { indice, dimensione }

  const seguitiRef = useRef(ordiniSeguiti)
  seguitiRef.current = ordiniSeguiti

  /**
   * Cambio utente: si riparte dalla prima pagina e si buttano via i dati del
   * precedente. La pagina 3 di mario non significa niente per lucia.
   *
   * Perche' qui e non dentro un useEffect? Perche' un effetto viene eseguito DOPO
   * il disegno: farebbe partire prima una richiesta con l'indice vecchio, e vedremmo
   * per un attimo i dati sbagliati. Questo confronto durante il render e' il modo
   * consigliato da React per adeguare lo stato quando cambia una prop.
   */
  const [utentePrec, setUtentePrec] = useState(utente)
  if (utente !== utentePrec) {
    setUtentePrec(utente)
    setIndice(0)
    setArretrate(0)
    setPubbliche([])
    setOrdiniSeguiti([])
  }

  // --- Il lato HTTP ---------------------------------------------------------

  /**
   * Va a chiedere al server la pagina e il conteggio. E' l'unica funzione che
   * aggiorna lo storico: tutto il resto, prima o poi, chiama questa.
   *
   * Le due richieste partono insieme con Promise.all: sono indipendenti,
   * aspettarle in fila raddoppierebbe l'attesa per niente.
   */
  const ricarica = useCallback(
    async (page = vistaRef.current.indice, size = vistaRef.current.dimensione) => {
      setCaricamento(true)
      try {
        const [p, conteggio] = await Promise.all([
          caricaPagina(utente, page, size),
          contaNonLette(utente),
        ])
        setPagina(p)
        setNonLette(conteggio)
        setErrore(null)
        // Se siamo sulla prima pagina abbiamo appena visto tutto: niente piu' arretrati.
        if (page === 0) setArretrate(0)
      } catch (e) {
        // Nel catch non si sa cosa e' stato lanciato: quasi sempre e' un Error, ma
        // potrebbe essere qualunque cosa, quindi il controllo lo facciamo comunque.
        setErrore(e instanceof Error ? e.message : String(e))
      } finally {
        // finally: il caricamento va spento sia se e' andata bene sia se e' andata male,
        // altrimenti la lista resta grigia per sempre dopo il primo errore.
        setCaricamento(false)
      }
    },
    [utente],
  )

  // Ricarica all'apertura e ogni volta che si cambia pagina, dimensione o utente.
  useEffect(() => {
    void ricarica(indice, dimensione)
  }, [ricarica, indice, dimensione])

  // --- Il lato WebSocket ----------------------------------------------------

  useEffect(() => {
    let socket
    let timer
    let chiusuraVoluta = false

    const apri = () => {
      // Il nome nell'indirizzo e' un ripiego didattico, non una soluzione: finisce
      // nei log del proxy e chiunque puo' scriverci il nome di un altro.
      socket = new WebSocket(`${WS_URL}?utente=${encodeURIComponent(utente)}`)
      socketRef.current = socket

      socket.onopen = () => {
        console.log('[WS] aperta per', utente)
        setConnesso(true)

        // Ricarichiamo anche qui, non solo all'avvio: mentre eravamo scollegati
        // possono essere nate notifiche che nessuno ha potuto spedirci.
        // E' il rimedio alla fragilita' del canale: la verita' sta sul database.
        void ricarica()

        // Le iscrizioni non sopravvivono alla connessione. Il server teneva
        // l'elenco degli ordini seguiti attaccato alla vecchia sessione, che ora
        // non esiste piu': va rimandato tutto da capo.
        for (const ordine of seguitiRef.current) {
          socket.send(JSON.stringify({ azione: 'segui', ordine }))
        }
      }

      socket.onmessage = (evento) => {
        // Arriva una stringa e nient'altro: nessuna intestazione, nessuna destinazione.
        // Il "canale" e' un campo che il backend ha messo dentro il contenuto.
        //
        // In TSX questa riga era piena di rassicurazioni per il compilatore
        // (JSON.parse(evento.data as string) as Busta). Qui non serve nessuna:
        // JSON.parse restituisce quello che trova, e che abbia i campi canale e
        // notifica e' una speranza basata sull'accordo col backend.
        let busta
        try {
          busta = JSON.parse(evento.data)
        } catch {
          console.warn('[WS] messaggio non in formato JSON, ignorato')
          return
        }
        console.log('[WS] ricevuto su', busta.canale, busta.notifica.title)

        if (busta.canale === 'personale') {
          setNonLette((n) => n + 1)

          // Se siamo sulla prima pagina rileggiamo dal server invece di incollare
          // la notifica in cima da soli: e' il server a decidere ordine e taglio
          // della pagina, e imitarlo qui vorrebbe dire duplicare quelle regole.
          if (vistaRef.current.indice === 0) void ricarica(0)
          else setArretrate((n) => n + 1)
          return
        }

        // Canali non personali: teniamo solo le ultime dieci buste, tanto servono
        // a mostrare cosa succede, non a essere consultate.
        setPubbliche((precedenti) =>
          [
            { ...busta, ricevutaAlle: new Date().toLocaleTimeString('it-IT') },
            ...precedenti,
          ].slice(0, 10),
        )
      }

      socket.onerror = (evento) => console.error('[WS] errore', evento)

      // Una WebSocket caduta non si riapre da sola: la riconnessione e' codice nostro.
      // Nella Dem 4 queste righe spariscono, le fa il client STOMP.
      socket.onclose = (evento) => {
        console.log('[WS] chiusa code=' + evento.code)
        setConnesso(false)
        if (!chiusuraVoluta) timer = window.setTimeout(apri, RITARDO_RICONNESSIONE)
      }
    }

    apri()

    // Pulizia: chiude la connessione quando il componente sparisce o cambia utente.
    // Senza, cambiando utente resterebbero aperte entrambe le connessioni e mario
    // continuerebbe a ricevere le sue notifiche mentre guardiamo la pagina di lucia.
    return () => {
      chiusuraVoluta = true
      window.clearTimeout(timer)
      socket.close()
    }
  }, [utente, ricarica])

  // --- I comandi che mandiamo sul canale ------------------------------------

  /**
   * Qui si tocca con mano il costo del WebSocket nudo: "seguire un argomento"
   * vuol dire inventarsi un comando, spedirlo, e sperare che il server lo capisca.
   * Nessuna conferma, nessun errore se il nome dell'azione e' sbagliato.
   *
   * Nella Dem 4 questa funzione diventa una riga: client.subscribe(destinazione, ...).
   */
  const segui = useCallback((ordine) => {
    const socket = socketRef.current
    if (socket?.readyState !== WebSocket.OPEN) {
      console.warn('[WS] non aperta: comando non inviato')
      return
    }
    socket.send(JSON.stringify({ azione: 'segui', ordine }))
    setOrdiniSeguiti((seguiti) => (seguiti.includes(ordine) ? seguiti : [...seguiti, ordine]))
  }, [])

  /**
   * Scrivere dentro un canale. Il server accetta solo se questa sessione segue
   * quell'ordine, e se rifiuta non lo comunica a nessuno: possiamo solo controllare
   * prima di mandare e sperare di avere la stessa idea del server su chi segue cosa.
   *
   * Restituisce true/false cosi' il componente sa se svuotare la casella di testo.
   */
  const pubblica = useCallback((ordine, testo) => {
    const socket = socketRef.current
    if (socket?.readyState !== WebSocket.OPEN) {
      console.warn('[WS] non aperta: messaggio non inviato')
      return false
    }
    if (!seguitiRef.current.includes(ordine)) {
      console.warn('[WS] ordine non seguito: il server scarterebbe il messaggio')
      return false
    }
    socket.send(JSON.stringify({ azione: 'pubblica', ordine, testo }))
    return true
  }, [])

  const smetti = useCallback((ordine) => {
    const socket = socketRef.current
    if (socket?.readyState === WebSocket.OPEN) {
      socket.send(JSON.stringify({ azione: 'smetti', ordine }))
    }
    // Togliamo l'ordine dall'elenco anche se la socket era chiusa: se non e' aperta,
    // il server ha gia' dimenticato tutto per conto suo.
    setOrdiniSeguiti((seguiti) => seguiti.filter((o) => o !== ordine))
  }, [])

  // --- Comandi per l'interfaccia --------------------------------------------

  const svuotaPubbliche = useCallback(() => setPubbliche([]), [])

  const vaiA = useCallback((page) => setIndice(Math.max(0, page)), [])

  // Cambiando quante notifiche stanno in una pagina, la pagina 3 non vuol piu' dire
  // la stessa cosa: si riparte dalla prima.
  const cambiaDimensione = useCallback((size) => {
    setDimensione(size)
    setIndice(0)
  }, [])

  const segnaTutte = useCallback(async () => {
    await segnaTutteLette(utente)
    await ricarica()
  }, [utente, ricarica])

  const segnaUna = useCallback(
    async (id) => {
      await segnaLetta(id)
      await ricarica()
    },
    [ricarica],
  )

  return {
    pagina,
    indice,
    dimensione,
    nonLette,
    arretrate,
    connesso,
    caricamento,
    errore,
    pubbliche,
    ordiniSeguiti,
    segui,
    smetti,
    pubblica,
    svuotaPubbliche,
    vaiA,
    cambiaDimensione,
    ricarica,
    segnaTutte,
    segnaUna,
  }
}
