import { useEffect, useState } from 'react'

const API = 'http://localhost:8080/api'
const GMAIL = 'https://mail.google.com'

type TokenView = {
  id: number
  valore: string
  scadenza: string
  usatoIl: string | null
  valido: boolean
}

type UtenteView = {
  id: number
  email: string
  verificato: boolean
  creatoIl: string
  token: TokenView[]
}

type EsitoVerifica = {
  stato: string
  messaggio: string
  email: string | null
}

const SPIEGAZIONE: Record<string, string> = {
  VERIFICATO: 'primo clic: il token viene consumato e l’utente attivato',
  GIA_USATO: 'secondo clic sullo stesso collegamento: il token risulta consumato',
  SCADUTO: 'il token esiste ma la scadenza e’ passata',
  NON_TROVATO: 'nessun token con questo valore',
}

export default function App() {
  // Nessun indirizzo predefinito: l'email di conferma parte davvero, quindi
  // ci si registra con il proprio indirizzo.
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('segretissima')
  const [registrato, setRegistrato] = useState<string | null>(null)
  const [errore, setErrore] = useState<string | null>(null)
  const [utenti, setUtenti] = useState<UtenteView[]>([])
  const [tokenManuale, setTokenManuale] = useState('')
  const [esito, setEsito] = useState<{ codice: number; corpo: EsitoVerifica } | null>(null)
  const [inCorso, setInCorso] = useState(false)

  async function leggiUtenti() {
    const res = await fetch(`${API}/demo/utenti`)
    setUtenti(await res.json())
  }

  useEffect(() => {
    leggiUtenti()
  }, [])

  async function registra() {
    setInCorso(true)
    setRegistrato(null)
    setErrore(null)
    const res = await fetch(`${API}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password }),
    })
    if (res.ok) {
      setRegistrato(`HTTP ${res.status} — corpo vuoto`)
    } else {
      const err = await res.json()
      setErrore(err.messages.join(' · '))
    }
    setInCorso(false)
    leggiUtenti()
  }

  // Accetta sia il token nudo sia il collegamento completo copiato dall'email.
  function estraiToken(testo: string) {
    const trovato = testo.match(/token=([A-Za-z0-9_-]+)/)
    return trovato ? trovato[1] : testo.trim()
  }

  async function verifica(valore: string) {
    setEsito(null)
    const res = await fetch(`${API}/auth/verify?token=${encodeURIComponent(estraiToken(valore))}`)
    setEsito({ codice: res.status, corpo: await res.json() })
    leggiUtenti()
  }

  async function azzera() {
    await fetch(`${API}/demo/utenti`, { method: 'DELETE' })
    setEsito(null)
    setRegistrato(null)
    leggiUtenti()
  }

  return (
    <main>
      <h1>La conferma dell&#39;indirizzo</h1>
      <p className="sub">
        Registrazione, token, email, conferma. Registrazione riuscita ed email consegnata sono due eventi
        distinti: ognuno puo&#39; fallire da solo.
      </p>

      <section className="card">
        <h2>1. Registrazione</h2>
        <label htmlFor="email">Indirizzo</label>
        <input
          id="email"
          placeholder="il tuo indirizzo"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
        <label htmlFor="password">Password (almeno 8 caratteri)</label>
        <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
        <div className="row">
          <button onClick={registra} disabled={inCorso || !email}>
            {inCorso ? 'invio…' : 'Registra'}
          </button>
          <a href={GMAIL} target="_blank" rel="noreferrer">
            Apri Gmail
          </a>
          <button onClick={azzera}>Azzera il database</button>
        </div>
        {errore && <pre className="errore">{errore}</pre>}
        {registrato && (
          <p className="ok">
            {registrato}. La risposta e&#39; <strong>identica</strong> per un indirizzo nuovo e per uno gia&#39;
            iscritto: la differenza la comunica l&#39;email, non l&#39;HTTP.
          </p>
        )}
      </section>

      <section className="card">
        <h2>2. Il collegamento</h2>
        <p className="sub">
          In aula si copia dall&#39;email aperta in Gmail. Vale sia il collegamento intero sia il solo token.
        </p>
        <label htmlFor="token">Collegamento o token</label>
        <input
          id="token"
          value={tokenManuale}
          placeholder="http://localhost:8080/api/auth/verify?token=…"
          onChange={(e) => setTokenManuale(e.target.value)}
        />
        <div className="row">
          <button onClick={() => verifica(tokenManuale)} disabled={!tokenManuale.trim()}>
            Apri il collegamento
          </button>
        </div>
      </section>

      {esito && (
        <section className="card">
          <h2>3. Esito della verifica</h2>
          <table>
            <tbody>
              <tr>
                <th>Codice HTTP</th>
                <td>{esito.codice}</td>
              </tr>
              <tr>
                <th>Stato</th>
                <td>
                  <code>{esito.corpo.stato}</code>
                </td>
              </tr>
              <tr>
                <th>Messaggio</th>
                <td>{esito.corpo.messaggio}</td>
              </tr>
              <tr>
                <th>Che cosa e&#39; successo</th>
                <td>{SPIEGAZIONE[esito.corpo.stato] ?? '—'}</td>
              </tr>
            </tbody>
          </table>
        </section>
      )}

      <section className="card">
        <h2>Stato del database</h2>
        <p className="sub">
          Questi dati non esistono in un&#39;applicazione vera: il valore di un token non si mostra da nessuna
          parte se non nell&#39;email.
        </p>
        <div className="row">
          <button onClick={leggiUtenti}>Aggiorna</button>
        </div>
        {utenti.length === 0 && <p>nessun utente registrato</p>}
        {utenti.map((u) => (
          <div key={u.id} className="utente">
            <p className="riga-utente">
              <strong>{u.email}</strong>{' '}
              <span className={u.verificato ? 'ok' : 'errore'}>
                {u.verificato ? 'verificato' : 'non verificato'}
              </span>
            </p>
            <table>
              <thead>
                <tr>
                  <th>Token</th>
                  <th>Scadenza</th>
                  <th>Usato il</th>
                  <th>Valido</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {u.token.map((t) => (
                  <tr key={t.id}>
                    <td>
                      <code>{t.valore.slice(0, 12)}…</code>
                    </td>
                    <td>{new Date(t.scadenza).toLocaleString('it-IT')}</td>
                    <td className={t.usatoIl ? 'errore' : undefined}>
                      {t.usatoIl ? new Date(t.usatoIl).toLocaleString('it-IT') : '—'}
                    </td>
                    <td className={t.valido ? 'ok' : 'errore'}>{t.valido ? 'sì' : 'no'}</td>
                    <td>
                      <button onClick={() => verifica(t.valore)}>Apri</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
      </section>
    </main>
  )
}
