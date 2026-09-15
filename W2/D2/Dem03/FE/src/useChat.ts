import { Client, type StompSubscription } from '@stomp/stompjs'
import { useCallback, useEffect, useRef, useState } from 'react'
import { cronologia, login, type MessaggioRisposta } from './api'

/**
 * L'indirizzo passa dal server di sviluppo, che lo inoltra al backend grazie a
 * `ws: true` nel proxy di Vite (vite.config.ts). Senza quell'opzione la
 * richiesta di upgrade resta a Vite e il browser parla di handshake fallito,
 * senza mai nominare il proxy (slide 33 e 46).
 */
function brokerURL() {
  const schema = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${schema}://${window.location.host}/ws`
}

export type StatoConnessione = 'offline' | 'connesso' | 'in riconnessione'

const DESTINAZIONE = '/user/queue/messaggi'

export type OpzioniChat = {
  /**
   * Se falso l'effetto NON chiude la connessione allo smontaggio. Serve solo
   * per far vedere in aula che cosa succede: il server accumula sessioni
   * abbandonate (slide 30 e 34). Non e' un'opzione da avere in un progetto.
   */
  pulizia: boolean
}

export function useChat(utente: string, conChi: string, opzioni: OpzioniChat) {
  const [stato, setStato] = useState<StatoConnessione>('offline')
  const [messaggi, setMessaggi] = useState<MessaggioRisposta[]>([])

  // Il client vive FUORI dall'albero di React: non e' un componente e non deve
  // provocare ridisegni. Un ref lo conserva fra un ridisegno e l'altro
  // (slide 26 e 28).
  const clientRef = useRef<Client | null>(null)
  const subRef = useRef<StompSubscription | null>(null)

  useEffect(() => {
    // Catturata all'apertura: vale per tutta la vita di QUESTA connessione.
    const puliziaAttiva = opzioni.pulizia

    let attivo = true
    let client: Client | null = null

    async function apri() {
      const sessione = await login(utente)
      if (!attivo) return

      const storico = await cronologia(utente, conChi)
      if (!attivo) return
      setMessaggi(storico)

      client = new Client({
        brokerURL: brokerURL(),
        // Il token nelle intestazioni del CONNECT, mai nella query string
        // (slide 9 e 33).
        connectHeaders: { Authorization: `Bearer ${sessione.token}` },
        // L'attesa fra due tentativi di riconnessione, in millisecondi.
        reconnectDelay: 5000,

        // Richiamata a OGNI collegamento riuscito, anche dopo una
        // riconnessione automatica. Per questo le sottoscrizioni si dichiarano
        // qui e non subito dopo activate(): dopo una riconnessione il server
        // non ricorda le iscrizioni precedenti (slide 27 e 31).
        onConnect: () => {
          setStato('connesso')
          subRef.current = client!.subscribe(DESTINAZIONE, (frame) => {
            const arrivato = JSON.parse(frame.body) as MessaggioRisposta
            if (arrivato.mittente !== conChi && arrivato.destinatario !== conChi) return
            // La forma funzionale riceve lo stato precedente aggiornato:
            // usare la variabile catturata perderebbe i messaggi arrivati nel
            // frattempo (slide 34).
            setMessaggi((precedenti) => [...precedenti, arrivato])
          })
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
      if (!puliziaAttiva) {
        // Il caso da NON scrivere mai: la connessione resta aperta e il server
        // la conta ancora. Con StrictMode se ne vede subito una di troppo.
        console.warn('[useChat] smontato SENZA pulizia: la connessione resta aperta')
        return
      }
      setStato('offline')
      subRef.current?.unsubscribe()
      subRef.current = null
      void client?.deactivate()
      clientRef.current = null
    }
  }, [utente, conChi, opzioni.pulizia])

  const invia = useCallback(
    (testo: string) => {
      // Il corpo e' SEMPRE una stringa: un oggetto passato a body viaggia come
      // «[object Object]» (slide 32 e 34).
      clientRef.current?.publish({
        destination: '/app/chat',
        body: JSON.stringify({ destinatario: conChi, testo }),
      })
    },
    [conChi],
  )

  return { stato, messaggi, invia }
}
