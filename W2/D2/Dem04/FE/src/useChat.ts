import { Client } from '@stomp/stompjs'
import { useCallback, useEffect, useRef, useState } from 'react'
import {
  cronologia,
  login,
  messaggiDopo,
  type Aggiornamento,
  type MessaggioRisposta,
} from './api'

function brokerURL() {
  const schema = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${schema}://${window.location.host}/ws`
}

export type StatoConnessione = 'offline' | 'connesso' | 'in riconnessione'

const MESSAGGI = '/user/queue/messaggi'
const AGGIORNAMENTI = '/user/queue/aggiornamenti'

/** L'indicatore si spegne da solo: il messaggio di fine puo' non arrivare mai (slide 41). */
const SCADENZA_SCRITTURA = 4000

/** Al massimo un frame «sta scrivendo» ogni tanto, non uno per tasto (slide 41). */
const FREQUENZA_SCRITTURA = 2500

/**
 * Unisce senza duplicare: il controllo sull'id evita di inserire due volte lo
 * stesso messaggio, e l'id temporaneo fa sostituire la riga mostrata in
 * anticipo invece di aggiungerne una seconda (slide 21 e 39).
 */
function unisci(precedenti: MessaggioRisposta[], arrivato: MessaggioRisposta): MessaggioRisposta[] {
  if (arrivato.idTemporaneo) {
    const posizione = precedenti.findIndex((m) => m.id === null && m.idTemporaneo === arrivato.idTemporaneo)
    if (posizione >= 0) {
      const copia = [...precedenti]
      copia[posizione] = arrivato
      return copia
    }
  }
  if (arrivato.id !== null && precedenti.some((m) => m.id === arrivato.id)) {
    return precedenti
  }
  return [...precedenti, arrivato]
}

