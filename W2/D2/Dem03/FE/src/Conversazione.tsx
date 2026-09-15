import { useState } from 'react'
import { useChat } from './useChat'

type Props = {
  utente: string
  conChi: string
  pulizia: boolean
}

/**
 * Il componente che possiede la connessione. Sta in un file a parte perche' la
 * demo lo monta e lo smonta a comando: e' il gesto che rende visibile la
 * funzione di pulizia dell'effetto.
 */
export function Conversazione({ utente, conChi, pulizia }: Props) {
  const { stato, messaggi, invia } = useChat(utente, conChi, { pulizia })
  const [bozza, setBozza] = useState('')

  function spedisci() {
    if (!bozza.trim()) return
    invia(bozza)
    setBozza('')
  }

  return (
    <div>
      <div className="row">
        <span>
          <span className={`pallino ${stato === 'connesso' ? 'acceso' : 'spento'}`} />
          {utente} → {conChi} · {stato}
        </span>
      </div>

      <ul className="messaggi">
        {messaggi.length === 0 && <li className="vuoto">nessun messaggio</li>}
        {messaggi.map((m) => (
          <li key={m.id} className={m.mittente === utente ? 'mio' : 'altrui'}>
            <p>{m.testo}</p>
            <span className="meta">
              {m.mittente} · {new Date(m.istante).toLocaleTimeString('it-IT')}
            </span>
          </li>
        ))}
      </ul>

      <label htmlFor="bozza">Messaggio</label>
      <input
        id="bozza"
        value={bozza}
        onChange={(e) => setBozza(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === 'Enter') spedisci()
        }}
        placeholder={`scrivi a ${conChi}…`}
      />
      <div className="row">
        <button onClick={spedisci} disabled={stato !== 'connesso'}>
          Pubblica su /app/chat
        </button>
      </div>
    </div>
  )
}
