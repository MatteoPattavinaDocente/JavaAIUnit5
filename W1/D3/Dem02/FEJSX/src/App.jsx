import { useState } from 'react'

const API = 'http://localhost:8080/api'
const DOCUMENT_ID = 1

// Forme dei dati scambiati con il backend (solo documentazione):
// Attachment = { id, originalName, contentType, sizeBytes, storageKey }
// ApiError   = { error: string, reasons: string[] }

export default function App() {
  const [scelti, setScelti] = useState([])
  const [salvati, setSalvati] = useState([])
  const [motivi, setMotivi] = useState([])
  const [inCorso, setInCorso] = useState(false)

  async function carica() {
    setInCorso(true)
    setMotivi([])
    setSalvati([])

    // FormData raccoglie i file: il nome 'files' deve corrispondere a @RequestPart("files").
    const form = new FormData()
    for (const file of scelti) form.append('files', file)

    // Nessun Content-Type impostato a mano: il delimitatore lo aggiunge il browser.
    const res = await fetch(`${API}/documents/${DOCUMENT_ID}/attachments`, {
      method: 'POST',
      body: form,
    })

    if (res.ok) {
      setSalvati(await res.json())
    } else {
      const corpo = await res.json()
      setMotivi([`HTTP ${res.status} — ${corpo.error}`, ...(corpo.reasons ?? [])])
    }
    setInCorso(false)
  }

  return (
    <main>
      <h1>Allegati del documento {DOCUMENT_ID}</h1>

      <section className="card">
        <input type="file" multiple onChange={(e) => setScelti(Array.from(e.target.files ?? []))} />
        <ul>
          {scelti.map((file) => (
            <li key={file.name}>
              {file.name} — {(file.size / 1024).toFixed(0)} KB — dichiarato {file.type || 'sconosciuto'}
            </li>
          ))}
        </ul>
        <button onClick={carica} disabled={inCorso || scelti.length === 0}>
          {inCorso ? 'Invio in corso...' : `Carica ${scelti.length} file`}
        </button>
      </section>

      {salvati.length > 0 && (
        <section className="card">
          <h2>Accettati</h2>
          <ul>
            {salvati.map((allegato) => (
              <li key={allegato.id}>
                {allegato.originalName} — tipo reale {allegato.contentType} — {allegato.sizeBytes} byte
                <br />
                <small>salvato come {allegato.storageKey}</small>
              </li>
            ))}
          </ul>
        </section>
      )}

      {motivi.length > 0 && (
        <section className="card errore">
          <h2>Rifiutati</h2>
          <ul>
            {motivi.map((motivo) => (
              <li key={motivo}>{motivo}</li>
            ))}
          </ul>
        </section>
      )}
    </main>
  )
}
