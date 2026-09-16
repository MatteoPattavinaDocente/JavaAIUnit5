import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'

type Configurazione = {
  baseUrl: string
  modello: string
  chiavePresente: boolean
}

type RiassuntoRisposta = {
  riassunto: string
  modello: string
  tokenIngresso: number
  tokenUscita: number
  motivoArresto: string
  millisEsterni: number
}

type Errore = {
  status: number
  error: string
  messages: string[]
}

const TESTO_INIZIALE = `Il backend come proxy non e' soltanto una questione di sicurezza.
E' il punto in cui misuriamo il traffico, contiamo le richieste, fermiamo un consumo
anomalo e teniamo stabile il contratto verso il nostro frontend anche quando il
servizio esterno cambia formato. La chiave, poi, non puo' stare nel browser: tutto
quello che sta in una pagina web e' leggibile.`

export default function App() {
  const [config, setConfig] = useState<Configurazione | null>(null)
  const [testo, setTesto] = useState(TESTO_INIZIALE)
  const [risposta, setRisposta] = useState<RiassuntoRisposta | null>(null)
  const [errore, setErrore] = useState<Errore | null>(null)
  const [inCorso, setInCorso] = useState(false)
  const [millisTotali, setMillisTotali] = useState(0)

  useEffect(() => {
    let attivo = true
    fetch(`${API}/configurazione`)
      .then((r) => r.json())
      .then((c) => {
        if (attivo) setConfig(c)
      })
    return () => {
      attivo = false
    }
  }, [])

  async function riassumi() {
    setInCorso(true)
    setRisposta(null)
    setErrore(null)
    const inizio = performance.now()
    try {
      const res = await fetch(`${API}/riassunto`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ testo }),
      })
      setMillisTotali(Math.round(performance.now() - inizio))
      if (res.ok) {
        setRisposta(await res.json())
      } else {
        setErrore(await res.json())
      }
    } catch (e) {
      setErrore({ status: 0, error: 'Rete', messages: [String(e)] })
    }
    setInCorso(false)
  }

  return (
    <main>
      <h1>Il backend come proxy</h1>
      <p className="sub">
        Il browser chiama <code>/api/riassunto</code> sul nostro server. Il nostro server chiama il servizio
        esterno. Il browser non parla mai con <code>openrouter.ai</code>, e non vede mai la chiave.
      </p>

      <section className="card">
        <h2>Configurazione del server</h2>
        {!config && <p>lettura in corso…</p>}
        {config && (
          <table>
            <tbody>
              <tr>
                <th>Indirizzo base</th>
                <td>
                  <code>{config.baseUrl}</code>
                </td>
              </tr>
              <tr>
                <th>Modello</th>
                <td>
                  <code>{config.modello}</code>
                </td>
              </tr>
              <tr>
                <th>Chiave</th>
                <td className={config.chiavePresente ? 'ok' : 'errore'}>
                  {config.chiavePresente ? 'presente sul server' : 'assente: le chiamate daranno 401'}
                </td>
              </tr>
            </tbody>
          </table>
        )}
        <p className="sub">
          Della chiave il frontend sa soltanto <strong>se c&#39;è</strong>. Non il valore, nemmeno abbreviato.
        </p>
      </section>

      <section className="card">
        <h2>Il testo da riassumere</h2>
        <label htmlFor="testo">Testo</label>
        <textarea id="testo" rows={8} value={testo} onChange={(e) => setTesto(e.target.value)} />
        <div className="row">
          <button onClick={riassumi} disabled={inCorso}>
            {inCorso ? 'attendo il servizio…' : 'Riassumi'}
          </button>
          <span className="sub">{testo.length} caratteri</span>
        </div>
        <p className="sub">
          Per provare i rami di errore: <code>LLM_MODEL=modello/inesistente</code> dà un 4xx,{' '}
          <code>LLM_BASE_URL=http://localhost:9</code> dà un servizio non raggiungibile,{' '}
          <code>LLM_READ_TIMEOUT=1s</code> dà il timeout di lettura. Il 429 arriva da solo con i modelli{' '}
          <code>:free</code>, se si insiste.
        </p>
      </section>

      {errore && (
        <section className="card">
          <h2>Errore</h2>
          <table>
            <tbody>
              <tr>
                <th>Codice</th>
                <td>
                  {errore.status} {errore.error}
                </td>
              </tr>
              <tr>
                <th>Messaggio</th>
                <td className="errore">{errore.messages.join(' · ')}</td>
              </tr>
            </tbody>
          </table>
          <p className="sub">
            Il testo dell&#39;errore del servizio esterno <strong>non</strong> è qui: sta nel log del server.
            Rimandarlo al browser è il modo più rapido per esporre la chiave.
          </p>
        </section>
      )}

      {risposta && (
        <section className="card">
          <h2>Riassunto</h2>
          <pre>{risposta.riassunto}</pre>
          <table>
            <tbody>
              <tr>
                <th>Modello</th>
                <td>
                  <code>{risposta.modello}</code>
                </td>
              </tr>
              <tr>
                <th>Token in entrata</th>
                <td>{risposta.tokenIngresso}</td>
              </tr>
              <tr>
                <th>Token in uscita</th>
                <td>{risposta.tokenUscita}</td>
              </tr>
              <tr>
                <th>Motivo di arresto</th>
                <td>
                  <code>{risposta.motivoArresto}</code>
                </td>
              </tr>
              <tr>
                <th>Tempo della chiamata esterna</th>
                <td>{risposta.millisEsterni} ms</td>
              </tr>
              <tr>
                <th>Tempo della nostra richiesta</th>
                <td>{millisTotali} ms</td>
              </tr>
            </tbody>
          </table>
        </section>
      )}
    </main>
  )
}
