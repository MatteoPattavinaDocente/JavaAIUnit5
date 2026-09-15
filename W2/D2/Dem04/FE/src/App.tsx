import { useCallback, useEffect, useState } from 'react'
import { azzeraTutto, conversazioni, riempi, type ConversazioneRiepilogo } from './api'
import { ListaMessaggi } from './ListaMessaggi'
import { useChat } from './useChat'

const UTENTI = ['anna', 'bruno', 'carla']

export default function App() {
  const [utente, setUtente] = useState('anna')
  const [conChi, setConChi] = useState('bruno')
  const [bozza, setBozza] = useState('')
  const [elenco, setElenco] = useState<ConversazioneRiepilogo[]>([])

  const { stato, messaggi, conversazione, scrive, invia, segnaLetti, segnalaScrittura } = useChat(
    utente,
    conChi,
  )

  const ricaricaElenco = useCallback(async () => {
    setElenco(await conversazioni(utente))
  }, [utente])

  useEffect(() => {
    void ricaricaElenco()
    const timer = setInterval(() => void ricaricaElenco(), 2000)
    return () => clearInterval(timer)
  }, [ricaricaElenco])

  // Il conteggio scende quando il client dichiara di aver MOSTRATO i messaggi,
  // non quando li riceve (slide 42). Qui il gesto e' l'avere la conversazione
  // aperta con la finestra a fuoco.
  useEffect(() => {
    if (stato !== 'connesso' || messaggi.length === 0) return
    const daLeggere = messaggi.some((m) => m.destinatario === utente && m.stato !== 'LETTO')
    if (daLeggere) segnaLetti()
  }, [messaggi, stato, utente, segnaLetti])

  function spedisci() {
    if (!bozza.trim() || stato !== 'connesso') return
    invia(bozza)
    setBozza('')
  }

  return (
    <main>
      <h1>La chat completa</h1>
      <p className="sub">
        Cronologia e tempo reale nella stessa lista, riconnessione automatica, non letti contati dal server.
      </p>

      <section className="card">
        <div className="row">
          <span>
            <span className={`pallino ${stato === 'connesso' ? 'acceso' : 'spento'}`} />
            {utente} · {stato}
          </span>
        </div>

        {stato !== 'connesso' && (
          <p className="errore">
            Connessione interrotta: l&#39;invio è disabilitato. Una riga che lo dichiara è più utile di una chat
            che sembra vuota.
          </p>
        )}

        <label htmlFor="utente">Sei</label>
        <select
          id="utente"
          value={utente}
          onChange={(e) => {
            const nuovo = e.target.value
            setUtente(nuovo)
            if (nuovo === conChi) setConChi(UTENTI.find((u) => u !== nuovo)!)
          }}
        >
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>

        <div className="row">
          <button onClick={() => void riempi(utente, conChi, 120).then(ricaricaElenco)}>
            Riempi con 120 messaggi
          </button>
          <button
            onClick={async () => {
              await azzeraTutto()
              await ricaricaElenco()
            }}
          >
            Azzera tutto
          </button>
        </div>
      </section>

      <section className="card">
        <h2>Conversazioni</h2>
        <table>
          <thead>
            <tr>
              <th>Con</th>
              <th>Ultimo messaggio</th>
              <th>Non letti</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {elenco.length === 0 && (
              <tr>
                <td colSpan={4}>nessuna conversazione</td>
              </tr>
            )}
            {elenco.map((c) => (
              <tr key={c.id}>
                <td>
                  <span className={`pallino ${c.controparteCollegata ? 'acceso' : 'spento'}`} />
                  <strong>{c.controparte}</strong>
                </td>
                <td className="troncato">{c.ultimoTesto ?? '—'}</td>
                <td>{c.nonLetti > 0 ? <span className="badge">{c.nonLetti}</span> : '—'}</td>
                <td>
                  <button onClick={() => setConChi(c.controparte)} disabled={c.controparte === conChi}>
                    Apri
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="row">
          {UTENTI.filter((u) => u !== utente && !elenco.some((c) => c.controparte === u)).map((u) => (
            <button key={u} onClick={() => setConChi(u)} disabled={u === conChi}>
              Inizia con {u}
            </button>
          ))}
        </div>
      </section>

      <section className="card">
        <h2>
          Conversazione con {conChi}
          {conversazione !== null && <span className="sub"> · id {conversazione}</span>}
        </h2>

        <ListaMessaggi messaggi={messaggi} utente={utente} />

        <p className="scrive">{scrive ? `${conChi} sta scrivendo…` : ' '}</p>

        <label htmlFor="bozza">Messaggio</label>
        <input
          id="bozza"
          value={bozza}
          onChange={(e) => {
            setBozza(e.target.value)
            segnalaScrittura()
          }}
          onKeyDown={(e) => {
            if (e.key === 'Enter') spedisci()
          }}
          placeholder={`scrivi a ${conChi}…`}
          disabled={stato !== 'connesso'}
        />
        <div className="row">
          <button onClick={spedisci} disabled={stato !== 'connesso'}>
            Invia
          </button>
          <span className="sub">{messaggi.length} messaggi in lista</span>
        </div>
      </section>
    </main>
  )
}
