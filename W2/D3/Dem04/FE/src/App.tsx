import { useCallback, useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'
const UTENTI = ['anna', 'bruno', 'carla']

type Configurazione = {
  baseUrl: string
  modello: string
  maxTokens: number
  limiteGiornalieroToken: number
  chiavePresente: boolean
}

type Consumo = { utente: string; usatiOggi: number; limiteGiornaliero: number; residui: number }

type GenerazioneRisposta = {
  testo: string
  completa: boolean
  modello: string
  motivoArresto: string
  tokenIngresso: number
  tokenUscita: number
  millisEsterni: number
  usatiOggi: number
  limiteGiornaliero: number
}

type VoceRegistro = {
  id: number
  istante: string
  utente: string
  modello: string
  tokenIngresso: number
  tokenUscita: number
  tokenTotali: number
  esito: string
  durataMs: number
}

type Errore = { status: number; error: string; messages: string[] }

const TESTO_INIZIALE = `Senza un limite per utente una sola persona puo' esaurire il budget del
progetto in poche ore. Il controllo va fatto prima della chiamata, su una stima, e il
consumo reale va registrato dopo, con i valori dichiarati dalla risposta. Nel registro
finiscono utente, modello, token, esito e durata: senza questi dati non e' possibile
rispondere alla domanda «perche' la fattura e' cresciuta».`

export default function App() {
  const [config, setConfig] = useState<Configurazione | null>(null)
  const [utente, setUtente] = useState('anna')
  const [testo, setTesto] = useState(TESTO_INIZIALE)
  const [consumo, setConsumo] = useState<Consumo | null>(null)
  const [registro, setRegistro] = useState<VoceRegistro[]>([])
  const [risposta, setRisposta] = useState<GenerazioneRisposta | null>(null)
  const [errore, setErrore] = useState<Errore | null>(null)
  const [inCorso, setInCorso] = useState(false)

  const aggiorna = useCallback(async () => {
    const [c, r] = await Promise.all([
      fetch(`${API}/consumo?utente=${encodeURIComponent(utente)}`).then((x) => x.json()),
      fetch(`${API}/registro`).then((x) => x.json()),
    ])
    setConsumo(c)
    setRegistro(r)
  }, [utente])

  useEffect(() => {
    fetch(`${API}/configurazione`)
      .then((r) => r.json())
      .then(setConfig)
  }, [])

  useEffect(() => {
    void aggiorna()
  }, [aggiorna])

  async function genera() {
    setInCorso(true)
    setRisposta(null)
    setErrore(null)
    const res = await fetch(`${API}/genera`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ utente, testo }),
    })
    if (res.ok) setRisposta(await res.json())
    else setErrore(await res.json())
    setInCorso(false)
    void aggiorna()
  }

  const percentuale = consumo
    ? Math.min(100, Math.round((consumo.usatiOggi / consumo.limiteGiornaliero) * 100))
    : 0

  return (
    <main>
      <h1>Chiave e limiti</h1>
      <p className="sub">
        Tutti i valori del servizio in un record, la chiave da variabile d&#39;ambiente, un registro delle
        chiamate e un tetto giornaliero per utente.
      </p>

      <section className="card">
        <h2>La configurazione, in un punto</h2>
        {config && (
          <table>
            <tbody>
              <tr>
                <th>
                  <code>app.llm.base-url</code>
                </th>
                <td>
                  <code>{config.baseUrl}</code>
                </td>
              </tr>
              <tr>
                <th>
                  <code>app.llm.model</code>
                </th>
                <td>
                  <code>{config.modello}</code>
                </td>
              </tr>
              <tr>
                <th>
                  <code>app.llm.max-tokens</code>
                </th>
                <td>{config.maxTokens}</td>
              </tr>
              <tr>
                <th>
                  <code>app.llm.limite-giornaliero-token</code>
                </th>
                <td>{config.limiteGiornalieroToken}</td>
              </tr>
              <tr>
                <th>
                  <code>app.llm.api-key</code>
                </th>
                <td className={config.chiavePresente ? 'ok' : 'errore'}>
                  {config.chiavePresente
                    ? 'presente — il valore non lascia il server'
                    : 'assente (e allora l’applicazione non sarebbe partita)'}
                </td>
              </tr>
            </tbody>
          </table>
        )}
        <p className="sub">
          Se la chiave manca, l&#39;applicazione <strong>non parte</strong>: meglio un errore all&#39;avvio che un
          401 al primo utente.
        </p>
      </section>

      <section className="card">
        <h2>Consumo di oggi</h2>
        <label htmlFor="utente">Utente</label>
        <select id="utente" value={utente} onChange={(e) => setUtente(e.target.value)}>
          {UTENTI.map((u) => (
            <option key={u} value={u}>
              {u}
            </option>
          ))}
        </select>

        {consumo && (
          <>
            <div className="barra">
              <div
                className={`riempimento ${percentuale >= 100 ? 'pieno' : ''}`}
                style={{ width: `${percentuale}%` }}
              />
            </div>
            <p className="sub">
              {consumo.usatiOggi} token su {consumo.limiteGiornaliero} · residui {consumo.residui} ({percentuale}%)
            </p>
          </>
        )}
      </section>

      <section className="card">
        <h2>Genera</h2>
        <label htmlFor="testo">Testo</label>
        <textarea id="testo" rows={6} value={testo} onChange={(e) => setTesto(e.target.value)} />
        <div className="row">
          <button onClick={genera} disabled={inCorso}>
            {inCorso ? 'attendo il servizio…' : 'Riassumi'}
          </button>
          <button
            onClick={async () => {
              await fetch(`${API}/registro`, { method: 'DELETE' })
              setRisposta(null)
              setErrore(null)
              await aggiorna()
            }}
          >
            Azzera il registro
          </button>
        </div>
        <p className="sub">
          Premi più volte con lo stesso utente: al superamento del tetto la chiamata esterna{' '}
          <strong>non parte nemmeno</strong>.
        </p>

        {errore && (
          <p className="errore">
            {errore.status} {errore.error} — {errore.messages.join(' · ')}
          </p>
        )}

        {risposta && (
          <>
            <pre>{risposta.testo}</pre>
            <p className="sub">
              <code>{risposta.modello}</code> · <code>{risposta.motivoArresto}</code> ·{' '}
              {risposta.tokenIngresso} in / {risposta.tokenUscita} out · {risposta.millisEsterni} ms
            </p>
          </>
        )}
      </section>

      <section className="card">
        <h2>Il registro delle chiamate</h2>
        <p className="sub">
          Utente, modello, token, esito, durata. Il testo dell&#39;utente <strong>non</strong> è registrato.
        </p>
        <table>
          <thead>
            <tr>
              <th>Ora</th>
              <th>Utente</th>
              <th>Modello</th>
              <th>In</th>
              <th>Out</th>
              <th>Tot</th>
              <th>Esito</th>
              <th>Durata</th>
            </tr>
          </thead>
          <tbody>
            {registro.length === 0 && (
              <tr>
                <td colSpan={8}>nessuna chiamata registrata</td>
              </tr>
            )}
            {registro.map((v) => (
              <tr key={v.id}>
                <td>{new Date(v.istante).toLocaleTimeString('it-IT')}</td>
                <td>{v.utente}</td>
                <td>
                  <code>{v.modello}</code>
                </td>
                <td>{v.tokenIngresso}</td>
                <td>{v.tokenUscita}</td>
                <td>
                  <strong>{v.tokenTotali}</strong>
                </td>
                <td className={v.esito === 'stop' ? 'ok' : 'errore'}>
                  <code>{v.esito}</code>
                </td>
                <td>{v.durataMs} ms</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  )
}
