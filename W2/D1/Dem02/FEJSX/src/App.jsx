import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'
const GMAIL = 'https://mail.google.com'

const MODI = [
  { valore: 'TESTO', etichetta: 'TESTO — SimpleMailMessage', nota: 'una sola parte, text/plain' },
  { valore: 'HTML', etichetta: 'HTML — MimeMessageHelper', nota: 'multipart/alternative: testo + HTML' },
  { valore: 'ALLEGATO', etichetta: 'ALLEGATO — addAttachment', nota: 'multipart/mixed: ricevuta.txt scaricabile' },
  { valore: 'INLINE', etichetta: 'INLINE — addInline + cid:', nota: 'multipart/related: logo dentro il corpo' },
]

export default function App() {
  // Nessun indirizzo predefinito: con Gmail le email partono davvero.
  const [destinatario, setDestinatario] = useState('')
  const [nome, setNome] = useState('Anna')
  const [modo, setModo] = useState('HTML')
  const [esito, setEsito] = useState(null)
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(false)
  const [registro, setRegistro] = useState([])

  async function leggiRegistro() {
    const res = await fetch(`${API}/email/registro`)
    setRegistro(await res.json())
  }

  useEffect(() => {
    leggiRegistro()
  }, [])

  async function invia() {
    setInCorso(true)
    setEsito(null)
    setErrore(null)
    const res = await fetch(`${API}/email/invia`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ destinatario, nome, modo, asincrono: false, ritardoMs: 0 }),
    })
    if (res.ok) {
      setEsito(await res.json())
    } else {
      const err = await res.json()
      setErrore(err.messages.join(' · '))
    }
    setInCorso(false)
    leggiRegistro()
  }

  async function svuota() {
    await fetch(`${API}/email/registro`, { method: 'DELETE' })
    leggiRegistro()
  }

  const modoScelto = MODI.find((m) => m.valore === modo)

  return (
    <main>
      <h1>Testo, HTML e allegati</h1>
      <p className="sub">
        Lo stesso messaggio composto in quattro modi, inviato attraverso Gmail. Cambia solo come si costruisce
        il MIME: una parte sola, due alternative, un allegato, un&#39;immagine dentro il corpo.
      </p>

      <section className="card">
        <h2>Il messaggio</h2>
        <label htmlFor="destinatario">Destinatario</label>
        <input
          id="destinatario"
          placeholder="il tuo indirizzo"
          value={destinatario}
          onChange={(e) => setDestinatario(e.target.value)}
        />
        <label htmlFor="nome">Nome</label>
        <input id="nome" value={nome} onChange={(e) => setNome(e.target.value)} />

        <label htmlFor="modo">Come lo componiamo</label>
        <select id="modo" value={modo} onChange={(e) => setModo(e.target.value)}>
          {MODI.map((m) => (
            <option key={m.valore} value={m.valore}>
              {m.etichetta}
            </option>
          ))}
        </select>
        {modoScelto && <p className="sub">{modoScelto.nota}</p>}

        <div className="row">
          <button onClick={invia} disabled={inCorso || !destinatario}>
            {inCorso ? 'invio…' : 'Invia'}
          </button>
          <a href={GMAIL} target="_blank" rel="noreferrer">
            Apri Gmail
          </a>
        </div>

        {errore && <pre className="errore">{errore}</pre>}
      </section>

      {esito && (
        <section className="card">
          <h2>Esito</h2>
          <table>
            <tbody>
              <tr>
                <th>Modo</th>
                <td>{esito.modo}</td>
              </tr>
              <tr>
                <th>Tempo dell&#39;invio</th>
                <td>{esito.millisInvio} ms</td>
              </tr>
            </tbody>
          </table>
          <p className={esito.accettato ? 'ok' : 'errore'}>{esito.messaggio}</p>
          {esito.causa && <pre className="errore">{esito.causa}</pre>}
        </section>
      )}

      <section className="card">
        <h2>Registro degli invii</h2>
        <p className="sub">Che cosa è partito, verso chi e con quale composizione.</p>
        <div className="row">
          <button onClick={leggiRegistro}>Aggiorna</button>
          <button onClick={svuota}>Svuota</button>
        </div>
        <table>
          <thead>
            <tr>
              <th>Momento</th>
              <th>Destinatario</th>
              <th>Modo</th>
              <th>Esito</th>
              <th>Invio</th>
            </tr>
          </thead>
          <tbody>
            {registro.length === 0 && (
              <tr>
                <td colSpan={5}>nessun invio registrato</td>
              </tr>
            )}
            {registro.map((v, i) => (
              <tr key={`${v.momento}-${i}`}>
                <td>{new Date(v.momento).toLocaleTimeString('it-IT')}</td>
                <td>{v.destinatario}</td>
                <td>{v.modo}</td>
                <td className={v.accettato ? 'ok' : 'errore'}>
                  {v.accettato ? 'accettato' : v.errore}
                </td>
                <td>{v.millisInvio} ms</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </main>
  )
}
