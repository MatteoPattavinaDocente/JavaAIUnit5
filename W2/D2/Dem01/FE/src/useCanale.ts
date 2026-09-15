import { Client } from '@stomp/stompjs'
import { useCallback, useEffect, useRef, useState } from 'react'
import { login, type FrameLog } from './api'

/**
 * L'indirizzo passa dal server di sviluppo, che lo inoltra al backend grazie a
 * `ws: true` nel proxy di Vite. Costruirlo da window.location evita di scrivere
 * la porta a mano e funziona anche in produzione, dove diventa wss://.
 */
function brokerURL() {
  const schema = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${schema}://${window.location.host}/ws`
}

export type StatoConnessione = 'offline' | 'connesso' | 'in riconnessione'

/** La destinazione e' la stessa per tutti i client: la traduzione avviene sul server. */
const DESTINAZIONE = '/user/queue/messaggi'

export function useCanale(utente: string | null) {
  const [stato, setStato] = useState<StatoConnessione>('offline')
  const [frames, setFrames] = useState<FrameLog[]>([])
  const [errore, setErrore] = useState<string | null>(null)

  // Il client vive fuori dall'albero di React: un ref lo conserva fra un
  // ridisegno e l'altro senza provocarne altri (slide 28).
  const clientRef = useRef<Client | null>(null)

  useEffect(() => {
    if (!utente) return

    let attivo = true
    let client: Client | null = null

    async function apri() {
      const sessione = await login(utente!)
      if (!attivo) return

      client = new Client({
        brokerURL: brokerURL(),
        // Il token va nelle intestazioni del CONNECT, non nell'indirizzo:
        // la query string finisce nei log dei proxy (slide 9 e 33).
        connectHeaders: { Authorization: `Bearer ${sessione.token}` },
        reconnectDelay: 5000,

        onConnect: () => {
          setStato('connesso')
          setErrore(null)
          // La sottoscrizione va dichiarata QUI: dopo una riconnessione il
          // server non ricorda le iscrizioni precedenti (slide 27 e 31).
          client!.subscribe(DESTINAZIONE, (frame) => {
            const arrivato = JSON.parse(frame.body) as FrameLog
            setFrames((precedenti) => [arrivato, ...precedenti])
          })
        },

        // Il tubo e' caduto: reconnectDelay riprovera' da solo.
        onWebSocketClose: () => setStato((s) => (s === 'offline' ? s : 'in riconnessione')),

        onStompError: (frame) => {
          setErrore(frame.headers.message ?? 'errore dal broker')
          setStato('offline')
        },
      })

      client.activate()
      clientRef.current = client
    }

    void apri()

    // Senza questa pulizia il server accumula sessioni abbandonate, e in
    // sviluppo StrictMode ne apre subito una seconda (slide 30 e 34).
    return () => {
      attivo = false
      setStato('offline')
      void client?.deactivate()
      clientRef.current = null
    }
  }, [utente])

  const ping = useCallback(() => {
    clientRef.current?.publish({ destination: '/app/ping', body: '{}' })
  }, [])

  const inviaPrivato = useCallback((destinatario: string, testo: string) => {
    // Il corpo del frame e' SEMPRE una stringa: un oggetto passato a body
    // viaggia come «[object Object]» (slide 32 e 34).
    clientRef.current?.publish({
      destination: '/app/privato',
      body: JSON.stringify({ destinatario, testo }),
    })
  }, [])

  return { stato, frames, errore, ping, inviaPrivato, svuota: () => setFrames([]) }
}
