import { useEffect, useState } from 'react'
import { leggiPresenza, leggiSessioni, type Presenza } from './api'
import { useCanale } from './useCanale'

const UTENTI = ['anna', 'bruno', 'carla']

export default function App() {
  const [utente, setUtente] = useState<string | null>(null)
  const [scelta, setScelta] = useState('anna')
  const [destinatario, setDestinatario] = useState('bruno')
  const [testo, setTesto] = useState('Ciao!')
  const [presenza, setPresenza] = useState<Presenza | null>(null)
  const [aperte, setAperte] = useState(0)

  const { stato, frames, errore, ping, inviaPrivato, svuota } = useCanale(utente)

  useEffect(() => {
    let attivo = true
    async function aggiorna() {
      const [p, s] = await Promise.all([leggiPresenza(), leggiSessioni()])
      if (!attivo) return
      setPresenza(p)
      setAperte(s.aperte)
    }
    void aggiorna()
    const timer = setInterval(() => void aggiorna(), 2000)
    return () => {
      attivo = false
      clearInterval(timer)
    }
  }, [])

  return (
    <main>
      <h1>Il canale privato</h1>
      <p className="sub">
        Tutti i client si iscrivono alla stessa stringa, <code>/user/queue/messaggi</code>, e ognuno riceve solo
        i propri messaggi. La traduzione avviene sul server.
      </p>

      <section className="card">
        <h2>1. Chi sei</h2>
        <p className="sub">
          Apri questa pagina in due schede — o in due browser — con utenti diversi.
        </p>
        <label htmlFor="utente">Utente</label>
        <select id="utente" value={scelta} onChange={(e) => setScelta(e.target.value)} disabled={!!utente}>
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>
        <div className="row">
          {!utente && <button onClick={() => setUtente(scelta)}>Collegati</button>}
          {utente && <button onClick={() => setUtente(null)}>Scollegati</button>}
          <span>
            <span className={`pallino ${stato === 'connesso' ? 'acceso' : 'spento'}`} />
            {utente ? `${utente} — ${stato}` : 'nessuna sessione'}
          </span>
        </div>
        {errore && <pre className="errore">{errore}</pre>}
      </section>

      <section className="card">
        <h2>2. Chi è collegato adesso</h2>
        <p className="sub">
          Da <code>SimpUserRegistry</code>. Vale per questo nodo soltanto: con più istanze ognuna conosce solo i
          propri collegati.
        </p>
        <table>
          <thead>
            <tr>
              <th>Utente</th>
              <th>Sessioni</th>
            </tr>
          </thead>
          <tbody>
            {(!presenza || presenza.collegati.length === 0) && (
              <tr>
                <td colSpan={2}>nessun utente collegato</td>
              </tr>
            )}
            {presenza?.collegati.map((c) => (
              <tr key={c.nome}>
                <td>
                  <strong>{c.nome}</strong>
                </td>
                <td>{c.sessioni}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <p className="sub">
          Registro: {presenza?.utenti ?? 0} utenti, {presenza?.sessioni ?? 0} sessioni · eventi di ciclo di vita:{' '}
          {aperte} connessioni aperte.
        </p>
      </section>

      <section className="card">
        <h2>3. Manda un messaggio a uno solo</h2>
        <label htmlFor="destinatario">Destinatario</label>
        <select id="destinatario" value={destinatario} onChange={(e) => setDestinatario(e.target.value)}>
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>
        <label htmlFor="testo">Testo</label>
        <input id="testo" value={testo} onChange={(e) => setTesto(e.target.value)} />
        <div className="row">
          <button onClick={() => inviaPrivato(destinatario, testo)} disabled={stato !== 'connesso'}>
            Invia su /app/privato
          </button>
          <button onClick={ping} disabled={stato !== 'connesso'}>
            Ping (rimbalza a me)
          </button>
          <button onClick={svuota}>Svuota il riquadro</button>
        </div>
        {stato !== 'connesso' && <p className="sub">L&#39;invio è disabilitato finché la connessione è chiusa.</p>}
      </section>

      <section className="card">
        <h2>4. Frame ricevuti su {'/user/queue/messaggi'}</h2>
        <table>
          <thead>
            <tr>
              <th>Ora</th>
              <th>Da</th>
              <th>Testo</th>
            </tr>
          </thead>
          <tbody>
            {frames.length === 0 && (
              <tr>
                <td colSpan={3}>nessun frame ricevuto</td>
              </tr>
            )}
            {frames.map((f, i) => (
              <tr key={`${f.istante}-${i}`}>
                <td>{new Date(f.istante).toLocaleTimeString('it-IT')}</td>
                <td>
                  <strong>{f.mittente}</strong>
                </td>
                <td>{f.testo}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  )
}