export function useChat(utente: string, conChi: string) {
  const [stato, setStato] = useState<StatoConnessione>('offline')
  const [messaggi, setMessaggi] = useState<MessaggioRisposta[]>([])
  const [conversazione, setConversazione] = useState<number | null>(null)
  const [scrive, setScrive] = useState(false)

  const clientRef = useRef<Client | null>(null)
  // L'ultimo id noto, per chiedere il buco temporale dopo una riconnessione.
  const ultimoIdRef = useRef<number>(0)
  const conversazioneRef = useRef<number | null>(null)
  const timerScritturaRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const ultimaScritturaInviataRef = useRef(0)

  useEffect(() => {
    let attivo = true
    let client: Client | null = null

    // Il recupero del buco temporale: si chiama a OGNI collegamento riuscito,
    // quindi anche dopo una riconnessione automatica (slide 38).
    async function recupera() {
      const pagina =
        ultimoIdRef.current > 0
          ? await messaggiDopo(utente, conChi, ultimoIdRef.current)
          : await cronologia(utente, conChi)
      if (!attivo) return
      setConversazione(pagina.conversazione)
      conversazioneRef.current = pagina.conversazione
      if (pagina.ultimoId && pagina.ultimoId > ultimoIdRef.current) {
        ultimoIdRef.current = pagina.ultimoId
      }
      setMessaggi((precedenti) => pagina.messaggi.reduce(unisci, precedenti))
    }

    async function apri() {
      const sessione = await login(utente)
      if (!attivo) return

      const pagina = await cronologia(utente, conChi)
      if (!attivo) return
      setMessaggi(pagina.messaggi)
      setConversazione(pagina.conversazione)
      conversazioneRef.current = pagina.conversazione
      ultimoIdRef.current = pagina.ultimoId ?? 0

      client = new Client({
        brokerURL: brokerURL(),
        connectHeaders: { Authorization: `Bearer ${sessione.token}` },
        reconnectDelay: 3000,

        onConnect: () => {
          setStato('connesso')

          client!.subscribe(MESSAGGI, (frame) => {
            const arrivato = JSON.parse(frame.body) as MessaggioRisposta
            if (arrivato.mittente !== conChi && arrivato.destinatario !== conChi) return
            // Il PRIMO messaggio di una chat nuova porta con sé l'id della
            // conversazione appena creata dal server. Senza raccoglierlo qui
            // l'id resta null per chi lo riceve: le conferme di lettura non
            // partono e gli aggiornamenti dell'altro vengono scartati dal
            // filtro sulla conversazione.
            if (conversazioneRef.current === null && arrivato.conversazione !== null) {
              conversazioneRef.current = arrivato.conversazione
              setConversazione(arrivato.conversazione)
            }
            if (arrivato.id !== null && arrivato.id > ultimoIdRef.current) {
              ultimoIdRef.current = arrivato.id
            }
            setMessaggi((precedenti) => unisci(precedenti, arrivato))
            if (arrivato.mittente === conChi) setScrive(false)
          })

          client!.subscribe(AGGIORNAMENTI, (frame) => {
            const agg = JSON.parse(frame.body) as Aggiornamento
            if (agg.conversazione !== conversazioneRef.current) return

            if (agg.tipo === 'LETTI') {
              const letti = new Set(agg.messaggi)
              setMessaggi((precedenti) =>
                precedenti.map((m) => (m.id !== null && letti.has(m.id) ? { ...m, stato: 'LETTO' } : m)),
              )
              return
            }

            if (agg.tipo === 'SCRIVE' && agg.utente === conChi) {
              setScrive(true)
              if (timerScritturaRef.current) clearTimeout(timerScritturaRef.current)
              timerScritturaRef.current = setTimeout(() => setScrive(false), SCADENZA_SCRITTURA)
            }
          })

          // Dopo il collegamento si chiede quello che e' arrivato mentre la
          // connessione era chiusa: il broker non lo ripete.
          void recupera()
        },

        onWebSocketClose: () => setStato((s) => (s === 'offline' ? s : 'in riconnessione')),
        onStompError: () => setStato('offline'),
      })

      client.activate()
      clientRef.current = client
    }

    void apri()

    return () => {
      attivo = false
      setStato('offline')
      if (timerScritturaRef.current) clearTimeout(timerScritturaRef.current)
      void client?.deactivate()
      clientRef.current = null
      ultimoIdRef.current = 0
    }
  }, [utente, conChi])

  /**
   * Invio ottimistico: la riga compare subito con un id temporaneo, e viene
   * sostituita quando torna il messaggio salvato (slide 21).
   */
  const invia = useCallback(
    (testo: string) => {
      const idTemporaneo = `temp-${crypto.randomUUID()}`
      const temporaneo: MessaggioRisposta = {
        id: null,
        conversazione: conversazioneRef.current,
        mittente: utente,
        destinatario: conChi,
        testo,
        istante: new Date().toISOString(),
        stato: 'IN INVIO',
        idTemporaneo,
      }
      setMessaggi((precedenti) => [...precedenti, temporaneo])
      clientRef.current?.publish({
        destination: '/app/chat',
        body: JSON.stringify({ destinatario: conChi, testo, idTemporaneo }),
      })
    },
    [utente, conChi],
  )

  /** Il conteggio scende quando il client dichiara di aver MOSTRATO i messaggi. */
  const segnaLetti = useCallback(() => {
    const id = conversazioneRef.current
    if (id === null) return
    clientRef.current?.publish({
      destination: '/app/letti',
      body: JSON.stringify({ conversazione: id }),
    })
  }, [])

  const segnalaScrittura = useCallback(() => {
    const adesso = Date.now()
    if (adesso - ultimaScritturaInviataRef.current < FREQUENZA_SCRITTURA) return
    ultimaScritturaInviataRef.current = adesso
    clientRef.current?.publish({
      destination: '/app/scrivendo',
      body: JSON.stringify({ destinatario: conChi }),
    })
  }, [conChi])

  return { stato, messaggi, conversazione, scrive, invia, segnaLetti, segnalaScrittura }
}
