import { Client } from '@stomp/stompjs'
import { useEffect, useMemo, useRef, useState } from 'react'
import type { ReactNode } from 'react'
import { API_BASE } from '../api/client'
import type { Notifica } from '../api/types'
import { useAuth } from '../auth/authContext'
import { StompContext } from './stompContext'
import type { AscoltatoreNotifica, StompContextValue } from './stompContext'

const URL_WS = API_BASE.replace(/^http/, 'ws') + '/ws'

export function StompProvider({ children }: { children: ReactNode }) {
  const { sessione } = useAuth()
  const [attivo, setAttivo] = useState(false)
  const ascoltatori = useRef(new Set<AscoltatoreNotifica>())

  useEffect(() => {
    if (!sessione) return

    const client = new Client({
      brokerURL: URL_WS,
      // il backend autentica la sessione STOMP leggendo questo header nel frame CONNECT
      connectHeaders: { Authorization: `Bearer ${sessione.token}` },
      reconnectDelay: 4000,
      onConnect: () => {
        setAttivo(true)
        client.subscribe('/user/queue/notifiche', (frame) => {
          const notifica = JSON.parse(frame.body) as Notifica
          ascoltatori.current.forEach((ascolta) => ascolta(notifica))
        })
      },
      onDisconnect: () => setAttivo(false),
      onWebSocketClose: () => setAttivo(false),
      onStompError: () => {
        // token rifiutato dal broker: insistere con la riconnessione non servirebbe
        setAttivo(false)
        void client.deactivate()
      },
    })

    client.activate()
    return () => {
      setAttivo(false)
      void client.deactivate()
    }
  }, [sessione])

  const value = useMemo<StompContextValue>(
    () => ({
      // senza sessione non c'e' connessione, a prescindere dallo stato del client
      connesso: Boolean(sessione) && attivo,
      onNotifica: (ascoltatore) => {
        const insieme = ascoltatori.current
        insieme.add(ascoltatore)
        return () => {
          insieme.delete(ascoltatore)
        }
      },
    }),
    [sessione, attivo],
  )

  return <StompContext.Provider value={value}>{children}</StompContext.Provider>
}
