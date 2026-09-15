import { useEffect, useState } from 'react'
import { azzeraArchivio, leggiPresenza, type Presenza } from './api'
import { useChat } from './useChat'

const UTENTI = ['anna', 'bruno', 'carla']

export default function App() {
  const [utente, setUtente] = useState<string | null>(null)
  const [scelta, setScelta] = useState('anna')
  const [conChi, setConChi] = useState('bruno')
  const [bozza, setBozza] = useState('')
  const [difettoso, setDifettoso] = useState(false)
  const [presenza, setPresenza] = useState<Presenza | null>(null)

  const { stato, messaggi, invia, ricarica } = useChat(utente, conChi)

  useEffect(() => {
    let attivo = true
    async function aggiorna() {
      const p = await leggiPresenza()
      if (attivo) setPresenza(p)
    }
    void aggiorna()
    const timer = setInterval(() => void aggiorna(), 2000)
    return () => {
      attivo = false
      clearInterval(timer)
    }
  }, [])

  function spedisci() {
    if (!bozza.trim()) return
    invia(bozza, difettoso)
    setBozza('')
  }

  return (
    <main>
      <h1>Invio e ricezione</h1>
      <p className="sub">
        Il messaggio viene <strong>salvato</strong> e poi consegnato al destinatario e al mittente. La cronologia
        arriva da REST, il tempo reale dal canale.
      </p>

      <section className="card">
        <h2>Chi sei, e con chi parli</h2>
        <div className="row">
          <span>
            <span className={`pallino ${stato === 'connesso' ? 'acceso' : 'spento'}`} />
            {utente ? `${utente} — ${stato}` : 'nessuna sessione'}
          </span>
        </div>
        <label htmlFor="utente">Utente</label>
        <select
          id="utente"
          value={scelta}
          onChange={(e) => {
            const nuovo = e.target.value
            setScelta(nuovo)
            // Senza questa riga conChi resta sul nome appena scelto: la select
            // sotto non ha piu' quell'opzione e mostra un campo vuoto, ma lo
            // stato vale ancora l'utente stesso. Si finisce a scrivere a se'
            // stessi e l'altra scheda non riceve nulla.
            if (nuovo === conChi) setConChi(UTENTI.find((u) => u !== nuovo)!)
          }}
          disabled={!!utente}
        >
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>
        <label htmlFor="conChi">Conversazione con</label>
        <select id="conChi" value={conChi} onChange={(e) => setConChi(e.target.value)}>
          {UTENTI.filter((u) => u !== (utente ?? scelta)).map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>
        <div className="row">
          {!utente && <button onClick={() => setUtente(scelta)}>Collegati</button>}
          {utente && <button onClick={() => setUtente(null)}>Scollegati</button>}
          <button onClick={() => void ricarica()}>Ricarica dal database</button>
          <button
            onClick={async () => {
              await azzeraArchivio()
              await ricarica()
            }}
          >
            Azzera l&#39;archivio
          </button>
        </div>
        <p className="sub">
          Collegati: {presenza?.collegati.map((c) => `${c.nome}(${c.sessioni})`).join(' · ') || 'nessuno'}
        </p>
      </section>

      <section className="card">
        <h2>Conversazione</h2>
        <ul className="messaggi">
          {messaggi.length === 0 && <li className="vuoto">nessun messaggio</li>}
          {messaggi.map((m, i) => (
            <li key={m.id ?? `finto-${i}`} className={m.mittente === utente ? 'mio' : 'altrui'}>
              <p>{m.testo}</p>
              <span className="meta">
                {m.mittente} · {new Date(m.istante).toLocaleTimeString('it-IT')} · id {m.id ?? '—'} ·{' '}
                <span className={m.id === null ? 'errore' : ''}>{m.stato}</span>
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
            Invia
          </button>
          <label className="inline">
            <input type="checkbox" checked={difettoso} onChange={(e) => setDifettoso(e.target.checked)} />{' '}
            consegna <strong>senza</strong> salvare (il difetto della slide 18)
          </label>
        </div>
        {difettoso && (
          <p className="errore">
            Il messaggio comparirà nella chat con id <code>—</code> e stato <code>MAI SALVATO</code>: premi
            <em> Ricarica dal database</em> e sparisce.
          </p>
        )}
      </section>
    </main>
  )
}
