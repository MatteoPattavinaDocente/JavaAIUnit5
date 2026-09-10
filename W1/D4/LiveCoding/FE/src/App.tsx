import { useCallback, useEffect, useState } from 'react'
import './App.css'
import { api } from './api/client'
import type { Canale } from './api/types'
import { AuthProvider } from './auth/AuthProvider'
import { useAuth } from './auth/authContext'
import { CanalePage } from './components/CanalePage'
import { CanaliPage } from './components/CanaliPage'
import { ComponiNotifica } from './components/ComponiNotifica'
import { LoginPage } from './components/LoginPage'
import { PannelloNotifiche } from './components/PannelloNotifiche'
import { StompProvider } from './ws/StompProvider'
import { useStomp } from './ws/stompContext'

function Applicazione() {
  const { sessione, logout } = useAuth()
  const { connesso, onNotifica } = useStomp()
  const [nonLette, setNonLette] = useState(0)
  /** Incrementato a ogni evento che puo' cambiare il numero di non lette. */
  const [versioneConteggio, setVersioneConteggio] = useState(0)
  /** Quando e' valorizzato si mostra la pagina del canale al posto dell'elenco. */
  const [canaleAperto, setCanaleAperto] = useState<Canale | null>(null)
  /** Bump per far ricaricare l'elenco quando le iscrizioni cambiano dalla pagina del canale. */
  const [versioneIscrizioni, setVersioneIscrizioni] = useState(0)

  const aggiornaConteggio = useCallback(() => setVersioneConteggio((v) => v + 1), [])

  useEffect(() => {
    let annullato = false

    async function leggiConteggio() {
      try {
        const risposta = await api.contaNonLette()
        if (!annullato) setNonLette(risposta.nonLette)
      } catch {
        // conteggio non critico: si riprova al prossimo evento
      }
    }

    void leggiConteggio()
    return () => {
      annullato = true
    }
  }, [versioneConteggio])

  useEffect(() => onNotifica(aggiornaConteggio), [onNotifica, aggiornaConteggio])

  return (
    <div className="app">
      <header className="barra">
        <h1 className="titolo-app">
          <span className="pallino" /> LiveCoding
        </h1>

        <div className="barra-destra">
          <span className={connesso ? 'stato online' : 'stato offline'}>
            <span className="spia" />
            {connesso ? 'Realtime attivo' : 'Realtime offline'}
          </span>

          <span className="campanella" title={`${nonLette} notifiche non lette`}>
            🔔
            {nonLette > 0 && <span className="badge badge-campanella">{nonLette}</span>}
          </span>

          <span className="utente" title={`ID: ${sessione?.id}`}>
            {sessione?.username}
          </span>

          <button className="bottone piccolo attenuato" onClick={() => void logout()}>
            Esci
          </button>
        </div>
      </header>

      <main className="griglia">
        {canaleAperto ? (
          <CanalePage
            key={canaleAperto.id}
            idCanale={canaleAperto.id}
            canaleIniziale={canaleAperto}
            onIndietro={() => setCanaleAperto(null)}
            onIscrizioniCambiate={() => setVersioneIscrizioni((v) => v + 1)}
          />
        ) : (
          <CanaliPage onApriCanale={setCanaleAperto} versioneEsterna={versioneIscrizioni} />
        )}

        <div className="colonna">
          <ComponiNotifica />
          <PannelloNotifiche nonLette={nonLette} onConteggio={aggiornaConteggio} />
        </div>
      </main>

      <footer className="pie">
        Il tuo ID utente è <code>{sessione?.id}</code> — usalo per provare le notifiche personali.
      </footer>
    </div>
  )
}

function Radice() {
  const { sessione } = useAuth()
  return sessione ? <Applicazione /> : <LoginPage />
}

export default function App() {
  return (
    <AuthProvider>
      <StompProvider>
        <Radice />
      </StompProvider>
    </AuthProvider>
  )
}
