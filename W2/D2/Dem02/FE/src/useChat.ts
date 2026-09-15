import { Client } from '@stomp/stompjs'
import { useCallback, useEffect, useRef, useState } from 'react'
import { cronologia, login, type MessaggioRisposta } from './api'

function brokerURL() {
  const schema = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${schema}://${window.location.host}/ws`
}

export type StatoConnessione = 'offline' | 'connesso' | 'in riconnessione'

const DESTINAZIONE = '/user/queue/messaggi'

export function useChat(utente: string | null, conChi: string) {
  const [stato, setStato] = useState<StatoConnessione>('offline')
  const [messaggi, setMessaggi] = useState<MessaggioRisposta[]>([])
  const clientRef = useRef<Client | null>(null)

  // Ricarica la cronologia dal database: e' il gesto che smaschera un
  // messaggio consegnato ma non salvato (slide 18).
  const ricarica = useCallback(async () => {
    if (!utente) return
    setMessaggi(await cronologia(utente, conChi))
  }, [utente, conChi])

  useEffect(() => {
    if (!utente) {
      setMessaggi([])
      return
    }

    let attivo = true
    let client: Client | null = null

    async function apri() {
      const sessione = await login(utente!)
      if (!attivo) return

      setMessaggi(await cronologia(utente!, conChi))
      if (!attivo) return

      client = new Client({
        brokerURL: brokerURL(),
        connectHeaders: { Authorization: `Bearer ${sessione.token}` },
        reconnectDelay: 5000,

        onConnect: () => {
          setStato('connesso')
          client!.subscribe(DESTINAZIONE, (frame) => {
            const arrivato = JSON.parse(frame.body) as MessaggioRisposta
            // Il messaggio va aggiunto solo se riguarda la conversazione
            // aperta: il canale consegna tutto quello che arriva all'utente.
            const dentroLaConversazione =
              arrivato.mittente === conChi || arrivato.destinatario === conChi
            if (!dentroLaConversazione) return
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
      setStato('offline')
      void client?.deactivate()
      clientRef.current = null
    }
  }, [utente, conChi])

  const invia = useCallback(
    (testo: string, difettoso: boolean) => {
      clientRef.current?.publish({
        destination: difettoso ? '/app/chat-difettoso' : '/app/chat',
        body: JSON.stringify({ destinatario: conChi, testo }),
      })
    },
    [conChi],
  )

  return { stato, messaggi, invia, ricarica }
}
