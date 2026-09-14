import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'
const GMAIL = 'https://mail.google.com'

export default function App() {
  const [stato, setStato] = useState(null)
  // Nessun indirizzo predefinito: con Gmail le email partono davvero, quindi
// si scrive soltanto a se stessi.
  const [destinatario, setDestinatario] = useState('')
  const [oggetto, setOggetto] = useState('Benvenuta in EPICODE')
  const [corpo, setCorpo] = useState('Ciao Anna,\nil tuo account e stato creato.\n')
  const [esito, setEsito] = useState(null)
  const [errore, setErrore] = useState(null)
  const [inCorso, setInCorso] = useState(false)

  async function leggiStato() {
    setStato(null)
    const res = await fetch(`${API}/email/stato`)
    setStato(await res.json())
  }

  useEffect(() => {
    leggiStato()
  }, [])

  async function invia() {
    setInCorso(true)
    setEsito(null)
    setErrore(null)
    const res = await fetch(`${API}/email/testo`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ destinatario, oggetto, corpo }),
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
      <h1>La prima email</h1>
      <p className="sub">
        Configurazione SMTP, timeout e un messaggio di solo testo. Il server di posta e&#39; Gmail: le email
        partono davvero, quindi si scrive solo al proprio indirizzo.
      </p>

      <section className="card">
        <h2>Che cosa sa l&#39;applicazione del server</h2>
        {!stato && <p>lettura in corso…</p>}
        {stato && (
          <>
            <p>
              <span className={`pallino ${stato.raggiungibile ? 'acceso' : 'spento'}`} />
              {stato.raggiungibile
                ? 'connessione aperta e chiusa correttamente'
                : 'il server non risponde'}
            </p>
            <table>
              <tbody>
                <tr>
                  <th>Host</th>
                  <td>{stato.host}</td>
                </tr>
                <tr>
                  <th>Porta</th>
                  <td>{stato.porta}</td>
                </tr>
                <tr>
                  <th>Utenza</th>
                  <td>{stato.utenza}</td>
                </tr>
                <tr>
                  <th>connectiontimeout</th>
                  <td>{stato.connectionTimeout} ms</td>
                </tr>
                <tr>
                  <th>timeout</th>
                  <td>{stato.readTimeout} ms</td>
                </tr>
                <tr>
                  <th>writetimeout</th>
                  <td>{stato.writeTimeout} ms</td>
                </tr>
              </tbody>
            </table>
            {stato.errore && <pre className="errore">{stato.errore}</pre>}
          </>
        )}
        <div className="row">
          <button onClick={leggiStato}>Rileggi lo stato</button>
          <a href={GMAIL} target="_blank" rel="noreferrer">
            Apri Gmail
          </a>
        </div>
      </section>

      <section className="card">
        <h2>Invia un messaggio di solo testo</h2>
        <label htmlFor="destinatario">Destinatario</label>
        <input
          id="destinatario"
          placeholder="il tuo indirizzo"
          value={destinatario}
          onChange={(e) => setDestinatario(e.target.value)}
        />
        <label htmlFor="oggetto">Oggetto</label>
        <input id="oggetto" value={oggetto} onChange={(e) => setOggetto(e.target.value)} />
        <label htmlFor="corpo">Corpo</label>
        <textarea id="corpo" rows={5} value={corpo} onChange={(e) => setCorpo(e.target.value)} />
        <div className="row">
          <button onClick={invia} disabled={inCorso || !destinatario}>
            {inCorso ? 'invio…' : 'Invia'}
          </button>
        </div>

        {errore && <pre className="errore">{errore}</pre>}

        {esito && esito.accettato && (
          <p className="ok">
            {esito.messaggio} in {esito.millis} ms. Accettato non vuol dire consegnato: la consegna avviene dopo,
            e l&#39;applicazione non ne ha notizia.
          </p>
        )}

        {esito && !esito.accettato && (
          <>
            <p className="errore">
              {esito.messaggio} dopo {esito.millis} ms.
            </p>
            <pre className="errore">
              {esito.eccezione}
              {'\n\n'}
              causa annidata:{'\n'}
              {esito.causa}
            </pre>
          </>
        )}
      </section>
    </main>
  )
}
