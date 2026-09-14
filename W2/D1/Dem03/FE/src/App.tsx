import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'
const GMAIL = 'https://mail.google.com'

// L'anteprima chiede al server lo STESSO HTML che finirebbe nel messaggio: il
// motore restituisce una stringa, il resto e' identico.
async function leggiAnteprima(nome: string, lingua: string) {
  const res = await fetch(`${API}/benvenuto/anteprima?nome=${encodeURIComponent(nome)}&lingua=${lingua}`)
  return res.text()
}

type EsitoInvio = {
  accettato: boolean
  millis: number
  messaggio: string
  causa: string | null
}

export default function App() {
  // Nessun indirizzo predefinito: con Gmail le email partono davvero.
  const [destinatario, setDestinatario] = useState('')
  const [nome, setNome] = useState('Anna')
  const [lingua, setLingua] = useState('it')
  const [html, setHtml] = useState('')
  const [esito, setEsito] = useState<EsitoInvio | null>(null)
  const [errore, setErrore] = useState<string | null>(null)
  const [inCorso, setInCorso] = useState(false)
  const [mostraSorgente, setMostraSorgente] = useState(false)

  function anteprima() {
    leggiAnteprima(nome, lingua).then(setHtml)
  }

  useEffect(() => {
    let vivo = true
    leggiAnteprima(nome, lingua).then((testo) => {
      if (vivo) setHtml(testo)
    })
    return () => {
      vivo = false
    }
  }, [nome, lingua])

  async function invia() {
    setInCorso(true)
    setEsito(null)
    setErrore(null)
    const res = await fetch(`${API}/benvenuto/invia`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ destinatario, nome, lingua }),
    })
    if (res.ok) {
      setEsito(await res.json())
    } else {
      const err = await res.json()
      setErrore(err.messages.join(' · '))
    }
    setInCorso(false)
  }

  return (
    <main>
      <h1>L&#39;email di benvenuto</h1>
      <p className="sub">
        L&#39;HTML non sta piu&#39; nel codice Java: sta in <code>templates/email/benvenuto.html</code>. I dati
        arrivano da un <code>Context</code>, i testi da <code>messages.properties</code>.
      </p>

      <section className="card">
        <h2>I dati del contesto</h2>
        <label htmlFor="destinatario">Destinatario</label>
        <input
          id="destinatario"
          placeholder="il tuo indirizzo"
          value={destinatario}
          onChange={(e) => setDestinatario(e.target.value)}
        />
        <label htmlFor="nome">
          Nome — variabile <code>${'{'}nome{'}'}</code> del template
        </label>
        <input id="nome" value={nome} onChange={(e) => setNome(e.target.value)} />
        <label htmlFor="lingua">
          Lingua — <code>Locale</code> passato al costruttore di <code>Context</code>
        </label>
        <select id="lingua" value={lingua} onChange={(e) => setLingua(e.target.value)}>
          <option value="it">it — messages.properties</option>
          <option value="en">en — messages_en.properties</option>
        </select>

        <div className="row">
          <button onClick={invia} disabled={inCorso || !destinatario}>
            {inCorso ? 'invio…' : 'Invia l\u2019email'}
          </button>
          <button onClick={anteprima}>Rigenera l&#39;anteprima</button>
          <a href={GMAIL} target="_blank" rel="noreferrer">
            Apri Gmail
          </a>
        </div>

        {errore && <pre className="errore">{errore}</pre>}
        {esito && (
          <p className={esito.accettato ? 'ok' : 'errore'}>
            {esito.messaggio} ({esito.millis} ms)
          </p>
        )}
        {esito?.causa && <pre className="errore">{esito.causa}</pre>}
      </section>

      <section className="card">
        <h2>Anteprima — lo stesso HTML che finisce nel messaggio</h2>
        <p className="sub">
          Il logo non si vede qui: <code>cid:logo</code> esiste solo dentro il messaggio. Nel client di posta
          compare.
        </p>
        <iframe title="anteprima" srcDoc={html} sandbox="" />
        <div className="row">
          <button onClick={() => setMostraSorgente(!mostraSorgente)}>
            {mostraSorgente ? 'Nascondi il sorgente' : 'Mostra il sorgente elaborato'}
          </button>
        </div>
        {mostraSorgente && <pre>{html}</pre>}
      </section>
    </main>
  )
}
