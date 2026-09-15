import { useEffect, useState } from 'react'
import { azzeraStorico, leggiSessioni, type Sessioni } from './api'
import { Conversazione } from './Conversazione'

const UTENTI = ['anna', 'bruno', 'carla']

export default function App() {
  const [utente, setUtente] = useState('anna')
  const [conChi, setConChi] = useState('bruno')
  const [montato, setMontato] = useState(false)
  const [pulizia, setPulizia] = useState(true)
  const [sessioni, setSessioni] = useState<Sessioni | null>(null)

  useEffect(() => {
    let attivo = true
    async function aggiorna() {
      const s = await leggiSessioni()
      if (attivo) setSessioni(s)
    }
    void aggiorna()
    const timer = setInterval(() => void aggiorna(), 1500)
    return () => {
      attivo = false
      clearInterval(timer)
    }
  }, [])

  return (
    <main>
      <h1>Il client React</h1>
      <p className="sub">
        Un effetto, una pulizia, una connessione. Il client STOMP non è un componente: vive fuori dall&#39;albero
        di React e va chiuso a mano.
      </p>

      <section className="card">
        <h2>1. Il componente che possiede la connessione</h2>
        <label htmlFor="utente">Utente</label>
        <select
          id="utente"
          value={utente}
          onChange={(e) => {
            const nuovo = e.target.value
            setUtente(nuovo)
            // conChi non si aggiorna da solo: senza questo la select sotto
            // perde la propria opzione, resta vuota, e la conversazione parte
            // con se' stessi come destinatario.
            if (nuovo === conChi) setConChi(UTENTI.find((u) => u !== nuovo)!)
          }}
          disabled={montato}
        >
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>
        <label htmlFor="conChi">Conversazione con</label>
        <select id="conChi" value={conChi} onChange={(e) => setConChi(e.target.value)} disabled={montato}>
          {UTENTI.filter((u) => u !== utente).map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>

        <div className="row">
          <button onClick={() => setMontato(!montato)}>
            {montato ? 'Smonta il componente' : 'Monta il componente'}
          </button>
          <label className="inline">
            <input
              type="checkbox"
              checked={pulizia}
              onChange={(e) => setPulizia(e.target.checked)}
              disabled={montato}
            />{' '}
            la funzione di pulizia chiama <code>deactivate()</code>
          </label>
        </div>
        {!pulizia && (
          <p className="errore">
            Senza pulizia lo smontaggio <strong>non</strong> chiude la connessione: guarda il contatore qui sotto
            salire e non scendere più.
          </p>
        )}
      </section>

      {montato && (
        <section className="card">
          <h2>2. La conversazione</h2>
          <Conversazione utente={utente} conChi={conChi} pulizia={pulizia} />
        </section>
      )}

      <section className="card">
        <h2>3. Le sessioni viste dal server</h2>
        <p className="sub">
          Con <code>StrictMode</code> attivo, in sviluppo React monta il componente, lo smonta e lo rimonta
          subito: nel log del server compaiono <strong>due</strong> connessioni e una disconnessione.
        </p>
        <p>
          Connessioni aperte adesso: <strong>{sessioni?.aperte ?? 0}</strong>
        </p>
        <div className="row">
          <button onClick={() => void azzeraStorico()}>Svuota lo storico</button>
        </div>
        <table>
          <thead>
            <tr>
              <th>Ora</th>
              <th>Evento</th>
              <th>Utente</th>
              <th>Aperte dopo</th>
            </tr>
          </thead>
          <tbody>
            {(!sessioni || sessioni.storico.length === 0) && (
              <tr>
                <td colSpan={4}>nessun evento</td>
              </tr>
            )}
            {sessioni?.storico.map((e, i) => (
              <tr key={`${e.istante}-${i}`}>
                <td>{new Date(e.istante).toLocaleTimeString('it-IT')}</td>
                <td className={e.tipo === 'APERTA' ? 'ok' : 'errore'}>{e.tipo}</td>
                <td>{e.utente}</td>
                <td>{e.aperteDopo}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  )
}
