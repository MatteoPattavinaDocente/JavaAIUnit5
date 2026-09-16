import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'

type Configurazione = {
  baseUrl: string
  modello: string
  maxTokens: number
  chiavePresente: boolean
}

type SceltaVista = {
  indice: number
  motivoArresto: string | null
  motivoArrestoNativo: string | null
  caratteri: number
  conRifiuto: boolean
  conRagionamento: boolean
}

type GenerazioneRisposta = {
  testo: string
  testoIngenuo: string | null
  utilizzabile: boolean
  avviso: string | null
  modello: string
  fornitore: string | null
  motivoArresto: string | null
  motivoArrestoNativo: string | null
  scelte: SceltaVista[]
  tokenIngresso: number
  tokenUscita: number
  maxTokensRichiesti: number
  millisEsterni: number
}

type Errore = { status: number; error: string; messages: string[] }

const TESTO_INIZIALE = `L'API dei messaggi e' senza stato: il servizio non ricorda le chiamate
precedenti. Per continuare un dialogo si rimanda l'intero elenco dei turni a ogni
richiesta, quindi il costo cresce con la lunghezza della conversazione. La cronologia
e' responsabilita' della nostra applicazione: sta nel database, non nel servizio.`

export default function App() {
  const [config, setConfig] = useState<Configurazione | null>(null)
  const [testo, setTesto] = useState(TESTO_INIZIALE)
  const [maxTokens, setMaxTokens] = useState(2048)
  const [risposta, setRisposta] = useState<GenerazioneRisposta | null>(null)
  const [errore, setErrore] = useState<Errore | null>(null)
  const [inCorso, setInCorso] = useState(false)

  useEffect(() => {
    let attivo = true
    fetch(`${API}/configurazione`)
      .then((r) => r.json())
      .then((c: Configurazione) => {
        if (!attivo) return
        setConfig(c)
        setMaxTokens(c.maxTokens)
      })
    return () => {
      attivo = false
    }
  }, [])

  async function genera() {
    setInCorso(true)
    setRisposta(null)
    setErrore(null)
    const res = await fetch(`${API}/genera`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ testo, maxTokens }),
    })
    if (res.ok) setRisposta(await res.json())
    else setErrore(await res.json())
    setInCorso(false)
  }

  return (
    <main>
      <h1>La prima risposta</h1>
      <p className="sub">
        Una intestazione di autenticazione, due campi obbligatori. Il contenuto della risposta non sta in
        cima: sta dentro <code>choices</code>, che è un <strong>elenco</strong>, e va letto solo dopo aver
        guardato <code>finish_reason</code>.
      </p>

      <section className="card">
        <h2>Il corpo che inviamo</h2>
        <pre>{`{
  "model": "${config?.modello ?? '…'}",
  "max_tokens": ${maxTokens},
  "messages": [
    { "role": "system", "content": "Sei un assistente che riassume testi. …" },
    { "role": "user",   "content": "Riassumi il testo seguente: …" }
  ],
  "reasoning": { "effort": "low" }
}`}</pre>
        <p className="sub">
          Le istruzioni permanenti <strong>sono un turno</strong>, con ruolo <code>system</code>, e vanno per
          prime nell&#39;elenco. Non esiste un campo <code>system</code> di primo livello: chi arriva dal
          protocollo di Anthropic lo cerca e non lo trova.
        </p>
      </section>

      <section className="card">
        <h2>Il testo e il tetto dei token</h2>
        <label htmlFor="testo">Testo</label>
        <textarea id="testo" rows={7} value={testo} onChange={(e) => setTesto(e.target.value)} />

        <label htmlFor="max">
          <code>max_tokens</code>: {maxTokens}
        </label>
        <input
          id="max"
          type="range"
          min={10}
          max={2048}
          step={10}
          value={maxTokens}
          onChange={(e) => setMaxTokens(Number(e.target.value))}
        />
        <p className="sub">
          Portalo a venti e guarda <code>finish_reason</code> passare da <code>stop</code> a{' '}
          <code>length</code>: è la verifica da fare sempre.
        </p>

        <div className="row">
          <button onClick={genera} disabled={inCorso}>
            {inCorso ? 'attendo il servizio…' : 'Genera'}
          </button>
          <span className="sub">
            modello <code>{config?.modello}</code> ·{' '}
            {config?.chiavePresente ? 'chiave presente' : 'chiave assente'}
          </span>
        </div>
        <p className="sub">
          I nomi cambiano rispetto al protocollo di Anthropic: <code>finish_reason</code> invece di{' '}
          <code>stop_reason</code>, con i valori <code>stop</code>, <code>length</code>,{' '}
          <code>content_filter</code>. E <code>usage</code> parla di <code>prompt_tokens</code> e{' '}
          <code>completion_tokens</code>.
        </p>
      </section>

      {errore && (
        <section className="card">
          <h2>Errore</h2>
          <p className="errore">
            {errore.status} {errore.error} — {errore.messages.join(' · ')}
          </p>
        </section>
      )}

      {risposta && (
        <>
          <section className="card">
            <h2>Le scelte ricevute</h2>
            <table>
              <thead>
                <tr>
                  <th>Indice</th>
                  <th>
                    <code>finish_reason</code>
                  </th>
                  <th>Grezzo del fornitore</th>
                  <th>Caratteri</th>
                  <th>Note</th>
                </tr>
              </thead>
              <tbody>
                {risposta.scelte.length === 0 && (
                  <tr>
                    <td colSpan={5} className="errore">
                      nessuna scelta: <code>choices</code> è vuoto
                    </td>
                  </tr>
                )}
                {risposta.scelte.map((s) => (
                  <tr key={s.indice}>
                    <td>
                      <code>choices[{s.indice}]</code>
                    </td>
                    <td className={s.motivoArresto === 'stop' ? 'ok' : 'errore'}>
                      <code>{s.motivoArresto ?? 'assente'}</code>
                    </td>
                    <td>
                      <code>{s.motivoArrestoNativo ?? '—'}</code>
                    </td>
                    <td>{s.caratteri}</td>
                    <td>
                      {s.conRifiuto && <span className="errore">rifiuto dichiarato</span>}
                      {s.conRagionamento && <span className="sub"> ragionamento presente</span>}
                      {!s.conRifiuto && !s.conRagionamento && '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <table>
              <tbody>
                <tr>
                  <th>
                    Letto <strong>con i controlli</strong>
                  </th>
                  <td className="ok">
                    {risposta.testo ? `${risposta.testo.length} caratteri` : 'niente'}
                  </td>
                </tr>
                <tr>
                  <th>
                    Letto con <code>choices[0].message.content</code>
                  </th>
                  <td className={risposta.testoIngenuo ? '' : 'errore'}>
                    {risposta.testoIngenuo === null
                      ? 'null — choices è vuoto, oppure content è nullo'
                      : `${risposta.testoIngenuo.length} caratteri`}
                  </td>
                </tr>
              </tbody>
            </table>
          </section>

          <section className="card">
            <h2>Motivo di arresto e consumo</h2>
            <table>
              <tbody>
                <tr>
                  <th>
                    <code>finish_reason</code>
                  </th>
                  <td className={risposta.motivoArresto === 'stop' ? 'ok' : 'errore'}>
                    <code>{risposta.motivoArresto ?? 'assente'}</code>
                    {risposta.motivoArrestoNativo && (
                      <span className="sub">
                        {' '}
                        · grezzo: <code>{risposta.motivoArrestoNativo}</code>
                      </span>
                    )}
                  </td>
                </tr>
                <tr>
                  <th>Modello servito</th>
                  <td>
                    <code>{risposta.modello}</code>
                    {risposta.fornitore && <span className="sub"> · fornitore: {risposta.fornitore}</span>}
                  </td>
                </tr>
                <tr>
                  <th>Utilizzabile</th>
                  <td className={risposta.utilizzabile ? 'ok' : 'errore'}>
                    {risposta.utilizzabile ? 'sì' : 'no'}
                  </td>
                </tr>
                <tr>
                  <th>
                    Token in entrata <code>prompt_tokens</code>
                  </th>
                  <td>{risposta.tokenIngresso} — istruzioni, turni e testo inviato</td>
                </tr>
                <tr>
                  <th>
                    Token in uscita <code>completion_tokens</code>
                  </th>
                  <td>
                    {risposta.tokenUscita} su {risposta.maxTokensRichiesti} concessi
                  </td>
                </tr>
                <tr>
                  <th>Durata</th>
                  <td>{risposta.millisEsterni} ms</td>
                </tr>
              </tbody>
            </table>
            {risposta.avviso && <p className="errore">{risposta.avviso}</p>}
          </section>

          <section className="card">
            <h2>Il testo</h2>
            {risposta.testo ? <pre>{risposta.testo}</pre> : <p className="errore">nessun testo utilizzabile</p>}
          </section>
        </>
      )}
    </main>
  )
}
